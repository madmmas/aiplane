import { render, screen } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import App from "./App";

describe("Guardrail App", () => {
  it("renders the remote landing copy", () => {
    render(<App />);
    expect(screen.getByRole("heading", { name: "Guardrail" })).toBeInTheDocument();
    expect(screen.getByText(/Configure guardrails for the AIPlane/i)).toBeInTheDocument();
  });
});
