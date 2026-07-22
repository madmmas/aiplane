import path from "node:path";
import { fileURLToPath } from "node:url";
import react from "@vitejs/plugin-react";
import { type UserConfig, defineConfig } from "vitest/config";

const repoRoot = path.dirname(fileURLToPath(import.meta.url));

export type SharedVitestOptions = {
  /** Absolute path to the package/app directory (usually `__dirname`). */
  root: string;
  /** Extra path aliases relative to this package (e.g. `@` → `./src`). */
  alias?: Record<string, string>;
  /** Override setup files; defaults to the repo-root jest-dom setup. */
  setupFiles?: string[];
  /** Vitest environment; packages without DOM can use `node`. */
  environment?: "jsdom" | "node" | "happy-dom";
};

/**
 * Shared Vitest base used by every app/package.
 * Keep federation plugins out of test configs — unit tests mock remote boundaries.
 */
export function createVitestConfig(options: SharedVitestOptions): UserConfig {
  const { root, alias, setupFiles, environment = "jsdom" } = options;

  return defineConfig({
    plugins: environment === "node" ? [] : [react()],
    test: {
      root,
      environment,
      setupFiles: setupFiles ?? [path.join(repoRoot, "vitest.setup.ts")],
      include: ["src/**/*.{test,spec}.{ts,tsx}"],
      css: false,
      restoreMocks: true,
    },
    resolve: {
      alias,
    },
  });
}
