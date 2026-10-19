# CarePoint — Spring Boot + MySQL

The backend lives in this directory and now includes a same-origin development interface in src/main/resources/static. The original root-level index.html/app.js remains a separate browser-only prototype.

## Implemented scaffold
- Java 21 / Maven / Spring Boot 3.5.x, Spring Security, Spring Data JPA, MySQL Connector and Flyway.
- Session-based login, BCrypt password hashes, session-fixation protection, HTTP-only session cookie and CSRF cookie/header flow for same-origin browser requests.
- ADMIN, DOCTOR and RECEPTIONIST roles.
- Administrator-only endpoints to list, create and deactivate ordinary staff accounts. New staff passwords are hashed; administrator accounts cannot be created through the ordinary staff endpoint.
- Patient directory, patient registration and demographic updates for authenticated staff. Receptionist read views omit clinical notes and prescription notes. Clinical note/prescription reads and writes are limited to doctors and administrators.
- Flyway migration for the initial MySQL tables.
- Same-origin browser UI at / with patient search/registration and administrator staff management. The browser UI and API are served by the same Spring Boot service.

## Critical safety status
**Development scaffold only. Fictional test records only. Do not use real patient information.** The code has not been compiled, deployed or security-tested in this environment. Run the build and tests locally, inspect the code and complete the requirements below before any production use.

Not yet implemented/verified: append-only audit events for every record view and change, MFA, forced first-login password change, secure password recovery, rate limiting/lockout, more complete session lifecycle controls, field-level encryption/data protection, backup/restore drills, monitoring/alerting, independent penetration/security testing, clinical workflow validation, and privacy/legal review. The staff form currently issues a temporary password but does not yet force a change on first login. Do not represent this software as compliant or production-ready.

## Local development
1. Install JDK 21, Maven and MySQL 8.4.
2. Create an empty development database named carepoint and a dedicated least-privilege database user.
3. Set environment variables:
   - DB_URL=jdbc:mysql://localhost:3306/carepoint?useSSL=true&serverTimezone=UTC
   - DB_USERNAME=your development database username
   - DB_PASSWORD=your development database password
   - INITIAL_ADMIN_USERNAME=your chosen administrator username
   - INITIAL_ADMIN_PASSWORD=a unique password of at least 20 characters
   - COOKIE_SECURE=false (local HTTP development only)
4. From this directory run mvn spring-boot:run.
5. Visit http://localhost:8080/login. The first administrator is created only if both initial-admin variables are supplied and the username does not already exist. Never commit these secrets or a .env file.

Flyway creates the initial tables. Hibernate is configured to validate rather than automatically alter the schema.

## Single-platform deployment plan
Railway can host this Spring Boot web service and a private MySQL service within one project. Create a Railway project, deploy this repository from GitHub with the service root directory set to backend, add a MySQL database service, and set DB_URL, DB_USERNAME and DB_PASSWORD as private/reference variables based on that database service. Set INITIAL_ADMIN_USERNAME and INITIAL_ADMIN_PASSWORD as deployment secrets before the first start. Set COOKIE_SECURE=true behind HTTPS. Keep MySQL private and enable/test backups. Review the current Railway instructions before deploying.

## API outline
- GET /api/admin/staff — administrator only.
- POST /api/admin/staff — administrator creates DOCTOR or RECEPTIONIST accounts; temporary password must be at least 14 characters.
- POST /api/admin/staff/{id}/deactivate — administrator deactivates a non-admin account.
- GET /api/patients and GET /api/patients/{id} — authenticated staff; receptionists receive demographics only.
- POST /api/patients and PUT /api/patients/{id}/demographics — authenticated staff.
- PUT /api/patients/{id}/clinical — doctor or administrator only.

Spring Security CSRF protection remains enabled. Add and run automated tests for every role and API action before relying on this scaffold.
