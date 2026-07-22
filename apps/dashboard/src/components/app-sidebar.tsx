import { cn } from "@repo/ui";
import type { NavId } from "../lib/nav";
import { SidebarNav } from "./sidebar-nav";

/** Expanded sidebar width (SPEC §5.1 / issue #10: 160px). */
export const SIDEBAR_WIDTH_PX = 160;
/** Icon-only collapsed width. */
export const SIDEBAR_COLLAPSED_PX = 48;

interface AppSidebarProps {
  activeId: NavId;
  onSelect: (id: NavId) => void;
  collapsed: boolean;
  className?: string;
}

export function AppSidebar({ activeId, onSelect, collapsed, className }: AppSidebarProps) {
  return (
    <aside
      aria-label="Application"
      className={cn(
        "flex h-full shrink-0 flex-col border-r border-border bg-background text-foreground",
        "transition-[width] duration-200 ease-linear",
        className,
      )}
      style={{ width: collapsed ? SIDEBAR_COLLAPSED_PX : SIDEBAR_WIDTH_PX }}
    >
      <SidebarNav activeId={activeId} onSelect={onSelect} collapsed={collapsed} />
    </aside>
  );
}
