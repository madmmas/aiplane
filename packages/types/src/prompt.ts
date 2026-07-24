import type { IsoDateTime, LLMProvider, PromptVersionStatus } from "./common";

export interface ModelParameters {
  temperature: number;
  maxTokens: number;
  topP?: number;
  stopSequences?: string[];
}

export interface VersionMetrics {
  requestCount: number;
  avgLatencyMs: number;
  errorRate: number;
  avgCostUsd: number;
}

export interface Prompt {
  id: string;
  projectId: string;
  /** Format: "project/slug" e.g. "news-radar/dedup-judge" */
  name: string;
  description?: string;
  tags: string[];
  activeVersionId?: string;
  createdAt: IsoDateTime;
  updatedAt: IsoDateTime;
}

export interface PromptVersion {
  id: string;
  promptId: string;
  /** Auto-increment; never recycled. */
  version: number;
  /** Optional alias e.g. "haiku-optimised" */
  label?: string;
  model: string;
  provider: LLMProvider;
  systemPrompt: string;
  /** Supports `{{variable}}` placeholders. */
  userPromptTemplate: string;
  parameters: ModelParameters;
  status: PromptVersionStatus;
  createdBy: string;
  createdAt: IsoDateTime;
  metrics?: VersionMetrics;
}

/** Response from `POST /api/v1/prompts/{id}/playground/run`. */
export interface PlaygroundRunResponse {
  content: string;
  inputTokens?: number | null;
  outputTokens?: number | null;
  latencyMs: number;
  provider: string;
  model: string;
  blockedByGuardrail?: boolean | null;
}
