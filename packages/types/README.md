# `@repo/types`

Canonical TypeScript interfaces shared by every AIPlane MFE. These mirror the
backend DTOs / entities described in [`docs/SPEC.md`](../../docs/SPEC.md) §6–§7.

## Usage

```ts
import type { Project, Prompt, PageResponse } from "@repo/types";
```

## Exports (core)

| Type | Notes |
|------|--------|
| `Project` | Tenancy root |
| `Prompt` / `PromptVersion` / `ModelParameters` | Prompt Manager |
| `Guardrail` / `GuardrailSet` | Guardrail MFE |
| `User` / `AuthUser` / `APIKey` | User Manager + shell session |
| `UsageEvent` / `UsageSummary` | Usage MFE |
| `PageResponse<T>` / `LLMProvider` | Shared API helpers |

Timestamps are `IsoDateTime` (`string`) because JSON has no `Date` type.
