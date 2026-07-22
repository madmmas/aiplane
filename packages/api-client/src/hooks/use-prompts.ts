import type { PageResponse, Prompt } from "@repo/types";
import { useQuery } from "@tanstack/react-query";
import type { ApiClient } from "../client";
import { useApiClient } from "../context";
import { listMockPrompts } from "../mocks";

export type UsePromptsParams = {
  projectId?: string;
};

export const promptKeys = {
  all: ["prompts"] as const,
  list: (params: UsePromptsParams = {}) => [...promptKeys.all, "list", params] as const,
};

async function fetchPrompts(client: ApiClient, params: UsePromptsParams): Promise<Prompt[]> {
  if (client.config.useMocks) {
    return listMockPrompts(params.projectId);
  }

  const page = await client.apiFetch<PageResponse<Prompt> | Prompt[]>("/api/v1/prompts", {
    query: { projectId: params.projectId },
  });
  return Array.isArray(page) ? page : page.content;
}

/** List prompts, optionally filtered by project. Mock-backed until the API exists. */
export function usePrompts(params: UsePromptsParams = {}) {
  const client = useApiClient();

  return useQuery({
    queryKey: promptKeys.list(params),
    queryFn: () => fetchPrompts(client, params),
  });
}
