ALTER TABLE users
    ADD COLUMN IF NOT EXISTS provider VARCHAR(20) NOT NULL DEFAULT 'LOCAL';

ALTER TABLE users
    ADD COLUMN IF NOT EXISTS google_id VARCHAR(255);

ALTER TABLE users
    ADD COLUMN IF NOT EXISTS profile_image_url VARCHAR(512);

CREATE UNIQUE INDEX IF NOT EXISTS uk_users_google_id ON users (google_id)
    WHERE google_id IS NOT NULL;

CREATE TABLE refresh_tokens (
    id              UUID PRIMARY KEY,
    token           VARCHAR(512) NOT NULL,
    user_id         UUID         NOT NULL,
    expires_at      TIMESTAMP    NOT NULL,
    revoked         BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_refresh_tokens_token UNIQUE (token),
    CONSTRAINT fk_refresh_tokens_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens (user_id);
