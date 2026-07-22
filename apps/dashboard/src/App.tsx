import { ApiClientProvider } from "@repo/api-client";
import type { Project } from "@repo/types";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import React, { useCallback, useState } from "react";
import { DashboardLayout } from "./components/dashboard-layout";
import { HostPlaceholderPage } from "./components/host-placeholder-page";
import { RemoteLoader } from "./components/remote-loader";
import { ThemeProvider } from "./hooks/use-theme";
import { type NavId, isRemoteNav } from "./lib/nav";

const PromptManagerApp = React.lazy(() => import("promptManager/App"));
const GuardrailApp = React.lazy(() => import("guardrail/App"));
const UserManagerApp = React.lazy(() => import("userManager/App"));
const UsagesDataApp = React.lazy(() => import("usagesData/App"));

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 30_000,
      retry: 1,
    },
  },
});

function RemoteViews({ activeId }: { activeId: NavId }) {
  switch (activeId) {
    case "prompts":
      return (
        <RemoteLoader remoteName="Prompt Manager">
          <PromptManagerApp />
        </RemoteLoader>
      );
    case "guardrails":
      return (
        <RemoteLoader remoteName="Guardrail">
          <GuardrailApp />
        </RemoteLoader>
      );
    case "users":
      return (
        <RemoteLoader remoteName="User Manager">
          <UserManagerApp />
        </RemoteLoader>
      );
    case "usage":
      return (
        <RemoteLoader remoteName="Usages Data">
          <UsagesDataApp />
        </RemoteLoader>
      );
    case "providers":
      return (
        <HostPlaceholderPage
          title="Providers"
          description="Configure Anthropic, OpenAI, Azure OpenAI, Bedrock, Ollama, and Gemini credentials per project."
        />
      );
    case "apiKeys":
      return (
        <HostPlaceholderPage
          title="API Keys"
          description="Issue and revoke programmatic API keys (aimg_…) for client applications."
        />
      );
    default:
      return null;
  }
}

function DashboardApp() {
  const [activeId, setActiveId] = useState<NavId>("prompts");
  const [project, setProject] = useState<Project | null>(null);

  const onProjectChange = useCallback((next: Project) => {
    setProject(next);
  }, []);

  return (
    <DashboardLayout
      activeId={activeId}
      onSelectNav={setActiveId}
      project={project}
      onProjectChange={onProjectChange}
    >
      <RemoteViews activeId={activeId} />
      {!isRemoteNav(activeId) ? null : null}
    </DashboardLayout>
  );
}

export default function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <ApiClientProvider
        config={{
          baseUrl: import.meta.env.VITE_API_URL ?? "http://localhost:8080",
          getAccessToken: () => localStorage.getItem("aiplane-access-token"),
          useMocks: true,
        }}
      >
        <ThemeProvider>
          <DashboardApp />
        </ThemeProvider>
      </ApiClientProvider>
    </QueryClientProvider>
  );
}
