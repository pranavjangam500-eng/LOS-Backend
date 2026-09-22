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
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(150) NOT NULL,
    code            VARCHAR(20)  NOT NULL UNIQUE,
    type            VARCHAR(20)  NOT NULL,
    status          VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    contact_email   VARCHAR(150),
    contact_phone   VARCHAR(20),
    db_name         VARCHAR(100) NOT NULL UNIQUE,
    db_host         VARCHAR(150) NOT NULL DEFAULT 'localhost',
    db_port         INT          NOT NULL DEFAULT 5432,
    created_by      BIGINT,
    created_at      TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at      TIMESTAMP    NOT NULL DEFAULT now()
);

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

-- Migration safety: ensure newly added columns exist in older DB instances
ALTER TABLE identity.internal_users ADD COLUMN IF NOT EXISTS emp_no VARCHAR(30);
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
