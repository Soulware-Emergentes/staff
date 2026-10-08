-- Sets up the two roles this project uses instead of one shared
-- credential, so DDL (schema changes) and DML (application queries)
-- are handled by different roles with different privileges:
--
--   staff_migrator - owns the schema, runs Flyway migrations (DDL rights)
--   staff_app      - used by the Spring Boot app at runtime (DML rights only)
--
-- Run as the postgres superuser, e.g. on Fedora:
--   sudo -iu postgres psql -f scripts/db/setup-roles.sql
--
-- Passwords are read from environment variables so nothing sensitive
-- ends up in shell history or this file. Set them before running:
--   export STAFF_MIGRATOR_PASSWORD='...'
--   export STAFF_APP_PASSWORD='...'
-- (use the same values you put in your local .env, see .env.example)

\set migrator_pw `echo "${STAFF_MIGRATOR_PASSWORD:?STAFF_MIGRATOR_PASSWORD not set}"`
\set app_pw `echo "${STAFF_APP_PASSWORD:?STAFF_APP_PASSWORD not set}"`

CREATE ROLE staff_migrator WITH LOGIN PASSWORD :'migrator_pw';
CREATE ROLE staff_app WITH LOGIN PASSWORD :'app_pw';

-- staff_migrator owns the database, so it can create/alter/drop objects
-- in it (that's what Flyway needs to run migrations).
CREATE DATABASE staff OWNER staff_migrator;

\connect staff

-- staff_app only needs to connect and use the public schema - it does not
-- own anything here, so by default it has no privileges on the tables
-- staff_migrator creates (ownership does not imply privileges in Postgres).
GRANT CONNECT ON DATABASE staff TO staff_app;
GRANT USAGE ON SCHEMA public TO staff_app;

-- Grant DML on every table/sequence staff_migrator creates from now on,
-- automatically. Without this, each new Flyway migration would require
-- a manual GRANT before staff_app could use the new table.
ALTER DEFAULT PRIVILEGES FOR ROLE staff_migrator IN SCHEMA public
    GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO staff_app;

ALTER DEFAULT PRIVILEGES FOR ROLE staff_migrator IN SCHEMA public
    GRANT USAGE, SELECT ON SEQUENCES TO staff_app;
