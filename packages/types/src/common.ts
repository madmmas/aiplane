/** ISO-8601 datetime string as returned by the API (JSON has no Date type). */
export type IsoDateTime = string;

export interface PageRequest {
  page?: number;
  size?: number;
  sort?: string;
}

export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export type LLMProvider = "anthropic" | "openai" | "azure-openai" | "bedrock" | "ollama" | "gemini";

export type PromptVersionStatus = "draft" | "testing" | "active" | "archived";

export type GuardrailStage = "input" | "output" | "both";

export type GuardrailAction = "block" | "warn" | "redact" | "log-only";

export type GuardrailType =
  | "keyword-blocklist"
  | "regex-filter"
  | "pii-detection"
  | "max-length"
  | "custom-llm-judge";

export type UsageEventStatus = "success" | "error" | "guardrail-blocked";

export type UserRole = "ROLE_ADMIN" | "ROLE_DEVELOPER" | "ROLE_VIEWER";

export type UserStatus = "active" | "invited" | "disabled";
