import type {
  EvaluatorResult,
  Guardrail,
  GuardrailSet,
  GuardrailSetEvaluateResponse,
  LLMProvider,
  ModelParameters,
  PlaygroundRunResponse,
  Project,
  Prompt,
  PromptVersion,
  PromptVersionStatus,
} from "@repo/types";

/** In-memory fixtures used while the Spring API is not yet available. */
export const MOCK_PROJECTS: Project[] = [
  {
    id: "proj_news_radar",
    slug: "news-radar",
    name: "News Radar",
    createdAt: "2026-01-15T10:00:00.000Z",
  },
  {
    id: "proj_ackloop",
    slug: "ackloop",
    name: "Ackloop",
    createdAt: "2026-02-01T12:00:00.000Z",
  },
];

export let MOCK_PROMPTS: Prompt[] = [];
export let MOCK_PROMPT_VERSIONS: PromptVersion[] = [];
export let MOCK_GUARDRAILS: Guardrail[] = [];
export let MOCK_GUARDRAIL_SETS: GuardrailSet[] = [];

const DEFAULT_PARAMS: ModelParameters = {
  temperature: 0.2,
  maxTokens: 1024,
};

const INITIAL_PROMPTS: Prompt[] = [
  {
    id: "prompt_dedup_judge",
    projectId: "proj_news_radar",
    name: "news-radar/dedup-judge",
    description: "Decide whether two headlines refer to the same story.",
    tags: ["judge", "dedup"],
    activeVersionId: "ver_6",
    createdAt: "2026-03-01T09:00:00.000Z",
    updatedAt: "2026-07-01T14:30:00.000Z",
  },
  {
    id: "prompt_summary",
    projectId: "proj_news_radar",
    name: "news-radar/cluster-summary",
    description: "Summarise a cluster of related articles.",
    tags: ["summary"],
    activeVersionId: "ver_3",
    createdAt: "2026-03-10T11:00:00.000Z",
    updatedAt: "2026-06-20T08:15:00.000Z",
  },
  {
    id: "prompt_ack_triage",
    projectId: "proj_ackloop",
    name: "ackloop/incident-triage",
    description: "Classify inbound incident severity.",
    tags: ["triage"],
    createdAt: "2026-04-02T16:00:00.000Z",
    updatedAt: "2026-04-02T16:00:00.000Z",
  },
];

const INITIAL_PROMPT_VERSIONS: PromptVersion[] = [
  {
    id: "ver_5",
    promptId: "prompt_dedup_judge",
    version: 5,
    label: "baseline",
    model: "claude-sonnet-4-20250514",
    provider: "anthropic",
    systemPrompt: "You are a careful news deduplication judge.",
    userPromptTemplate:
      "Headline A: {{headline_a}}\nHeadline B: {{headline_b}}\nSame story? Reply yes or no.",
    parameters: { ...DEFAULT_PARAMS },
    status: "archived",
    createdBy: "dev@aiplane.local",
    createdAt: "2026-06-01T10:00:00.000Z",
  },
  {
    id: "ver_6",
    promptId: "prompt_dedup_judge",
    version: 6,
    label: "active",
    model: "claude-sonnet-4-20250514",
    provider: "anthropic",
    systemPrompt: "You are a careful news deduplication judge.",
    userPromptTemplate:
      "Headline A: {{headline_a}}\nHeadline B: {{headline_b}}\nSame story? Reply yes or no with brief rationale.",
    parameters: { ...DEFAULT_PARAMS, temperature: 0.1 },
    status: "active",
    createdBy: "dev@aiplane.local",
    createdAt: "2026-07-01T14:30:00.000Z",
  },
  {
    id: "ver_7",
    promptId: "prompt_dedup_judge",
    version: 7,
    label: "testing-candidate",
    model: "gpt-4o-mini",
    provider: "openai",
    systemPrompt: "You are a careful news deduplication judge.",
    userPromptTemplate:
      "A: {{headline_a}}\nB: {{headline_b}}\nAre these the same story? Answer YES or NO.",
    parameters: { ...DEFAULT_PARAMS },
    status: "testing",
    createdBy: "dev@aiplane.local",
    createdAt: "2026-07-10T09:00:00.000Z",
  },
  {
    id: "ver_8",
    promptId: "prompt_dedup_judge",
    version: 8,
    label: "draft-rewrite",
    model: "claude-sonnet-4-20250514",
    provider: "anthropic",
    systemPrompt: "You judge whether two headlines refer to the same news event.",
    userPromptTemplate: "Compare:\n1) {{headline_a}}\n2) {{headline_b}}",
    parameters: { ...DEFAULT_PARAMS },
    status: "draft",
    createdBy: "dev@aiplane.local",
    createdAt: "2026-07-20T11:00:00.000Z",
  },
  {
    id: "ver_3",
    promptId: "prompt_summary",
    version: 3,
    model: "gpt-4o-mini",
    provider: "openai",
    systemPrompt: "Summarise related news articles concisely.",
    userPromptTemplate: "Articles:\n{{articles}}\n\nWrite a 3-sentence cluster summary.",
    parameters: { temperature: 0.4, maxTokens: 512 },
    status: "active",
    createdBy: "dev@aiplane.local",
    createdAt: "2026-06-20T08:15:00.000Z",
  },
  {
    id: "ver_ack_1",
    promptId: "prompt_ack_triage",
    version: 1,
    model: "claude-haiku-4-20250414",
    provider: "anthropic",
    systemPrompt: "Classify incident severity for on-call triage.",
    userPromptTemplate: "Incident:\n{{incident}}\n\nSeverity: critical|high|medium|low",
    parameters: { ...DEFAULT_PARAMS },
    status: "draft",
    createdBy: "dev@aiplane.local",
    createdAt: "2026-04-02T16:00:00.000Z",
  },
];

