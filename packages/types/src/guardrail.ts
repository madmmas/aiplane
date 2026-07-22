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
  /** Ordered list of guardrail IDs in this set. */
  guardrailIds: string[];
  createdAt: IsoDateTime;
}
