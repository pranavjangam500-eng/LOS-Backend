# Loan Origination System (LOS) - Multi-Tenant Backend

An enterprise-grade, multi-tenant Spring Boot backend for Loan Origination System (LOS) supporting multi-bank dynamic routing, RBAC, JWT authentication, and hardened security controls.

---

## 🔒 Hardened Security Features

- **OWASP HTTP Security Headers**: HSTS, Content-Security-Policy (CSP), X-Frame-Options (DENY), X-Content-Type-Options (nosniff), X-XSS-Protection, Referrer-Policy, Permissions-Policy.
- **Strict CORS Control**: Environment-configurable allowed origins (`app.cors.allowed-origins`).
- **Anti-Brute Force Rate Limiting**: In-memory sliding-window throttling for auth endpoints (10 req/min) and API endpoints (120 req/min).
- **XSS Protection & Request Sanitization**: Automatic input filter stripping malicious scripts and tags.
- **Account Lockout Policy**: Automatic account lock after 5 consecutive failed login attempts.
- **JWT Token Revocation & Logout**: Session revocation endpoint (`/api/v1/auth/logout`).
- **Password Policy Enforcement**: Mandatory password complexity validation (min 8 chars, upper, lower, digit, special char).
- **Error Information Protection**: Sanitized 500 internal server error payloads preventing sensitive data/SQL leaks.

---

## 📋 Lead Management Module

The Lead Management Module handles end-to-end loan application intake, verification, CSV bulk imports, and reporting.

### Updated Lead Fields Structure

#### Step 1 – Personal Details
- **Added**: `passportExpiryDate` (Date in `YYYY-MM-DD` format).
- **Removed**: `mobileNumber`.
- **Removed**: All OTP-related fields (`otp`) from Lead form/model/entity.
- **Retained**: `customerName` (or `firstNameBusinessName`), `dateOfBirth`, `age`, `customerType`, `panNumber`, `panValidationStatus`, `aadhaarNumber`, `aadhaarValidationStatus`, `residentialStatus`, `gender`, `maritalStatus`, `passportNumber`, `dedupeStatus`, `blacklistStatus`, `email`, `pinCode`, `numberOfDependents`.

#### Step 2 – Loan Details
- **Added**: `interestRate` (Annual interest rate percentage, e.g., `11.5`).
- **Replaced**: `propertyValue` with `totalInterest` (Total estimated interest payable over the loan tenure).
- **Removed**: `propertyValue` completely from the Lead entity, DTOs, database schema, CSV import/export, and APIs.
- **Retained**: `loanProductType`, `loanAmount`, `purposeOfLoan`, `tenure`, `numberOfInstalments`, `emi`, `securityAmount`, `downPaymentOrCollateral`.

### Lead APIs

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/leads` | Create a new lead (atomic payload with Step 1–4 details) |
| `GET` | `/api/leads/{id}` | Get lead details by Lead ID |
| `GET` | `/api/leads` | Search and list leads with pagination/filtering |
| `PUT` | `/api/leads/{id}` | Update existing lead details |
| `POST` | `/api/leads/import` | Bulk import leads from CSV (multipart file upload) |
| `GET` | `/api/leads/export` | Export filtered or all leads to CSV |
| `GET` | `/api/leads/sample-csv` | Download sample CSV template matching current schema |

---

## 📬 Postman API Testing Guide & Collection

A ready-to-use Postman Collection and comprehensive guide are included:

- 📦 **Lead Management Postman Collection**: [LOS_Lead_Management_Postman_Collection.json](file:///c:/Users/Gourav%20Patil/Documents/LOS-Backend-main/LOS_Lead_Management_Postman_Collection.json)
- 📄 **Postman Guide**: `docs/POSTMAN_API_TESTING_GUIDE.md`
- 📦 **General Backend Postman Collection**: `docs/LOS_Backend_Postman_Collection.json`

---

## 🚀 Getting Started

### Requirements
- **Java**: 21+
- **Database**: PostgreSQL (or embedded H2 for local development)
- **Build Tool**: Apache Maven

### Running Locally
```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```
or
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

Access Swagger UI documentation at: `http://localhost:8080/swagger-ui.html`  
Access UI Dashboard at: `http://localhost:8080/index.html`