const INITIAL_GUARDRAILS: Guardrail[] = [
  {
    id: "gr_block_pii_hint",
    projectId: "proj_news_radar",
    name: "block-secret-keyword",
    type: "keyword-blocklist",
    stage: "input",
    config: { keywords: ["classified", "secret"] },
    enabled: true,
    action: "block",
    blockMessage: "Contains a blocked keyword",
    createdAt: "2026-07-01T10:00:00.000Z",
  },
  {
    id: "gr_max_input",
    projectId: "proj_news_radar",
    name: "max-input-length",
    type: "max-length",
    stage: "input",
    config: { maxChars: 500 },
    enabled: true,
    action: "warn",
    createdAt: "2026-07-01T10:05:00.000Z",
  },
  {
    id: "gr_email_regex",
    projectId: "proj_news_radar",
    name: "block-emails",
    type: "regex-filter",
    stage: "both",
    config: { patterns: ["[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}"] },
    enabled: true,
    action: "block",
    createdAt: "2026-07-02T09:00:00.000Z",
  },
];

const INITIAL_GUARDRAIL_SETS: GuardrailSet[] = [
  {
    id: "gs_news_input",
    projectId: "proj_news_radar",
    name: "news-radar-input",
    shortCircuitOnBlock: true,
    guardrailIds: ["gr_block_pii_hint", "gr_max_input", "gr_email_regex"],
    createdAt: "2026-07-03T12:00:00.000Z",
  },
];

/** Reset mutable mock collections (call from tests between cases). */
export function resetGuardrailMocks(): void {
  MOCK_GUARDRAILS = structuredClone(INITIAL_GUARDRAILS);
  MOCK_GUARDRAIL_SETS = structuredClone(INITIAL_GUARDRAIL_SETS);
}

/** Reset mutable prompt/version fixtures (call from tests between cases). */
export function resetPromptMocks(): void {
  MOCK_PROMPTS = structuredClone(INITIAL_PROMPTS);
  MOCK_PROMPT_VERSIONS = structuredClone(INITIAL_PROMPT_VERSIONS);
}

resetGuardrailMocks();
resetPromptMocks();

export function listMockProjects(): Project[] {
  return MOCK_PROJECTS;
}

export function listMockPrompts(projectId?: string): Prompt[] {
  if (!projectId) return [...MOCK_PROMPTS];
  return MOCK_PROMPTS.filter((prompt) => prompt.projectId === projectId);
}

