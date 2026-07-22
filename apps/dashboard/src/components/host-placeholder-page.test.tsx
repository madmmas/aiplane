import { render, screen } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import { HostPlaceholderPage } from "./host-placeholder-page";

describe("HostPlaceholderPage", () => {
  it("renders the host-owned placeholder copy", () => {
    render(
      <HostPlaceholderPage
        title="Providers"
        description="Configure provider credentials per project."
      />,
    );

    expect(screen.getByRole("heading", { name: "Providers" })).toBeInTheDocument();
    expect(screen.getByText("Configure provider credentials per project.")).toBeInTheDocument();
    expect(screen.getByText("host")).toBeInTheDocument();
  });
});
