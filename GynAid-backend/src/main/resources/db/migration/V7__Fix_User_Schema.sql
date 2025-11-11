-- Migration V7: Fix User Schema
-- Add missing columns to users table

ALTER TABLE users ADD COLUMN IF NOT EXISTS date_of_birth DATE;
ALTER TABLE users ADD COLUMN IF NOT EXISTS phone_number VARCHAR(20);
ALTER TABLE users ADD COLUMN IF NOT EXISTS physical_address TEXT;
ALTER TABLE users ADD COLUMN IF NOT EXISTS preferred_language VARCHAR(10) DEFAULT 'en';

-- Rename column to match entity
ALTER TABLE users ALTER COLUMN user_status RENAME TO status;