export function getMockPrompt(id: string): Prompt | undefined {
  return MOCK_PROMPTS.find((p) => p.id === id);
}

export function createMockPrompt(
  input: Omit<Prompt, "id" | "createdAt" | "updatedAt" | "tags" | "activeVersionId"> & {
    id?: string;
    tags?: string[];
    activeVersionId?: string;
  },
): Prompt {
  const now = new Date().toISOString();
  const prompt: Prompt = {
    id: input.id ?? `prompt_mock_${Date.now()}`,
    projectId: input.projectId,
    name: input.name,
    description: input.description,
    tags: input.tags ?? [],
    activeVersionId: input.activeVersionId,
    createdAt: now,
    updatedAt: now,
  };
  MOCK_PROMPTS = [...MOCK_PROMPTS, prompt];
  return prompt;
}

export function updateMockPrompt(
  id: string,
  patch: Partial<Pick<Prompt, "name" | "description" | "tags" | "activeVersionId">>,
): Prompt {
  const idx = MOCK_PROMPTS.findIndex((p) => p.id === id);
  if (idx < 0) throw new Error(`Prompt not found: ${id}`);
  const updated: Prompt = {
    ...MOCK_PROMPTS[idx],
    ...patch,
    id,
    updatedAt: new Date().toISOString(),
  };
  MOCK_PROMPTS = MOCK_PROMPTS.map((p, i) => (i === idx ? updated : p));
  return updated;
}

export function listMockPromptVersions(promptId: string): PromptVersion[] {
  return MOCK_PROMPT_VERSIONS.filter((v) => v.promptId === promptId).sort(
    (a, b) => a.version - b.version,
  );
}

export function getMockPromptVersion(
  promptId: string,
  versionId: string,
): PromptVersion | undefined {
  return MOCK_PROMPT_VERSIONS.find((v) => v.promptId === promptId && v.id === versionId);
}

export type CreateMockPromptVersionInput = {
  label?: string;
  model: string;
  provider: LLMProvider;
  systemPrompt?: string;
  userPromptTemplate?: string;
  parameters?: ModelParameters;
  createdBy?: string;
  id?: string;
};

export function createMockPromptVersion(
  promptId: string,
  input: CreateMockPromptVersionInput,
): PromptVersion {
  if (!getMockPrompt(promptId)) throw new Error(`Prompt not found: ${promptId}`);
  const existing = listMockPromptVersions(promptId);
  const nextVersion = existing.reduce((max, v) => Math.max(max, v.version), 0) + 1;
  const version: PromptVersion = {
    id: input.id ?? `ver_mock_${Date.now()}`,
    promptId,
    version: nextVersion,
    label: input.label,
    model: input.model,
    provider: input.provider,
    systemPrompt: input.systemPrompt ?? "",
    userPromptTemplate: input.userPromptTemplate ?? "",
    parameters: input.parameters ?? { ...DEFAULT_PARAMS },
    status: "draft",
    createdBy: input.createdBy?.trim() || "dev@aiplane.local",
    createdAt: new Date().toISOString(),
  };
  MOCK_PROMPT_VERSIONS = [...MOCK_PROMPT_VERSIONS, version];
  return version;
}

const PROMOTE_NEXT: Partial<Record<PromptVersionStatus, PromptVersionStatus>> = {
  draft: "testing",
  testing: "active",
};

export function promoteMockPromptVersion(promptId: string, versionId: string): PromptVersion {
  const version = getMockPromptVersion(promptId, versionId);
  if (!version) throw new Error(`Prompt version not found: ${versionId}`);
  const next = PROMOTE_NEXT[version.status];
  if (!next) {
    throw new Error(`Cannot promote from status: ${version.status}`);
  }
  return updateMockPromptVersionStatus(promptId, versionId, next);
}

