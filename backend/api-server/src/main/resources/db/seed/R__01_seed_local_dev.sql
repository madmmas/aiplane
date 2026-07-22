-- Local/dev seed: demo project + admin user (idempotent).
-- Admin password: changeme  (bcrypt)
-- Override / skip in production by setting spring.flyway.locations=classpath:db/migration

INSERT INTO projects (id, slug, name, created_at)
VALUES ('proj_news_radar', 'news-radar', 'News Radar', NOW())
ON CONFLICT (id) DO NOTHING;

INSERT INTO projects (id, slug, name, created_at)
VALUES ('proj_ackloop', 'ackloop', 'Ackloop', NOW())
ON CONFLICT (id) DO NOTHING;

INSERT INTO users (id, email, name, status, password_hash, created_at)
VALUES (
    'user_admin',
    'admin@aiplane.local',
    'AIPlane Admin',
    'active',
    -- bcrypt hash for "changeme" (htpasswd -nbBC 10)
    '$2y$10$IzBvl6WPbo.HN8VLZFewXuhus.ZKn3Fszb.tpE.3UE2JgOrM0xMz6',
    NOW()
)
ON CONFLICT (id) DO NOTHING;

INSERT INTO project_memberships (user_id, project_id, role, created_at)
VALUES
    ('user_admin', 'proj_news_radar', 'ROLE_ADMIN', NOW()),
    ('user_admin', 'proj_ackloop', 'ROLE_ADMIN', NOW())
ON CONFLICT (user_id, project_id) DO NOTHING;
