import { describe, expectTypeOf, it } from "vitest";
import type { Project } from "./project";

describe("Project type contract", () => {
  it("requires id, slug, name, and createdAt", () => {
    expectTypeOf<Project>().toMatchTypeOf<{
      id: string;
      slug: string;
      name: string;
      createdAt: string;
    }>();
  });
});
