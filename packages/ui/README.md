# `@repo/ui`

Shared AIPlane design system — CSS tokens, Tailwind preset, and shadcn/ui primitives used by every MFE.

## What's included

| Export | Purpose |
|--------|---------|
| `@repo/ui` | React components (`Button`, `Input`, `Badge`, `Card`) + `cn()` |
| `@repo/ui/tokens.css` | Light / dark CSS variables (SPEC §6 / §12 palette) |
| `@repo/ui/tailwind.config` | Shared Tailwind preset (colors, fonts, type scale) |

**Typography:** Inter (body) · JetBrains Mono (code / slugs)  
**Theming:** class-based dark mode (`.dark`). AIPlane's visual default is dark — start apps with `class="dark"` on `<html>` or apply it via a theme provider.

## Install in an app

From the monorepo root (workspace already covers `packages/*`):

```bash
pnpm add @repo/ui --filter @repo/dashboard
pnpm add -D tailwindcss postcss autoprefixer --filter @repo/dashboard
```

### 1. Import tokens

```ts
// apps/<app>/src/main.tsx
import "@repo/ui/tokens.css";
```

Load fonts once (e.g. in `index.html`):

```html
<link rel="preconnect" href="https://fonts.googleapis.com" />
<link rel="preconnect" href="https://fonts.gstatic.com" crossorigin />
<link
  href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&family=JetBrains+Mono:wght@400;500;600&display=swap"
  rel="stylesheet"
/>
```

### 2. Extend Tailwind with the preset

```js
// apps/<app>/tailwind.config.js
import aiplanePreset from "@repo/ui/tailwind.config";

/** @type {import('tailwindcss').Config} */
export default {
  presets: [aiplanePreset],
  content: ["./index.html", "./src/**/*.{ts,tsx}", "../../packages/ui/src/**/*.{ts,tsx}"],
  // darkMode is already ["class"] in the preset
};
```

### 3. Use components

```tsx
import { Badge, Button, Card, CardContent, CardHeader, CardTitle, Input } from "@repo/ui";

export function Example() {
  return (
    <Card>
      <CardHeader>
        <CardTitle className="font-mono">news-radar/dedup-judge</CardTitle>
      </CardHeader>
      <CardContent className="flex flex-col gap-3">
        <Input placeholder="Prompt slug" />
        <div className="flex items-center gap-2">
          <Badge variant="success">active</Badge>
          <Button>Save</Button>
        </div>
      </CardContent>
    </Card>
  );
}
```

## Adding more shadcn components

This package is configured with `components.json`. From `packages/ui`:

```bash
pnpm dlx shadcn@latest add dialog --yes
```

Re-export new primitives from `src/index.ts`.

## Scripts

```bash
pnpm --filter @repo/ui typecheck
pnpm --filter @repo/ui lint
pnpm --filter @repo/ui build
```
