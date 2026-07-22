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
