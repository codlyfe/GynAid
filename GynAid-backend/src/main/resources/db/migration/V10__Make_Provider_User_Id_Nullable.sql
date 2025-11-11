-- Migration V10: Make Provider User Id Nullable
-- Allows providers to exist without being linked to a user account

-- Make user_id nullable in healthcare_providers table
ALTER TABLE healthcare_providers ALTER COLUMN user_id SET NULL;
