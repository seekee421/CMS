"use client";

import { useEffect, useRef, useState, useCallback } from "react";
import { useQuery } from "@tanstack/react-query";
import { getJSON, postJSON } from "@/lib/http";
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { Checkbox } from "@/components/ui/checkbox";

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

export default function UsersPage() {
  const [keyword, setKeyword] = useState("");
  const [page, setPage] = useState(0);
  const [size] = useState(10);
  // TanStack Query 管理列表数据
  const [search, setSearch] = useState("");
  const { data, isLoading, isError, refetch } = useQuery<PageResponse<UserItem>>({
    queryKey: ["users", page, size, search],
    queryFn: () => getJSON<PageResponse<UserItem>>("/api/users", { page, size, keyword: search }),
    staleTime: 5000,
    retry: 1,
  });
  const items = data?.content ?? [];
  const totalPages = data?.totalPages ?? 0;
  const totalElements = data?.totalElements ?? 0;
  const loading = isLoading;
  const error = isError ? "加载失败，请稍后重试" : null;
  // 角色常量（契约统一：ROLE_*）
  const allRoles = ["ROLE_ADMIN", "ROLE_EDITOR", "ROLE_USER"];
  // 邀请与批量分配相关状态
  const [selectedIds, setSelectedIds] = useState<number[]>([]);
  const [inviteEmail, setInviteEmail] = useState("");
  const [inviteRoles, setInviteRoles] = useState<string[]>([]);
  const [batchRoles, setBatchRoles] = useState<string[]>([]);
  const [inviteLoading, setInviteLoading] = useState(false);
  const [batchLoading, setBatchLoading] = useState(false);

  const [meRoles, setMeRoles] = useState<string[]>([]);
  const [resetLoadingId, setResetLoadingId] = useState<number | null>(null);
  const [toastMsg, setToastMsg] = useState<string | null>(null);

  const debounceTimer = useRef<NodeJS.Timeout | null>(null);

  // 查询参数由 TanStack Query 管理，不再需要单独的 params 变量

  const fetchUsers = useCallback(async () => {
    await refetch();
  }, [refetch]);

  const handleResetPassword = async (user: UserItem) => {
    if (!user?.email) {
      setToastMsg("该用户没有邮箱，无法发送重置邮件");
      return;
    }
    try {
      setResetLoadingId(user.id);
      const res = await postJSON<{ message?: string }>("/api/auth/password-reset", { email: user.email });
      setToastMsg(res?.message || "重置邮件已发送");
    } catch (e: unknown) {
      const msg = (e as { response?: { data?: { message?: string } }; message?: string })?.response?.data?.message || (e as Error)?.message || "发送失败，请稍后重试";
      setToastMsg(msg);
    } finally {
      setResetLoadingId(null);
    }
  };

  // 选择与角色勾选逻辑
  const toggleSelectUser = (id: number) => {
    setSelectedIds((prev) => (prev.includes(id) ? prev.filter((x) => x !== id) : [...prev, id]));
  };
  const toggleInviteRole = (role: string) => {
    setInviteRoles((prev) => (prev.includes(role) ? prev.filter((r) => r !== role) : [...prev, role]));
  };
  const toggleBatchRole = (role: string) => {
    setBatchRoles((prev) => (prev.includes(role) ? prev.filter((r) => r !== role) : [...prev, role]));
  };

  const handleInvite = async () => {
    if (!inviteEmail) {
      setToastMsg("请输入邮箱");
      return;
    }
    if (inviteRoles.length === 0) {
      setToastMsg("请选择至少一个角色");
      return;
    }
    try {
      setInviteLoading(true);
      const res = await postJSON<{ message?: string }>("/api/users/invite", { email: inviteEmail, roles: inviteRoles });
      setToastMsg(res?.message || "邀请已发送");
      setInviteEmail("");
      setInviteRoles([]);
    } catch (e: unknown) {
      const msg = (e as { response?: { data?: { message?: string } }; message?: string })?.response?.data?.message || (e as Error)?.message || "邀请失败，请稍后重试";
      setToastMsg(msg);
    } finally {
      setInviteLoading(false);
    }
  };

  const handleBatchAssign = async () => {
    if (selectedIds.length === 0) {
      setToastMsg("请先选择用户");
      return;
    }
    if (batchRoles.length === 0) {
      setToastMsg("请选择至少一个角色");
      return;
    }
    try {
      setBatchLoading(true);
      const res = await postJSON<{ message?: string }>("/api/users/roles/batch", { userIds: selectedIds, roles: batchRoles });
      setToastMsg(res?.message || "批量分配成功");
      setSelectedIds([]);
      setBatchRoles([]);
      fetchUsers();
    } catch (e: unknown) {
      const msg = (e as { response?: { data?: { message?: string } }; message?: string })?.response?.data?.message || (e as Error)?.message || "批量分配失败，请稍后重试";
      setToastMsg(msg);
    } finally {
      setBatchLoading(false);
    }
  };

  useEffect(() => {
    (async () => {
      try {
        const res = await getJSON<{ roles: string[] }>("/api/auth/me");
        setMeRoles(Array.isArray(res?.roles) ? res.roles : []);
      } catch {
        setMeRoles([]);
      }
    })();
    fetchUsers();
  }, [page, fetchUsers]);

  useEffect(() => {
    if (debounceTimer.current) clearTimeout(debounceTimer.current);
    debounceTimer.current = setTimeout(() => {
      setPage(0);
      setSearch(keyword);
    }, 300);
    return () => {
      if (debounceTimer.current) clearTimeout(debounceTimer.current);
    };
  }, [keyword, setSearch]);

  return (
    <div className="space-y-4">
      <h1 className="text-2xl font-semibold">用户管理</h1>

      <Card>
        <CardHeader>
          <CardTitle>搜索与分页</CardTitle>
          <CardDescription>按用户名、邮箱或角色关键字过滤用户</CardDescription>
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
              <Button variant="outline" size="sm" onClick={() => setPage((p) => Math.max(0, p - 1))} disabled={loading || page <= 0}>上一页</Button>
              <Button variant="outline" size="sm" onClick={() => setPage((p) => Math.min(Math.max(totalPages - 1, 0), p + 1))} disabled={loading || page >= Math.max(totalPages - 1, 0)}>下一页</Button>
            </div>
          </div>
        </CardContent>
      </Card>

      {/* 非管理员权限提示 */}
      {!meRoles.includes("ROLE_ADMIN") && (
        <div className="text-xs text-muted-foreground">当前角色无法进行用户邀请与批量角色分配</div>
      )}

      {/* 邀请用户 */}
      {meRoles.includes("ROLE_ADMIN") && (
        <Card>
          <CardHeader>
            <CardTitle>邀请用户</CardTitle>
            <CardDescription>输入邮箱并选择角色，发送邀请</CardDescription>
          </CardHeader>
          <CardContent>
            <div className="flex flex-col md:flex-row md:items-center gap-3">
              <Input
                placeholder="邮箱，例如 user@example.com"
                value={inviteEmail}
                onChange={(e) => setInviteEmail(e.target.value)}
              />
              <div className="flex items-center gap-3">
                {allRoles.map((r) => (
                  <label key={r} className="flex items-center gap-2 text-sm">
                    <Checkbox
                      checked={inviteRoles.includes(r)}
                      onChange={() => toggleInviteRole(r)}
                    />
                    <span>{r}</span>
                  </label>
                ))}
              </div>
              <Button onClick={handleInvite} disabled={inviteLoading}>
                {inviteLoading ? "发送中..." : "邀请用户"}
              </Button>
            </div>
          </CardContent>
        </Card>
      )}

      {loading && (
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

      {error && <div className="text-red-500 text-sm">{error}</div>}

      {!loading && !error && (
        <div className="grid grid-cols-1 md:grid-cols-2 lg-grid-cols-3 gap-4 md:grid-cols-2 lg:grid-cols-3">
          {items.map((u) => (
            <Card key={u.id}>
              <CardHeader className="flex flex-row items-center justify-between">
                <div>
                  <CardTitle className="line-clamp-1">{u.username}</CardTitle>
                  <CardDescription className="line-clamp-2">{u.email || "无邮箱"}</CardDescription>
                </div>
                {meRoles.includes("ROLE_ADMIN") && (
                  <Checkbox
                    checked={selectedIds.includes(u.id)}
                    onChange={() => toggleSelectUser(u.id)}
                    aria-label="选择用户"
                  />
                )}
              </CardHeader>
              <CardContent className="text-sm text-muted-foreground">
                <div>
                  角色：{(u.roles || []).map((r) => r.name).join(", ") || "无角色"}
                </div>
                <div className="mt-2">
                  {meRoles.includes("ROLE_ADMIN") && (
                    <Button
                      variant="outline"
                      size="sm"
                      disabled={resetLoadingId === u.id}
                      onClick={() => handleResetPassword(u)}
                    >
                      {resetLoadingId === u.id ? "发送中..." : "重置密码"}
                    </Button>
                  )}
                </div>
                {toastMsg && (
                  <div className="mt-2 text-xs text-muted-foreground">{toastMsg}</div>
                )}
              </CardContent>
            </Card>
          ))}
          {items.length === 0 && (
            <div className="text-sm text-muted-foreground">暂无数据</div>
          )}
        </div>
      )}

      {/* 批量角色分配 */}
      {meRoles.includes("ROLE_ADMIN") && (
        <Card className="mt-6">
          <CardHeader>
            <CardTitle>批量角色分配</CardTitle>
            <CardDescription>选择多个用户后，勾选角色并提交</CardDescription>
          </CardHeader>
          <CardContent>
            <div className="flex flex-col md:flex-row md:items-center gap-3">
              <div className="flex items-center gap-3">
                {allRoles.map((r) => (
                  <label key={r} className="flex items-center gap-2 text-sm">
                    <Checkbox
                      checked={batchRoles.includes(r)}
                      onChange={() => toggleBatchRole(r)}
                    />
                    <span>{r}</span>
                  </label>
                ))}
              </div>
              <Button onClick={handleBatchAssign} disabled={batchLoading || selectedIds.length === 0}>
                {batchLoading ? "提交中..." : `为 ${selectedIds.length} 个用户赋予角色`}
              </Button>
            </div>
          </CardContent>
        </Card>
      )}
    </div>
  );
}