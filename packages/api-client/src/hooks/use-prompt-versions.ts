import type {
  LLMProvider,
  ModelParameters,
  PlaygroundRunResponse,
  PromptVersion,
  PromptVersionStatus,
} from "@repo/types";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useApiClient } from "../context";
import {
  createMockPromptVersion,
  listMockPromptVersions,
  promoteMockPromptVersion,
  runMockPlayground,
  updateMockPromptVersionStatus,
} from "../mocks";
import { promptKeys } from "./use-prompts";

export type PromptVersionCreateInput = {
  promptId: string;
  label?: string;
  model: string;
  provider: LLMProvider;
  systemPrompt?: string;
  userPromptTemplate?: string;
  parameters?: ModelParameters;
  createdBy?: string;
};

export type PlaygroundRunInput = {
  promptId: string;
  versionId?: string;
  variables?: Record<string, string>;
  provider: string;
  model: string;
  temperature?: number;
  maxTokens?: number;
};

export function usePromptVersions(promptId: string | undefined) {
  const client = useApiClient();

  return useQuery({
    queryKey: promptKeys.versions(promptId ?? ""),
    queryFn: async (): Promise<PromptVersion[]> => {
      if (!promptId) return [];
      if (client.config.useMocks) {
        return listMockPromptVersions(promptId);
      }
      return client.apiFetch<PromptVersion[]>(`/api/v1/prompts/${promptId}/versions`);
    },
    enabled: Boolean(promptId),
  });
}

export function useCreatePromptVersion() {
  const client = useApiClient();
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async ({ promptId, ...body }: PromptVersionCreateInput) => {
      if (client.config.useMocks) {
        return createMockPromptVersion(promptId, body);
      }
      return client.apiFetch<PromptVersion>(`/api/v1/prompts/${promptId}/versions`, {
        method: "POST",
        body,
      });
    },
    onSuccess: (_created, variables) => {
      void queryClient.invalidateQueries({ queryKey: promptKeys.versions(variables.promptId) });
      void queryClient.invalidateQueries({ queryKey: promptKeys.all });
    },
  });
}

export function usePromotePromptVersion() {
  const client = useApiClient();
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async ({ promptId, versionId }: { promptId: string; versionId: string }) => {
      if (client.config.useMocks) {
        return promoteMockPromptVersion(promptId, versionId);
      }
      return client.apiFetch<PromptVersion>(
        `/api/v1/prompts/${promptId}/versions/${versionId}/promote`,
        { method: "POST" },
      );
    },
    onSuccess: (_result, variables) => {
      void queryClient.invalidateQueries({ queryKey: promptKeys.versions(variables.promptId) });
      void queryClient.invalidateQueries({ queryKey: promptKeys.all });
    },
  });
}

export function useUpdatePromptVersionStatus() {
  const client = useApiClient();
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async ({
      promptId,
      versionId,
      status,
    }: {
      promptId: string;
      versionId: string;
      status: PromptVersionStatus;
    }) => {
      if (client.config.useMocks) {
        return updateMockPromptVersionStatus(promptId, versionId, status);
      }
      return client.apiFetch<PromptVersion>(
        `/api/v1/prompts/${promptId}/versions/${versionId}/status`,
        {
          method: "PATCH",
          body: { status },
        },
      );
    },
    onSuccess: (_result, variables) => {
      void queryClient.invalidateQueries({ queryKey: promptKeys.versions(variables.promptId) });
      void queryClient.invalidateQueries({ queryKey: promptKeys.all });
    },
  });
}

export function useRunPlayground() {
  const client = useApiClient();

  return useMutation({
    mutationFn: async ({ promptId, ...body }: PlaygroundRunInput) => {
      if (client.config.useMocks) {
        return runMockPlayground(promptId, body);
      }
      return client.apiFetch<PlaygroundRunResponse>(`/api/v1/prompts/${promptId}/playground/run`, {
        method: "POST",
        body,
      });
    },
  });
}
