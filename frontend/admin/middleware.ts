import { NextResponse } from "next/server";
import type { NextRequest } from "next/server";

export const config = {
  matcher: ["/admin", "/admin/:path*"],
};

export async function middleware(req: NextRequest) {
  console.log("[middleware] path=", req.nextUrl.pathname);
  const session = req.cookies.get("session");
  console.log("[middleware] hasSession=", !!session);

  // 解析 session Cookie 并校验角色（Edge Runtime 无 Buffer，使用 atob）
  let authed = false;
  if (session?.value) {
    try {
      const json = atob(session.value);
      const payload = JSON.parse(json);
      const roles: string[] = Array.isArray(payload?.user?.roles)
        ? payload.user.roles
        : Array.isArray(payload?.roles)
        ? payload.roles
        : [];
      authed = roles.length > 0; // roles 缺失视为未登录
      console.log("[middleware] roles=", roles);
    } catch (e) {
      authed = false;
      console.log("[middleware] session parse failed:", (e as Error).message);
    }
  }

  if (!authed) {
    const url = req.nextUrl.clone();
    url.pathname = "/login";
    url.searchParams.set("redirect", req.nextUrl.pathname + req.nextUrl.search);
    console.log("[middleware] redirect to ", url.toString());
    return NextResponse.redirect(url);
  }

  // TODO: 可在此解析角色并做服务器端的基础守卫，例如不含 ROLE_ADMIN 则禁止进入 /admin/users 等

  return NextResponse.next();
}