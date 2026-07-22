import { render, screen } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import App from "./App";

describe("User Manager App", () => {
  it("renders the remote landing copy", () => {
    render(<App />);
    expect(screen.getByRole("heading", { name: "User Manager" })).toBeInTheDocument();
    expect(screen.getByText(/Manage users for the AIPlane/i)).toBeInTheDocument();
  });
});
