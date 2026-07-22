import { render, screen } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import App from "./App";

describe("Usages Data App", () => {
  it("renders the remote landing copy", () => {
    render(<App />);
    expect(screen.getByRole("heading", { name: "Usages Data" })).toBeInTheDocument();
    expect(screen.getByText(/View usage data for the AIPlane/i)).toBeInTheDocument();
  });
});
