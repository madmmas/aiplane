import { beforeEach, describe, expect, it } from "vitest";
import {
  createMockPrompt,
  createMockPromptVersion,
  listMockPromptVersions,
  listMockPrompts,
  promoteMockPromptVersion,
  resetPromptMocks,
  runMockPlayground,
  updateMockPrompt,
} from "../mocks";

describe("prompt mocks", () => {
  beforeEach(() => {
    resetPromptMocks();
  });

  it("lists prompts filtered by project", () => {
    const prompts = listMockPrompts("proj_news_radar");
    expect(prompts.map((p) => p.id)).toEqual(["prompt_dedup_judge", "prompt_summary"]);
  });

  it("creates and updates a prompt", () => {
    const created = createMockPrompt({
      projectId: "proj_news_radar",
      name: "news-radar/new-prompt",
      description: "fresh",
      tags: ["test"],
    });
    expect(listMockPrompts("proj_news_radar").some((p) => p.id === created.id)).toBe(true);

    const updated = updateMockPrompt(created.id, { description: "updated" });
    expect(updated.description).toBe("updated");
  });

  it("creates a draft version and promotes draft → testing → active", () => {
    const draft = createMockPromptVersion("prompt_summary", {
      model: "gpt-4o-mini",
      provider: "openai",
      systemPrompt: "sys",
      userPromptTemplate: "Hello {{name}}",
    });
    expect(draft.status).toBe("draft");
    expect(draft.version).toBeGreaterThan(3);

    const testing = promoteMockPromptVersion("prompt_summary", draft.id);
    expect(testing.status).toBe("testing");

    const active = promoteMockPromptVersion("prompt_summary", draft.id);
    expect(active.status).toBe("active");

    const versions = listMockPromptVersions("prompt_summary");
    expect(versions.find((v) => v.id === "ver_3")?.status).toBe("archived");
  });

  it("runs playground with canned completion and latency", () => {
    const result = runMockPlayground("prompt_dedup_judge", {
      versionId: "ver_6",
      provider: "anthropic",
      model: "claude-sonnet-4-20250514",
      variables: { headline_a: "A", headline_b: "B" },
    });
    expect(result.content).toContain("Headline A: A");
    expect(result.latencyMs).toBeGreaterThan(0);
    expect(result.provider).toBe("anthropic");
  });
});
