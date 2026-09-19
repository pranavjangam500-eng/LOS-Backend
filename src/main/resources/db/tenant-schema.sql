CREATE SCHEMA IF NOT EXISTS organization;
CREATE SCHEMA IF NOT EXISTS identity;
CREATE SCHEMA IF NOT EXISTS customer;

CREATE TABLE IF NOT EXISTS organization.branches (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(150) NOT NULL,
    code            VARCHAR(20) NOT NULL UNIQUE,
    address         VARCHAR(255),
    city            VARCHAR(100),
    state           VARCHAR(100),
    pincode         VARCHAR(10),
    status          VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at      TIMESTAMP NOT NULL DEFAULT now(),
    updated_at      TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS identity.roles (
    id            SERIAL PRIMARY KEY,
    name          VARCHAR(50) NOT NULL UNIQUE,
    panel         VARCHAR(30) NOT NULL,
    description   VARCHAR(255),
    created_at    TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS identity.users (
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
    created_by              BIGINT,
    created_at              TIMESTAMP NOT NULL DEFAULT now(),
    updated_at              TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS customer.customers (
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

CREATE TABLE IF NOT EXISTS identity.refresh_tokens (
    id                  BIGSERIAL PRIMARY KEY,
    user_id             BIGINT NOT NULL,
    token               VARCHAR(500) NOT NULL UNIQUE,
    user_type           VARCHAR(20) NOT NULL,
    organization_code   VARCHAR(30),
    expiry_date         TIMESTAMP NOT NULL,
    revoked             BOOLEAN NOT NULL DEFAULT false,
    created_at          TIMESTAMP NOT NULL DEFAULT now()
);
