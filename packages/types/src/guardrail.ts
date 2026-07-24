import type { GuardrailAction, GuardrailStage, GuardrailType, IsoDateTime } from "./common";

/** Type-specific config payload (discriminated by `Guardrail.type`). */
export type GuardrailConfig =
  | { keywords: string[] }
  | { patterns: string[] }
  | { entities: string[] }
  | { maxChars: number }
  | { judgePromptId: string }
  | Record<string, unknown>;

export interface Guardrail {
  id: string;
  projectId: string;
  name: string;
  type: GuardrailType;
  stage: GuardrailStage;
  config: GuardrailConfig;
  enabled: boolean;
  action: GuardrailAction;
  blockMessage?: string;
  createdAt: IsoDateTime;
}

export interface GuardrailSet {
  id: string;
  projectId: string;
  name: string;
  /** When true, evaluation stops on the first blocking failure. */
  shortCircuitOnBlock: boolean;
  /** Ordered list of guardrail IDs in this set. */
  guardrailIds: string[];
  createdAt: IsoDateTime;
}

/** Per-rule result from `POST /api/v1/guardrails/:id/test` or set evaluate. */
export interface EvaluatorResult {
  guardrailId: string;
  name: string;
  type: string;
  stage: string;
  passed: boolean;
  reason: string;
  action?: string | null;
  matchedFragment?: string | null;
}

export interface GuardrailSetEvaluateResponse {
  blocked: boolean;
  shortCircuited: boolean;
  results: EvaluatorResult[];
}
