-- V8: usage telemetry events
CREATE TABLE usage_events (
    id                  VARCHAR(64) PRIMARY KEY,
    project_id          VARCHAR(64) NOT NULL REFERENCES projects (id) ON DELETE CASCADE,
    prompt_id           VARCHAR(64) REFERENCES prompts (id) ON DELETE SET NULL,
    prompt_version_id   VARCHAR(64) REFERENCES prompt_versions (id) ON DELETE SET NULL,
    api_key_id          VARCHAR(64) REFERENCES api_keys (id) ON DELETE SET NULL,
    provider            VARCHAR(64) NOT NULL,
    model               VARCHAR(128) NOT NULL,
    input_tokens        INTEGER NOT NULL DEFAULT 0,
    output_tokens       INTEGER NOT NULL DEFAULT 0,
    latency_ms          INTEGER NOT NULL DEFAULT 0,
    cost_usd            NUMERIC(16, 8) NOT NULL DEFAULT 0,
    status              VARCHAR(32) NOT NULL,
    timestamp           TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT ck_usage_events_status CHECK (
        status IN ('success', 'error', 'guardrail-blocked')
    ),
    CONSTRAINT ck_usage_events_provider CHECK (
        provider IN ('anthropic', 'openai', 'azure-openai', 'bedrock', 'ollama', 'gemini')
    ),
    CONSTRAINT ck_usage_events_tokens CHECK (
        input_tokens >= 0 AND output_tokens >= 0 AND latency_ms >= 0 AND cost_usd >= 0
    )
);

CREATE INDEX idx_usage_events_project_timestamp ON usage_events (project_id, timestamp DESC);
CREATE INDEX idx_usage_events_prompt_id ON usage_events (prompt_id);
