# CarePoint — Spring Boot + MySQL

The backend lives in this directory and includes a same-origin web interface in `src/main/resources/static`. The original root-level `index.html`/`app.js` remains a separate browser-only prototype.

## Implemented development features
- Java 21 / Maven / Spring Boot 3.5.x, Spring Security, Spring Data JPA, MySQL Connector and Flyway.
- Session-based login, BCrypt password hashes, session-fixation protection, HTTP-only session cookie and CSRF cookie/header flow for same-origin browser requests.
- ADMIN, DOCTOR and RECEPTIONIST roles.
- Patient search, registration, demographic editing, clinical notes and prescription management.
- Receptionist API responses omit clinical notes and prescriptions; only doctors and administrators can update clinical information.
- Administrator-only staff directory, account creation, display-name/status updates, password resets and deactivation.
- Staff roles cannot be changed through the normal staff editor; administrator accounts are excluded from ordinary staff operations.
- Security audit events for patient record/list access, patient updates and staff administration. Audit entries contain actor, action, record type/ID and timestamp only; they do not store patient names, notes, prescriptions, passwords or tokens.
- Administrator-only endpoint and interface panel for the latest 100 audit events.
- Active sessions are tracked; staff sessions are expired after account deactivation, disabling or password reset.
- Flyway migration for audit-event storage and unit tests for patient data separation, audit recording and staff session revocation.

## Critical safety status
**Development scaffold only. Use fictional test records only. Do not use real patient information.** The code has not been compiled, deployed or independently security-tested in this environment. Run the build and tests locally and review the code before relying on it.

Still required before any real patient data: MFA, forced first-login password change, secure password recovery, rate limiting/lockout, append-only/tamper-evident audit retention and restricted audit access, encryption/key management appropriate to the data, backup/restore drills, monitoring/alerting, independent penetration testing, clinical workflow validation, and Jamaican privacy/legal review. The audit table is not yet protected against a database administrator altering records. Do not represent this software as compliant or production-ready.

Never commit database credentials, administrator passwords, real patient details, prescriptions, or other confidential information to this repository. Do not publish or use real patient information through GitHub Pages or the browser-only prototype.

## Option A — free hosted MySQL for development (recommended: Aiven)
Aiven currently offers a MySQL free tier with 1 GB storage, 1 GB RAM, one node, backups and monitoring, with no time limit and no credit card required. It is intended for development and small prototypes, not high-availability production workloads. Free services may be powered off after extended inactivity; the free tier has no 99.99% SLA and limited support. Confirm the current limits before creating the service.

1. Create an account at https://aiven.io/free-mysql-database.
2. In the Aiven Console, create a project, choose **Create service**, select **MySQL**, and select the **Free** plan. The free plan does not let you choose a specific cloud or region.
3. Wait until the service is running. Open its Overview page and use **Quick connect** to get the Java JDBC URI and connection details.
4. Create a separate database named `carepoint` if the service has not already created it. Create a dedicated service user for this application rather than sharing an administrator credential where possible.
5. Copy the JDBC URI from Aiven's Java connection instructions. Set it as the private environment variable `DB_URL`. Set `DB_USERNAME` and `DB_PASSWORD` from the service-user details. Never put these values in Java source, GitHub commits, screenshots, or this README.
6. Use TLS. Aiven's Java example uses `sslmode=require`; for certificate verification at the stronger level, download the service CA certificate and configure Connector/J to verify it. Do not disable TLS.
7. Connect MySQL Workbench using Aiven's hostname, port, username and password, and configure the downloaded CA certificate in the SSL tab. Aiven assigns a port; do not assume port 3306.
8. Deploy the Spring Boot service with the same private database variables. Flyway runs the database migrations at startup.

Aiven guides:
- Free tier: https://aiven.io/docs/products/mysql/concepts/mysql-free-tier
- Create a service: https://aiven.io/docs/products/mysql/get-started
- Java/JDBC connection: https://aiven.io/docs/products/mysql/howto/connect-with-java
- Workbench connection: https://aiven.io/docs/products/mysql/howto/connect-from-mysql-workbench
- TLS certificates: https://aiven.io/docs/platform/concepts/tls-ssl-certificates

For a different MySQL-compatible option, TiDB Cloud Starter has monthly free quotas and can bill beyond the free allowance if consumption billing is enabled. It is MySQL-protocol compatible but is not Oracle MySQL; test the application's SQL and Flyway migrations before choosing it.

## Option B — local MySQL for development
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
5. Visit `http://localhost:8080/login`.

Flyway creates the initial and audit tables. Hibernate is configured to validate rather than automatically alter the schema.

## Management API outline
- `GET /api/session` — signed-in username, display name and role.
- `GET /api/patients?q=...` and `GET /api/patients/{id}` — authenticated staff; receptionist responses contain demographics only.
- `POST /api/patients` and `PUT /api/patients/{id}/demographics` — authenticated staff.
- `PUT /api/patients/{id}/clinical` — doctor or administrator only.
- `GET /api/admin/staff` — administrator only.
- `POST /api/admin/staff` — administrator creates DOCTOR or RECEPTIONIST accounts; temporary password must be at least 14 characters.
- `PUT /api/admin/staff/{id}` — administrator updates display name and enabled status.
- `POST /api/admin/staff/{id}/reset-password` — administrator sets a new temporary password and expires the staff member's sessions.
- `POST /api/admin/staff/{id}/deactivate` — administrator deactivates a non-admin account and expires its sessions.
- `GET /api/admin/audit` — administrator reads the latest 100 audit events.

Spring Security CSRF protection remains enabled. Passwords are stored using BCrypt. Staff sessions are tracked so administrative disable/reset operations can expire existing sessions. The audit API is restricted to administrators.

## Single-platform deployment plan
Railway can host the Spring Boot web service and a MySQL service in one project, but check its current pricing and free allowances before deploying. Alternatively, host the Spring Boot application on a suitable Java host and use Aiven for MySQL. For either setup, keep DB credentials in private environment variables, set `COOKIE_SECURE=true` behind HTTPS, keep database access restricted, configure backups and test restoration. Do not put real patient data in a public repository or free-tier development service.
