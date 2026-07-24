import { ApiClientProvider, resetPromptMocks } from "@repo/api-client";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { render, screen, within } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import type { ReactNode } from "react";
import { beforeEach, describe, expect, it } from "vitest";
import App from "./App";

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

describe("Prompt Manager App", () => {
  beforeEach(() => {
    resetPromptMocks();
  });

  it("renders library and section headings", async () => {
    render(
      <Wrapper>
        <App />
      </Wrapper>,
    );

    expect(screen.getByRole("heading", { name: "Prompt Manager" })).toBeInTheDocument();
    expect(screen.getByRole("heading", { name: "Library" })).toBeInTheDocument();
    expect(screen.getByRole("heading", { name: "Create prompt" })).toBeInTheDocument();

    expect(await screen.findByText("news-radar/dedup-judge")).toBeInTheDocument();
    expect(screen.getByRole("heading", { name: "Version timeline" })).toBeInTheDocument();
    expect(screen.getByRole("heading", { name: "Editor" })).toBeInTheDocument();
    expect(screen.getByRole("heading", { name: "Playground" })).toBeInTheDocument();
  });

  it("creates a prompt via the form", async () => {
    const user = userEvent.setup();
    render(
      <Wrapper>
        <App />
      </Wrapper>,
    );

    const nameInput = await screen.findByPlaceholderText("news-radar/my-prompt");
    await user.clear(nameInput);
    await user.type(nameInput, "news-radar/ui-new-prompt");
    await user.type(screen.getByPlaceholderText("What this prompt does"), "created in test");
    await user.click(screen.getByRole("button", { name: "Create prompt" }));

    const library = screen.getByRole("heading", { name: "Library" });
    const librarySection = library.closest("section");
    expect(librarySection).not.toBeNull();
    expect(
      await within(librarySection as HTMLElement).findByText("news-radar/ui-new-prompt"),
    ).toBeInTheDocument();
  });

  it("shows timeline promote and runs the playground", async () => {
    const user = userEvent.setup();
    render(
      <Wrapper>
        <App />
      </Wrapper>,
    );

    expect(await screen.findByText("news-radar/dedup-judge")).toBeInTheDocument();

    const timeline = await screen.findByLabelText("Prompt versions");
    expect(within(timeline).getByText("draft")).toBeInTheDocument();
    expect(
      within(timeline).getByRole("button", { name: "Promote to testing" }),
    ).toBeInTheDocument();

    await user.type(await screen.findByLabelText("Variable headline_a"), "Alpha headline");
    await user.type(screen.getByLabelText("Variable headline_b"), "Beta headline");
    await user.click(screen.getByRole("button", { name: "Run" }));

    expect(await screen.findByLabelText("Playground response")).toHaveTextContent("Alpha headline");
    expect(screen.getByText(/Latency \d+ ms/)).toBeInTheDocument();
  });
});
