-- V9: Spring Cloud Config JDBC property store (CONFIG_MODE=jdbc)
-- Aligns with SPEC §4 JDBC backend; "KEY" is quoted (SQL reserved word).
CREATE TABLE config_properties (
    id              BIGSERIAL PRIMARY KEY,
    application     VARCHAR(100) NOT NULL,
    profile         VARCHAR(50) NOT NULL,
    label           VARCHAR(50) NOT NULL DEFAULT 'main',
    "KEY"           VARCHAR(200) NOT NULL,
    value           TEXT,
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_config_properties_lookup UNIQUE (application, profile, label, "KEY")
);

CREATE INDEX idx_config_properties_lookup
    ON config_properties (application, profile, label);

COMMENT ON TABLE config_properties IS
    'JDBC backend for Spring Cloud Config Server (SPEC §4).';
