import { type ReactNode, createContext, useContext, useMemo } from "react";
import { type ApiClient, type ApiClientConfig, createApiClient } from "./client";

const ApiClientContext = createContext<ApiClient | null>(null);

export function ApiClientProvider({
  config,
  client,
  children,
}: {
  config?: ApiClientConfig;
  client?: ApiClient;
  children: ReactNode;
}) {
  const value = useMemo(
    () =>
      client ??
      createApiClient(
        config ?? {
          baseUrl: "http://localhost:8080",
          useMocks: true,
        },
      ),
    [client, config],
  );

  return <ApiClientContext.Provider value={value}>{children}</ApiClientContext.Provider>;
}

export function useApiClient(): ApiClient {
  const ctx = useContext(ApiClientContext);
  if (!ctx) {
    throw new Error("useApiClient must be used within ApiClientProvider");
  }
  return ctx;
}
