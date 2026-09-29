-- =====================================================================
-- LOS DATABASE SCHEMA — PHASE 1 (LOGIN ONLY)
-- =====================================================================
-- This file has TWO parts:
--   PART A -> run once, inside los_master_db      (central registry)
--   PART B -> run once, inside EACH tenant database (e.g. los_hdfc01_db)
--
-- HOW TO USE:
--   1. Create and connect to los_master_db, run PART A only.
--   2. For every new NBFC/Bank: create its own database
--      (e.g. los_hdfc01_db), connect to it, and run PART B only.
--
-- The \connect lines below are psql-only markers so you remember which
-- database each part belongs to — remove/ignore them if running through
-- a GUI tool (pgAdmin/DBeaver) where you select the database from the UI.
-- =====================================================================


-- =====================================================================
-- PART A: MASTER DATABASE  (run inside los_master_db)
-- =====================================================================

CREATE SCHEMA IF NOT EXISTS organization;
CREATE SCHEMA IF NOT EXISTS identity;

-- ---------------------------------------------------------------------
-- organization.organizations
-- Registry of every NBFC/Bank + which physical database holds its data
-- ---------------------------------------------------------------------
CREATE TABLE organization.organizations (
    id                      BIGSERIAL PRIMARY KEY,
    uuid                    UUID UNIQUE DEFAULT gen_random_uuid(),
    institution_code        VARCHAR(50) NOT NULL UNIQUE,
    institution_name        VARCHAR(150) NOT NULL,
    legal_name              VARCHAR(200),
    short_name              VARCHAR(50),
    institution_type        VARCHAR(50) NOT NULL,
    registration_number     VARCHAR(100),
    pan                     VARCHAR(20),
    cin                     VARCHAR(50),
    website                 VARCHAR(255),
    logo                    TEXT,
    regulatory_authority_id UUID,
    regulatory_status       VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    country                 VARCHAR(100) NOT NULL DEFAULT 'India',
    status                  VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'
                                CHECK (status IN ('ACTIVE','INACTIVE','SUSPENDED')),
    contact_email           VARCHAR(150),
    contact_phone           VARCHAR(20),
    db_name                 VARCHAR(100) NOT NULL UNIQUE,
    db_host                 VARCHAR(150) NOT NULL DEFAULT 'localhost',
    db_port                 INT NOT NULL DEFAULT 5432,
    created_by              BIGINT,
    created_at              TIMESTAMP NOT NULL DEFAULT now(),
    updated_at              TIMESTAMP NOT NULL DEFAULT now()
);


-- ---------------------------------------------------------------------
-- identity.roles  (Internal panel only)
-- ---------------------------------------------------------------------
CREATE TABLE identity.roles (
    id            SERIAL PRIMARY KEY,
    name          VARCHAR(50) NOT NULL UNIQUE,
    panel         VARCHAR(30) NOT NULL CHECK (panel IN ('INTERNAL')),
    description   VARCHAR(255),
    created_at    TIMESTAMP NOT NULL DEFAULT now()
);

INSERT INTO identity.roles (name, panel, description) VALUES
    ('INTERNAL_ADMIN', 'INTERNAL', 'Platform team; manages tenants and features');

-- ---------------------------------------------------------------------
-- identity.internal_users  (your own team)
-- ---------------------------------------------------------------------
CREATE TABLE identity.internal_users (
    id                      BIGSERIAL PRIMARY KEY,
    user_code               VARCHAR(30) UNIQUE,
    role_id                 INT NOT NULL REFERENCES identity.roles(id),
    first_name              VARCHAR(80) NOT NULL,
    middle_name             VARCHAR(80),
    last_name               VARCHAR(80) NOT NULL,
    email                   VARCHAR(150) UNIQUE,
    phone                   VARCHAR(20) UNIQUE,
    password_hash           VARCHAR(255) NOT NULL,
    is_active               BOOLEAN NOT NULL DEFAULT true,
    last_login_at           TIMESTAMP,
    failed_login_attempts   INT NOT NULL DEFAULT 0,
    created_by              BIGINT REFERENCES identity.internal_users(id),
    created_at              TIMESTAMP NOT NULL DEFAULT now(),
    updated_at              TIMESTAMP NOT NULL DEFAULT now()
);

