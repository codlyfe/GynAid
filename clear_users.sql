-- Clear all users from the database for testing
DELETE FROM users;
-- Reset auto-increment counter for users table
ALTER TABLE users ALTER COLUMN id RESTART WITH 1;