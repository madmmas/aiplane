import { useProjects } from "@repo/api-client";
import type { Project } from "@repo/types";
import { cn } from "@repo/ui";
import { IconChevronDown, IconLayoutGrid } from "@tabler/icons-react";
import { useEffect, useId, useRef, useState } from "react";

const PROJECT_STORAGE_KEY = "aiplane-current-project";

interface ProjectSwitcherProps {
  project: Project | null;
  onProjectChange: (project: Project) => void;
}

export function ProjectSwitcher({ project, onProjectChange }: ProjectSwitcherProps) {
  const { data: projects = [], isLoading } = useProjects();
  const [open, setOpen] = useState(false);
  const rootRef = useRef<HTMLDivElement>(null);
  const menuId = useId();

  useEffect(() => {
    if (!project && projects.length > 0) {
      const storedId = localStorage.getItem(PROJECT_STORAGE_KEY);
      const next = projects.find((p) => p.id === storedId) ?? projects[0];
      onProjectChange(next);
    }
  }, [project, projects, onProjectChange]);

  useEffect(() => {
    if (!open) return;
    const onPointer = (event: MouseEvent) => {
      if (!rootRef.current?.contains(event.target as Node)) setOpen(false);
    };
    const onKey = (event: KeyboardEvent) => {
      if (event.key === "Escape") setOpen(false);
    };
    window.addEventListener("mousedown", onPointer);
    window.addEventListener("keydown", onKey);
    return () => {
      window.removeEventListener("mousedown", onPointer);
      window.removeEventListener("keydown", onKey);
    };
  }, [open]);

  const label = project?.slug ?? (isLoading ? "Loading…" : "Select project");

  return (
    <div ref={rootRef} className="relative">
      <button
        type="button"
        aria-haspopup="menu"
        aria-expanded={open}
        aria-controls={menuId}
        onClick={() => setOpen((prev) => !prev)}
        className={cn(
          "inline-flex items-center gap-1.5 rounded-md border border-border bg-surface-card",
          "px-2 py-1 text-xs text-muted-foreground transition-colors",
          "hover:border-border hover:text-foreground",
        )}
      >
        <IconLayoutGrid className="size-3.5 shrink-0" aria-hidden />
        <span className="font-mono max-w-[10rem] truncate">{label}</span>
        <IconChevronDown className="size-3 shrink-0 opacity-70" aria-hidden />
      </button>

      {open ? (
        <div
          id={menuId}
          role="menu"
          aria-label="Projects"
          className={cn(
            "absolute left-0 top-full z-50 mt-1 min-w-[12rem] overflow-hidden rounded-md",
            "border border-border bg-popover py-1 text-popover-foreground shadow-md",
          )}
        >
          {projects.map((item) => {
            const selected = item.id === project?.id;
            return (
              <button
                key={item.id}
                type="button"
                role="menuitem"
                className={cn(
                  "flex w-full flex-col items-start gap-0.5 px-3 py-2 text-left text-xs",
                  "hover:bg-surface-raised",
                  selected && "bg-surface-raised text-accent",
                )}
                onClick={() => {
                  localStorage.setItem(PROJECT_STORAGE_KEY, item.id);
                  onProjectChange(item);
                  setOpen(false);
                }}
              >
                <span className="font-medium text-foreground">{item.name}</span>
                <span className="font-mono text-[10px] text-muted-foreground">{item.slug}</span>
              </button>
            );
          })}
        </div>
      ) : null}
    </div>
  );
}
