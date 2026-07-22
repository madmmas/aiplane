-- V7: API keys (store SHA-256 hash; prefix aimg_ for scanner detection)
CREATE TABLE api_keys (
    id              VARCHAR(64) PRIMARY KEY,
    project_id      VARCHAR(64) NOT NULL REFERENCES projects (id) ON DELETE CASCADE,
    name            VARCHAR(255) NOT NULL,
    prefix          VARCHAR(32) NOT NULL,
    key_hash        VARCHAR(128) NOT NULL,
    scopes          TEXT[] NOT NULL DEFAULT '{}',
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    last_used_at    TIMESTAMPTZ,
    expires_at      TIMESTAMPTZ,
    CONSTRAINT uq_api_keys_key_hash UNIQUE (key_hash),
    CONSTRAINT uq_api_keys_project_name UNIQUE (project_id, name)
);

CREATE INDEX idx_api_keys_project_id ON api_keys (project_id);
CREATE INDEX idx_api_keys_prefix ON api_keys (prefix);
