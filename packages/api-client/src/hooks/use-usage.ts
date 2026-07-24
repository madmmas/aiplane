import type {
  UsageCostProjection,
  UsageEvent,
  UsageEventCreateInput,
  UsageEventIngestResponse,
  UsageSummary,
} from "@repo/types";
import { useMutation, useQuery } from "@tanstack/react-query";
import { useApiClient } from "../context";
import {
  getMockUsageCostProjection,
  getMockUsageSummary,
  ingestMockUsageEvents,
  listMockUsageEvents,
} from "../mocks";

export type UsageEventIngestInput = {
  events: UsageEventCreateInput[];
};

export type UseUsageSummaryParams = {
  projectId: string;
  period: string;
  enabled?: boolean;
};

export type UseUsageEventsParams = {
  projectId: string;
  from: string;
  to: string;
  enabled?: boolean;
};

export type UseUsageCostProjectionParams = {
  projectId: string;
  enabled?: boolean;
};

export const usageKeys = {
  all: ["usage"] as const,
  events: () => [...usageKeys.all, "events"] as const,
  eventsList: (projectId: string, from: string, to: string) =>
    [...usageKeys.events(), projectId, from, to] as const,
  summary: (projectId: string, period: string) =>
    [...usageKeys.all, "summary", projectId, period] as const,
  projection: (projectId: string) => [...usageKeys.all, "projection", projectId] as const,
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

/** Aggregated usage KPIs (`GET /api/v1/usage/summary`). */
export function useUsageSummary(params: UseUsageSummaryParams) {
  const client = useApiClient();
  const { projectId, period, enabled = true } = params;

  return useQuery({
    queryKey: usageKeys.summary(projectId, period),
    enabled: enabled && Boolean(projectId) && Boolean(period),
    queryFn: async (): Promise<UsageSummary> => {
      if (client.config.useMocks) {
        return getMockUsageSummary(projectId, period);
      }
      return client.apiFetch<UsageSummary>("/api/v1/usage/summary", {
        query: { projectId, period },
      });
    },
  });
}

/** Usage events in a time range (`GET /api/v1/usage/events`), capped server-side at 500. */
export function useUsageEvents(params: UseUsageEventsParams) {
  const client = useApiClient();
  const { projectId, from, to, enabled = true } = params;

  return useQuery({
    queryKey: usageKeys.eventsList(projectId, from, to),
    enabled: enabled && Boolean(projectId) && Boolean(from) && Boolean(to),
    queryFn: async (): Promise<UsageEvent[]> => {
      if (client.config.useMocks) {
        return listMockUsageEvents(projectId, from, to);
      }
      return client.apiFetch<UsageEvent[]>("/api/v1/usage/events", {
        query: { projectId, from, to },
      });
    },
  });
}

/** Monthly cost projection (`GET /api/v1/usage/costs/projection`). */
export function useUsageCostProjection(params: UseUsageCostProjectionParams) {
  const client = useApiClient();
  const { projectId, enabled = true } = params;

  return useQuery({
    queryKey: usageKeys.projection(projectId),
    enabled: enabled && Boolean(projectId),
    queryFn: async (): Promise<UsageCostProjection> => {
      if (client.config.useMocks) {
        return getMockUsageCostProjection(projectId);
      }
      return client.apiFetch<UsageCostProjection>("/api/v1/usage/costs/projection", {
        query: { projectId },
      });
    },
  });
}
