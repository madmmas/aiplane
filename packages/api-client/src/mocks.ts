import type {
  EvaluatorResult,
  Guardrail,
  GuardrailSet,
  GuardrailSetEvaluateResponse,
  Project,
  Prompt,
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

export const MOCK_PROMPTS: Prompt[] = [
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

export let MOCK_GUARDRAILS: Guardrail[] = [];
export let MOCK_GUARDRAIL_SETS: GuardrailSet[] = [];

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

resetGuardrailMocks();

export function listMockProjects(): Project[] {
  return MOCK_PROJECTS;
}

export function listMockPrompts(projectId?: string): Prompt[] {
  if (!projectId) return MOCK_PROMPTS;
  return MOCK_PROMPTS.filter((prompt) => prompt.projectId === projectId);
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
