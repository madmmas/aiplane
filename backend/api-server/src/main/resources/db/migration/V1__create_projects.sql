-- V1: projects (tenancy root)
CREATE TABLE projects (
    id              VARCHAR(64) PRIMARY KEY,
    slug            VARCHAR(128) NOT NULL,
    name            VARCHAR(255) NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_projects_slug UNIQUE (slug)
);

CREATE INDEX idx_projects_created_at ON projects (created_at DESC);
