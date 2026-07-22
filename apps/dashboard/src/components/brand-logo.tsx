import { cn } from "@repo/ui";
import type { ThemeMode } from "../hooks/use-theme";

interface BrandLogoProps {
  theme: ThemeMode;
  collapsed?: boolean;
  height?: number;
  className?: string;
}

/** Theme-aware AIPlane brand mark from `/public` assets (#6). */
export function BrandLogo({ theme, collapsed = false, height, className }: BrandLogoProps) {
  const src = collapsed
    ? "/aiplane-icon.svg"
    : theme === "dark"
      ? "/aiplane-logo-dark.svg"
      : "/aiplane-logo-light.svg";

  const resolvedHeight = height ?? (collapsed ? 28 : 28);
  const alt = collapsed ? "AIPlane" : "AIPlane logo";

  return (
    <img
      src={src}
      alt={alt}
      height={resolvedHeight}
      className={cn("block w-auto max-w-full", className)}
      style={{ height: resolvedHeight }}
      decoding="async"
    />
  );
}
