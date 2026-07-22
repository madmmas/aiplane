# `@repo/api-client`

Typed fetch wrapper + React Query hooks shared by every AIPlane MFE.

## Install

Workspace packages are already listed in `pnpm-workspace.yaml`. From an app:

```bash
pnpm add @repo/api-client @tanstack/react-query --filter @repo/dashboard
```

## Setup

Wrap the shell (or each MFE) with React Query + the API client:

```tsx
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { ApiClientProvider, useProjects, usePrompts } from "@repo/api-client";

const queryClient = new QueryClient();

export function AppProviders({ children }: { children: React.ReactNode }) {
  return (
    <QueryClientProvider client={queryClient}>
      <ApiClientProvider
        config={{
          baseUrl: import.meta.env.VITE_API_URL ?? "http://localhost:8080",
          getAccessToken: () => localStorage.getItem("aiplane-access-token"),
          // Default true until the Spring API is up — set false to hit the network.
          useMocks: true,
        }}
      >
        {children}
      </ApiClientProvider>
    </QueryClientProvider>
  );
}
```

## Hooks

```tsx
const { data: projects, isLoading } = useProjects();
const { data: prompts } = usePrompts({ projectId: projects?.[0]?.id });
```

## Low-level client

```ts
import { createApiClient } from "@repo/api-client";

const api = createApiClient({
  baseUrl: "http://localhost:8080",
  getAccessToken: () => token,
  useMocks: false,
});

const prompts = await api.apiFetch("/api/v1/prompts", {
  query: { projectId: "proj_news_radar" },
});
```

Auth: when `getAccessToken` returns a value, every request (unless `skipAuth: true`)
gets `Authorization: Bearer <token>`.
