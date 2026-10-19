# CarePoint — Patient Records & Staff Management

A starter frontend prototype for a doctor's office. It is intended for learning and interface testing with **fictional sample records only**.

## Current prototype features

- Clinic overview and patient directory
- Search by patient name or demo ID
- Demo patient detail view
- Add fictional patient records with date of birth, address, visit notes, and prescription notes
- Staff role-planning page for doctor and receptionist workflows
- Responsive layout for desktop and mobile

## Run locally

1. Download or clone this repository.
2. Open `index.html` in a modern browser.
3. Use the sample records or add fictional records.

New demo records are stored in the current browser's `localStorage`. They do not sync between devices, and they are not encrypted or protected as medical records.

## GitHub Pages

In the repository, open **Settings → Pages**. Under build and deployment, select **Deploy from a branch**, choose the default branch and the root folder, then save. Once GitHub finishes publishing, it will show the site URL.

## Important security limitation

This is **not a production medical records system**. It has no real login, server-side authorization, shared database, encrypted medical-record workflow, audit log, or backup system. The doctor and receptionist roles are a design plan only; the current frontend does not enforce permissions.

**Never enter real patient data, passwords, prescriptions, or other confidential information into this prototype.** Do not deploy it for actual clinical use until a qualified developer has implemented and tested a secure backend, authentication, authorization, audit trails, backups, and applicable privacy/data-protection requirements.

## Suggested next development stages

1. Confirm the clinic's workflows and exactly what each staff role may view or change.
2. Select a backend and database; keep all credentials and secrets out of frontend code and GitHub.
3. Implement server-enforced login, role permissions, validation, and audit logging.
4. Add testing, secure deployment, backups, recovery procedures, and a privacy review before any real data is entered.
