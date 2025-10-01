-- PostgreSQL initialization script
-- This ensures the postgres user and agencia_db database are properly created

-- Create the database if it doesn't exist (though it should be created by POSTGRES_DB)
SELECT 'CREATE DATABASE agencia_db' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'agencia_db')\gexec

-- Ensure postgres user has proper permissions
ALTER USER postgres WITH SUPERUSER;

-- Grant all privileges on the database
GRANT ALL PRIVILEGES ON DATABASE agencia_db TO postgres;

-- Connect to the agencia_db database
\c agencia_db;

-- Ensure postgres user owns the database
ALTER DATABASE agencia_db OWNER TO postgres;

-- Print confirmation
SELECT 'PostgreSQL initialization completed successfully' as status;
