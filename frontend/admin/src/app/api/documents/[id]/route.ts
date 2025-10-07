import { NextResponse, NextRequest } from "next/server";
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

export async function GET(_req: NextRequest, context: { params: Promise<{ id: string }> }) {
  try {
    const { id } = await context.params;
    const apiBase = getApiBase();
    const authHeader = await buildAuthHeader();
    const headers: HeadersInit = { Accept: "application/json", ...authHeader };
    const resp = await fetch(`${apiBase}/api/documents/${id}`, { method: "GET", headers, next: { revalidate: 0 } });
    const data = await resp.json().catch(() => null);
    if (resp.ok) return NextResponse.json(data ?? {}, { status: 200 });
    if (resp.status === 403) {
      return NextResponse.json(
        {
          message: data?.message || "权限不足：需要 DOC:VIEW:DETAIL。请联系管理员为你的角色分配该权限。",
          requiredPermission: "DOC:VIEW:DETAIL",
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

export async function PUT(req: NextRequest, context: { params: Promise<{ id: string }> }) {
  try {
    const { id } = await context.params;
    const apiBase = getApiBase();
    const body = await req.json().catch(() => ({}));
    const authHeader = await buildAuthHeader();
    const headers: HeadersInit = { "Content-Type": "application/json", Accept: "application/json", ...authHeader };
    const resp = await fetch(`${apiBase}/api/documents/${id}`, { method: "PUT", headers, body: JSON.stringify(body ?? {}) });
    const data = await resp.json().catch(() => null);
    if (resp.ok) return NextResponse.json(data ?? {}, { status: 200 });
    if (resp.status === 403) {
      return NextResponse.json(
        {
          message: data?.message || "权限不足：需要 DOC:EDIT。请联系管理员为你的角色分配该权限。",
          requiredPermission: "DOC:EDIT",
          code: 403,
        },
        { status: 403 }
      );
    }
    return NextResponse.json({ message: data?.message || "更新失败" }, { status: resp.status || 500 });
  } catch (e) {
    return NextResponse.json({ message: "网络错误", error: (e as Error).message }, { status: 502 });
  }
}

export async function DELETE(_req: NextRequest, context: { params: Promise<{ id: string }> }) {
  try {
    const { id } = await context.params;
    const apiBase = getApiBase();
    const authHeader = await buildAuthHeader();
    const headers: HeadersInit = { Accept: "application/json", ...authHeader };
    const resp = await fetch(`${apiBase}/api/documents/${id}`, { method: "DELETE", headers });
    if (resp.ok) return NextResponse.json({ success: true }, { status: 200 });
    const data = await resp.json().catch(() => null);
    if (resp.status === 403) {
      return NextResponse.json(
        {
          message: data?.message || "权限不足：需要 DOC:DELETE。请联系管理员为你的角色分配该权限。",
          requiredPermission: "DOC:DELETE",
          code: 403,
        },
        { status: 403 }
      );
    }
    return NextResponse.json({ message: data?.message || "删除失败" }, { status: resp.status || 500 });
  } catch (e) {
    return NextResponse.json({ message: "网络错误", error: (e as Error).message }, { status: 502 });
  }
}