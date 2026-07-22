import path from "node:path";
import { fileURLToPath } from "node:url";
import { createVitestConfig } from "../../vitest.shared";

const dirname = path.dirname(fileURLToPath(import.meta.url));

export default createVitestConfig({
  root: dirname,
  alias: {
    "@": path.resolve(dirname, "./src"),
  },
  // Federated `import("promptManager/App")` etc. break V8 coverage remapping.
  coverageExclude: ["src/App.tsx"],
});
