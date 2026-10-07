# Loan Origination System (LOS) Backend - Security & Postman API Testing Guide

This comprehensive guide details the architecture, security enhancements, and testing procedures for the **LOS Multi-Tenant Spring Boot Backend**, accompanied by the complete Postman API collection of **111 endpoints** across **21 functional modules**.

---

## 📦 Postman Collection File

The complete, importable Postman Collection (v2.1.0) is located at:
- **File**: [`docs/LOS_Backend_Postman_Collection.json`](file:///c:/Users/Pranav%20Jangam/Desktop/LOS-BACKEND/docs/LOS_Backend_Postman_Collection.json)

### Collection Summary
- **Total Requests**: 111 Endpoints
- **Total Modules / Folders**: 21 Folders
- **Pre-configured Variables**: `baseUrl`, `accessToken`, `refreshToken`, `preAuthToken`
- **Dynamic Token Automation**: Login scripts automatically extract and inject the JWT Bearer token into all subsequent authenticated requests.

---

## 🚀 Quickstart & Setup in Postman

1. Open **Postman**.
2. Click **Import** (top left).
3. Drag & drop or browse to [`docs/LOS_Backend_Postman_Collection.json`](file:///c:/Users/Pranav%20Jangam/Desktop/LOS-BACKEND/docs/LOS_Backend_Postman_Collection.json).
4. Click **Import**.
5. Ensure your collection variables or active environment has:
   - `baseUrl`: `http://localhost:8080` (or your deployed server domain)

> 💡 **Automated JWT Token Management**:
> Executing any login request (e.g., `01. Authentication & 2FA -> 1. Login - Master Internal Admin`) automatically executes a Postman Test script that stores `accessToken` and `refreshToken` into your active environment and collection variables. All subsequent requests automatically inherit `Bearer {{accessToken}}` in their Authorization header!

---

## 🔑 Pre-Seeded Default Test Credentials

| Account Persona / Role | Email / Username | Password | Organization DB | Description |
| :--- | :--- | :--- | :--- | :--- |
| **Master Platform Admin** | `admin@losplatform.com` | `Admin@123` | `los_master_db` | Platform-wide institution onboarding & central RBAC |
| **HDFC Bank Super Admin** | `superadmin@hdfcbank.com` | `Admin@123` | `los_hdfc01_db` | Tenant administrator for HDFC Bank |
| **HDFC Loan Maker** | `maker@hdfcbank.com` | `Maker@123` | `los_hdfc01_db` | Initiates customer profiles & loan applications |
| **HDFC Loan Checker** | `checker@hdfcbank.com` | `Checker@123` | `los_hdfc01_db` | Reviews, sanction decisions (Dual-control approval) |
| **HDFC Loan Viewer** | `viewer@hdfcbank.com` | `Viewer@123` | `los_hdfc01_db` | Read-only auditor & compliance reviewer |
| **Retail Loan Customer** | `rajesh.kumar@gmail.com` | `Customer@123` | `los_hdfc01_db` | Customer portal applicant account |
| **Bajaj NBFC Super Admin** | `superadmin@bajajfinance.com` | `Admin@123` | `los_bajaj01_db` | Tenant administrator for Bajaj Finance |

---

## 📋 Comprehensive API Module Breakdown (111 Endpoints)

### `00. System Health & Keep-Alive` (2 endpoints)
- `GET /health` - Root keep-alive ping for uptime monitors
- `GET /api/v1/health` - API v1 service health status & uptime counter

### `01. Authentication & 2FA` (13 endpoints)
- `POST /api/v1/auth/login` (Master Admin) - Master platform authentication
- `POST /api/v1/auth/login` (HDFC Super Admin) - Tenant admin authentication with dynamic routing
- `POST /api/v1/auth/login` (Maker) - Loan originator authentication
- `POST /api/v1/auth/login` (Checker) - Approver authentication
- `POST /api/v1/auth/login` (Customer) - Retail customer login
- `POST /api/v1/auth/verify-otp` - 2FA OTP verification for staff (Step 2 of login)
- `GET /api/v1/auth/me` - Current authenticated user profile & tenant info
- `POST /api/v1/auth/forgot-password` - Request password reset token
- `POST /api/v1/auth/reset-password` - Reset password via token
- `POST /api/v1/auth/refresh-token` - Refresh expired access token
- `POST /api/v1/auth/change-password` - Update authenticated password
- `POST /api/v1/auth/logout` - Revoke refresh token and terminate session
- `GET /api/v1/auth/health` - Auth microservice status

### `02. Administration - Organizations` (8 endpoints)
- `GET /api/v1/administration/organizations` - List all onboarded Bank/NBFC institutions
- `GET /api/v1/administration/organizations/{id}` - Fetch organization details by ID
- `POST /api/v1/administration/organizations` - Onboard new bank with automated Flyway database schema creation
- `PUT /api/v1/administration/organizations/{id}` - Full update of institution details
- `PATCH /api/v1/administration/organizations/{id}` - Partial update of institution details
- `DELETE /api/v1/administration/organizations/{id}` - Deactivate / delete institution (`?hardDelete=false/true`)
- `GET /api/v1/administration/organizations/{id}/roles` - Inspect roles inside target institution DB
- `GET /api/v1/administration/organizations/{id}/branches` - Inspect branches inside target institution DB

### `03. Administration - Platform Administrators` (3 endpoints)
- `GET /api/v1/administration/administrators` - List all internal platform administrators
- `GET /api/v1/administration/administrators/{id}` - Fetch administrator profile by ID
- `POST /api/v1/administration/administrators` - Create new platform administrator

### `04. Administration - Bank Management` (6 endpoints)
- `GET /api/v1/administration/banks` - List all institutions
- `POST /api/v1/administration/banks/onboard` - Onboard bank via bank management controller
- `PATCH /api/v1/administration/banks/{organizationId}/status` - Update operational status (`ACTIVE`, `INACTIVE`, `SUSPENDED`)
- `PUT /api/v1/administration/banks/{id}` - Update institution profile
- `PATCH /api/v1/administration/banks/{id}` - Patch institution profile
- `DELETE /api/v1/administration/banks/{id}` - Deactivate / remove institution

### `05. Administration - Bank User Management` (4 endpoints)
- `GET /api/v1/administration/user-management` - Cross-tenant staff listing (optional `?organizationId=1`)
- `GET /api/v1/administration/user-management/{id}` - View staff profile by ID
- `GET /api/v1/administration/user-management/organizations/{orgId}/users` - List all staff for specific bank
- `POST /api/v1/administration/user-management/users` - Provision Bank Super Admin or Staff in tenant DB

### `06. Administration - Master RBAC` (7 endpoints)
- `GET /api/v1/administration/rbac/permissions` - List central master permissions
- `POST /api/v1/administration/rbac/permissions` - Create new master permission
- `PUT /api/v1/administration/rbac/permissions/{id}` - Modify master permission description/module
- `GET /api/v1/administration/rbac/banks/{bankCode}/summary` - View complete RBAC summary for bank
- `PUT /api/v1/administration/rbac/banks/{bankCode}/roles/{roleName}/permissions` - Configure bank role permissions centrally
- `POST /api/v1/administration/rbac/sync/{bankCode}` - Push master permissions to target bank
- `POST /api/v1/administration/rbac/sync-all` - Push master permissions to ALL active bank databases

### `07. Administration - Master Lookups` (12 endpoints)
- `GET /api/v1/administration/lookups/types` - List all central lookup types
- `GET /api/v1/administration/lookups/types/{code}` - Fetch lookup type with sub-types
- `POST /api/v1/administration/lookups/types` - Create central master lookup type
- `PUT /api/v1/administration/lookups/types/{code}` - Update master lookup type
- `DELETE /api/v1/administration/lookups/types/{code}` - Delete non-fixed lookup type
- `POST /api/v1/administration/lookups/types/{typeCode}/sub-types` - Add option to central lookup
- `PUT /api/v1/administration/lookups/sub-types/{id}` - Update central lookup option
- `DELETE /api/v1/administration/lookups/sub-types/{id}` - Delete central lookup option
- `GET /api/v1/administration/lookups/banks/{bankCode}` - Inspect bank-customized lookups
- `PUT /api/v1/administration/lookups/banks/{bankCode}/types/{code}/permissions` - Set allowed modification permissions
- `POST /api/v1/administration/lookups/sync/{bankCode}` - Sync master lookups to target bank
- `POST /api/v1/administration/lookups/sync-all` - Sync master lookups to ALL bank databases

### `08. Administration - Dashboards, Metrics & Audits` (3 endpoints)
- `GET /api/v1/administration/dashboard` - Platform Master Admin Dashboard
- `GET /api/v1/administration/monitoring/metrics` - System JVM, thread count, multi-tenant connection metrics
- `GET /api/v1/administration/audit` - Paginated audit logs of platform operations

### `09. Bank Tenant - Staff User Management` (5 endpoints)
- `GET /api/v1/users` - List all staff users in organization
- `GET /api/v1/users/{id}` - Get staff user details by ID
- `POST /api/v1/users` - Create bank staff user (dual-control: `PENDING_VERIFICATION`)
- `PATCH /api/v1/users/{id}/verify` - Checker action: approve and activate staff user
- `POST /api/v1/users/{id}/reset-password` - Admin-initiated password reset

### `10. Bank Tenant - RBAC & Permissions` (11 endpoints)
- `GET /api/v1/roles` - List all bank roles
- `GET /api/v1/bank/rbac/permissions` - List all available permissions in bank database
- `GET /api/v1/bank/rbac/roles` - List roles with base, overrides, and effective permissions
- `PUT /api/v1/bank/rbac/roles/{roleName}/permissions` - Assign or modify role permissions
- `GET /api/v1/bank/rbac/designations` - List designation-to-role mappings
- `POST /api/v1/bank/rbac/designations` - Map designation to role
- `DELETE /api/v1/bank/rbac/designations/{designation}` - Delete designation mapping
- `GET /api/v1/bank/rbac/overrides` - List custom ALLOW / DENY overrides
- `POST /api/v1/bank/rbac/overrides` - Set permission override on Role or Designation
- `DELETE /api/v1/bank/rbac/overrides/{id}` - Remove permission override
- `GET /api/v1/bank/rbac/effective-permissions` - Compute effective permissions for user/role/designation

### `11. Bank Tenant - Branches` (3 endpoints)
- `GET /api/v1/branches` - List all active branches in organization
- `GET /api/v1/branches/{id}` - Get branch details by ID
- `POST /api/v1/branches` - Create a new branch

### `12. Bank Tenant - Customers` (4 endpoints)
- `GET /api/v1/customers` - Paginated customer listing (`?page=0&size=20`)
- `GET /api/v1/customers/{id}` - Fetch customer master details
- `POST /api/v1/customers` - Register a new customer
- `PUT /api/v1/customers/{id}` - Update customer details

### `13. Bank Tenant - Leads` (3 endpoints)
- `GET /api/v1/leads` - Paginated leads listing (`?page=0&size=20`)
- `GET /api/v1/leads/{id}` - Fetch lead details by ID
- `POST /api/v1/leads` - Capture new inbound loan lead

### `14. Bank Tenant - Loan Products` (3 endpoints)
- `GET /api/v1/loan-products` - List all loan products offered by bank
- `GET /api/v1/loan-products/{id}` - Get loan product parameters and limits
- `POST /api/v1/loan-products` - Configure a new loan product

### `15. Bank Tenant - Loan Applications` (3 endpoints)
- `GET /api/v1/loan-applications` - Paginated loan applications (`?page=0&size=20`)
- `GET /api/v1/loan-applications/{id}` - Get loan application details & stage
- `POST /api/v1/loan-applications` - Maker initiates loan application (`SUBMITTED`)

### `16. Bank Tenant - Loan Approvals (Maker-Checker)` (3 endpoints)
- `POST /api/v1/loan-approvals/{applicationId}/decision` (APPROVE) - Checker sanctions application
- `POST /api/v1/loan-approvals/{applicationId}/decision` (REJECT) - Checker rejects application
- `POST /api/v1/loan-approvals/{applicationId}/decision` (RETURN_TO_MAKER) - Checker returns application for rework

### `17. Bank Tenant - Lookups` (9 endpoints)
- `GET /api/v1/bank/lookups/types` - List active lookup types in bank
- `GET /api/v1/bank/lookups/types/{code}` - Get lookup type details & options
- `GET /api/v1/bank/lookups/{code}/options` - Get dropdown options for lookup code
- `POST /api/v1/bank/lookups/types/{typeCode}/sub-types` - Add custom option to bank DB
- `POST /api/v1/bank/lookups/types/{typeCode}/import-master/{subTypeCode}` - Import option from Master DB
- `PUT /api/v1/bank/lookups/sub-types/{id}` - Update bank-specific option
- `DELETE /api/v1/bank/lookups/sub-types/{id}` - Delete bank-specific option
- `PATCH /api/v1/bank/lookups/sub-types/{id}/activate` - Activate lookup option
- `PATCH /api/v1/bank/lookups/sub-types/{id}/deactivate` - Deactivate lookup option

### `18. Bank Tenant - Dashboards (Role-Based)` (5 endpoints)
- `GET /api/v1/dashboard/admin` - Bank/NBFC Admin Dashboard
- `GET /api/v1/dashboard/maker` - Maker Dashboard (Drafts, Submitted, Returned queues)
- `GET /api/v1/dashboard/checker` - Checker Dashboard (Sanction queues, SLA metrics)
- `GET /api/v1/dashboard/viewer` - Auditor / Viewer Dashboard (Portfolio trends)
- `GET /api/v1/dashboard/customer` - Customer Portal Dashboard (Applicant loan tracking)

### `19. Bank Tenant - Audit Logs` (1 endpoint)
- `GET /api/v1/audit-logs` - Paginated activity audit logs for bank operations

### `20. Bank Tenant - Third-Party Integrations` (3 endpoints)
- `POST /api/v1/bank/integration/kyc/verify` - KYC Document verification (PAN/Aadhaar/Passport)
- `POST /api/v1/bank/integration/bureau/check` - Credit Bureau score & report check (CIBIL/Experian)
- `POST /api/v1/bank/integration/lms/disburse-sync` - Disbursal & loan account handoff to Core Banking / LMS

---

## 🛡️ Enterprise Security Features Verified in Collection

1. **OWASP HTTP Security Headers**: CSP, HSTS, X-Frame-Options (`DENY`), X-Content-Type-Options (`nosniff`), XSS-Protection (`1; mode=block`).
2. **CORS Hardening**: Strict origin whitelisting with credentials support.
3. **Sliding-Window Rate Limiting**: 10 req/min on `/api/v1/auth/*`, 120 req/min on general endpoints (Returns `429 Too Many Requests`).
4. **Account Lockout Policy**: Automatically locks account after 5 consecutive failed login attempts.
5. **Maker-Checker Dual Control**: Staff users and Loan Sanctions strictly require different makers and checkers.
6. **JWT Token Revocation**: Revokes refresh tokens upon `/api/v1/auth/logout`.
