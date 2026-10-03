-- =======================================================================
-- MASTER DB SCHEMA  (run inside los_master_db)
-- =======================================================================
CREATE SCHEMA IF NOT EXISTS organization;
CREATE SCHEMA IF NOT EXISTS identity;
CREATE SCHEMA IF NOT EXISTS customer;

-- -----------------------------------------------------------------------
-- organization.organizations
-- -----------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS organization.organizations (
    id                          BIGSERIAL PRIMARY KEY,
    uuid                        UUID UNIQUE DEFAULT gen_random_uuid(),
    bank_code                   VARCHAR(50)  NOT NULL UNIQUE,
    bank_name                   VARCHAR(150) NOT NULL,
    legal_name                  VARCHAR(200),
    short_name                  VARCHAR(50),
    bank_type                   VARCHAR(50)  NOT NULL,
    license_number              VARCHAR(100),
    pan                         VARCHAR(20),
    gst_no                      VARCHAR(50),
    website                     VARCHAR(255),
    logo                        TEXT,
    regulatory_authority_id     UUID,
    regulatory_status           VARCHAR(50)  NOT NULL DEFAULT 'ACTIVE',
    country                     VARCHAR(100) NOT NULL DEFAULT 'India',
    status                      VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    contact_email               VARCHAR(150),
    contact_phone               VARCHAR(20),
    db_name                     VARCHAR(100) NOT NULL UNIQUE,
    db_host                     VARCHAR(150) NOT NULL DEFAULT 'localhost',
    db_port                     INT          NOT NULL DEFAULT 5432,
    created_by                  BIGINT,
    created_at                  TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at                  TIMESTAMP    NOT NULL DEFAULT now(),

    -- Regulatory details
    direct_clearing_member       BOOLEAN,
    direct_member_iftas          BOOLEAN,
    micr_city_code              VARCHAR(3),
    micr_bank_code              VARCHAR(3),
    micr_branch_code            VARCHAR(3),
    ifsc_code                   VARCHAR(11),
    number_of_branches          INTEGER,
    sponsor_bank_for_clearing   VARCHAR(150),
    sponsor_bank_for_iftas      VARCHAR(150),

    -- Address details
    address_type                VARCHAR(50),
    unit_gala_name_number        VARCHAR(200),
    street_road                 VARCHAR(200),
    landmark                    VARCHAR(150),
    city                        VARCHAR(100),
    state                       VARCHAR(100),
    pincode                     VARCHAR(6)
);

ALTER TABLE organization.organizations
    ADD COLUMN IF NOT EXISTS uuid UUID DEFAULT gen_random_uuid();

ALTER TABLE organization.organizations
    ADD COLUMN IF NOT EXISTS bank_code VARCHAR(50);

ALTER TABLE organization.organizations
    ADD COLUMN IF NOT EXISTS bank_name VARCHAR(150);

ALTER TABLE organization.organizations
    ADD COLUMN IF NOT EXISTS legal_name VARCHAR(200);

ALTER TABLE organization.organizations
    ADD COLUMN IF NOT EXISTS short_name VARCHAR(50);

ALTER TABLE organization.organizations
    ADD COLUMN IF NOT EXISTS bank_type VARCHAR(50);

ALTER TABLE organization.organizations
    ADD COLUMN IF NOT EXISTS license_number VARCHAR(100);

ALTER TABLE organization.organizations
    ADD COLUMN IF NOT EXISTS pan VARCHAR(20);

ALTER TABLE organization.organizations
    ADD COLUMN IF NOT EXISTS gst_no VARCHAR(15);

ALTER TABLE organization.organizations
    ADD COLUMN IF NOT EXISTS website VARCHAR(255);

ALTER TABLE organization.organizations
    ADD COLUMN IF NOT EXISTS logo TEXT;

ALTER TABLE organization.organizations
    ADD COLUMN IF NOT EXISTS regulatory_authority_id UUID;

ALTER TABLE organization.organizations
    ADD COLUMN IF NOT EXISTS regulatory_status VARCHAR(50) DEFAULT 'ACTIVE';

