import { render, screen } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import { Button } from "./button";

describe("Button", () => {
  it("renders accessible button text", () => {
    render(<Button>Save prompt</Button>);
    expect(screen.getByRole("button", { name: "Save prompt" })).toBeInTheDocument();
  });

  it("honors the disabled attribute", () => {
    render(<Button disabled>Save prompt</Button>);
    expect(screen.getByRole("button", { name: "Save prompt" })).toBeDisabled();
  });
});
