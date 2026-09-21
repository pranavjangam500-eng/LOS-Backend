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

## 📬 Postman API Testing Guide & Collection

A ready-to-use Postman Collection and comprehensive guide are included:

- 📄 **Postman Guide**: [docs/POSTMAN_API_TESTING_GUIDE.md](file:///c:/Users/Khushmeet%20Patil/Documents/Webiste/New%20folder/LOS-Backend/docs/POSTMAN_API_TESTING_GUIDE.md)
- 📦 **Postman Collection**: [docs/LOS_Backend_Postman_Collection.json](file:///c:/Users/Khushmeet%20Patil/Documents/Webiste/New%20folder/LOS-Backend/docs/LOS_Backend_Postman_Collection.json)

---

## 🚀 Getting Started

### Requirements
- **Java**: 21+
- **Database**: PostgreSQL (or embedded H2 for local development)
- **Build Tool**: Apache Maven

### Running Locally
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

Access Swagger UI documentation at: `http://localhost:8080/swagger-ui.html`