ALTER TABLE organization.organizations
    ADD COLUMN IF NOT EXISTS country VARCHAR(100) DEFAULT 'India';

-- Regulatory details
ALTER TABLE organization.organizations
    ADD COLUMN IF NOT EXISTS direct_clearing_member BOOLEAN,
    ADD COLUMN IF NOT EXISTS direct_member_iftas BOOLEAN,
    ADD COLUMN IF NOT EXISTS micr_city_code VARCHAR(3),
    ADD COLUMN IF NOT EXISTS micr_bank_code VARCHAR(3),
    ADD COLUMN IF NOT EXISTS micr_branch_code VARCHAR(3),
    ADD COLUMN IF NOT EXISTS ifsc_code VARCHAR(11),
    ADD COLUMN IF NOT EXISTS number_of_branches INTEGER,
    ADD COLUMN IF NOT EXISTS sponsor_bank_for_clearing VARCHAR(150),
    ADD COLUMN IF NOT EXISTS sponsor_bank_for_iftas VARCHAR(150);

-- Address details
ALTER TABLE organization.organizations
    ADD COLUMN IF NOT EXISTS address_type VARCHAR(50),
    ADD COLUMN IF NOT EXISTS unit_gala_name_number VARCHAR(200),
    ADD COLUMN IF NOT EXISTS street_road VARCHAR(200),
    ADD COLUMN IF NOT EXISTS landmark VARCHAR(150),
    ADD COLUMN IF NOT EXISTS city VARCHAR(100),
    ADD COLUMN IF NOT EXISTS state VARCHAR(100),
    ADD COLUMN IF NOT EXISTS pincode VARCHAR(6);

-- -----------------------------------------------------------------------
-- identity.roles  (master — INTERNAL_ADMIN / SUPER_ADMIN)
-- -----------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS identity.roles (
    id            SERIAL PRIMARY KEY,
    name          VARCHAR(50)  NOT NULL UNIQUE,
    panel         VARCHAR(30)  NOT NULL,
    description   VARCHAR(255),
    created_at    TIMESTAMP    NOT NULL DEFAULT now()
);

-- -----------------------------------------------------------------------
-- identity.permissions (Master DB)
-- -----------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS identity.permissions (
    id          SERIAL       PRIMARY KEY,
    code        VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255),
    module      VARCHAR(60),
    is_system   BOOLEAN      NOT NULL DEFAULT false,
    created_at  TIMESTAMP    NOT NULL DEFAULT now()
);

-- -----------------------------------------------------------------------
-- identity.role_permissions (Master DB)
-- -----------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS identity.role_permissions (
    role_id       INT NOT NULL REFERENCES identity.roles(id) ON DELETE CASCADE,
    permission_id INT NOT NULL REFERENCES identity.permissions(id) ON DELETE CASCADE,
    PRIMARY KEY (role_id, permission_id)
);


