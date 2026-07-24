import type { PageResponse, Prompt } from "@repo/types";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import type { ApiClient } from "../client";
import { useApiClient } from "../context";
import { createMockPrompt, getMockPrompt, listMockPrompts, updateMockPrompt } from "../mocks";

export type UsePromptsParams = {
  projectId?: string;
};

export type PromptCreateInput = {
  projectId: string;
  name: string;
  description?: string;
  tags?: string[];
};

export type PromptUpdateInput = {
  name?: string;
  description?: string;
  tags?: string[];
};

export const promptKeys = {
  all: ["prompts"] as const,
  list: (params: UsePromptsParams = {}) => [...promptKeys.all, "list", params] as const,
  detail: (id: string) => [...promptKeys.all, "detail", id] as const,
  versions: (promptId: string) => [...promptKeys.all, promptId, "versions"] as const,
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

/** List prompts, optionally filtered by project. */
export function usePrompts(params: UsePromptsParams = {}) {
  const client = useApiClient();

  return useQuery({
    queryKey: promptKeys.list(params),
    queryFn: () => fetchPrompts(client, params),
    enabled: Boolean(params.projectId),
  });
}

export function useCreatePrompt() {
  const client = useApiClient();
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async (input: PromptCreateInput) => {
      if (client.config.useMocks) {
        return createMockPrompt({
          projectId: input.projectId,
          name: input.name,
          description: input.description,
          tags: input.tags ?? [],
        });
      }
      return client.apiFetch<Prompt>("/api/v1/prompts", {
        method: "POST",
        body: input,
      });
    },
    onSuccess: (created) => {
      void queryClient.invalidateQueries({ queryKey: promptKeys.all });
      queryClient.setQueryData(promptKeys.detail(created.id), created);
    },
  });
}

export function useUpdatePrompt() {
  const client = useApiClient();
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async ({ id, ...patch }: PromptUpdateInput & { id: string }) => {
      if (client.config.useMocks) {
        if (!getMockPrompt(id)) throw new Error(`Prompt not found: ${id}`);
        return updateMockPrompt(id, patch);
      }
      return client.apiFetch<Prompt>(`/api/v1/prompts/${id}`, {
        method: "PATCH",
        body: patch,
      });
    },
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: promptKeys.all });
    },
  });
}
