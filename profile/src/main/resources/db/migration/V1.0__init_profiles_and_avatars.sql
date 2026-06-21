CREATE TABLE IF NOT EXISTS avatars
(
    id           UUID         NOT NULL,
    storage_key  VARCHAR(512) NOT NULL,
    content_type VARCHAR(100) NOT NULL,
    size_bytes   BIGINT       NOT NULL,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT (now() AT TIME ZONE 'UTC'),
    CONSTRAINT pk_avatars PRIMARY KEY (id),
    CONSTRAINT uk_avatars_storage_key UNIQUE (storage_key),
    CONSTRAINT chk_avatars_size_bytes_positive CHECK (size_bytes > 0)
);

CREATE TABLE IF NOT EXISTS profiles
(
    id          UUID        NOT NULL,
    nickname    VARCHAR(64) NOT NULL,
    description TEXT,
    avatar_id   UUID,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT (now() AT TIME ZONE 'UTC'),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT (now() AT TIME ZONE 'UTC'),
    CONSTRAINT pk_profiles PRIMARY KEY (id),
    CONSTRAINT fk_profiles_on_avatar FOREIGN KEY (avatar_id) REFERENCES avatars (id) ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_profiles_avatar_id ON profiles (avatar_id);
