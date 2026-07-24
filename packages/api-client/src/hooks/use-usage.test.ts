import { beforeEach, describe, expect, it } from "vitest";
import {
  getMockUsageCostProjection,
  getMockUsageSummary,
  ingestMockUsageEvents,
  listMockUsageEvents,
  resetUsageMocks,
} from "../mocks";

describe("usage ingest mocks", () => {
  beforeEach(() => {
    resetUsageMocks();
  });

  it("rejects an empty batch", () => {
    expect(() => ingestMockUsageEvents([])).toThrow(/non-empty/);
  });

  it("computes costUsd from rates when omitted", () => {
    const result = ingestMockUsageEvents([
      {
        projectId: "proj_news_radar",
        provider: "anthropic",
        model: "claude-sonnet-4-20250514",
        status: "success",
        inputTokens: 1000,
        outputTokens: 2000,
      },
    ]);
    expect(result.accepted).toBe(1);
    expect(result.events[0].id).toMatch(/^ue_mock_/);
    // 1000*0.003/1k + 2000*0.015/1k = 0.033
    expect(result.events[0].costUsd).toBe(0.033);
  });

  it("keeps an explicit costUsd override", () => {
    const result = ingestMockUsageEvents([
      {
        projectId: "proj_news_radar",
        provider: "openai",
        model: "gpt-4o-mini",
        status: "success",
        inputTokens: 1000,
        outputTokens: 1000,
        costUsd: 1.5,
      },
    ]);
    expect(result.events[0].costUsd).toBe(1.5);
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

describe("usage summary / events / projection mocks", () => {
  const recentTs = new Date(Date.now() - 60_000).toISOString();

  beforeEach(() => {
    resetUsageMocks();
    ingestMockUsageEvents([
      {
        projectId: "proj_news_radar",
        provider: "openai",
        model: "gpt-4o-mini",
        status: "success",
        inputTokens: 100,
        outputTokens: 50,
        costUsd: 0.01,
        timestamp: recentTs,
      },
      {
        projectId: "proj_news_radar",
        provider: "anthropic",
        model: "claude-haiku-4-20250414",
        status: "success",
        inputTokens: 200,
        outputTokens: 100,
        costUsd: 0.02,
        timestamp: recentTs,
      },
    ]);
  });

  it("aggregates summary by provider for 7d", () => {
    const summary = getMockUsageSummary("proj_news_radar", "7d");
    expect(summary.totalRequests).toBe(2);
    expect(summary.totalCostUsd).toBeCloseTo(0.03);
    expect(summary.byProvider).toHaveLength(2);
  });

  it("lists events in range", () => {
    const from = new Date(Date.now() - 86_400_000).toISOString();
    const to = new Date(Date.now() + 86_400_000).toISOString();
    const events = listMockUsageEvents("proj_news_radar", from, to);
    expect(events).toHaveLength(2);
  });

  it("projects monthly cost from 7d average", () => {
    const projection = getMockUsageCostProjection("proj_news_radar");
    expect(projection.windowDays).toBe(7);
    expect(projection.avgDailyCostUsd).toBeCloseTo(0.03 / 7);
    expect(projection.projectedMonthlyCostUsd).toBeCloseTo((0.03 / 7) * 30);
  });
});