-- ---------------------------------------------------------------------
-- identity.login_directory
-- Routing table: given an identifier (user_code/email/phone), find
-- which organization (and therefore which tenant database) it belongs to.
-- ---------------------------------------------------------------------
CREATE TABLE identity.login_directory (
    id                BIGSERIAL PRIMARY KEY,
    user_code         VARCHAR(30) UNIQUE,
    email             VARCHAR(150) UNIQUE,
    phone             VARCHAR(20) UNIQUE,
    organization_id   BIGINT NOT NULL REFERENCES organization.organizations(id),
    user_type         VARCHAR(20) NOT NULL CHECK (user_type IN ('STAFF','CUSTOMER','INTERNAL')),
    created_at        TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_login_directory_org ON identity.login_directory(organization_id);

-- ---------------------------------------------------------------------
-- identity.lookup_types (Table 51001) - Master Lookup Types
-- ---------------------------------------------------------------------
CREATE TABLE identity.lookup_types (
    id            BIGSERIAL PRIMARY KEY,
    code          VARCHAR(50) NOT NULL UNIQUE,
    description   VARCHAR(255) NOT NULL,
    is_fixed      BOOLEAN NOT NULL DEFAULT false,
    is_active     BOOLEAN NOT NULL DEFAULT true,
    created_by    BIGINT,
    created_at    TIMESTAMP NOT NULL DEFAULT now(),
    modified_by   BIGINT,
    updated_at    TIMESTAMP NOT NULL DEFAULT now()
);

-- ---------------------------------------------------------------------
-- identity.lookup_sub_types (Table 51101) - Master Lookup Sub Types
-- ---------------------------------------------------------------------
CREATE TABLE identity.lookup_sub_types (
    id                    BIGSERIAL PRIMARY KEY,
    lookup_type_code      VARCHAR(50) NOT NULL,
    type_description      VARCHAR(255),
    sub_type_code         VARCHAR(50) NOT NULL,
    sub_type_description  VARCHAR(255) NOT NULL,
    is_fixed              BOOLEAN NOT NULL DEFAULT false,
    is_active             BOOLEAN NOT NULL DEFAULT true,
    display_order         INT NOT NULL DEFAULT 0,
    created_by            BIGINT,
    created_at            TIMESTAMP NOT NULL DEFAULT now(),
    modified_by           BIGINT,
    updated_at            TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT uk_master_lookup_sub_type UNIQUE (lookup_type_code, sub_type_code)
);

CREATE INDEX idx_master_lst_type_code ON identity.lookup_sub_types(lookup_type_code);

-- =====================================================================
-- END OF PART A
-- =====================================================================


-- =====================================================================
-- PART B: TENANT DATABASE TEMPLATE
-- Run this exact block inside EVERY new NBFC/Bank database
-- e.g. los_hdfc01_db, los_bajaj02_db, etc.
-- =====================================================================

CREATE SCHEMA IF NOT EXISTS organization;
CREATE SCHEMA IF NOT EXISTS identity;
CREATE SCHEMA IF NOT EXISTS customer;

-- ---------------------------------------------------------------------
-- organization.branches
-- ---------------------------------------------------------------------
CREATE TABLE organization.branches (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(150) NOT NULL,
    code            VARCHAR(20) NOT NULL UNIQUE,
    address         VARCHAR(255),
    city            VARCHAR(100),
    state           VARCHAR(100),
    pincode         VARCHAR(10),
    status          VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'
                        CHECK (status IN ('ACTIVE','INACTIVE')),
    created_at      TIMESTAMP NOT NULL DEFAULT now(),
    updated_at      TIMESTAMP NOT NULL DEFAULT now()
);

-- ---------------------------------------------------------------------
-- identity.roles  (local copy — 5 roles for this tenant)
-- ---------------------------------------------------------------------
CREATE TABLE identity.roles (
    id            SERIAL PRIMARY KEY,
    name          VARCHAR(50) NOT NULL UNIQUE,
    panel         VARCHAR(30) NOT NULL CHECK (panel IN ('BANK_NBFC','CUSTOMER')),
    description   VARCHAR(255),
    created_at    TIMESTAMP NOT NULL DEFAULT now()
);

INSERT INTO identity.roles (name, panel, description) VALUES
    ('SUPER_ADMIN', 'BANK_NBFC', 'Full control within this NBFC/Bank'),
    ('CHECKER',     'BANK_NBFC', 'Reviews and approves Maker actions'),
    ('MAKER',       'BANK_NBFC', 'Creates/initiates records for Checker approval'),
    ('VIEWER',      'BANK_NBFC', 'Read-only access'),
    ('CUSTOMER',    'CUSTOMER',  'Loan applicant');

-- ---------------------------------------------------------------------
-- identity.users  (Bank/NBFC staff — branch-linked, split name)
-- ---------------------------------------------------------------------
CREATE TABLE identity.users (
    id                      BIGSERIAL PRIMARY KEY,
    user_code               VARCHAR(30) UNIQUE,
    branch_id               BIGINT REFERENCES organization.branches(id),
    role_id                 INT NOT NULL REFERENCES identity.roles(id),
    first_name              VARCHAR(80) NOT NULL,
    middle_name             VARCHAR(80),
    last_name               VARCHAR(80) NOT NULL,
    email                   VARCHAR(150) UNIQUE,
    phone                   VARCHAR(20) UNIQUE,
    password_hash           VARCHAR(255) NOT NULL,
    is_active               BOOLEAN NOT NULL DEFAULT true,
    last_login_at           TIMESTAMP,
    failed_login_attempts   INT NOT NULL DEFAULT 0,
    created_by              BIGINT REFERENCES identity.users(id),
    created_at              TIMESTAMP NOT NULL DEFAULT now(),
    updated_at              TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_users_branch_id ON identity.users(branch_id);
CREATE INDEX idx_users_role_id ON identity.users(role_id);

-- ---------------------------------------------------------------------
-- customer.customers  (branch-linked, split name)
-- ---------------------------------------------------------------------
CREATE TABLE customer.customers (
    id               BIGSERIAL PRIMARY KEY,
    customer_code    VARCHAR(30) UNIQUE,
    branch_id        BIGINT REFERENCES organization.branches(id),
    first_name       VARCHAR(80) NOT NULL,
    middle_name      VARCHAR(80),
    last_name        VARCHAR(80) NOT NULL,
    email            VARCHAR(150) UNIQUE,
    phone            VARCHAR(20) UNIQUE,
    password_hash    VARCHAR(255),
    is_active        BOOLEAN NOT NULL DEFAULT true,
    last_login_at    TIMESTAMP,
    created_at       TIMESTAMP NOT NULL DEFAULT now(),
    updated_at       TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_customers_branch_id ON customer.customers(branch_id);

-- ---------------------------------------------------------------------
-- identity.lookup_types (Table 51001) - Bank Lookup Types
-- ---------------------------------------------------------------------
CREATE TABLE identity.lookup_types (
    id            BIGSERIAL PRIMARY KEY,
    code          VARCHAR(50) NOT NULL UNIQUE,
    description   VARCHAR(255) NOT NULL,
    is_fixed      BOOLEAN NOT NULL DEFAULT false,
    is_active     BOOLEAN NOT NULL DEFAULT true,
    created_by    BIGINT,
    created_at    TIMESTAMP NOT NULL DEFAULT now(),
    modified_by   BIGINT,
    updated_at    TIMESTAMP NOT NULL DEFAULT now()
);

-- ---------------------------------------------------------------------
-- identity.lookup_sub_types (Table 51101) - Bank Lookup Sub Types
-- ---------------------------------------------------------------------
CREATE TABLE identity.lookup_sub_types (
    id                    BIGSERIAL PRIMARY KEY,
    lookup_type_code      VARCHAR(50) NOT NULL,
    type_description      VARCHAR(255),
    sub_type_code         VARCHAR(50) NOT NULL,
    sub_type_description  VARCHAR(255) NOT NULL,
    is_fixed              BOOLEAN NOT NULL DEFAULT false,
    is_active             BOOLEAN NOT NULL DEFAULT true,
    display_order         INT NOT NULL DEFAULT 0,
    created_by            BIGINT,
    created_at            TIMESTAMP NOT NULL DEFAULT now(),
    modified_by           BIGINT,
    updated_at            TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT uk_bank_lookup_sub_type UNIQUE (lookup_type_code, sub_type_code)
);

CREATE INDEX idx_bank_lst_type_code ON identity.lookup_sub_types(lookup_type_code);

-- ---------------------------------------------------------------------
-- identity.bank_lookup_type_permissions - Dynamic Bank Lookup Permissions
-- ---------------------------------------------------------------------
CREATE TABLE identity.bank_lookup_type_permissions (
    id                BIGSERIAL PRIMARY KEY,
    lookup_type_code  VARCHAR(50) NOT NULL,
    permission_code   VARCHAR(50) NOT NULL,
    created_at        TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT uk_bank_lt_permission UNIQUE (lookup_type_code, permission_code)
);

CREATE INDEX idx_bank_lt_perm_code ON identity.bank_lookup_type_permissions(lookup_type_code);

CREATE INDEX idx_bank_lst_type_code ON identity.lookup_sub_types(lookup_type_code);

-- =====================================================================
-- END OF PART B
-- =====================================================================

