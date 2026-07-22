import { useEffect, useState } from "react";

/** SPEC §12 breakpoints for the host shell sidebar. */
export type ShellBreakpoint = "desktop" | "compact" | "mobile";

function getBreakpoint(width: number): ShellBreakpoint {
  if (width >= 1280) return "desktop";
  if (width >= 1024) return "compact";
  return "mobile";
}

export function useShellBreakpoint(): ShellBreakpoint {
  const [breakpoint, setBreakpoint] = useState<ShellBreakpoint>(() =>
    typeof window === "undefined" ? "desktop" : getBreakpoint(window.innerWidth),
  );

  useEffect(() => {
    const onResize = () => setBreakpoint(getBreakpoint(window.innerWidth));
    window.addEventListener("resize", onResize);
    return () => window.removeEventListener("resize", onResize);
  }, []);

  return breakpoint;
}
