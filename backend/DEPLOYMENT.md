# CarePoint deployment checklist (Railway)

This config prepares the Spring Boot service for a Railway deployment; it does not create a Railway project or deploy the app automatically.

## 1. Create services
1. Create a Railway project and add a MySQL service.
2. Add a service from this GitHub repository.
3. Set the application service's **Root Directory** to `/backend`. Railway's service root setting is configured in the service settings.
4. If Railway does not automatically pick up the configuration file, set the config-as-code path to `/backend/railway.toml`.

## 2. Set application variables
Configure these on the **Spring Boot application service**, not in Git:
- `SPRING_PROFILES_ACTIVE=prod`
- `DB_URL=jdbc:mysql://${{MySQL.MYSQLHOST}}:${{MySQL.MYSQLPORT}}/${{MySQL.MYSQLDATABASE}}?sslMode=REQUIRED&serverTimezone=UTC`
- `DB_USERNAME=${{MySQL.MYSQLUSER}}`
- `DB_PASSWORD=${{MySQL.MYSQLPASSWORD}}`
- `INITIAL_ADMIN_USERNAME` = a unique administrator username
- `INITIAL_ADMIN_PASSWORD` = a unique random password of at least 20 characters

Replace `MySQL` in the Railway variable references with the exact name of your MySQL service. Use Railway's variable-reference picker if the service name differs. Confirm the database's TLS settings and supported MySQL connection options in your Railway environment before deploying.

The production profile forces the session cookie's Secure flag and does not provide fallback database credentials. Never put actual secret values in this file, GitHub, screenshots, or commit messages. Use Railway's private variables/secrets UI.

## 3. Network and database
- Keep MySQL private; do not enable public TCP access for the app to connect internally.
- Use the private database host and port supplied by Railway.
- Enable automatic backups and test restoring a backup into a separate test database.
- Set a strong unique initial administrator password before the first start. The app creates the initial administrator only when both initial-admin variables are set and that username does not already exist.
- After the first successful initialization, remove the initial-admin password variable if the application no longer needs it. Do not rotate or remove it before the initial admin is created.

## 4. Deploy and verify
- Railway's health check path is `/actuator/health`; the Actuator dependency was added for this endpoint.
- Review build and runtime logs. Verify the health endpoint returns HTTP 200 and login works over HTTPS.
- Test database connectivity, Flyway migration, administrator login, and role restrictions using fictional records only.
- Verify a receptionist cannot retrieve clinical notes or prescriptions by calling APIs directly.
- Confirm a database backup and restore procedure before treating the service as operational.

## Important limitations
This is deployment preparation, not a production approval. The repository still documents missing safeguards including append-only audit logs, MFA, forced first-login password change, password recovery, login rate limits/lockout, broader session lifecycle controls, independent security testing, monitoring/alerting, and privacy/legal review. Do not use real patient information until those gaps have been addressed and the system independently reviewed.
