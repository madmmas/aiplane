import type { Project, Prompt } from "@repo/types";

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

export function listMockProjects(): Project[] {
  return MOCK_PROJECTS;
}

export function listMockPrompts(projectId?: string): Prompt[] {
  if (!projectId) return MOCK_PROMPTS;
  return MOCK_PROMPTS.filter((prompt) => prompt.projectId === projectId);
}
