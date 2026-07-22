import { describe, expect, it } from "vitest";
import { createApiClient } from "./client";
import { MOCK_PROJECTS } from "./mocks";

describe("createApiClient", () => {
  it("fetches JSON through MSW when mocks are disabled", async () => {
    const client = createApiClient({
      baseUrl: "http://localhost:8080",
      useMocks: false,
    });

    const page = await client.apiFetch<{ content: typeof MOCK_PROJECTS }>("/api/v1/projects");
    expect(page.content).toEqual(MOCK_PROJECTS);
  });
});
