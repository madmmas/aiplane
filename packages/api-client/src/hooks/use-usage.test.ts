import { beforeEach, describe, expect, it } from "vitest";
import { ingestMockUsageEvents, resetUsageMocks } from "../mocks";

describe("usage ingest mocks", () => {
  beforeEach(() => {
    resetUsageMocks();
  });

  it("rejects an empty batch", () => {
    expect(() => ingestMockUsageEvents([])).toThrow(/non-empty/);
  });

  it("accepts a valid batch", () => {
    const result = ingestMockUsageEvents([
      {
        projectId: "proj_news_radar",
        provider: "anthropic",
        model: "claude-sonnet-4-20250514",
        status: "success",
        inputTokens: 10,
        outputTokens: 20,
      },
    ]);
    expect(result.accepted).toBe(1);
    expect(result.events[0].id).toMatch(/^ue_mock_/);
    expect(result.events[0].costUsd).toBe(0);
  });

  it("rejects the whole batch when any event is invalid", () => {
    expect(() =>
      ingestMockUsageEvents([
        {
          projectId: "proj_news_radar",
          provider: "openai",
          model: "gpt-4o",
          status: "success",
        },
        {
          projectId: "proj_missing",
          provider: "openai",
          model: "gpt-4o",
          status: "success",
        },
      ]),
    ).toThrow(/Unknown projectId/);
  });
});
