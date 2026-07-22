# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added

- Nothing yet — entries land here until the next tagged release.

## [0.1.0] - 2026-07-22

Phase 0 foundation: monorepo shell, shared packages, backend scaffold, and local full-stack tooling.

### Added

- OSS baseline: MIT license, community docs, Biome/Husky, Dependabot, and required `ci` branch protection
- Product spec (`docs/SPEC.md`), UI mock reference, and AIPlane brand assets
- `packages/ui` design-system tokens and shadcn-based primitives
- `packages/types` shared DTOs and `packages/api-client` fetch + React Query hooks
- Dashboard host shell with Module Federation remotes, theme switcher, and project switcher
- Spring Boot modular monolith (`api-server`, `config-server`) with Actuator health endpoints
- Flyway migrations V1–V9 (projects through config_properties) plus local seed data
- Docker Compose full-stack dev environment (Postgres, config-server, api-server, nginx UI)
- Frontend Vitest + React Testing Library setup across apps and packages
- Backend JUnit 5 / Mockito unit tests, Testcontainers Postgres ITs, and JaCoCo coverage

### Changed

- Rebranded product naming from AI Manager to **AIPlane** across docs and UI
