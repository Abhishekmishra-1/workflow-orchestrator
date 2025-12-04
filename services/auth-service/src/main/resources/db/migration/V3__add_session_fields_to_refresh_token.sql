-- Add session fields to refresh_token table
ALTER TABLE refresh_token
    ADD COLUMN session_id VARCHAR(36),
    ADD COLUMN device_info VARCHAR(500),
    ADD COLUMN ip_address VARCHAR(45),
    ADD COLUMN last_used_at TIMESTAMP;

-- Update existing rows to have unique session_id (using UUID() function or generating in application)
-- Note: MySQL doesn't have UUID() function in all versions, so we'll generate UUIDs in application
-- For existing rows, we'll set a placeholder that will be updated by the application
UPDATE refresh_token SET session_id = CONCAT('temp-', id, '-', UNIX_TIMESTAMP()) WHERE session_id IS NULL;

-- Make session_id NOT NULL after populating
ALTER TABLE refresh_token MODIFY COLUMN session_id VARCHAR(36) NOT NULL;

-- Create index for session_id
CREATE INDEX idx_session_id ON refresh_token(session_id);

-- Add unique constraint on session_id
ALTER TABLE refresh_token ADD CONSTRAINT uk_refresh_token_session_id UNIQUE (session_id);

