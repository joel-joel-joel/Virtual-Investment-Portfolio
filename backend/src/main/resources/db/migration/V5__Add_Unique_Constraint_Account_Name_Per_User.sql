-- Add unique constraint to ensure account names are unique per user, not globally
-- This prevents different users from being unable to use the same account name

-- First, check if there are any existing duplicate account names for the same user
-- (This should not exist in normal usage due to application validation)

-- Add unique constraint for (user_id, account_name) combination
-- This enforces per-user uniqueness at the database level
ALTER TABLE accounts
ADD CONSTRAINT uq_accounts_user_account_name
UNIQUE (user_id, account_name);

-- Optional: Add index for performance on filtered lookups by user
-- This helps with queries that filter accounts by user_id and account_name
CREATE INDEX idx_accounts_user_account_name
ON accounts(user_id, account_name);
