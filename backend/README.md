# CarePoint — Spring Boot + MySQL

The backend lives in this directory and includes a same-origin web interface in `src/main/resources/static`. The original root-level `index.html`/`app.js` remains a separate browser-only prototype.

## Implemented development features
- Java 21 / Maven / Spring Boot 3.5.x, Spring Security, Spring Data JPA, MySQL Connector and Flyway.
- Session-based login, BCrypt password hashes, session-fixation protection, HTTP-only session cookie and CSRF cookie/header flow for same-origin browser requests.
- ADMIN, DOCTOR and RECEPTIONIST roles.
- Patient directory search, patient registration and demographic editing for authenticated staff.
- Clinical notes and prescription notes are returned only to doctors and administrators; only those roles can update clinical information.
- Administrator-only staff directory, staff creation, display-name/status updates, password resets and deactivation. Ordinary staff accounts cannot be promoted to administrator or have their role changed through this interface.
- A session information endpoint used by the interface to show the signed-in user's role.
- Flyway migration for the initial MySQL tables and unit tests for key patient data exposure and staff-management behavior.

## Critical safety status
**Development scaffold only. Fictional test records only. Do not use real patient information.** The code has not been compiled, deployed or security-tested in this environment. Run the build and tests locally and review the code before relying on it.

Not yet implemented/verified: append-only audit events for every record view and change, MFA, forced first-login password change, secure password recovery, rate limiting/lockout, active-session revocation after staff deactivation or password reset, field-level encryption/data protection, backup/restore drills, monitoring/alerting, independent penetration/security testing, clinical workflow validation, and privacy/legal review. Do not represent this software as compliant or production-ready.

Never commit database credentials, administrator passwords, real patient details, prescriptions, or other confidential information to this repository. Do not publish or use real patient information through GitHub Pages or the browser-only prototype.

## Local development
1. Install JDK 21, Maven and MySQL 8.4.
2. Create an empty development database named `carepoint` and a dedicated least-privilege database user.
3. Set environment variables:
   - `DB_URL=jdbc:mysql://localhost:3306/carepoint?useSSL=true&serverTimezone=UTC`
   - `DB_USERNAME=your development database username`
   - `DB_PASSWORD=your development database password`
   - `INITIAL_ADMIN_USERNAME=your chosen administrator username`
   - `INITIAL_ADMIN_PASSWORD=a unique password of at least 20 characters`
   - `COOKIE_SECURE=false` (local HTTP development only)
4. From this directory run `mvn spring-boot:run`.
5. Visit `http://localhost:8080/login`. The first administrator is created only if both initial-admin variables are supplied and the username does not already exist. Never commit these secrets or a `.env` file.

Flyway creates the initial tables. Hibernate is configured to validate rather than automatically alter the schema.

## Management API outline
- `GET /api/session` — signed-in username, display name and role.
- `GET /api/patients?q=...` and `GET /api/patients/{id}` — authenticated staff; receptionist responses contain demographics only.
- `POST /api/patients` and `PUT /api/patients/{id}/demographics` — authenticated staff.
- `PUT /api/patients/{id}/clinical` — doctor or administrator only.
- `GET /api/admin/staff` — administrator only.
- `POST /api/admin/staff` — administrator creates DOCTOR or RECEPTIONIST accounts; temporary password must be at least 14 characters.
- `PUT /api/admin/staff/{id}` — administrator updates display name and enabled status.
- `POST /api/admin/staff/{id}/reset-password` — administrator sets a new temporary password (at least 14 characters).
- `POST /api/admin/staff/{id}/deactivate` — administrator deactivates a non-admin account.

Spring Security CSRF protection remains enabled. The role is intentionally immutable through ordinary staff-management operations; administrator role changes require a separately reviewed process.

## Single-platform deployment plan
Railway can host this Spring Boot web service and a private MySQL service within one project. Create a Railway project, deploy this repository from GitHub with the service root directory set to `backend`, add a MySQL database service, and set `DB_URL`, `DB_USERNAME` and `DB_PASSWORD` as private/reference variables based on that database service. Set `INITIAL_ADMIN_USERNAME` and `INITIAL_ADMIN_PASSWORD` as deployment secrets before the first start. Set `COOKIE_SECURE=true` behind HTTPS. Keep MySQL private and enable/test backups. Review the current Railway instructions before deploying.
