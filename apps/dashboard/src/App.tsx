import React, { Suspense, useState } from "react";
import { DashboardLayout } from "./components/dashboard-layout";
import { ThemeProvider } from "./hooks/use-theme";

const PromptManagerApp = React.lazy(() => import("promptManager/App"));
const GuardrailApp = React.lazy(() => import("guardrail/App"));
const UserManagerApp = React.lazy(() => import("userManager/App"));
const UsagesDataApp = React.lazy(() => import("usagesData/App"));

function RemoteFallback() {
  return <div>Loading...</div>;
}

function RemoteErrorBoundary({
  children,
  fallback,
}: {
  children: React.ReactNode;
  fallback: React.ReactNode;
}) {
  return (
    <ErrorBoundary fallback={fallback}>
      <Suspense fallback={<RemoteFallback />}>{children}</Suspense>
    </ErrorBoundary>
  );
}

class ErrorBoundary extends React.Component<
  { children: React.ReactNode; fallback: React.ReactNode },
  { hasError: boolean }
> {
  state = { hasError: false };

  static getDerivedStateFromError() {
    return { hasError: true };
  }

  render() {
    if (this.state.hasError) {
      return this.props.fallback;
    }
    return this.props.children;
  }
}

type TabId = "dashboard" | "promptManager" | "guardrail" | "userManager" | "usagesData";

const TABS: { id: TabId; label: string }[] = [
  { id: "dashboard", label: "Dashboard" },
  { id: "promptManager", label: "Prompt Manager" },
  { id: "guardrail", label: "Guardrail" },
  { id: "userManager", label: "User Manager" },
  { id: "usagesData", label: "Usages Data" },
];

export default function App() {
  const [activeTab, setActiveTab] = useState<TabId>("dashboard");

  const topbarNav = (
    <nav style={{ display: "flex", gap: "0.5rem", flexWrap: "wrap" }} aria-label="Apps">
      {TABS.map(({ id, label }) => (
        <button
          key={id}
          type="button"
          onClick={() => setActiveTab(id)}
          style={{
            padding: "0.4rem 0.75rem",
            border: "1px solid #ccc",
            borderRadius: "4px",
            background: activeTab === id ? "#f0f0f0" : "transparent",
            cursor: "pointer",
            color: "inherit",
            fontSize: "0.875rem",
          }}
        >
          {label}
        </button>
      ))}
    </nav>
  );

  return (
    <ThemeProvider>
      <DashboardLayout topbarNav={topbarNav}>
        {activeTab === "dashboard" && (
          <p>Welcome to the AIPlane dashboard. Use the tabs to open each micro-frontend.</p>
        )}
        {activeTab === "promptManager" && (
          <RemoteErrorBoundary fallback={<div>Failed to load Prompt Manager.</div>}>
            <PromptManagerApp />
          </RemoteErrorBoundary>
        )}
        {activeTab === "guardrail" && (
          <RemoteErrorBoundary fallback={<div>Failed to load Guardrail.</div>}>
            <GuardrailApp />
          </RemoteErrorBoundary>
        )}
        {activeTab === "userManager" && (
          <RemoteErrorBoundary fallback={<div>Failed to load User Manager.</div>}>
            <UserManagerApp />
          </RemoteErrorBoundary>
        )}
        {activeTab === "usagesData" && (
          <RemoteErrorBoundary fallback={<div>Failed to load Usages Data.</div>}>
            <UsagesDataApp />
          </RemoteErrorBoundary>
        )}
      </DashboardLayout>
    </ThemeProvider>
  );
}
