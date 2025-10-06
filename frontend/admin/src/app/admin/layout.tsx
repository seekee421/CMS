import type { ReactNode } from "react";
import Sidebar, { type MenuGroup } from "../../components/Sidebar";
import { cookies } from "next/headers";
import { redirect } from "next/navigation";


async function getRoles(): Promise<string[]> {
  const cookieStore = await cookies();
  const session = cookieStore.get("session");
  if (!session?.value) return [];
  try {
    const payload = JSON.parse(Buffer.from(session.value, "base64").toString("utf-8"));
    const user = payload?.user ?? null;
    return Array.isArray(user?.roles) ? user.roles : Array.isArray(payload?.roles) ? payload.roles : [];
  } catch {
    return [];
  }
}

export default async function AdminLayout({ children }: { children: ReactNode }) {
  const roles = await getRoles();

  // 未登录（无角色）直接重定向到登录页，附带来源参数
  if (!Array.isArray(roles) || roles.length === 0) {
    redirect(`/login?redirect=/admin`);
  }

  const groups: MenuGroup[] = [
    {
      label: "仪表板",
      defaultHref: "/admin/dashboard",
      roles: ["ROLE_USER", "ROLE_EDITOR", "ROLE_ADMIN"],
      children: [
        { label: "总览", href: "/admin/dashboard", roles: ["ROLE_USER", "ROLE_EDITOR", "ROLE_ADMIN"] },
      ],
    },
    {
      label: "文档管理",
      defaultHref: "/admin/documents",
      roles: ["ROLE_EDITOR", "ROLE_ADMIN", "ROLE_SUB_ADMIN"],
      children: [
        { label: "文档列表", href: "/admin/documents", roles: ["ROLE_EDITOR", "ROLE_ADMIN", "ROLE_SUB_ADMIN"] },
        { label: "新建文档", href: "/admin/documents/new", roles: ["ROLE_EDITOR", "ROLE_ADMIN"] },
      ],
    },
    {
      label: "分类管理",
      defaultHref: "/admin/categories",
      roles: ["ROLE_EDITOR", "ROLE_ADMIN", "ROLE_SUB_ADMIN"],
      children: [
        { label: "分类树", href: "/admin/categories", roles: ["ROLE_EDITOR", "ROLE_ADMIN", "ROLE_SUB_ADMIN"] },
      ],
    },
    {
      label: "用户管理",
      defaultHref: "/admin/users",
      roles: ["ROLE_ADMIN"],
      children: [
        { label: "用户列表", href: "/admin/users", roles: ["ROLE_ADMIN"] },
        { label: "角色与权限", href: "/admin/users/roles", roles: ["ROLE_ADMIN"] },
      ],
    },
    {
      label: "统计分析",
      defaultHref: "/admin/statistics",
      roles: ["ROLE_ADMIN"],
      children: [
        { label: "访问统计", href: "/admin/statistics", roles: ["ROLE_ADMIN"] },
      ],
    },
    {
      label: "系统设置",
      defaultHref: "/admin/settings",
      roles: ["ROLE_ADMIN"],
      children: [
        { label: "基础配置", href: "/admin/settings", roles: ["ROLE_ADMIN"] },
      ],
    },
  ];

  return (
    <div className="grid grid-cols-[240px_1fr] min-h-screen">
      <Sidebar roles={roles} groups={groups} />
      <main className="p-6">{children}</main>
    </div>
  );
}