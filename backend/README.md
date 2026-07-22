# AIPlane Backend

Spring Boot modular monolith for AIPlane (SPEC §3).

| Module | Port | Role |
|--------|------|------|
| `api-server` | 8080 | Main API (`/actuator/health`) |
| `config-server` | 8888 | Spring Cloud Config Server (native profile for local scaffold) |

**Java:** 21 · **Spring Boot:** 3.4.x · **Spring Cloud:** 2024.0.x · **Spring AI BOM:** managed in parent POM

## Prerequisites

- JDK 21+
- Maven 3.9+
- PostgreSQL 16+ for running the API server locally (Docker Compose arrives in [#12](https://github.com/madmmas/aiplane/issues/12))
- Docker (for Testcontainers during `make backend-test`)

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21 2>/dev/null || echo /opt/homebrew/opt/openjdk@21)
```

## Database (Flyway)

Migrations live on the API server classpath:

`api-server/src/main/resources/db/migration/` — V1–V9 (projects → config_properties)

Optional local seed (`classpath:db/seed`): demo projects + `admin@aiplane.local` / `changeme`.

Default datasource (override with env):

| Variable | Default |
|----------|---------|
| `DATABASE_URL` | `jdbc:postgresql://localhost:5432/aimanager` |
| `DB_USERNAME` | `aimanager` |
| `DB_PASSWORD` | `changeme` |

Production: set `spring.flyway.locations=classpath:db/migration` to skip seed scripts.

## Build

From the repo root:

```bash
make backend-build
# or
mvn -f backend/pom.xml -B verify
```

## Run

API server (required for acceptance):

```bash
make backend-api
# → http://localhost:8080/actuator/health
```

Config server (optional for local scaffold):

```bash
make backend-config
# → http://localhost:8888/actuator/health
```

## Package layout (`api-server`)

Domain packages match SPEC §3.3 (`prompt`, `guardrail`, `user`, `usage`, `provider`, `project`, `security`, `common`). They are placeholders until Phase 1+ feature issues land. Docker Compose lands in [#12](https://github.com/madmmas/aiplane/issues/12).
