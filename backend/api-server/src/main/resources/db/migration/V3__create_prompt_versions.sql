-- V3: prompt versions (version number auto-increments per prompt; never recycled)
CREATE TABLE prompt_versions (
    id                      VARCHAR(64) PRIMARY KEY,
    prompt_id               VARCHAR(64) NOT NULL REFERENCES prompts (id) ON DELETE CASCADE,
    version                 INTEGER NOT NULL,
    label                   VARCHAR(128),
    model                   VARCHAR(128) NOT NULL,
    provider                VARCHAR(64) NOT NULL,
    system_prompt           TEXT NOT NULL DEFAULT '',
    user_prompt_template    TEXT NOT NULL DEFAULT '',
    parameters              JSONB NOT NULL DEFAULT '{}'::jsonb,
    status                  VARCHAR(32) NOT NULL DEFAULT 'draft',
    created_by              VARCHAR(64) NOT NULL,
    created_at              TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    metrics                 JSONB,
    CONSTRAINT uq_prompt_versions_prompt_version UNIQUE (prompt_id, version),
    CONSTRAINT ck_prompt_versions_status CHECK (
        status IN ('draft', 'testing', 'active', 'archived')
    ),
    CONSTRAINT ck_prompt_versions_provider CHECK (
        provider IN ('anthropic', 'openai', 'azure-openai', 'bedrock', 'ollama', 'gemini')
    ),
    CONSTRAINT ck_prompt_versions_version_positive CHECK (version > 0)
);

CREATE INDEX idx_prompt_versions_prompt_id ON prompt_versions (prompt_id);
CREATE INDEX idx_prompt_versions_status ON prompt_versions (status);

-- Optional FK from prompts.active_version_id → prompt_versions.id (deferred until versions exist)
ALTER TABLE prompts
    ADD CONSTRAINT fk_prompts_active_version
    FOREIGN KEY (active_version_id) REFERENCES prompt_versions (id) ON DELETE SET NULL;
