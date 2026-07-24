import { ApiClientProvider, resetUsageMocks } from "@repo/api-client";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { render, screen, within } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import type { ReactNode } from "react";
import { beforeEach, describe, expect, it } from "vitest";
import App from "./App";

class ResizeObserverStub {
  observe() {}
  unobserve() {}
  disconnect() {}
}

beforeEach(() => {
  globalThis.ResizeObserver = ResizeObserverStub as unknown as typeof ResizeObserver;
});

function Wrapper({ children }: { children: ReactNode }) {
  const queryClient = new QueryClient({
    defaultOptions: { queries: { retry: false }, mutations: { retry: false } },
  });
  return (
    <QueryClientProvider client={queryClient}>
      <ApiClientProvider config={{ baseUrl: "http://localhost:8080", useMocks: true }}>
        {children}
      </ApiClientProvider>
    </QueryClientProvider>
  );
}

describe("Usages Data App", () => {
  beforeEach(() => {
    resetUsageMocks();
  });

  it("renders headings and KPI values after mock load", async () => {
    render(
      <Wrapper>
        <App />
      </Wrapper>,
    );

    expect(screen.getByRole("heading", { level: 2, name: "Usage" })).toBeInTheDocument();
    expect(screen.getByText(/Usage overview/i)).toBeInTheDocument();

    expect(
      await screen.findByRole("heading", { level: 3, name: "Total requests" }),
    ).toBeInTheDocument();
    expect(screen.getByRole("heading", { level: 3, name: "Total cost" })).toBeInTheDocument();
    expect(screen.getByRole("heading", { level: 3, name: "Avg latency" })).toBeInTheDocument();

    const requestsCard = screen
      .getByRole("heading", { level: 3, name: "Total requests" })
      .closest(".rounded-xl") as HTMLElement;
    expect(await within(requestsCard).findByText("6")).toBeInTheDocument();
    expect(screen.getByText(/\d+ ms$/)).toBeInTheDocument();
  });

  it("exposes project and period filters and updates on period change", async () => {
    const user = userEvent.setup();
    render(
      <Wrapper>
        <App />
      </Wrapper>,
    );

    const projectSelect = await screen.findByLabelText("Project");
    const periodSelect = screen.getByLabelText("Period");
    expect(projectSelect).toBeInTheDocument();
    expect(periodSelect).toBeInTheDocument();

    const requestsHeading = await screen.findByRole("heading", {
      level: 3,
      name: "Total requests",
    });
    const requestsCard = requestsHeading.closest(".rounded-xl") as HTMLElement;
    expect(await within(requestsCard).findByText("6")).toBeInTheDocument();

    await user.selectOptions(periodSelect, "30d");
    // 30d includes the two older seed events (days 10 and 18) → 8 requests.
    expect(await within(requestsCard).findByText("8")).toBeInTheDocument();
  });

  it("renders the usage-over-time chart region", async () => {
    render(
      <Wrapper>
        <App />
      </Wrapper>,
    );

    expect(
      await screen.findByRole("heading", { level: 3, name: "Usage over time" }),
    ).toBeInTheDocument();
    expect(screen.getByRole("img", { name: "Usage over time" })).toBeInTheDocument();

    const breakdown = await screen.findByLabelText("Provider breakdown");
    expect(within(breakdown).getByText("anthropic")).toBeInTheDocument();
    expect(within(breakdown).getByText("openai")).toBeInTheDocument();
  });
});
