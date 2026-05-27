ALTER TABLE app_user
  ADD COLUMN IF NOT EXISTS verification_token VARCHAR(255),
  ADD COLUMN IF NOT EXISTS verification_token_expiry TIMESTAMP,
  ADD COLUMN IF NOT EXISTS reset_token VARCHAR(255),
  ADD COLUMN IF NOT EXISTS reset_token_expiry TIMESTAMP;

-- Mark all existing users as verified so they can still log in after this update
UPDATE app_user SET verified = TRUE WHERE verified IS NULL OR verified = FALSE;
