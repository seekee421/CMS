import { NextResponse } from "next/server";
import { cookies } from "next/headers";

function getApiBase() {
  return process.env.API_BASE_URL || process.env.NEXT_PUBLIC_API_BASE_URL || "http://localhost:8080";
}

async function buildAuthHeader(): Promise<Record<string, string>> {
  try {
    const cookieStore = await cookies();
    const session = cookieStore.get("session");
    if (!session?.value) return {};
    const parsed = JSON.parse(Buffer.from(session.value, "base64").toString("utf-8"));
    const token: string | null = parsed?.token ?? null;
    if (token) return { Authorization: `Bearer ${token}` };
    return {};
  } catch {
    return {};
  }
}

export async function POST(req: Request) {
  try {
    const apiBase = getApiBase();
    const authHeader = await buildAuthHeader();
    const headers: HeadersInit = {
      "Content-Type": "application/json",
      Accept: "application/json",
      ...authHeader,
    };
    const body = await req.text();
    const resp = await fetch(`${apiBase}/api/users/roles/batch`, { method: "POST", headers, body });
    const data = await resp.json().catch(() => null);
    if (resp.ok) {
      return NextResponse.json(data ?? { success: true }, { status: 200 });
    }
    if (resp.status === 403) {
      return NextResponse.json(
        {
          message: data?.message || "权限不足：需要 USER:ROLE:ASSIGN。请联系管理员为你的角色分配该权限。",
          requiredPermission: "USER:ROLE:ASSIGN",
          code: 403,
        },
        { status: 403 }
      );
    }
    return NextResponse.json({ message: data?.message || "请求失败" }, { status: resp.status || 500 });
  } catch (e) {
    return NextResponse.json({ message: "网络错误", error: (e as Error).message }, { status: 502 });
  }
}