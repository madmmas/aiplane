-- V2: prompts
CREATE TABLE prompts (
    id                  VARCHAR(64) PRIMARY KEY,
    project_id          VARCHAR(64) NOT NULL REFERENCES projects (id) ON DELETE CASCADE,
    name                VARCHAR(255) NOT NULL,
    description         TEXT,
    tags                TEXT[] NOT NULL DEFAULT '{}',
    active_version_id   VARCHAR(64),
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_prompts_project_name UNIQUE (project_id, name)
);

CREATE INDEX idx_prompts_project_id ON prompts (project_id);
CREATE INDEX idx_prompts_updated_at ON prompts (updated_at DESC);
