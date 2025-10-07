import { describe, it, expect } from "vitest";
import { computePreviewText } from "./content";

describe("computePreviewText", () => {
  it("returns placeholder when empty", () => {
    expect(computePreviewText("")).toBe("暂无内容");
  });
  it("returns trimmed content when non-empty", () => {
    expect(computePreviewText("  Hello \n" )).toBe("  Hello \n" );
  });
});