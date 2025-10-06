import { describe, it, expect } from "vitest";
import { POST as postLogin } from "./route";

function makeReq(body: any) {
  return new Request("http://localhost/api/auth/login", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(body),
  });
}

describe("/api/auth/login POST", () => {
  it("rejects missing username/password with 400", async () => {
    const res = await postLogin(makeReq({ username: "", password: "" }));
    expect((res as any).status).toBe(400);
    const json = await (res as any).json();
    expect(json.message).toContain("用户名或密码不能为空");
  });

  it("falls back to mock and sets session cookie when backend unreachable", async () => {
    const res = await postLogin(makeReq({ username: "admin", password: "x" }));
    expect((res as any).status).toBe(200);
    const json = await (res as any).json();
    expect(json.user?.username).toBe("admin");
    expect(Array.isArray(json.roles)).toBe(true);
    // cookies are not directly exposed; this checks payload contract
  });
});