# CarePoint — Patient Records & Staff Management

This repository contains two development paths:

- **backend/** — Java 21, Spring Boot, Spring Security, Flyway and MySQL scaffold, with a same-origin web interface served by Spring Boot.
- Root-level index.html, styles.css and app.js — original browser-only visual prototype; patient data is stored in localStorage and is not protected.

## Run the visual prototype
Open the root index.html in a browser. Use fictional sample records only. Data stays in that browser and does not sync.

## Run the Spring Boot + MySQL application
Read [backend/README.md](backend/README.md) for prerequisites and local startup. For deployment setup, follow [backend/DEPLOYMENT.md](backend/DEPLOYMENT.md). The backend's web interface is under backend/src/main/resources/static and is separate from the root prototype.

## Current safety status
**Neither version is production-ready. Use fictional records only.** The backend scaffold has not been independently security-tested. Before any real patient information is used, it needs automated role/permission tests, append-only audit trails, MFA, account recovery and first-login password change, login abuse controls, secure backup/restore procedures, security review, monitoring, privacy/legal review, and clinical workflow validation.

Never commit database credentials, administrator passwords, real patient details, prescriptions, or other confidential information to this repository. Do not publish or use real patient information through GitHub Pages or the browser-only prototype.
