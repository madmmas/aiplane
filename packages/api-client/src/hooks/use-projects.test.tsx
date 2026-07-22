import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { renderHook, waitFor } from "@testing-library/react";
import type { ReactNode } from "react";
import { describe, expect, it } from "vitest";
import { ApiClientProvider } from "../context";
import { MOCK_PROJECTS } from "../mocks";
import { useProjects } from "./use-projects";

function createWrapper() {
  const queryClient = new QueryClient({
    defaultOptions: {
      queries: { retry: false },
    },
  });

  return function Wrapper({ children }: { children: ReactNode }) {
    return (
      <QueryClientProvider client={queryClient}>
        <ApiClientProvider
          config={{
            baseUrl: "http://localhost:8080",
            // Hit the network so MSW can intercept — preferred over stubbing fetch.
            useMocks: false,
          }}
        >
          {children}
        </ApiClientProvider>
      </QueryClientProvider>
    );
  };
}

describe("useProjects", () => {
  it("loads projects from the API via MSW", async () => {
    const { result } = renderHook(() => useProjects(), {
      wrapper: createWrapper(),
    });

    await waitFor(() => {
      expect(result.current.isSuccess).toBe(true);
    });

    expect(result.current.data).toEqual(MOCK_PROJECTS);
  });
});