-- -----------------------------------------------------------------------
-- identity.internal_users
-- Full extended schema matching the senior's DB design
-- -----------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS identity.internal_users (
    -- Core identity
    id                        BIGSERIAL    PRIMARY KEY,                    -- Pkid
    emp_no                    VARCHAR(30)  UNIQUE,                         -- EmpNo
    username                  VARCHAR(80)  UNIQUE NOT NULL,                -- UserName (login credential)
    role_id                   INT          NOT NULL REFERENCES identity.roles(id),
    password_hash             VARCHAR(255) NOT NULL,                       -- Password

    -- Security
    two_fa_enabled            BOOLEAN      NOT NULL DEFAULT false,         -- 2fA
    two_fa_otp_hash           VARCHAR(255),
    two_fa_otp_expiry         TIMESTAMP,
    status                    VARCHAR(30)  NOT NULL DEFAULT 'OPERATIVE',   -- Status

    -- Personal info
    first_name                VARCHAR(80)  NOT NULL,                       -- Name (split)
    middle_name               VARCHAR(80),
    last_name                 VARCHAR(80)  NOT NULL,
    dob                       DATE,                                         -- DOB
    email                     VARCHAR(150) UNIQUE,                         -- Mail
    mobile                    VARCHAR(20)  UNIQUE,                         -- Mobile
    gender                    VARCHAR(10),                                  -- MALE/FEMALE/OTHER
    designation               VARCHAR(100),                                 -- Designation

    -- Login controls
    login_on_holidays         BOOLEAN      NOT NULL DEFAULT true,          -- Holiday_Login
    login_time                TIME,
    logout_time               TIME,
    inactive_session_timeout  INT          NOT NULL DEFAULT 1800,          -- Inactive_session_timeout

    -- Login tracking
    no_of_bad_logins          INT          NOT NULL DEFAULT 0,             -- noofbadlogin
    last_login_date           DATE,                                         -- lastlogindate
    last_login_time           TIME,
    is_active                 BOOLEAN      NOT NULL DEFAULT true,

    created_by                BIGINT,                                       -- CreatedBy
    created_at                TIMESTAMP    NOT NULL DEFAULT now(),          -- CreatedDate
    verified_by               BIGINT,                                       -- VerifiedBy
    verified_date             TIMESTAMP,                                    -- VerifiedDate
    modified_by               BIGINT,                                       -- ModifiedBy
    updated_at                TIMESTAMP    NOT NULL DEFAULT now()           -- ModifiedDate
);

ALTER TABLE identity.internal_users ADD COLUMN IF NOT EXISTS emp_no VARCHAR(30);
ALTER TABLE identity.internal_users ADD COLUMN IF NOT EXISTS username VARCHAR(80);
ALTER TABLE identity.internal_users ADD COLUMN IF NOT EXISTS status VARCHAR(30) NOT NULL DEFAULT 'OPERATIVE';
ALTER TABLE identity.internal_users ADD COLUMN IF NOT EXISTS is_active BOOLEAN NOT NULL DEFAULT true;
ALTER TABLE identity.internal_users ADD COLUMN IF NOT EXISTS first_name VARCHAR(80);
ALTER TABLE identity.internal_users ADD COLUMN IF NOT EXISTS middle_name VARCHAR(80);
ALTER TABLE identity.internal_users ADD COLUMN IF NOT EXISTS last_name VARCHAR(80);
ALTER TABLE identity.internal_users ADD COLUMN IF NOT EXISTS dob DATE;
ALTER TABLE identity.internal_users ADD COLUMN IF NOT EXISTS mobile VARCHAR(20);
ALTER TABLE identity.internal_users ADD COLUMN IF NOT EXISTS gender VARCHAR(10);
ALTER TABLE identity.internal_users ADD COLUMN IF NOT EXISTS designation VARCHAR(100);
ALTER TABLE identity.internal_users ADD COLUMN IF NOT EXISTS login_on_holidays BOOLEAN NOT NULL DEFAULT true;
ALTER TABLE identity.internal_users ADD COLUMN IF NOT EXISTS login_time TIME;
ALTER TABLE identity.internal_users ADD COLUMN IF NOT EXISTS logout_time TIME;
ALTER TABLE identity.internal_users ADD COLUMN IF NOT EXISTS inactive_session_timeout INT NOT NULL DEFAULT 1800;
ALTER TABLE identity.internal_users ADD COLUMN IF NOT EXISTS no_of_bad_logins INT NOT NULL DEFAULT 0;
ALTER TABLE identity.internal_users ADD COLUMN IF NOT EXISTS last_login_date DATE;
ALTER TABLE identity.internal_users ADD COLUMN IF NOT EXISTS last_login_time TIME;
ALTER TABLE identity.internal_users ADD COLUMN IF NOT EXISTS two_fa_enabled BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE identity.internal_users ADD COLUMN IF NOT EXISTS two_fa_otp_hash VARCHAR(255);
ALTER TABLE identity.internal_users ADD COLUMN IF NOT EXISTS two_fa_otp_expiry TIMESTAMP;
ALTER TABLE identity.internal_users ADD COLUMN IF NOT EXISTS verified_by BIGINT;
ALTER TABLE identity.internal_users ADD COLUMN IF NOT EXISTS verified_date TIMESTAMP;
ALTER TABLE identity.internal_users ADD COLUMN IF NOT EXISTS modified_by BIGINT;

