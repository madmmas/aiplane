import type {
  EvaluatorResult,
  Guardrail,
  GuardrailAction,
  GuardrailConfig,
  GuardrailStage,
  GuardrailType,
} from "@repo/types";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import type { ApiClient } from "../client";
import { useApiClient } from "../context";
import {
  createMockGuardrail,
  getMockGuardrail,
  listMockGuardrails,
  testMockGuardrail,
  updateMockGuardrail,
} from "../mocks";

export type UseGuardrailsParams = {
  projectId?: string;
};

export type GuardrailCreateInput = {
  projectId: string;
  name: string;
  type: GuardrailType;
  stage: GuardrailStage;
  config: GuardrailConfig;
  enabled?: boolean;
  action: GuardrailAction;
  blockMessage?: string;
};

export type GuardrailUpdateInput = Partial<Omit<GuardrailCreateInput, "projectId">>;

export const guardrailKeys = {
  all: ["guardrails"] as const,
  list: (params: UseGuardrailsParams = {}) => [...guardrailKeys.all, "list", params] as const,
  detail: (id: string) => [...guardrailKeys.all, "detail", id] as const,
};

async function fetchGuardrails(
  client: ApiClient,
  params: UseGuardrailsParams,
): Promise<Guardrail[]> {
  if (client.config.useMocks) {
    return listMockGuardrails(params.projectId);
  }
  return client.apiFetch<Guardrail[]>("/api/v1/guardrails", {
    query: { projectId: params.projectId },
  });
}

export function useGuardrails(params: UseGuardrailsParams = {}) {
  const client = useApiClient();
  return useQuery({
    queryKey: guardrailKeys.list(params),
    queryFn: () => fetchGuardrails(client, params),
    enabled: Boolean(params.projectId),
  });
}

export function useCreateGuardrail() {
  const client = useApiClient();
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async (input: GuardrailCreateInput) => {
      if (client.config.useMocks) {
        return createMockGuardrail({
          ...input,
          enabled: input.enabled ?? true,
        });
      }
      return client.apiFetch<Guardrail>("/api/v1/guardrails", {
        method: "POST",
        body: input,
      });
    },
    onSuccess: (created) => {
      void queryClient.invalidateQueries({ queryKey: guardrailKeys.all });
      queryClient.setQueryData(guardrailKeys.detail(created.id), created);
    },
  });
}

export function useUpdateGuardrail() {
  const client = useApiClient();
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async ({ id, ...patch }: GuardrailUpdateInput & { id: string }) => {
      if (client.config.useMocks) {
        if (!getMockGuardrail(id)) throw new Error(`Guardrail not found: ${id}`);
        return updateMockGuardrail(id, patch);
      }
      return client.apiFetch<Guardrail>(`/api/v1/guardrails/${id}`, {
        method: "PATCH",
        body: patch,
      });
    },
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: guardrailKeys.all });
    },
  });
}

export function useTestGuardrail() {
  const client = useApiClient();

  return useMutation({
    mutationFn: async ({ id, text }: { id: string; text: string }) => {
      if (client.config.useMocks) {
        return testMockGuardrail(id, text);
      }
      return client.apiFetch<EvaluatorResult>(`/api/v1/guardrails/${id}/test`, {
        method: "POST",
        body: { text },
      });
    },
  });
}
