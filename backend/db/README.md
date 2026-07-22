# Database migrations

Canonical Flyway scripts live on the API server classpath (so they run on startup):

`backend/api-server/src/main/resources/db/migration/` (V1–V9)

Optional local seed data:

`backend/api-server/src/main/resources/db/seed/`

This directory exists to match SPEC §2 (`backend/db/migration/`) as a documentation pointer —
do not duplicate SQL here.
