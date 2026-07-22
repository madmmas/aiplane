import { render, screen } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import App from "./App";

describe("Prompt Manager App", () => {
  it("renders the remote landing copy", () => {
    render(<App />);
    expect(screen.getByRole("heading", { name: "Prompt Manager" })).toBeInTheDocument();
    expect(screen.getByText(/Manage prompts for the AIPlane/i)).toBeInTheDocument();
  });
});