export function updateMockPromptVersionStatus(
  promptId: string,
  versionId: string,
  status: PromptVersionStatus,
): PromptVersion {
  const version = getMockPromptVersion(promptId, versionId);
  if (!version) throw new Error(`Prompt version not found: ${versionId}`);

  if (status === "active") {
    MOCK_PROMPT_VERSIONS = MOCK_PROMPT_VERSIONS.map((v) => {
      if (v.promptId !== promptId) return v;
      if (v.id === versionId) return { ...v, status: "active" };
      if (v.status === "active") return { ...v, status: "archived" };
      return v;
    });
    updateMockPrompt(promptId, { activeVersionId: versionId });
  } else {
    MOCK_PROMPT_VERSIONS = MOCK_PROMPT_VERSIONS.map((v) =>
      v.id === versionId && v.promptId === promptId ? { ...v, status } : v,
    );
  }

  const updated = getMockPromptVersion(promptId, versionId);
  if (!updated) throw new Error(`Prompt version not found: ${versionId}`);
  return updated;
}

export type RunMockPlaygroundInput = {
  versionId?: string;
  variables?: Record<string, string>;
  provider: string;
  model: string;
  temperature?: number;
  maxTokens?: number;
};

export function runMockPlayground(
  promptId: string,
  input: RunMockPlaygroundInput,
): PlaygroundRunResponse {
  const prompt = getMockPrompt(promptId);
  if (!prompt) throw new Error(`Prompt not found: ${promptId}`);

  const versionId = input.versionId || prompt.activeVersionId;
  if (!versionId) throw new Error("No versionId provided and prompt has no active version");
  const version = getMockPromptVersion(promptId, versionId);
  if (!version) throw new Error(`Prompt version not found: ${versionId}`);

  const variables = input.variables ?? {};
  let rendered = version.userPromptTemplate;
  for (const [key, value] of Object.entries(variables)) {
    rendered = rendered.split(`{{${key}}}`).join(value);
  }

  const latencyMs = 42 + Object.keys(variables).length * 3;
  return {
    content: `[mock ${input.provider}/${input.model}] Completed playground run.\n\n${rendered}`,
    inputTokens: Math.max(12, Math.ceil(rendered.length / 4)),
    outputTokens: 48,
    latencyMs,
    provider: input.provider,
    model: input.model,
    blockedByGuardrail: false,
  };
}

export function listMockGuardrails(projectId?: string): Guardrail[] {
  if (!projectId) return [...MOCK_GUARDRAILS];
  return MOCK_GUARDRAILS.filter((g) => g.projectId === projectId);
}

export function listMockGuardrailSets(projectId?: string): GuardrailSet[] {
  if (!projectId) return [...MOCK_GUARDRAIL_SETS];
  return MOCK_GUARDRAIL_SETS.filter((s) => s.projectId === projectId);
}

export function getMockGuardrail(id: string): Guardrail | undefined {
  return MOCK_GUARDRAILS.find((g) => g.id === id);
}

export function getMockGuardrailSet(id: string): GuardrailSet | undefined {
  return MOCK_GUARDRAIL_SETS.find((s) => s.id === id);
}

export function createMockGuardrail(
  input: Omit<Guardrail, "id" | "createdAt"> & { id?: string },
): Guardrail {
  const guardrail: Guardrail = {
    ...input,
    id: input.id ?? `gr_mock_${Date.now()}`,
    createdAt: new Date().toISOString(),
  };
  MOCK_GUARDRAILS = [...MOCK_GUARDRAILS, guardrail];
  return guardrail;
}

export function updateMockGuardrail(id: string, patch: Partial<Guardrail>): Guardrail {
  const idx = MOCK_GUARDRAILS.findIndex((g) => g.id === id);
  if (idx < 0) throw new Error(`Guardrail not found: ${id}`);
  const updated = { ...MOCK_GUARDRAILS[idx], ...patch, id };
  MOCK_GUARDRAILS = MOCK_GUARDRAILS.map((g, i) => (i === idx ? updated : g));
  return updated;
}

export function createMockGuardrailSet(
  input: Omit<GuardrailSet, "id" | "createdAt"> & { id?: string },
): GuardrailSet {
  const set: GuardrailSet = {
    ...input,
    id: input.id ?? `gs_mock_${Date.now()}`,
    createdAt: new Date().toISOString(),
  };
  MOCK_GUARDRAIL_SETS = [...MOCK_GUARDRAIL_SETS, set];
  return set;
}

