CREATE TABLE profiles (
    user_id UUID PRIMARY KEY,
    fullname VARCHAR(150) NOT NULL,
    rut VARCHAR(20) NOT NULL UNIQUE,
    language VARCHAR(2) NOT NULL,
    description VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_rut ON profiles (rut);
CREATE INDEX idx_created_at ON profiles (created_at);
CREATE INDEX idx_updated_at ON profiles (updated_at);

ALTER TABLE profiles
ADD CONSTRAINT uk_profile_rut UNIQUE (rut);

COMMENT ON TABLE profiles IS 'User profile information for StoreGo';
COMMENT ON COLUMN profiles.user_id IS 'UUID of the user from auth-service (JWT sub claim)';
COMMENT ON COLUMN profiles.fullname IS 'User full name';
COMMENT ON COLUMN profiles.rut IS 'Unique RUT identifier (Chilean tax ID format or similar)';
COMMENT ON COLUMN profiles.language IS 'ISO 639-1 language code (es, en, pt, etc.)';
COMMENT ON COLUMN profiles.description IS 'User profile description / bio';
COMMENT ON COLUMN profiles.created_at IS 'Profile creation timestamp';
COMMENT ON COLUMN profiles.updated_at IS 'Profile last update timestamp';
