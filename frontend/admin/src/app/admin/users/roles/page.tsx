"use client";

import { useState, useMemo, useEffect } from "react";
import { useQuery } from "@tanstack/react-query";
import { getJSON } from "@/lib/http";
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Checkbox } from "@/components/ui/checkbox";
import { useRouter } from "next/navigation";

interface Role { name: string }
interface UserItem {
  id: number;
  username: string;
  email?: string;
  roles?: Role[];
}
interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
  empty: boolean;
}

const ALL_ROLES = ["ROLE_ADMIN", "ROLE_EDITOR", "ROLE_USER"] as const;

export default function UsersRolesMatrixPage() {
  const router = useRouter();
  const [keyword, setKeyword] = useState("");
  const [page, setPage] = useState(0);
  const [size] = useState(10);
  const { data, isLoading, isError, error, refetch } = useQuery<PageResponse<UserItem>>({
    queryKey: ["users-roles-matrix", page, size, keyword],
    queryFn: () => getJSON<PageResponse<UserItem>>("/api/users", { page, size, keyword }),
    staleTime: 5000,
    retry: 1,
  });

  // 前端路由守卫：仅允许 ROLE_ADMIN 访问；未登录重定向到登录页
  useEffect(() => {
    (async () => {
      try {
        const res = await getJSON<{ roles: string[] }>("/api/auth/me");
        const roles = Array.isArray(res?.roles) ? res.roles : [];
        if (!roles.includes("ROLE_ADMIN")) {
          router.replace("/admin/dashboard");
          return;
        }
        await refetch();
      } catch {
        router.replace("/login?redirect=/admin/users/roles");
      }
    })();
  }, [page, refetch, router]);

  const items = data?.content ?? [];
  const totalPages = data?.totalPages ?? 0;
  const totalElements = data?.totalElements ?? 0;

  const roleStats = useMemo(() => {
    const map: Record<string, number> = {};
    ALL_ROLES.forEach((r) => (map[r] = 0));
    items.forEach((u) => {
      (u.roles || []).forEach((r) => {
        if (map[r.name] !== undefined) map[r.name] += 1;
      });
    });
    return map;
  }, [items]);

  // 统一错误提示：优先显示 err.message（403 将包含 requiredPermission）
  const errorMsg = isError ? (error instanceof Error ? error.message : "加载失败，请稍后重试") : null;

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-semibold">角色与权限矩阵</h1>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>筛选与概览</CardTitle>
          <CardDescription>只读矩阵视图：展示每个用户是否具备指定角色</CardDescription>
        </CardHeader>
        <CardContent>
          <div className="flex items-center gap-3">
            <Input
              placeholder="输入关键词，例如：admin、editor、user"
              value={keyword}
              onChange={(e) => setKeyword(e.target.value)}
            />
            <div className="ml-auto flex items-center gap-3 text-sm text-muted-foreground">
              <span>共 {totalElements ?? 0} 条</span>
              <span>第 {page + 1} / {Math.max(totalPages, 1)} 页</span>
              <button className="inline-flex items-center rounded-md border px-3 py-1 text-sm" disabled={isLoading || page <= 0} onClick={() => setPage((p) => Math.max(0, p - 1))}>上一页</button>
              <button className="inline-flex items-center rounded-md border px-3 py-1 text-sm" disabled={isLoading || page >= Math.max(totalPages - 1, 0)} onClick={() => setPage((p) => Math.min(Math.max(totalPages - 1, 0), p + 1))}>下一页</button>
            </div>
          </div>
          <div className="mt-3 text-xs text-muted-foreground">
            角色统计：{ALL_ROLES.map((r) => `${r}=${roleStats[r]}`).join("，")}。
          </div>
        </CardContent>
      </Card>

      {isLoading && (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {Array.from({ length: 6 }).map((_, i) => (
            <div key={i} className="rounded-lg border p-4 animate-pulse">
              <div className="h-5 w-2/3 bg-muted mb-2" />
              <div className="h-4 w-full bg-muted mb-2" />
              <div className="h-4 w-3/4 bg-muted" />
            </div>
          ))}
        </div>
      )}

      {isError && <div className="text-red-500 text-sm">{errorMsg}</div>}

      {!isLoading && !isError && (
        <Card>
          <CardHeader>
            <CardTitle>矩阵表格</CardTitle>
            <CardDescription>横向为角色，纵向为用户</CardDescription>
          </CardHeader>
          <CardContent>
            <div className="overflow-x-auto">
              <table className="min-w-full text-sm">
                <thead>
                  <tr className="text-left">
                    <th className="p-2">用户</th>
                    <th className="p-2">邮箱</th>
                    {ALL_ROLES.map((r) => (
                      <th key={r} className="p-2">{r}</th>
                    ))}
                  </tr>
                </thead>
                <tbody>
                  {items.map((u) => {
                    const roleNames = new Set((u.roles || []).map((r) => r.name));
                    return (
                      <tr key={u.id} className="border-t">
                        <td className="p-2 font-medium">{u.username}</td>
                        <td className="p-2 text-muted-foreground">{u.email || "无邮箱"}</td>
                        {ALL_ROLES.map((r) => (
                          <td key={r} className="p-2">
                            <div className="flex items-center gap-2">
                              <Checkbox checked={roleNames.has(r)} onChange={() => {}} disabled />
                              <span className="sr-only">{roleNames.has(r) ? "已具备" : "未具备"}</span>
                            </div>
                          </td>
                        ))}
                      </tr>
                    );
                  })}
                  {items.length === 0 && (
                    <tr>
                      <td className="p-2 text-muted-foreground" colSpan={2 + ALL_ROLES.length}>暂无数据</td>
                    </tr>
                  )}
                </tbody>
              </table>
            </div>
          </CardContent>
        </Card>
      )}
    </div>
  );
}