-- Bootstrap roles & schemas for microservices (runs once on a fresh volume)

-- NOTE: This script executes as the DB owner (POSTGRES_USER) during container init.

-- === Create service roles (idempotent) ===
DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'crm_user') THEN
CREATE ROLE crm_user LOGIN PASSWORD 'crm_pass';
END IF;

  IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'analytics_user') THEN
CREATE ROLE analytics_user LOGIN PASSWORD 'analytics_pass';
END IF;
END $$;

-- === Allow service users to connect to the app database ===
-- (docker-entrypoint usually creates DB named by POSTGRES_DB, e.g., appdb)
GRANT CONNECT ON DATABASE appdb TO crm_user;
GRANT CONNECT ON DATABASE appdb TO analytics_user;

-- Switch to appdb to create/own schemas (some tools run this file in the right DB already,
-- but this makes the script robust if executed manually).

-- === Create schemas, owned by the corresponding service roles ===
-- If the schemas already exist, change the owner to the service role (idempotent).
DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM information_schema.schemata WHERE schema_name = 'crm') THEN
CREATE SCHEMA crm AUTHORIZATION crm_user;
ELSE
    ALTER SCHEMA crm OWNER TO crm_user;
END IF;

  IF NOT EXISTS (SELECT 1 FROM information_schema.schemata WHERE schema_name = 'analytics') THEN
CREATE SCHEMA analytics AUTHORIZATION analytics_user;
ELSE
    ALTER SCHEMA analytics OWNER TO analytics_user;
END IF;
END $$;

-- === Ensure service users can use their schemas (ownership already implies this, but safe) ===
GRANT USAGE ON SCHEMA crm       TO crm_user;
GRANT USAGE ON SCHEMA analytics TO analytics_user;

-- === Default privileges for future objects created in each schema ===
-- So tables/sequences created by Flyway with the service user are accessible to that user.
ALTER DEFAULT PRIVILEGES IN SCHEMA crm
  GRANT ALL ON TABLES    TO crm_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA crm
  GRANT ALL ON SEQUENCES TO crm_user;

ALTER DEFAULT PRIVILEGES IN SCHEMA analytics
  GRANT ALL ON TABLES    TO analytics_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA analytics
  GRANT ALL ON SEQUENCES TO analytics_user;

-- (Optional) if you expect functions to be created by Flyway and need execution rights:
-- ALTER DEFAULT PRIVILEGES IN SCHEMA crm       GRANT ALL ON FUNCTIONS TO crm_user;
-- ALTER DEFAULT PRIVILEGES IN SCHEMA analytics GRANT ALL ON FUNCTIONS TO analytics_user;

-- Done.
