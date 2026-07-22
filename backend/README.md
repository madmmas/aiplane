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

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21 2>/dev/null || echo /opt/homebrew/opt/openjdk@21)
```

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

Domain packages match SPEC §3.3 (`prompt`, `guardrail`, `user`, `usage`, `provider`, `project`, `security`, `common`). They are placeholders until Phase 1+ feature issues land. Flyway migrations arrive in [#11](https://github.com/madmmas/aiplane/issues/11); Docker Compose in [#12](https://github.com/madmmas/aiplane/issues/12).
