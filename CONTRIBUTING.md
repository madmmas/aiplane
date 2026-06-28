# Contributing to AIPlane

Thank you for your interest in contributing to AIPlane! This document outlines how to get started.

## Prerequisites

- **Node.js** ≥ 18 (see [`.nvmrc`](.nvmrc) for the recommended version)
- **pnpm** 9.x

## Getting started

1. Fork the repository and clone your fork.
2. Install dependencies from the repo root:

   ```bash
   pnpm install
   ```

3. Start all apps in development mode:

   ```bash
   pnpm dev
   ```

   The dashboard (host) runs at http://localhost:5173. All remotes must be running for Module Federation to work.

## Development workflow

Before submitting a pull request, run:

```bash
pnpm lint        # Check formatting and lint rules
pnpm typecheck   # TypeScript type checking
pnpm build       # Production build
```

You can also use `make lint`, `make typecheck`, and `make build` as shortcuts.

To auto-fix lint and format issues:

```bash
pnpm lint:fix
```

## Architecture

For product intent, architecture decisions, and the full roadmap, see [`docs/SPEC.md`](docs/SPEC.md).

## Pull requests

1. Create a feature branch from `main`.
2. Make your changes with clear, focused commits.
3. Fill out the pull request template completely.
4. Ensure CI passes (lint, typecheck, build).

## Code of conduct

This project follows the [Contributor Covenant](CODE_OF_CONDUCT.md). By participating, you agree to uphold this code.

## Questions

Open a [GitHub issue](https://github.com/madmmas/aiplane/issues) for bugs, feature requests, or questions.
