import { NextResponse } from "next/server";

// Contract-first mock login. In production, proxy to Spring Boot and set HttpOnly cookie here.
export async function POST(req: Request) {
  try {
    const body = await req.json();
    const { username, password } = body || {};

    // Basic validation
    if (!username || !password) {
      return NextResponse.json(
        { message: "用户名或密码不能为空" },
        { status: 400 }
      );
    }

    // 优先尝试对接后端 Spring Boot 登录接口；失败时回退到契约一致的 mock
    try {
      const API_BASE = process.env.API_BASE_URL || process.env.NEXT_PUBLIC_API_BASE_URL || "http://localhost:8080";
      const backendRes = await fetch(`${API_BASE}/api/auth/login`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ username, password }),
      });

      if (backendRes.ok) {
        const data = await backendRes.json();
        const user = data?.user ?? { id: data?.id, username: data?.username, roles: data?.roles };
        const roles: string[] = Array.isArray(data?.roles) ? data.roles : Array.isArray(user?.roles) ? user.roles : [];
        const token: string | null = data?.token ?? null;

        const sessionPayload = { user, token };
        const res = NextResponse.json({ user, roles }, { status: 200 });
        res.cookies.set({
          name: "session",
          value: Buffer.from(JSON.stringify(sessionPayload)).toString("base64"),
          httpOnly: true,
          sameSite: "lax",
          secure: process.env.NODE_ENV === "production",
          path: "/",
          maxAge: 60 * 60 * 12,
        });
        return res;
      }
      // 非 200，返回后端错误信息
      const errData = await backendRes.json().catch(() => ({}));
      return NextResponse.json({ message: errData?.message || "登录失败" }, { status: backendRes.status || 500 });
    } catch (backendError) {
      // 后端不可用时：契约一致的 mock 回退
      const mockUser = {
        id: 1,
        username,
        roles: username === "admin" ? ["ROLE_ADMIN", "ROLE_EDITOR", "ROLE_USER"] : username === "editor" ? ["ROLE_EDITOR", "ROLE_USER"] : ["ROLE_USER"],
      };

      const sessionPayload = {
        user: mockUser,
        token: null,
      };

      const res = NextResponse.json(
        { user: mockUser, roles: mockUser.roles },
        { status: 200 }
      );

      res.cookies.set({
        name: "session",
        value: Buffer.from(JSON.stringify(sessionPayload)).toString("base64"),
        httpOnly: true,
        sameSite: "lax",
        secure: process.env.NODE_ENV === "production",
        path: "/",
        maxAge: 60 * 60 * 12,
      });

      return res;
    }
  } catch (e) {
    return NextResponse.json(
      { message: "登录失败", error: (e as Error).message },
      { status: 500 }
    );
  }
}