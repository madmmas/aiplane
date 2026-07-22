import type { ThemeMode } from "../hooks/use-theme";

interface BrandLogoProps {
  theme: ThemeMode;
  /** When true, show the square icon mark instead of the full wordmark. */
  collapsed?: boolean;
  /** CSS height for the image; width scales with the SVG aspect ratio (crisp at 1x/2x). */
  height?: number;
}

/**
 * Theme-aware AIPlane brand mark.
 * - Expanded: light/dark wordmark SVGs from /public
 * - Collapsed: square icon mark for icon-only sidebar
 */
export function BrandLogo({ theme, collapsed = false, height }: BrandLogoProps) {
  const src = collapsed
    ? "/aiplane-icon.svg"
    : theme === "dark"
      ? "/aiplane-logo-dark.svg"
      : "/aiplane-logo-light.svg";

  const resolvedHeight = height ?? (collapsed ? 28 : 32);
  const alt = collapsed ? "AIPlane" : "AIPlane logo";

  return (
    <img
      src={src}
      alt={alt}
      height={resolvedHeight}
      // Intrinsic SVG viewBoxes scale cleanly on retina; width auto preserves aspect ratio.
      style={{
        height: resolvedHeight,
        width: "auto",
        display: "block",
        maxWidth: "100%",
      }}
      decoding="async"
    />
  );
}
