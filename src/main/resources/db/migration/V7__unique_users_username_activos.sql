ALTER TABLE users
    DROP CONSTRAINT IF EXISTS users_username_key;

CREATE UNIQUE INDEX IF NOT EXISTS ux_users_username_active
    ON users (username)
    WHERE erased = false;
