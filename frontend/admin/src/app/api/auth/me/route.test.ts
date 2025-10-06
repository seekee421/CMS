import { describe, it, expect, vi } from "vitest";
import { GET as getMe } from "./route";

function makeMockCookies(sessionValue?: string) {
  return {
    get: (name: string) => {
      if (name !== "session" || !sessionValue) return undefined as unknown as undefined;
      return { name: "session", value: sessionValue } as { name: string; value: string };
    },
  } as unknown as { get: (name: string) => { name: string; value: string } | undefined };
}

// Monkey-patch next/headers cookies for testing
vi.mock("next/headers", () => {
  return {
    cookies: async () => makeMockCookies(Buffer.from(JSON.stringify({ user: { id: 1, username: "admin", roles: ["ROLE_ADMIN"] } }), "utf-8").toString("base64")),
  };
});

describe("/api/auth/me GET", () => {
  it("returns authenticated true and roles when session cookie exists", async () => {
    const res = await getMe();
    const json = await res.json();
    expect(json.authenticated).toBe(true);
    expect(Array.isArray(json.roles)).toBe(true);
    expect(json.roles).toContain("ROLE_ADMIN");
    expect(json.user?.username).toBe("admin");
  });
});