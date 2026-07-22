import { cn } from "@repo/ui";
import {
  type Icon,
  IconBook,
  IconBrandGithub,
  IconChartBar,
  IconKey,
  IconMessages,
  IconPlug,
  IconShieldCheck,
  IconUsers,
} from "@tabler/icons-react";
import type { NavId } from "../lib/nav";

type NavItem = {
  id: NavId;
  label: string;
  icon: Icon;
};

const MANAGE_ITEMS: NavItem[] = [
  { id: "prompts", label: "Prompts", icon: IconMessages },
  { id: "guardrails", label: "Guardrails", icon: IconShieldCheck },
  { id: "users", label: "Users", icon: IconUsers },
  { id: "usage", label: "Usage", icon: IconChartBar },
];

const CONFIG_ITEMS: NavItem[] = [
  { id: "providers", label: "Providers", icon: IconPlug },
  { id: "apiKeys", label: "API Keys", icon: IconKey },
];

interface SidebarNavProps {
  activeId: NavId;
  onSelect: (id: NavId) => void;
  collapsed: boolean;
}

function NavSection({
  title,
  items,
  activeId,
  onSelect,
  collapsed,
}: {
  title: string;
  items: NavItem[];
  activeId: NavId;
  onSelect: (id: NavId) => void;
  collapsed: boolean;
}) {
  return (
    <div className="flex flex-col gap-0.5">
      {!collapsed ? (
        <div className="px-3 pb-1 pt-2 text-[10px] font-medium uppercase tracking-[0.07em] text-muted-foreground/60">
          {title}
        </div>
      ) : null}
      {items.map(({ id, label, icon: ItemIcon }) => {
        const active = id === activeId;
        return (
          <button
            key={id}
            type="button"
            title={collapsed ? label : undefined}
            aria-current={active ? "page" : undefined}
            onClick={() => onSelect(id)}
            className={cn(
              "mx-1.5 flex items-center gap-2 rounded-md px-2.5 py-1.5 text-xs transition-colors",
              "text-muted-foreground hover:bg-surface-card hover:text-foreground",
              active && "bg-surface-raised font-medium text-accent hover:text-accent",
              collapsed && "justify-center px-0",
            )}
          >
            <ItemIcon className="size-4 shrink-0" aria-hidden />
            {!collapsed ? <span className="truncate">{label}</span> : null}
          </button>
        );
      })}
    </div>
  );
}

export function SidebarNav({ activeId, onSelect, collapsed }: SidebarNavProps) {
  return (
    <nav className="flex h-full flex-col gap-1 py-2" aria-label="Primary">
      <NavSection
        title="Manage"
        items={MANAGE_ITEMS}
        activeId={activeId}
        onSelect={onSelect}
        collapsed={collapsed}
      />
      <div className="mx-3 my-1.5 h-px bg-border" />
      <NavSection
        title="Config"
        items={CONFIG_ITEMS}
        activeId={activeId}
        onSelect={onSelect}
        collapsed={collapsed}
      />
      <div className="mt-auto">
        <div className="mx-3 my-1.5 h-px bg-border" />
        {!collapsed ? (
          <div className="flex flex-col gap-0.5">
            <a
              href="https://github.com/madmmas/aiplane/blob/main/docs/SPEC.md"
              target="_blank"
              rel="noreferrer"
              className="mx-1.5 flex items-center gap-2 rounded-md px-2.5 py-1.5 text-xs text-muted-foreground hover:bg-surface-card hover:text-foreground"
            >
              <IconBook className="size-4 shrink-0" aria-hidden />
              Docs
            </a>
            <a
              href="https://github.com/madmmas/aiplane"
              target="_blank"
              rel="noreferrer"
              className="mx-1.5 flex items-center gap-2 rounded-md px-2.5 py-1.5 text-xs text-muted-foreground hover:bg-surface-card hover:text-foreground"
            >
              <IconBrandGithub className="size-4 shrink-0" aria-hidden />
              GitHub
            </a>
          </div>
        ) : null}
      </div>
    </nav>
  );
}
