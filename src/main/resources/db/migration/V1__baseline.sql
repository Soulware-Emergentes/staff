-- Baseline migration, run by staff_migrator at startup. Proves the DDL/DML
-- role split actually works end to end: this table gets created by
-- staff_migrator, and staff_app should be able to read/write it without any
-- manual GRANT thanks to the ALTER DEFAULT PRIVILEGES set up in
-- scripts/db/setup-roles.sql.
CREATE TABLE flyway_setup_check (
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
