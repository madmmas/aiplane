import { useTheme } from "../hooks/use-theme";

export function ThemeSwitcher() {
  const { theme, toggleTheme } = useTheme();
  const isDark = theme === "dark";

  return (
    <button
      type="button"
      onClick={toggleTheme}
      aria-label={`Current theme: ${theme}. Click to toggle.`}
      style={{
        padding: "0.4rem 0.75rem",
        border: `1px solid ${isDark ? "#2e3248" : "#ccc"}`,
        borderRadius: "4px",
        background: "transparent",
        cursor: "pointer",
        fontSize: "0.875rem",
        color: "inherit",
      }}
    >
      {isDark ? "Light" : "Dark"}
    </button>
  );
}
