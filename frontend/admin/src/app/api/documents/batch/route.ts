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
    const body = await req.json().catch(() => ({}));
    const authHeader = await buildAuthHeader();
    const headers: HeadersInit = { "Content-Type": "application/json", Accept: "application/json", ...authHeader };

    // 根据前端请求的 operation 推断更友好的 requiredPermission
    const op = String((body as Record<string, unknown>)?.operation || "").toUpperCase();
    const permMap: Record<string, string> = {
      UPDATE_STATUS: "DOC:STATUS:UPDATE",
      DELETE: "DOC:DELETE",
      PUBLISH: "DOC:PUBLISH",
      ASSIGN: "DOC:ASSIGN",
    };
    const requiredPermission = permMap[op] || "DOC:BATCH";

    const resp = await fetch(`${apiBase}/api/documents/batch`, { method: "POST", headers, body: JSON.stringify(body ?? {}) });
    const data = await resp.json().catch(() => null);
    if (resp.ok) return NextResponse.json(data ?? { success: true }, { status: 200 });

    if (resp.status === 403) {
      return NextResponse.json(
        {
          message: data?.message || `权限不足：需要 ${requiredPermission}。请联系管理员为你的角色分配该权限。`,
          requiredPermission,
          code: 403,
        },
        { status: 403 }
      );
    }

    return NextResponse.json({ message: data?.message || "批量操作失败" }, { status: resp.status || 500 });
  } catch (e) {
    return NextResponse.json({ message: "网络错误", error: (e as Error).message }, { status: 502 });
  }
}