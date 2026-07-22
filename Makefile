# AIPlane micro-frontend — common commands
# Usage: make [target]; run 'make help' for list

.PHONY: help install dev build preview lint typecheck clean \
	dev-dashboard dev-prompt dev-guardrail dev-user dev-usages \
	backend-build backend-api backend-config backend-test

# Default target
help:
	@echo "AIPlane — available targets:"
	@echo ""
	@echo "  make install      Install dependencies (pnpm install)"
	@echo "  make dev          Run all apps in development mode"
	@echo "  make build        Build all apps for production"
	@echo "  make preview      Preview production build (run after make build)"
	@echo "  make lint         Run Biome lint and format check"
	@echo "  make typecheck    Run TypeScript type checking"
	@echo "  make clean        Remove dist/ and .turbo caches"
	@echo ""
	@echo "  make dev-dashboard    Run only dashboard (port 5173)"
	@echo "  make dev-prompt      Run only prompt-manager (port 5174)"
	@echo "  make dev-guardrail   Run only guardrail (port 5175)"
	@echo "  make dev-user        Run only user-manager (port 5176)"
	@echo "  make dev-usages      Run only usages-data (port 5177)"
	@echo ""
	@echo "  make backend-build   Maven verify for backend modules"
	@echo "  make backend-test    Maven tests for backend modules"
	@echo "  make backend-api     Run api-server on :8080"
	@echo "  make backend-config  Run config-server on :8888"
	@echo ""

install:
	pnpm install

dev:
	pnpm dev

build:
	pnpm build

preview:
	pnpm preview

lint:
	pnpm lint

typecheck:
	pnpm typecheck

clean:
	rm -rf apps/*/dist apps/*/.turbo .turbo backend/**/target

# Single-app dev (for when you only need one app)
dev-dashboard:
	pnpm turbo dev --filter=@repo/dashboard

dev-prompt:
	pnpm turbo dev --filter=@repo/prompt-manager

dev-guardrail:
	pnpm turbo dev --filter=@repo/guardrail

dev-user:
	pnpm turbo dev --filter=@repo/user-manager

dev-usages:
	pnpm turbo dev --filter=@repo/usages-data

# Backend (Java 21 + Maven)
backend-build:
	mvn -f backend/pom.xml -B verify

backend-test:
	mvn -f backend/pom.xml -B test

backend-api:
	mvn -f backend/api-server/pom.xml spring-boot:run

backend-config:
	mvn -f backend/config-server/pom.xml spring-boot:run