export function updateMockGuardrailSet(id: string, patch: Partial<GuardrailSet>): GuardrailSet {
  const idx = MOCK_GUARDRAIL_SETS.findIndex((s) => s.id === id);
  if (idx < 0) throw new Error(`Guardrail set not found: ${id}`);
  const updated = { ...MOCK_GUARDRAIL_SETS[idx], ...patch, id };
  MOCK_GUARDRAIL_SETS = MOCK_GUARDRAIL_SETS.map((s, i) => (i === idx ? updated : s));
  return updated;
}

function evaluateRule(guardrail: Guardrail, text: string): EvaluatorResult {
  if (!guardrail.enabled) {
    return {
      guardrailId: guardrail.id,
      name: guardrail.name,
      type: guardrail.type,
      stage: guardrail.stage,
      passed: true,
      reason: "",
    };
  }

  const config = guardrail.config as Record<string, unknown>;
  let passed = true;
  let reason = "";
  let matchedFragment: string | undefined;

  if (guardrail.type === "keyword-blocklist") {
    const keywords = Array.isArray(config.keywords) ? (config.keywords as string[]) : [];
    const lower = text.toLowerCase();
    for (const keyword of keywords) {
      if (keyword && lower.includes(String(keyword).toLowerCase())) {
        passed = false;
        matchedFragment = String(keyword);
        reason = guardrail.blockMessage ?? `Blocked keyword: ${keyword}`;
        break;
      }
    }
  } else if (guardrail.type === "regex-filter") {
    const patterns = Array.isArray(config.patterns) ? (config.patterns as string[]) : [];
    for (const pattern of patterns) {
      try {
        const match = new RegExp(String(pattern)).exec(text);
        if (match) {
          passed = false;
          matchedFragment = match[0];
          reason = guardrail.blockMessage ?? `Blocked by regex: ${pattern}`;
          break;
        }
      } catch {
        reason = `Invalid regex: ${pattern}`;
        passed = false;
        break;
      }
    }
  } else if (guardrail.type === "max-length") {
    const maxChars = Number(config.maxChars ?? 0);
    if (text.length > maxChars) {
      passed = false;
      reason = guardrail.blockMessage ?? `Text length ${text.length} exceeds max of ${maxChars}`;
    }
  }

  return {
    guardrailId: guardrail.id,
    name: guardrail.name,
    type: guardrail.type,
    stage: guardrail.stage,
    passed,
    reason,
    action: passed ? null : guardrail.action,
    matchedFragment: matchedFragment ?? null,
  };
}

export function testMockGuardrail(id: string, text: string): EvaluatorResult {
  const guardrail = getMockGuardrail(id);
  if (!guardrail) throw new Error(`Guardrail not found: ${id}`);
  return evaluateRule(guardrail, text);
}

export function evaluateMockGuardrailSet(
  id: string,
  input: string,
  output: string,
  shortCircuitOnBlock?: boolean,
): GuardrailSetEvaluateResponse {
  const set = getMockGuardrailSet(id);
  if (!set) throw new Error(`Guardrail set not found: ${id}`);
  const shortCircuit = shortCircuitOnBlock ?? set.shortCircuitOnBlock;
  const results: EvaluatorResult[] = [];
  let blocked = false;
  let shortCircuited = false;

  for (const guardrailId of set.guardrailIds) {
    const guardrail = getMockGuardrail(guardrailId);
    if (!guardrail || !guardrail.enabled) continue;

    const stages: Array<"input" | "output"> = [];
    if (guardrail.stage === "input" || guardrail.stage === "both") stages.push("input");
    if (guardrail.stage === "output" || guardrail.stage === "both") stages.push("output");

    for (const stage of stages) {
      const result = {
        ...evaluateRule(guardrail, stage === "input" ? input : output),
        stage,
      };
      results.push(result);
      if (!result.passed && result.action === "block") {
        blocked = true;
        if (shortCircuit) {
          shortCircuited = true;
          return { blocked, shortCircuited, results };
        }
      }
    }
  }

  return { blocked, shortCircuited, results };
}
