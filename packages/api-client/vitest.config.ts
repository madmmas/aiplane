import path from "node:path";
import { fileURLToPath } from "node:url";
import { createVitestConfig } from "../../vitest.shared";

const dirname = path.dirname(fileURLToPath(import.meta.url));

export default createVitestConfig({
  root: dirname,
  setupFiles: [path.join(dirname, "vitest.setup.ts")],
});
