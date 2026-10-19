# Database schema and safety notes

The application intentionally uses Hibernate schema validation (ddl-auto=validate); it does not automatically create or alter tables. Add and review a Flyway or Liquibase migration before starting the app.

Required tables:
- staff_accounts: id, username, display_name, password_hash, role, enabled, created_at
- patient_records: id, full_name, date_of_birth, address, phone, clinical_notes, prescription_notes, created_at, updated_at

This is an early development scaffold, not a production-ready medical records system. It does not yet implement an append-only clinical audit trail, encryption-at-rest configuration, account recovery, forced password change, MFA, rate limiting, or a completed automated test suite. Do not use real patient data.