-- Backfill existing rows that were created before these columns were added
UPDATE identity.internal_users SET username = COALESCE(email, 'admin') WHERE username IS NULL;
UPDATE identity.internal_users SET emp_no = 'EMP001' WHERE emp_no IS NULL;

-- -----------------------------------------------------------------------
-- identity.login_directory
-- -----------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS identity.login_directory (
    id                BIGSERIAL    PRIMARY KEY,
    user_code         VARCHAR(30)  UNIQUE,
    email             VARCHAR(150) UNIQUE,
    phone             VARCHAR(20)  UNIQUE,
    organization_id   BIGINT       NOT NULL REFERENCES organization.organizations(id),
    user_type         VARCHAR(20)  NOT NULL,
    created_at        TIMESTAMP    NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_login_directory_org ON identity.login_directory(organization_id);

-- -----------------------------------------------------------------------
-- identity.master_password_reset_tokens
-- Self-service forgot-password tokens for SUPER_ADMIN / internal users
-- -----------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS identity.master_password_reset_tokens (
    id          BIGSERIAL    PRIMARY KEY,
    emp_no      VARCHAR(30)  NOT NULL,
    token_hash  VARCHAR(255) NOT NULL,            -- SHA-256 of raw token
    used        BOOLEAN      NOT NULL DEFAULT false,
    expires_at  TIMESTAMP    NOT NULL,
    created_at  TIMESTAMP    NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_master_prt_emp_no ON identity.master_password_reset_tokens(emp_no);

-- -----------------------------------------------------------------------
-- identity.refresh_tokens (Master DB)
-- Needed for Super Admin refresh tokens
-- -----------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS identity.refresh_tokens (
    id                  BIGSERIAL    PRIMARY KEY,
    user_id             BIGINT       NOT NULL,
    token               VARCHAR(500) NOT NULL UNIQUE,
    user_type           VARCHAR(20)  NOT NULL,
    organization_code   VARCHAR(30),
    expiry_date         TIMESTAMP    NOT NULL,
    revoked             BOOLEAN      NOT NULL DEFAULT false,
    created_at          TIMESTAMP    NOT NULL DEFAULT now()
);

-- -----------------------------------------------------------------------
-- identity.lookup_types (Table 51001) - Master Lookup Types
-- -----------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS identity.lookup_types (
    id            BIGSERIAL    PRIMARY KEY,
    code          VARCHAR(50)  NOT NULL UNIQUE,
    description   VARCHAR(255) NOT NULL,
    is_fixed      BOOLEAN      NOT NULL DEFAULT false,
    is_active     BOOLEAN      NOT NULL DEFAULT true,
    created_by    BIGINT,
    created_at    TIMESTAMP    NOT NULL DEFAULT now(),
    modified_by   BIGINT,
    updated_at    TIMESTAMP    NOT NULL DEFAULT now()
);

-- -----------------------------------------------------------------------
-- identity.lookup_sub_types (Table 51101) - Master Lookup Sub Types
-- -----------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS identity.lookup_sub_types (
    id                    BIGSERIAL    PRIMARY KEY,
    lookup_type_code      VARCHAR(50)  NOT NULL,
    type_description      VARCHAR(255),
    sub_type_code         VARCHAR(50)  NOT NULL,
    sub_type_description  VARCHAR(255) NOT NULL,
    is_fixed              BOOLEAN      NOT NULL DEFAULT false,
    is_active             BOOLEAN      NOT NULL DEFAULT true,
    display_order         INT          NOT NULL DEFAULT 0,
    created_by            BIGINT,
    created_at            TIMESTAMP    NOT NULL DEFAULT now(),
    modified_by           BIGINT,
    updated_at            TIMESTAMP    NOT NULL DEFAULT now(),
    CONSTRAINT uk_master_lookup_sub_type UNIQUE (lookup_type_code, sub_type_code)
);

CREATE INDEX IF NOT EXISTS idx_master_lst_type_code ON identity.lookup_sub_types(lookup_type_code);

