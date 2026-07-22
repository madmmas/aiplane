import { cn } from "@repo/ui";
import { IconMoon, IconSun } from "@tabler/icons-react";
import { useTheme } from "../hooks/use-theme";

export function ThemeSwitcher({ className }: { className?: string }) {
  const { theme, toggleTheme } = useTheme();

  return (
    <button
      type="button"
      onClick={toggleTheme}
      aria-label={`Current theme: ${theme}. Click to toggle.`}
      className={cn(
        "inline-flex size-7 items-center justify-center rounded-md text-muted-foreground",
        "transition-colors hover:bg-surface-raised hover:text-foreground",
        className,
      )}
    >
      {theme === "dark" ? <IconSun className="size-4" /> : <IconMoon className="size-4" />}
    </button>
  );
}
