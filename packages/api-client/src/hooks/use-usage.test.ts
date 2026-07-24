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
  beforeEach(() => {
    resetUsageMocks();
  });

  it("aggregates summary by provider for 7d from seed fixtures", () => {
    const summary = getMockUsageSummary("proj_news_radar", "7d");
    // Seed has 6 news-radar events within 7d (days 0,0,1,2,4,6) and 2 older.
    expect(summary.totalRequests).toBe(6);
    expect(summary.totalCostUsd).toBeGreaterThan(0);
    expect(summary.byProvider.length).toBeGreaterThanOrEqual(2);
    expect(summary.byProvider.map((r) => r.provider).sort()).toEqual(["anthropic", "openai"]);
  });

  it("lists seed events in range", () => {
    const from = new Date(Date.now() - 3 * 86_400_000).toISOString();
    const to = new Date().toISOString();
    const events = listMockUsageEvents("proj_news_radar", from, to);
    expect(events.length).toBeGreaterThanOrEqual(3);
  });

  it("projects monthly cost from 7d average over seed data", () => {
    const summary = getMockUsageSummary("proj_news_radar", "7d");
    const projection = getMockUsageCostProjection("proj_news_radar");
    expect(projection.windowDays).toBe(7);
    expect(projection.avgDailyCostUsd).toBeCloseTo(summary.totalCostUsd / 7);
    expect(projection.projectedMonthlyCostUsd).toBeCloseTo((summary.totalCostUsd / 7) * 30);
  });

  it("includes ingested events on top of seed fixtures", () => {
    const recentTs = new Date(Date.now() - 60_000).toISOString();
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
    ]);
    const summary = getMockUsageSummary("proj_news_radar", "7d");
    expect(summary.totalRequests).toBe(7);
  });
});
