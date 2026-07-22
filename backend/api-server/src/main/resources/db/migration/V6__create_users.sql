-- V6: users + per-project memberships
CREATE TABLE users (
    id              VARCHAR(64) PRIMARY KEY,
    email           VARCHAR(320) NOT NULL,
    name            VARCHAR(255) NOT NULL,
    status          VARCHAR(32) NOT NULL DEFAULT 'invited',
    password_hash   VARCHAR(255),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_users_email UNIQUE (email),
    CONSTRAINT ck_users_status CHECK (status IN ('active', 'invited', 'disabled'))
);

CREATE TABLE project_memberships (
    user_id         VARCHAR(64) NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    project_id      VARCHAR(64) NOT NULL REFERENCES projects (id) ON DELETE CASCADE,
    role            VARCHAR(32) NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    PRIMARY KEY (user_id, project_id),
    CONSTRAINT ck_project_memberships_role CHECK (
        role IN ('ROLE_ADMIN', 'ROLE_DEVELOPER', 'ROLE_VIEWER')
    )
);

CREATE INDEX idx_project_memberships_project_id ON project_memberships (project_id);
