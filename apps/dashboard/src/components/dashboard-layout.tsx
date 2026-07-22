import { useCallback, useEffect, useState } from "react";
import { useTheme } from "../hooks/use-theme";
import { AppSidebar } from "./app-sidebar";
import { BrandLogo } from "./brand-logo";
import { ThemeSwitcher } from "./theme-switcher";

const SIDEBAR_STORAGE_KEY = "aiplane-sidebar-open";

function getStoredSidebarOpen(): boolean {
  if (typeof window === "undefined") return true;
  const stored = localStorage.getItem(SIDEBAR_STORAGE_KEY);
  if (stored === null) return true;
  return stored !== "false";
}

interface DashboardLayoutProps {
  children: React.ReactNode;
  /** Optional controls rendered in the topbar (e.g. app tabs). */
  topbarNav?: React.ReactNode;
}

/**
 * Minimal host shell chrome for brand assets (#6).
 * Full mock layout (nav icons, tokens, etc.) lands in #10.
 */
export function DashboardLayout({ children, topbarNav }: DashboardLayoutProps) {
  const { theme } = useTheme();
  const [sidebarOpen, setSidebarOpen] = useState(getStoredSidebarOpen);
  const collapsed = !sidebarOpen;

  const toggleSidebar = useCallback(() => {
    setSidebarOpen((prev) => {
      const next = !prev;
      localStorage.setItem(SIDEBAR_STORAGE_KEY, String(next));
      return next;
    });
  }, []);

  useEffect(() => {
    const handler = (e: KeyboardEvent) => {
      if ((e.metaKey || e.ctrlKey) && e.key === "b") {
        e.preventDefault();
        toggleSidebar();
      }
    };
    window.addEventListener("keydown", handler);
    return () => window.removeEventListener("keydown", handler);
  }, [toggleSidebar]);

  const isDark = theme === "dark";
  const border = isDark ? "#2e3248" : "#e5e5e5";

  return (
    <div
      style={{
        display: "flex",
        minHeight: "100vh",
        width: "100%",
        background: isDark ? "#0f1117" : "#ffffff",
        color: isDark ? "#e8eaf0" : "#1a1d27",
      }}
    >
      <AppSidebar theme={theme} collapsed={collapsed} />
      <div style={{ display: "flex", flex: 1, flexDirection: "column", minWidth: 0 }}>
        <header
          style={{
            display: "flex",
            alignItems: "center",
            gap: "0.75rem",
            height: "3rem",
            padding: "0 1rem",
            borderBottom: `1px solid ${border}`,
            background: isDark ? "rgba(15,17,23,0.85)" : "rgba(255,255,255,0.85)",
            position: "sticky",
            top: 0,
            zIndex: 10,
          }}
        >
          <button
            type="button"
            onClick={toggleSidebar}
            aria-label={collapsed ? "Expand sidebar" : "Collapse sidebar"}
            aria-expanded={!collapsed}
            style={{
              padding: "0.35rem 0.6rem",
              border: `1px solid ${border}`,
              borderRadius: "4px",
              background: "transparent",
              cursor: "pointer",
              fontSize: "0.875rem",
              color: "inherit",
            }}
          >
            {collapsed ? "»" : "«"}
          </button>
          {/* Topbar brand: icon when sidebar is collapsed so the mark stays visible. */}
          {collapsed ? <BrandLogo theme={theme} collapsed height={24} /> : null}
          <div
            style={{ display: "flex", flex: 1, alignItems: "center", gap: "0.5rem", minWidth: 0 }}
          >
            {topbarNav}
          </div>
          <ThemeSwitcher />
        </header>
        <main style={{ flex: 1, padding: "1.5rem", overflow: "auto" }}>{children}</main>
      </div>
    </div>
  );
}
