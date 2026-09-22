-- =======================================================================
-- TENANT DB SCHEMA TEMPLATE
-- Run inside EACH bank/NBFC database (e.g. los_hdfc01_db, los_bajaj02_db)
-- =======================================================================
CREATE SCHEMA IF NOT EXISTS organization;
CREATE SCHEMA IF NOT EXISTS identity;
CREATE SCHEMA IF NOT EXISTS customer;

-- -----------------------------------------------------------------------
-- organization.branches
-- -----------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS organization.branches (
    id          BIGSERIAL    PRIMARY KEY,
    name        VARCHAR(150) NOT NULL,
    code        VARCHAR(20)  NOT NULL UNIQUE,
    address     VARCHAR(255),
    city        VARCHAR(100),
    state       VARCHAR(100),
    pincode     VARCHAR(10),
    status      VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    created_at  TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at  TIMESTAMP    NOT NULL DEFAULT now()
);

-- -----------------------------------------------------------------------
-- identity.roles  (5 tenant roles: ADMIN, CHECKER, MAKER, VIEWER, CUSTOMER)
-- -----------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS identity.roles (
    id            SERIAL       PRIMARY KEY,
    name          VARCHAR(50)  NOT NULL UNIQUE,
    panel         VARCHAR(30)  NOT NULL,
    description   VARCHAR(255),
    created_at    TIMESTAMP    NOT NULL DEFAULT now()
);

-- -----------------------------------------------------------------------
-- identity.permissions  (lookup codes — DB-backed, configurable by ADMIN)
-- -----------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS identity.permissions (
    id          SERIAL       PRIMARY KEY,
    code        VARCHAR(100) NOT NULL UNIQUE,   -- e.g. LOAN_APPLICATION_CREATE
    description VARCHAR(255),
    module      VARCHAR(60),                    -- e.g. LOAN, USER, BRANCH
    created_at  TIMESTAMP    NOT NULL DEFAULT now()
);

-- -----------------------------------------------------------------------
-- identity.role_permissions  (M2M: role → permissions)
-- -----------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS identity.role_permissions (
    role_id       INT NOT NULL REFERENCES identity.roles(id),
    permission_id INT NOT NULL REFERENCES identity.permissions(id),
    PRIMARY KEY (role_id, permission_id)
);

