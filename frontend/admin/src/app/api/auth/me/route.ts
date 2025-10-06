import { NextResponse } from "next/server";
import { cookies } from "next/headers";

export async function GET() {
  try {
    const cookieStore = await cookies();
    const session = cookieStore.get("session");

    if (!session) {
      return NextResponse.json({ authenticated: false, user: null, roles: [] }, { status: 200 });
    }

    let payload: unknown = null;
    try {
      payload = JSON.parse(Buffer.from(session.value, "base64").toString("utf-8"));
    } catch {
      return NextResponse.json({ authenticated: false, user: null, roles: [] }, { status: 200 });
    }

    const user = (payload as { user?: { roles?: string[] } | null })?.user ?? null;
    const roles = Array.isArray(user?.roles) ? user!.roles! : [];

    return NextResponse.json({ authenticated: true, user, roles }, { status: 200 });
  } catch (e) {
    return NextResponse.json(
      { authenticated: false, user: null, roles: [], error: (e as Error).message },
      { status: 200 }
    );
  }
}