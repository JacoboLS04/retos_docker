-- Create additional databases for orchestrator and notifications services
-- This script runs on container init via docker-entrypoint (mounted at /docker-entrypoint-initdb.d)

-- Create orchestrator_db
DO $$
BEGIN
   IF NOT EXISTS (
      SELECT FROM pg_database WHERE datname = 'orchestrator_db'
   ) THEN
      PERFORM dblink_exec('dbname=' || current_database(), 'CREATE DATABASE orchestrator_db OWNER retos_user');
   END IF;
END $$;

-- Create notifications_db
DO $$
BEGIN
   IF NOT EXISTS (
      SELECT FROM pg_database WHERE datname = 'notifications_db'
   ) THEN
      PERFORM dblink_exec('dbname=' || current_database(), 'CREATE DATABASE notifications_db OWNER retos_user');
   END IF;
END $$;