-- -----------------------------------------------------------------------
-- identity.users  (Bank/NBFC staff — full schema per senior's design)
-- -----------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS identity.users (
    -- Core identity
    id                        BIGSERIAL    PRIMARY KEY,                        -- Pkid
    emp_no                    VARCHAR(30)  UNIQUE,                             -- EmpNo (auto-generated)
    username                  VARCHAR(80)  UNIQUE NOT NULL,                    -- UserName (login credential)
    password_hash             VARCHAR(255) NOT NULL,                           -- Password (BCrypt)

    -- Security
    two_fa_enabled            BOOLEAN      NOT NULL DEFAULT true,              -- 2fA
    two_fa_otp_hash           VARCHAR(255),                                    -- BCrypt hash of OTP
    two_fa_otp_expiry         TIMESTAMP,
    status                    VARCHAR(30)  NOT NULL DEFAULT 'PENDING_VERIFICATION', -- Status

    -- Personal info
    first_name                VARCHAR(80)  NOT NULL,                           -- Name (split for best practice)
    middle_name               VARCHAR(80),
    last_name                 VARCHAR(80)  NOT NULL,
    dob                       DATE,                                             -- DOB
    email                     VARCHAR(150) UNIQUE,                             -- Mail
    mobile                    VARCHAR(20)  UNIQUE,                             -- Mobile
    gender                    VARCHAR(10),                                      -- MALE/FEMALE/OTHER
    designation               VARCHAR(100),                                     -- Designation

    -- Role & Branch
    role_id                   INT          NOT NULL REFERENCES identity.roles(id), -- Role
    multi_branch_access       BOOLEAN      NOT NULL DEFAULT false,             -- M_Br_access
    login_branch_id           BIGINT       REFERENCES organization.branches(id), -- Login_Branch

    -- Login controls
    login_on_holidays         BOOLEAN      NOT NULL DEFAULT false,             -- Holiday_Login
    login_time                TIME,                                             -- allowed login window start
    logout_time               TIME,                                             -- allowed logout window end
    inactive_session_timeout  INT          NOT NULL DEFAULT 1800,              -- Inactive_session_timeout (seconds)

    -- Login tracking
    no_of_bad_logins          INT          NOT NULL DEFAULT 0,                 -- noofbadlogin
    last_login_date           DATE,                                             -- lastlogindate
    last_login_time           TIME,
    is_active                 BOOLEAN      NOT NULL DEFAULT true,

    -- Audit (senior's design)
    created_by                BIGINT,                                           -- CreatedBy
    created_at                TIMESTAMP    NOT NULL DEFAULT now(),              -- CreatedDate
    verified_by               BIGINT,                                           -- VerifiedBy
    verified_date             TIMESTAMP,                                        -- VerifiedDate
    modified_by               BIGINT,                                           -- ModifiedBy
    updated_at                TIMESTAMP    NOT NULL DEFAULT now()               -- ModifiedDate
);

CREATE INDEX IF NOT EXISTS idx_users_role_id       ON identity.users(role_id);
CREATE INDEX IF NOT EXISTS idx_users_login_branch  ON identity.users(login_branch_id);
CREATE INDEX IF NOT EXISTS idx_users_status        ON identity.users(status);

-- -----------------------------------------------------------------------
-- identity.user_branches  (for multi_branch_access = true)
-- -----------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS identity.user_branches (
    user_id     BIGINT NOT NULL REFERENCES identity.users(id) ON DELETE CASCADE,
    branch_id   BIGINT NOT NULL REFERENCES organization.branches(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, branch_id)
);

-- -----------------------------------------------------------------------
-- identity.otp_tokens  (2FA OTP — always enforced for tenant users)
-- -----------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS identity.otp_tokens (
    id          BIGSERIAL    PRIMARY KEY,
    user_id     BIGINT       NOT NULL REFERENCES identity.users(id) ON DELETE CASCADE,
    otp_hash    VARCHAR(255) NOT NULL,            -- BCrypt hash of the 6-digit OTP
    used        BOOLEAN      NOT NULL DEFAULT false,
    expires_at  TIMESTAMP    NOT NULL,
    created_at  TIMESTAMP    NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_otp_user_id ON identity.otp_tokens(user_id);

-- -----------------------------------------------------------------------
-- identity.user_password_resets
-- Admin-initiated password reset (maker-checker dual-control)
-- Senior's DB design: Pkid, EmpNo, Password, CreatedBy/Date, VerifiedBy/Date
-- -----------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS identity.user_password_resets (
    id                BIGSERIAL    PRIMARY KEY,                                -- Pkid
    emp_no            VARCHAR(30)  NOT NULL,                                   -- EmpNo
    new_password_hash VARCHAR(255) NOT NULL,                                   -- Password (BCrypt of new pwd)
    created_by        BIGINT       NOT NULL,                                   -- CreatedBy
    created_at        TIMESTAMP    NOT NULL DEFAULT now(),                     -- CreatedDate
    verified_by       BIGINT,                                                   -- VerifiedBy
    verified_date     TIMESTAMP,                                                -- VerifiedDate
    applied           BOOLEAN      NOT NULL DEFAULT false,
    expires_at        TIMESTAMP    NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_upr_emp_no ON identity.user_password_resets(emp_no);

-- -----------------------------------------------------------------------
-- identity.self_service_reset_tokens
-- Forgot-password OTP tokens (self-service, NOT admin-initiated)
-- Production-grade: token_hash = SHA-256 of raw token (raw never stored)
-- -----------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS identity.self_service_reset_tokens (
    id          BIGSERIAL    PRIMARY KEY,
    emp_no      VARCHAR(30)  NOT NULL,
    token_hash  VARCHAR(255) NOT NULL,            -- SHA-256 of raw token
    used        BOOLEAN      NOT NULL DEFAULT false,
    expires_at  TIMESTAMP    NOT NULL,
    created_at  TIMESTAMP    NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_ssrt_emp_no ON identity.self_service_reset_tokens(emp_no);

-- -----------------------------------------------------------------------
-- identity.session_activity  (per-JWT activity for auto-logout)
-- -----------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS identity.session_activity (
    jti           VARCHAR(100) PRIMARY KEY,       -- JWT ID (jti claim)
    user_id       BIGINT       NOT NULL,
    last_seen     TIMESTAMP    NOT NULL,
    timeout_secs  INT          NOT NULL DEFAULT 1800,
    invalidated   BOOLEAN      NOT NULL DEFAULT false,
    created_at    TIMESTAMP    NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_session_user_id ON identity.session_activity(user_id);
CREATE INDEX IF NOT EXISTS idx_session_invalidated ON identity.session_activity(invalidated);

-- -----------------------------------------------------------------------
-- identity.refresh_tokens
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
-- customer.customers
-- -----------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS customer.customers (
    id               BIGSERIAL    PRIMARY KEY,
    customer_code    VARCHAR(30)  UNIQUE,
    branch_id        BIGINT       REFERENCES organization.branches(id),
    first_name       VARCHAR(80)  NOT NULL,
    middle_name      VARCHAR(80),
    last_name        VARCHAR(80)  NOT NULL,
    email            VARCHAR(150) UNIQUE,
    phone            VARCHAR(20)  UNIQUE,
    password_hash    VARCHAR(255),
    is_active        BOOLEAN      NOT NULL DEFAULT true,
    last_login_at    TIMESTAMP,
    created_at       TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at       TIMESTAMP    NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_customers_branch_id ON customer.customers(branch_id);
