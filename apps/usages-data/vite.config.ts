import path from "node:path";
import { fileURLToPath } from "node:url";
import federation from "@originjs/vite-plugin-federation";
import react from "@vitejs/plugin-react";
import { defineConfig } from "vite";

const __dirname = path.dirname(fileURLToPath(import.meta.url));

export default defineConfig({
  plugins: [
    react(),
    federation({
      name: "usagesData",
      filename: "remoteEntry.js",
      exposes: {
        "./App": "./src/App.tsx",
      },
      shared: {
        react: { singleton: true, requiredVersion: "^18.3.1" },
        "react-dom": { singleton: true, requiredVersion: "^18.3.1" },
      },
    }),
  ],
  resolve: {
    alias: {
      "@": path.resolve(__dirname, "./src"),
    },
  },
  server: {
    port: 5177,
    cors: true,
  },
  preview: {
    port: 5177,
    cors: true,
  },
  build: {
    target: "esnext",
  },
});
