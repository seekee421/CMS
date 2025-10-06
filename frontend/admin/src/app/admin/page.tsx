import { redirect } from "next/navigation";
import { cookies } from "next/headers";

export default async function AdminIndex() {
  const cookieStore = await cookies();
  const session = cookieStore.get("session");
  let roles: string[] = [];

  if (session?.value) {
    try {
      const payload = JSON.parse(Buffer.from(session.value, "base64").toString("utf-8"));
      roles = Array.isArray(payload?.user?.roles)
        ? payload.user.roles
        : Array.isArray(payload?.roles)
        ? payload.roles
        : [];
    } catch {
      roles = [];
    }
  }

  if (!Array.isArray(roles) || roles.length === 0) {
    redirect(`/login?redirect=/admin`);
  }

  redirect("/admin/dashboard");
}