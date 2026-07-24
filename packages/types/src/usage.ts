import type { IsoDateTime, LLMProvider, UsageEventStatus } from "./common";

export interface UsageEvent {
  id: string;
  projectId: string;
  promptId?: string;
  promptVersionId?: string;
  apiKeyId?: string;
  provider: LLMProvider;
  model: string;
  inputTokens: number;
  outputTokens: number;
  latencyMs: number;
  costUsd: number;
  status: UsageEventStatus;
  timestamp: IsoDateTime;
}

/** Payload for `POST /api/v1/usage/events` — one row inside the batch envelope. */
export interface UsageEventCreateInput {
  id?: string;
  projectId: string;
  promptId?: string;
  promptVersionId?: string;
  apiKeyId?: string;
  provider: LLMProvider;
  model: string;
  inputTokens?: number;
  outputTokens?: number;
  latencyMs?: number;
  costUsd?: number;
  status: UsageEventStatus;
  timestamp?: IsoDateTime;
}

/** Forward-compatible ingest envelope: `{ "events": [ ... ] }`. */
export interface UsageEventIngestRequest {
  events: UsageEventCreateInput[];
}

export interface UsageEventIngestResponse {
  accepted: number;
  events: UsageEvent[];
}

export interface UsageProviderBreakdown {
  provider: LLMProvider;
  requests: number;
  inputTokens: number;
  outputTokens: number;
  costUsd: number;
}

export interface UsageSummary {
  projectId: string;
  /** e.g. `7d`, `30d`, `2026-07` */
  period: string;
  totalRequests: number;
  totalInputTokens: number;
  totalOutputTokens: number;
  totalCostUsd: number;
  byProvider: UsageProviderBreakdown[];
}
