import { beforeEach, describe, expect, it } from "vitest";
import { evaluateMockGuardrailSet, resetGuardrailMocks, testMockGuardrail } from "../mocks";

describe("guardrail mocks", () => {
  beforeEach(() => {
    resetGuardrailMocks();
  });

  it("fails keyword test when text contains a blocked word", () => {
    const result = testMockGuardrail("gr_block_pii_hint", "top secret plan");
    expect(result.passed).toBe(false);
    expect(result.action).toBe("block");
  });

  it("short-circuits set evaluation on first block by default", () => {
    const response = evaluateMockGuardrailSet("gs_news_input", "this is classified material", "");
    expect(response.blocked).toBe(true);
    expect(response.shortCircuited).toBe(true);
    expect(response.results).toHaveLength(1);
  });

  it("runs all members when short-circuit is disabled", () => {
    const response = evaluateMockGuardrailSet(
      "gs_news_input",
      "this is classified material",
      "",
      false,
    );
    expect(response.blocked).toBe(true);
    expect(response.shortCircuited).toBe(false);
    expect(response.results.length).toBeGreaterThan(1);
  });
});
