import type { GuardrailSet, GuardrailSetEvaluateResponse } from "@repo/types";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import type { ApiClient } from "../client";
import { useApiClient } from "../context";
import {
  createMockGuardrailSet,
  evaluateMockGuardrailSet,
  getMockGuardrailSet,
  listMockGuardrailSets,
  updateMockGuardrailSet,
} from "../mocks";

export type UseGuardrailSetsParams = {
  projectId?: string;
};

export type GuardrailSetCreateInput = {
  projectId: string;
  name: string;
  shortCircuitOnBlock?: boolean;
  guardrailIds?: string[];
};

export type GuardrailSetUpdateInput = {
  name?: string;
  shortCircuitOnBlock?: boolean;
  guardrailIds?: string[];
};

export const guardrailSetKeys = {
  all: ["guardrail-sets"] as const,
  list: (params: UseGuardrailSetsParams = {}) => [...guardrailSetKeys.all, "list", params] as const,
  detail: (id: string) => [...guardrailSetKeys.all, "detail", id] as const,
};

async function fetchGuardrailSets(
  client: ApiClient,
  params: UseGuardrailSetsParams,
): Promise<GuardrailSet[]> {
  if (client.config.useMocks) {
    return listMockGuardrailSets(params.projectId);
  }
  return client.apiFetch<GuardrailSet[]>("/api/v1/guardrail-sets", {
    query: { projectId: params.projectId },
  });
}

export function useGuardrailSets(params: UseGuardrailSetsParams = {}) {
  const client = useApiClient();
  return useQuery({
    queryKey: guardrailSetKeys.list(params),
    queryFn: () => fetchGuardrailSets(client, params),
    enabled: Boolean(params.projectId),
  });
}

export function useCreateGuardrailSet() {
  const client = useApiClient();
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async (input: GuardrailSetCreateInput) => {
      if (client.config.useMocks) {
        return createMockGuardrailSet({
          projectId: input.projectId,
          name: input.name,
          shortCircuitOnBlock: input.shortCircuitOnBlock ?? true,
          guardrailIds: input.guardrailIds ?? [],
        });
      }
      return client.apiFetch<GuardrailSet>("/api/v1/guardrail-sets", {
        method: "POST",
        body: input,
      });
    },
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: guardrailSetKeys.all });
    },
  });
}

export function useUpdateGuardrailSet() {
  const client = useApiClient();
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async ({ id, ...patch }: GuardrailSetUpdateInput & { id: string }) => {
      if (client.config.useMocks) {
        if (!getMockGuardrailSet(id)) throw new Error(`Guardrail set not found: ${id}`);
        return updateMockGuardrailSet(id, patch);
      }
      return client.apiFetch<GuardrailSet>(`/api/v1/guardrail-sets/${id}`, {
        method: "PATCH",
        body: patch,
      });
    },
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: guardrailSetKeys.all });
    },
  });
}

export function useEvaluateGuardrailSet() {
  const client = useApiClient();

  return useMutation({
    mutationFn: async ({
      id,
      input,
      output,
      shortCircuitOnBlock,
    }: {
      id: string;
      input: string;
      output?: string;
      shortCircuitOnBlock?: boolean;
    }) => {
      if (client.config.useMocks) {
        return evaluateMockGuardrailSet(id, input, output ?? "", shortCircuitOnBlock);
      }
      return client.apiFetch<GuardrailSetEvaluateResponse>(
        `/api/v1/guardrail-sets/${id}/evaluate`,
        {
          method: "POST",
          body: { input, output, shortCircuitOnBlock },
        },
      );
    },
  });
}
