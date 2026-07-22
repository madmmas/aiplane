-- V4: guardrails
CREATE TABLE guardrails (
    id              VARCHAR(64) PRIMARY KEY,
    project_id      VARCHAR(64) NOT NULL REFERENCES projects (id) ON DELETE CASCADE,
    name            VARCHAR(255) NOT NULL,
    type            VARCHAR(64) NOT NULL,
    stage           VARCHAR(16) NOT NULL,
    config          JSONB NOT NULL DEFAULT '{}'::jsonb,
    enabled         BOOLEAN NOT NULL DEFAULT TRUE,
    action          VARCHAR(32) NOT NULL,
    block_message   TEXT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_guardrails_project_name UNIQUE (project_id, name),
    CONSTRAINT ck_guardrails_type CHECK (
        type IN (
            'keyword-blocklist',
            'regex-filter',
            'pii-detection',
            'max-length',
            'custom-llm-judge'
        )
    ),
    CONSTRAINT ck_guardrails_stage CHECK (stage IN ('input', 'output', 'both')),
    CONSTRAINT ck_guardrails_action CHECK (
        action IN ('block', 'warn', 'redact', 'log-only')
    )
);

CREATE INDEX idx_guardrails_project_id ON guardrails (project_id);
CREATE INDEX idx_guardrails_enabled ON guardrails (project_id, enabled);
