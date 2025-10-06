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

// 将后端 PageResult 或 Spring Page 结构统一为前端当前使用的 Spring Page 结构
function normalizeListResponse(json: any) {
  if (json && Array.isArray(json.content)) {
    // Spring Page 格式，直接返回
    return json;
  }
  if (json && Array.isArray(json.items)) {
    const total: number = json.total ?? json.totalElements ?? json.count ?? json.items.length;
    const page: number = json.page ?? json.number ?? 0;
    const size: number = json.size ?? json.pageSize ?? json.limit ?? json.items.length;
    const totalPages = size > 0 ? Math.ceil(total / size) : 1;
    return {
      content: json.items,
      totalElements: total,
      totalPages,
      size,
      number: page,
      first: page === 0,
      last: page + 1 >= totalPages,
      empty: (json.items?.length ?? 0) === 0,
    };
  }
  // 非分页列表，包一层
  if (Array.isArray(json)) {
    const content = json;
    return {
      content,
      totalElements: content.length,
      totalPages: 1,
      size: content.length,
      number: 0,
      first: true,
      last: true,
      empty: content.length === 0,
    };
  }
  return json;
}

export async function GET(req: Request) {
  try {
    const apiBase = getApiBase();
    const url = new URL(req.url);
    const qs = url.search ? url.search : "";
    const backendURL = `${apiBase}/api/users${qs}`;
    const authHeader = await buildAuthHeader();
    const headers: HeadersInit = {
      Accept: "application/json",
      ...authHeader,
    };
    const resp = await fetch(backendURL, { method: "GET", headers, next: { revalidate: 0 } });
    const data = await resp.json().catch(() => null);
    if (resp.ok) {
      return NextResponse.json(normalizeListResponse(data ?? {}), { status: 200 });
    }
    return NextResponse.json({ message: data?.message || "请求失败" }, { status: resp.status || 500 });
  } catch (e) {
    return NextResponse.json({ message: "网络错误", error: (e as Error).message }, { status: 502 });
  }
}

// 预留：创建用户（与后端契约一致直通）
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
    const resp = await fetch(`${apiBase}/api/users`, { method: "POST", headers, body });
    const data = await resp.json().catch(() => null);
    return NextResponse.json(data ?? {}, { status: resp.status || 500 });
  } catch (e) {
    return NextResponse.json({ message: "网络错误", error: (e as Error).message }, { status: 502 });
  }
}