import type { ThemeMode } from "../hooks/use-theme";
import { BrandLogo } from "./brand-logo";

const SIDEBAR_WIDTH = "16rem";
const SIDEBAR_WIDTH_COLLAPSED = "3.5rem";

interface AppSidebarProps {
  theme: ThemeMode;
  collapsed: boolean;
}

/**
 * Host chrome sidebar that owns the brand mark.
 * Expanded → theme-aware wordmark; collapsed → icon-only mark.
 */
export function AppSidebar({ theme, collapsed }: AppSidebarProps) {
  const isDark = theme === "dark";
  const border = isDark ? "#2e3248" : "#e5e5e5";

  return (
    <aside
      aria-label="AIPlane brand"
      style={{
        display: "flex",
        flexDirection: "column",
        width: collapsed ? SIDEBAR_WIDTH_COLLAPSED : SIDEBAR_WIDTH,
        flexShrink: 0,
        borderRight: `1px solid ${border}`,
        background: isDark ? "#0f1117" : "#ffffff",
        color: isDark ? "#e8eaf0" : "#1a1d27",
        transition: "width 200ms ease",
      }}
    >
      <div
        style={{
          display: "flex",
          alignItems: "center",
          justifyContent: collapsed ? "center" : "flex-start",
          height: "3rem",
          padding: collapsed ? "0.5rem" : "0.5rem 0.75rem",
          borderBottom: `1px solid ${border}`,
          overflow: "hidden",
        }}
      >
        <BrandLogo theme={theme} collapsed={collapsed} />
      </div>
      {!collapsed && (
        <div
          style={{
            padding: "0.75rem",
            fontSize: "0.75rem",
            opacity: 0.65,
          }}
        >
          Micro-frontend host
        </div>
      )}
    </aside>
  );
}
