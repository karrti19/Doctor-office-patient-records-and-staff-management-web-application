# CarePoint — Patient Records & Staff Management

This repository now contains the original browser-only UI prototype and an early Java/Spring Boot + MySQL backend scaffold under **backend/**.

## Backend foundations added
- Java 21, Maven, Spring Boot, Spring Security, Spring Data JPA, MySQL Connector and Flyway.
- Session-based form login, BCrypt password hashing, session fixation protection, HTTP-only session cookie and Spring Security CSRF protection.
- Roles: ADMIN, DOCTOR and RECEPTIONIST.
- Administrator-only staff listing, staff creation and deactivation endpoints. Staff passwords are submitted as temporary passwords and stored as BCrypt hashes; only an administrator can create ordinary staff accounts.
- Patient demographic and clinical endpoints. Receptionist read responses omit clinical notes and prescription notes; only doctors and administrators can read or update clinical fields.
- Flyway migration for the initial tables.

## Important status: development scaffold, not production-ready
**Use fictional records only. Do not use this with real patient information.** The code has not been built or tested in this environment and must be reviewed and tested locally before deployment. The old root-level index.html and app.js remain the browser-only demo and are not yet connected to the backend API. A complete integrated user interface is still needed.

Missing production requirements include a tested append-only audit trail for every view/change, MFA, password reset and forced password change, login rate limiting and lockout, stronger session lifecycle controls, field-level data protection, a documented backup/restore drill, monitoring/alerting, security testing, privacy/legal review, and clinical workflow validation. Do not represent this scaffold as compliant or production-ready.

## Local backend setup
1. Install JDK 21 and Maven; install MySQL 8.4 or another supported MySQL 8 release.
2. Create a dedicated empty development database named carepoint and a least-privilege database user. Keep the database private.
3. Set environment variables before starting the app:
   - DB_URL=jdbc:mysql://localhost:3306/carepoint?useSSL=true&serverTimezone=UTC
   - DB_USERNAME=your development database username
   - DB_PASSWORD=your development database password
   - INITIAL_ADMIN_USERNAME=your chosen administrator username
   - INITIAL_ADMIN_PASSWORD=a unique password with at least 20 characters
   - COOKIE_SECURE=false for local HTTP development only
4. From the backend directory, run: mvn spring-boot:run
5. Visit http://localhost:8080/login. The initial administrator is created only when both initial-admin variables are supplied and the username does not already exist. Never commit these values or a .env file.

Flyway creates the initial tables when the application starts. JPA uses schema validation rather than automatically modifying tables.

## One-platform hosting plan
Railway can host the Spring Boot web service and a private MySQL service in one project. Configure the service's root directory as backend (or use the supplied Maven project path in the build configuration), add a MySQL database service, and set the backend environment variables using private/reference variables from that database service. Set COOKIE_SECURE=true for HTTPS deployment. Keep the database off public networking. Enable and test backups before any real data is considered.

The same Spring Boot service should ultimately serve the integrated frontend from its classpath static resources so login, UI and API share one origin. The existing root prototype is not yet bundled into that service; do not treat the prototype as the deployed secured UI.

## API outline
- GET /api/admin/staff — administrator only.
- POST /api/admin/staff — administrator creates a DOCTOR or RECEPTIONIST account; temporary password must be at least 14 characters.
- POST /api/admin/staff/{id}/deactivate — administrator deactivates a non-admin account.
- GET /api/patients and GET /api/patients/{id} — authenticated staff; receptionists receive demographics only.
- POST /api/patients and PUT /api/patients/{id}/demographics — authenticated staff.
- PUT /api/patients/{id}/clinical — doctor or administrator only.

For write requests made by a browser, retain and submit the Spring Security CSRF token. Do not disable CSRF as a shortcut. Add automated tests for each role before relying on these endpoints.
