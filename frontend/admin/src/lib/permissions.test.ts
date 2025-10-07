import { describe, it, expect } from "vitest";
import { computeCanCreate, hasRole } from "./permissions";

describe("computeCanCreate", () => {
  it("allows ROLE_ADMIN", () => {
    expect(computeCanCreate(["ROLE_ADMIN"], [])).toBe(true);
  });
  it("allows ROLE_EDITOR", () => {
    expect(computeCanCreate(["ROLE_EDITOR"], [])).toBe(true);
  });
  it("allows DOC:CREATE permission", () => {
    expect(computeCanCreate([], ["DOC:CREATE"])).toBe(true);
  });
  it("denies when no roles nor permission", () => {
    expect(computeCanCreate([], [])).toBe(false);
  });
});

describe("hasRole", () => {
  it("returns true when role present", () => {
    expect(hasRole(["ROLE_USER", "ROLE_EDITOR"], "ROLE_EDITOR")).toBe(true);
  });
  it("returns false when role missing", () => {
    expect(hasRole(["ROLE_USER"], "ROLE_ADMIN")).toBe(false);
  });
});