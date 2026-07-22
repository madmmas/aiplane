-- V5: guardrail sets + ordered membership
CREATE TABLE guardrail_sets (
    id              VARCHAR(64) PRIMARY KEY,
    project_id      VARCHAR(64) NOT NULL REFERENCES projects (id) ON DELETE CASCADE,
    name            VARCHAR(255) NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_guardrail_sets_project_name UNIQUE (project_id, name)
);

CREATE TABLE guardrail_set_members (
    guardrail_set_id    VARCHAR(64) NOT NULL REFERENCES guardrail_sets (id) ON DELETE CASCADE,
    guardrail_id        VARCHAR(64) NOT NULL REFERENCES guardrails (id) ON DELETE CASCADE,
    position            INTEGER NOT NULL DEFAULT 0,
    PRIMARY KEY (guardrail_set_id, guardrail_id),
    CONSTRAINT ck_guardrail_set_members_position CHECK (position >= 0)
);

CREATE INDEX idx_guardrail_sets_project_id ON guardrail_sets (project_id);
CREATE INDEX idx_guardrail_set_members_set ON guardrail_set_members (guardrail_set_id, position);
