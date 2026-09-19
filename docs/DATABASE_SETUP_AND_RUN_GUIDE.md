# Complete Guide: Running the LOS Backend & Connecting Local/Live PostgreSQL

This document provides step-by-step instructions on how to set up, configure, and run the **Loan Origination System (LOS)** Spring Boot backend with both **Local PostgreSQL** and **Live/Production PostgreSQL** databases.

---

## 📋 Table of Contents
1. [Prerequisites](#1-prerequisites)
2. [PostgreSQL Database Setup (Local & Live)](#2-postgresql-database-setup-local--live)
   - [Step 2.1: Create Databases](#step-21-create-databases)
   - [Step 2.2: Apply Database Schemas](#step-22-apply-database-schemas)
3. [Configuring Connection Credentials](#3-configuring-connection-credentials)
   - [Option A: Using Environment Variables (Recommended)](#option-a-using-environment-variables-recommended)
   - [Option B: Modifying `application-dev.yml`](#option-b-modifying-application-devyml)
4. [Running the Application](#4-running-the-application)
   - [Mode 1: Live / Local PostgreSQL (`dev` profile)](#mode-1-live--local-postgresql-dev-profile)
   - [Mode 2: Zero-Config In-Memory Emulation (`local` profile)](#mode-2-zero-config-in-memory-emulation-local-profile)
   - [Mode 3: Using Docker](#mode-3-using-docker)
5. [Testing & Verification](#5-testing--verification)
   - [Interactive Swagger UI](#51-interactive-swagger-ui)
   - [Testing via cURL](#52-testing-via-curl)
6. [Demo User Credentials](#6-demo-user-credentials)
7. [Troubleshooting Common Issues](#7-troubleshooting-common-issues)

---

## 1. Prerequisites

Make sure the following tools are installed on your system:
- **Java 21 LTS** (`java -version`)
- **Maven 3.8+** (`mvn -v`)
- **PostgreSQL 14+** (`psql --version` or pgAdmin / DBeaver)

---

## 2. PostgreSQL Database Setup (Local & Live)

The LOS architecture uses **database-per-tenant isolation**:
- `los_master_db` — Central registry and global login routing directory.
- `los_hdfc01_db` — Dedicated database for HDFC Bank tenant.
- `los_bajaj02_db` — Dedicated database for Bajaj Finance tenant (optional sample).

---

### Step 2.1: Create Databases

Open your terminal or PostgreSQL CLI (`psql`) and execute:

```bash
# Connect to PostgreSQL as superuser
psql -U postgres

# Or if you are on Mac Homebrew:
# psql postgres
```

Inside `psql`, create the databases:

```sql
-- 1. Create Master Database
CREATE DATABASE los_master_db;

-- 2. Create Tenant Databases
CREATE DATABASE los_hdfc01_db;
CREATE DATABASE los_bajaj02_db;

-- Exit psql
\q
```

*(Alternatively, you can create these databases using **pgAdmin** or **DBeaver** with right-click -> Create Database)*.

---

### Step 2.2: Apply Database Schemas

The repository provides the complete schema script in [`LOS_Database_Schema.sql`](file:///Users/khushmeet/Project/LOS/LOS_Database_Schema.sql).

#### Apply Part A to `los_master_db`:
```bash
# Apply Part A (Master DB Schema & Roles)
psql -U postgres -d los_master_db -f src/main/resources/db/master-schema.sql
```

#### Apply Part B to each Tenant Database:
```bash
# Apply Part B to HDFC Bank database
psql -U postgres -d los_hdfc01_db -f src/main/resources/db/tenant-schema.sql

# Apply Part B to Bajaj Finance database
psql -U postgres -d los_bajaj02_db -f src/main/resources/db/tenant-schema.sql
```

> [!NOTE]
> When the application starts, the built-in [`DatabaseSeeder`](file:///Users/khushmeet/Project/LOS/src/main/java/com/bank/los/db/init/DatabaseSeeder.java) automatically verifies tables, creates missing schemas, and seeds initial demo users and organizations if they do not exist.

---

## 3. Configuring Connection Credentials

### Option A: Using Environment Variables (Recommended)

Set environment variables in your terminal or IDE run configuration:

```bash
# Master Database Configuration
export MASTER_DB_URL=jdbc:postgresql://localhost:5432/los_master_db
export MASTER_DB_USERNAME=postgres
export MASTER_DB_PASSWORD=your_postgres_password

# Tenant Database Configuration (Used as base template for dynamic routing)
export TENANT_DB_HOST=localhost
export TENANT_DB_PORT=5432
export TENANT_DB_USERNAME=postgres
export TENANT_DB_PASSWORD=your_postgres_password
```

For a **Live/Remote PostgreSQL Server** (e.g. AWS RDS, Azure, Supabase, Neon), simply update the host and credentials:
```bash
export MASTER_DB_URL=jdbc:postgresql://db.example.com:5432/los_master_db
export MASTER_DB_USERNAME=dbadmin
export MASTER_DB_PASSWORD=SecurePassword123!
export TENANT_DB_HOST=db.example.com
export TENANT_DB_PORT=5432
export TENANT_DB_USERNAME=dbadmin
export TENANT_DB_PASSWORD=SecurePassword123!
```

---

### Option B: Modifying `application-dev.yml`

You can directly edit [`src/main/resources/application-dev.yml`](file:///Users/khushmeet/Project/LOS/src/main/resources/application-dev.yml):

```yaml
spring:
  config:
    activate:
      on-profile: dev
  jpa:
    database-platform: org.hibernate.dialect.PostgreSQLDialect
    hibernate:
      ddl-auto: update
    show-sql: true

master:
  datasource:
    url: jdbc:postgresql://localhost:5432/los_master_db
    username: postgres
    password: postgres
    driver-class-name: org.postgresql.Driver

tenant:
  datasource:
    driver-class-name: org.postgresql.Driver
    default-host: localhost
    default-port: 5432
    username: postgres
    password: postgres
```

---

## 4. Running the Application

### Mode 1: Live / Local PostgreSQL (`dev` profile)

Run the application pointing to your PostgreSQL instance:

```bash
JAVA_HOME=$(/usr/libexec/java_home -v 21) mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

---

### Mode 2: Zero-Config In-Memory Emulation (`local` profile)

If PostgreSQL is not running and you want to test immediately without installing or starting a database server:

```bash
JAVA_HOME=$(/usr/libexec/java_home -v 21) mvn spring-boot:run -Dspring-boot.run.profiles=local
```
*(Runs with full multi-tenant schema emulation and pre-seeded demo accounts in memory)*.

---

### Mode 3: Using Docker

Build and run in a container:

```bash
# 1. Package JAR
JAVA_HOME=$(/usr/libexec/java_home -v 21) mvn clean package -DskipTests

# 2. Build Docker Image
docker build -t los-backend:1.0.0 .

# 3. Run Container
docker run -p 8080:8080 \
  -e MASTER_DB_URL=jdbc:postgresql://host.docker.internal:5432/los_master_db \
  -e MASTER_DB_USERNAME=postgres \
  -e MASTER_DB_PASSWORD=postgres \
  -e TENANT_DB_HOST=host.docker.internal \
  los-backend:1.0.0
```

---

## 5. Testing & Verification

### 5.1 Interactive Swagger UI

Open your browser and navigate to:
🔗 **[http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)**

1. Scroll to the **Authentication** section (`POST /api/v1/auth/login`).
2. Click **Try it out** and enter credentials (e.g. `superadmin@hdfcbank.com` / `Admin@123`).
3. Click **Execute**.
4. Copy the `accessToken` from the response.
5. Click the green **Authorize 🔓** button at the top right of the Swagger UI.
6. Paste the token into the `Value` box and click **Authorize**.
7. Now you can execute any secured endpoint like `GET /api/v1/dashboard/tenant-admin` or `GET /api/v1/users`!

---

### 5.2 Testing via cURL

#### 1. Login as Bank Super Admin:
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "superadmin@hdfcbank.com",
    "password": "Admin@123"
  }'
```

**Sample Response:**
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
    "tokenType": "Bearer",
    "expiresIn": 86400000,
    "user": {
      "id": 1,
      "userCode": "HDFC-ADM-01",
      "fullName": "Vikram Aditya Mehta",
      "email": "superadmin@hdfcbank.com",
      "role": "SUPER_ADMIN",
      "userType": "STAFF",
      "organizationName": "HDFC Bank",
      "organizationCode": "HDFC01"
    },
    "dashboardUrl": "/dashboard/tenant-admin",
    "permissions": [
      "BRANCH_MANAGE",
      "STAFF_USER_MANAGE",
      "LOAN_SCHEME_CONFIG",
      "REPORTS_EXPORT",
      "CUSTOMER_VIEW_ALL"
    ]
  }
}
```

#### 2. Access Tenant Admin Dashboard:
```bash
curl -X GET http://localhost:8080/api/v1/dashboard/tenant-admin \
  -H "Authorization: Bearer <YOUR_ACCESS_TOKEN>"
```

#### 3. Login as Loan Maker & Access Maker Dashboard:
```bash
# Login
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email": "maker@hdfcbank.com", "password": "Maker@123"}'

# Access Maker Dashboard
curl -X GET http://localhost:8080/api/v1/dashboard/maker \
  -H "Authorization: Bearer <MAKER_ACCESS_TOKEN>"
```

#### 4. Login as Loan Checker & Access Checker Queue:
```bash
# Login
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email": "checker@hdfcbank.com", "password": "Checker@123"}'

# Access Checker Approval Queue
curl -X GET http://localhost:8080/api/v1/dashboard/checker \
  -H "Authorization: Bearer <CHECKER_ACCESS_TOKEN>"
```

---

## 6. Demo User Credentials

| Role | Email | Password | Organization | Permitted Dashboard |
| :--- | :--- | :--- | :--- | :--- |
| **`INTERNAL_ADMIN`** | `admin@losplatform.com` | `Admin@123` | Platform Master | `GET /api/v1/dashboard/internal-admin` |
| **`SUPER_ADMIN`** | `superadmin@hdfcbank.com` | `Admin@123` | HDFC Bank (`HDFC01`) | `GET /api/v1/dashboard/tenant-admin` |
| **`MAKER`** | `maker@hdfcbank.com` | `Maker@123` | HDFC Bank (`HDFC01`) | `GET /api/v1/dashboard/maker` |
| **`CHECKER`** | `checker@hdfcbank.com` | `Checker@123` | HDFC Bank (`HDFC01`) | `GET /api/v1/dashboard/checker` |
| **`VIEWER`** | `viewer@hdfcbank.com` | `Viewer@123` | HDFC Bank (`HDFC01`) | `GET /api/v1/dashboard/viewer` |
| **`CUSTOMER`** | `rajesh.kumar@gmail.com` | `Customer@123` | HDFC Bank (`HDFC01`) | `GET /api/v1/dashboard/customer` |
| **`SUPER_ADMIN`** | `superadmin@bajajfinance.com` | `Admin@123` | Bajaj Finance (`BAJAJ02`) | `GET /api/v1/dashboard/tenant-admin` |

---

## 7. Troubleshooting Common Issues

### ❌ Issue 1: `Connection to localhost:5432 refused`
- **Cause**: PostgreSQL service is not running on your machine.
- **Fix**:
  - On macOS (Homebrew): `brew services start postgresql@14` (or `brew services start postgresql`)
  - On Linux: `sudo systemctl start postgresql`
  - On Windows: Start PostgreSQL service via Services app (`services.msc`)
  - *Or run with `-Dspring-boot.run.profiles=local` for embedded mode.*

### ❌ Issue 2: `FATAL: database "los_master_db" does not exist`
- **Cause**: You haven't created the `los_master_db` database in PostgreSQL.
- **Fix**: Run `createdb los_master_db` or `CREATE DATABASE los_master_db;` in `psql`.

### ❌ Issue 3: `password authentication failed for user "postgres"`
- **Cause**: The PostgreSQL password does not match `postgres`.
- **Fix**: Pass your actual password via environment variable `export MASTER_DB_PASSWORD=your_actual_password` or edit `src/main/resources/application-dev.yml`.
