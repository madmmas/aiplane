import type { UsageEventCreateInput, UsageEventIngestResponse } from "@repo/types";
import { useMutation } from "@tanstack/react-query";
import { useApiClient } from "../context";
import { ingestMockUsageEvents } from "../mocks";

export type UsageEventIngestInput = {
  events: UsageEventCreateInput[];
};

export const usageKeys = {
  all: ["usage"] as const,
  events: () => [...usageKeys.all, "events"] as const,
};

/** Batched ingest for client apps / usages-data MFE (`POST /api/v1/usage/events`). */
export function useIngestUsageEvents() {
  const client = useApiClient();

  return useMutation({
    mutationFn: async (input: UsageEventIngestInput): Promise<UsageEventIngestResponse> => {
      if (client.config.useMocks) {
        return ingestMockUsageEvents(input.events);
      }
      return client.apiFetch<UsageEventIngestResponse>("/api/v1/usage/events", {
        method: "POST",
        body: input,
      });
    },
  });
}
