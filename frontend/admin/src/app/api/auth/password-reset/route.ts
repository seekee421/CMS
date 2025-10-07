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
    const email: string | undefined = body?.email;

    if (!email || typeof email !== "string") {
      return NextResponse.json({ message: "邮箱必填" }, { status: 400 });
    }

    const authHeader = await buildAuthHeader();
    const headers: HeadersInit = {
      "Content-Type": "application/json",
      Accept: "application/json",
      ...authHeader,
    };

    const resp = await fetch(`${apiBase}/api/auth/password-reset`, {
      method: "POST",
      headers,
      body: JSON.stringify({ email }),
    });

    const data = await resp.json().catch(() => null);
    if (resp.ok) {
      // 直接透传后端响应（契约一致）
      return NextResponse.json(data ?? { message: "已发送重置密码邮件" }, { status: 200 });
    }
    return NextResponse.json({ message: data?.message || "请求失败" }, { status: resp.status || 500 });
  } catch (e) {
    return NextResponse.json({ message: "网络错误", error: (e as Error).message }, { status: 502 });
  }
}