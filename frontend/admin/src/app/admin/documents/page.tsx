"use client";

import { useEffect, useMemo, useRef, useState, useCallback } from "react";
import { getJSON } from "@/lib/http";
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Separator } from "@/components/ui/separator";
import { Button } from "@/components/ui/button";
import { useRouter } from "next/navigation";
import { Checkbox } from "@/components/ui/checkbox";
import { DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuTrigger } from "@/components/ui/dropdown-menu";

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

interface DocumentItem {
  id: number;
  title: string;
  summary?: string;
  categoryName?: string;
  updatedAt?: string;
  viewCount?: number;
  downloadCount?: number;
  status?: "DRAFT" | "PUBLISHED" | "ARCHIVED" | string;
}

// 状态徽章：根据文档状态显示不同颜色与文案
const statusBadge = (status?: string) => {
  const s = (status || "").toUpperCase();
  const map: Record<string, { label: string; cls: string }> = {
    DRAFT: { label: "草稿", cls: "bg-yellow-100 text-yellow-800 border-yellow-300" },
    PUBLISHED: { label: "已发布", cls: "bg-green-100 text-green-800 border-green-300" },
    ARCHIVED: { label: "已归档", cls: "bg-gray-100 text-gray-800 border-gray-300" },
  };
  const conf = map[s] || { label: status || "未知", cls: "bg-muted text-muted-foreground border-gray-300" };
  return <span className={`inline-block text-xs px-2 py-0.5 rounded border ${conf.cls}`}>{conf.label}</span>;
};

export default function DocumentsPage() {
  const router = useRouter();
  const [query, setQuery] = useState("");
  const [page, setPage] = useState(0);
  const [size] = useState(10);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [items, setItems] = useState<DocumentItem[]>([]);
  const [selected, setSelected] = useState<number[]>([]);
  const [status, setStatus] = useState<string | null>(null);
  const [batchLoading, setBatchLoading] = useState(false);
  // 角色与反馈状态
  const [roles, setRoles] = useState<string[]>([]);
  const [permissions, setPermissions] = useState<string[]>([]);
  const [toastMsg, setToastMsg] = useState<string | null>(null);
  const [showStatusDialog, setShowStatusDialog] = useState(false);
  const [showDeleteConfirm, setShowDeleteConfirm] = useState(false);
  const [batchStatus, setBatchStatus] = useState<"DRAFT" | "PUBLISHED" | "ARCHIVED">("DRAFT");
  const isAdmin = useMemo(() => roles.includes("ROLE_ADMIN"), [roles]);
  const hasPerm = useCallback((p: string) => permissions.includes(p), [permissions]);
  const canPublish = useMemo(() => isAdmin || roles.includes("ROLE_EDITOR") || hasPerm("DOC:PUBLISH"), [isAdmin, roles, hasPerm]);
  const canUpdateStatus = useMemo(() => isAdmin || roles.includes("ROLE_EDITOR") || hasPerm("DOC:EDIT"), [isAdmin, roles, hasPerm]);
  const canDelete = useMemo(() => isAdmin || hasPerm("DOC:DELETE"), [isAdmin, hasPerm]);
  useEffect(() => {
    (async () => {
      try {
        const resp = await fetch("/api/auth/me");
        const data = await resp.json().catch(() => ({}));
        setRoles(Array.isArray(data?.roles) ? data.roles : []);
        setPermissions(Array.isArray(data?.permissions) ? data.permissions : []);
      } catch {
        setRoles([]);
        setPermissions([]);
      }
    })();
  }, []);
  const showToast = (msg: string) => { setToastMsg(msg); window.setTimeout(() => setToastMsg(null), 3000); };
  const formatDateTime = (iso?: string) => {
    if (!iso) return "";
    try {
      const d = new Date(iso);
      return new Intl.DateTimeFormat(undefined, { year: "numeric", month: "2-digit", day: "2-digit", hour: "2-digit", minute: "2-digit" }).format(d);
    } catch {
      return "";
    }
  };
  const toggleSelected = (id: number, checked: boolean) => {
    setSelected((prev) => (checked ? [...prev, id] : prev.filter((x) => x !== id)));
  };
  const clearSelected = () => setSelected([]);
  // 类型守卫
  const isRecord = (v: unknown): v is Record<string, unknown> => typeof v === "object" && v !== null;
  const readNumber = (v: unknown, fallback = 0): number => {
    const n = Number(v);
    return Number.isFinite(n) ? n : fallback;
  };

  const runBatch = async (operation: string, payload?: Record<string, unknown>) => {
    if (selected.length === 0) return;
    setBatchLoading(true);
    try {
      const resp = await fetch("/api/documents/batch", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ operation, documentIds: selected, ...(payload || {}) }),
      });
      const data: unknown = await resp.json().catch(() => ({}));
      if (!resp.ok) {
        const perm = isRecord(data) && typeof data.requiredPermission === "string" ? data.requiredPermission : undefined;
        const msg = isRecord(data) && typeof data.message === "string" ? data.message : resp.status === 403 ? `权限不足${perm ? `（需要 ${perm}）` : ""}` : "批量操作失败";
        throw new Error(msg);
      }
      const success = isRecord(data) ? readNumber(data.successCount, selected.length) : selected.length;
      const failure = isRecord(data) ? readNumber(data.failureCount, 0) : 0;
      clearSelected();
      fetchDocuments();
      showToast(failure > 0 ? `成功 ${success} 项，失败 ${failure} 项` : `操作成功（共 ${success} 项）`);
    } catch (e: unknown) {
      console.error(e);
      const msg = (e as { message?: string })?.message || "操作失败，请稍后重试";
      showToast(msg);
    } finally {
      setBatchLoading(false);
    }
  };
  const visibleIds = useMemo(() => items.map((d) => d.id), [items]);
  const isAllSelectedOnPage = useMemo(() => visibleIds.length > 0 && visibleIds.every((id) => selected.includes(id)), [visibleIds, selected]);
  const toggleSelectAllPage = () => {
    if (isAllSelectedOnPage) {
      setSelected((prev) => prev.filter((id) => !visibleIds.includes(id)));
    } else {
      setSelected((prev) => Array.from(new Set([...prev, ...visibleIds])));
    }
  };

  const debounceTimer = useRef<NodeJS.Timeout | null>(null);

  const params = useMemo(() => ({ page, size, keyword: query, status: status ?? undefined }), [page, size, query, status]);

  const fetchDocuments = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const pageData = await getJSON<PageResponse<DocumentItem>>("/api/documents", params);
      setItems(pageData.content || []);
      setTotalPages(pageData.totalPages ?? 0);
      setTotalElements(pageData.totalElements ?? 0);
    } catch (e: unknown) {
      const msg = (e as { message?: string })?.message || "加载失败，请稍后重试";
      setError(msg);
    } finally {
      setLoading(false);
    }
  }, [params]);

  useEffect(() => {
    fetchDocuments();
  }, [page, fetchDocuments]);

  // 防抖处理关键词输入
  useEffect(() => {
    if (debounceTimer.current) clearTimeout(debounceTimer.current);
    debounceTimer.current = setTimeout(() => {
      setPage(0); // 查询变化时回到第一页
      fetchDocuments();
    }, 300);
    return () => {
      if (debounceTimer.current) clearTimeout(debounceTimer.current);
    };
  }, [query, params, fetchDocuments]);

  const goPrev = () => setPage((p) => Math.max(0, p - 1));
  const goNext = () => setPage((p) => Math.min(Math.max(totalPages - 1, 0), p + 1));

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-semibold">文档管理</h1>
        <div className="flex gap-2">
          <Button onClick={() => router.push("/admin/documents/new")}>新建文档</Button>
          <DropdownMenu>
            <DropdownMenuTrigger asChild>
              <Button variant="outline" disabled={selected.length === 0 || batchLoading}>{batchLoading ? "批量处理中…" : `批量操作（${selected.length}）`}</Button>
            </DropdownMenuTrigger>
            <DropdownMenuContent align="end">
              {canPublish && (
                <DropdownMenuItem disabled={batchLoading} onClick={() => runBatch("UPDATE_STATUS", { status: "PUBLISHED" })}>
                  {batchLoading ? "处理中..." : "批量发布"}
                </DropdownMenuItem>
              )}
              {canUpdateStatus && (
                <DropdownMenuItem disabled={batchLoading} onClick={() => setShowStatusDialog(true)}>
                  {batchLoading ? "处理中..." : "批量更改状态…"}
                </DropdownMenuItem>
              )}
              {canDelete && (
                <DropdownMenuItem disabled={batchLoading} onClick={() => setShowDeleteConfirm(true)} className="text-red-600 focus:text-red-600">
                  {batchLoading ? "处理中..." : "批量删除"}
                </DropdownMenuItem>
              )}
            </DropdownMenuContent>
          </DropdownMenu>
        </div>
      </div>
      <Card>
        <CardHeader>
          <CardTitle>搜索</CardTitle>
          <CardDescription>按标题或关键字检索文档</CardDescription>
        </CardHeader>
        <CardContent>
          <div className="flex flex-col gap-3">
            <div className="flex gap-3">
              <Input
                placeholder="输入关键词，例如：缓存、安装、API"
                value={query}
                onChange={(e) => setQuery(e.target.value)}
              />
              <select
                className="px-3 py-2 border rounded-md bg-background text-sm"
                value={status ?? ""}
                onChange={(e) => setStatus(e.target.value || null)}
                aria-label="状态筛选"
              >
                <option value="">全部状态</option>
                <option value="DRAFT">草稿</option>
                <option value="PUBLISHED">已发布</option>
                <option value="ARCHIVED">已归档</option>
              </select>
              <label className="inline-flex items-center gap-2 text-sm">
                <Checkbox checked={isAllSelectedOnPage} onChange={() => toggleSelectAllPage()} aria-label="本页全选" />
                <span>本页全选</span>
              </label>
            </div>
            <div className="flex items-center gap-3 text-sm text-muted-foreground">
              <span>共 {totalElements ?? 0} 条</span>
              <span>第 {page + 1} / {Math.max(totalPages, 1)} 页</span>
              {selected.length > 0 && (
                <span className="ml-2">已选择 {selected.length} 项（跨页保留）</span>
              )}
              <div className="ml-auto flex gap-2">
                <Button variant="outline" size="sm" onClick={goPrev} disabled={loading || page <= 0}>上一页</Button>
                <Button variant="outline" size="sm" onClick={goNext} disabled={loading || page >= Math.max(totalPages - 1, 0)}>下一页</Button>
              </div>
            </div>
          </div>
        </CardContent>
      </Card>

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

      {error && (
        <div className="text-red-500 text-sm">
          {error}
          {error.includes("403") && (
            <div className="mt-2 text-xs text-muted-foreground">
              你可能缺少访问权限：DOC:VIEW:LIST。请联系管理员为你的角色（如 ROLE_EDITOR/ROLE_SUB_ADMIN/ROLE_ADMIN）分配该权限。
            </div>
          )}
        </div>
      )}

      {!loading && !error && (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {items.map((doc) => (
            <Card key={doc.id} onClick={() => router.push(`/admin/documents/${doc.id}`)} className="cursor-pointer">
              <CardHeader>
                <div className="flex items-start justify-between gap-2">
                  <div className="min-w-0">
                    <div className="flex items-center gap-2">
                      <CardTitle className="line-clamp-1">{doc.title}</CardTitle>
                      {/* 状态徽章 */}
                      {statusBadge(doc.status)}
                    </div>
                    <CardDescription className="line-clamp-2">{doc.summary || "无摘要"}</CardDescription>
                  </div>
                  <Checkbox
                    checked={selected.includes(doc.id)}
                    onChange={(e) => toggleSelected(doc.id, (e.target as HTMLInputElement).checked)}
                    onClick={(e) => e.stopPropagation()}
                    aria-label="选择文档"
                  />
                </div>
              </CardHeader>
              <Separator />
              <CardContent className="pt-4 text-sm text-muted-foreground">
                <div className="flex items-center justify-between">
                  <span>{doc.categoryName || "未分类"}</span>
                  <span>{formatDateTime(doc.updatedAt)}</span>
                </div>
                <div className="mt-2 flex items-center gap-4">
                  <span>访问：{doc.viewCount ?? 0}</span>
                  <span>下载：{doc.downloadCount ?? 0}</span>
                </div>
              </CardContent>
            </Card>
          ))}
          {items.length === 0 && (
            <div className="text-sm text-muted-foreground">暂无数据</div>
          )}
        </div>
      )}
      {toastMsg && (
        <div className="fixed right-4 bottom-4 z-50 rounded-md bg-black/80 text-white px-4 py-2 shadow-lg">
          {toastMsg}
        </div>
      )}
      {showStatusDialog && (
        <div className="fixed inset-0 z-50 bg-black/40 flex items-center justify-center">
          <div className="bg-background border rounded-lg shadow-lg w-[90%] max-w-sm">
            <div className="p-4 border-b">
              <div className="text-lg font-semibold">批量更改状态</div>
              <div className="text-sm text-muted-foreground mt-1">为选中的 {selected.length} 项设置新的状态</div>
            </div>
            <div className="p-4 space-y-3">
              <label className="text-sm">选择状态</label>
              <select
                className="w-full px-3 py-2 border rounded-md bg-background text-sm"
                value={batchStatus}
                onChange={(e) => setBatchStatus((e.target as HTMLSelectElement).value as "DRAFT" | "PUBLISHED" | "ARCHIVED")}
              >
                <option value="DRAFT">草稿</option>
                <option value="PUBLISHED">已发布</option>
                <option value="ARCHIVED">已归档</option>
              </select>
              <div className="flex justify-end gap-2 pt-2">
                <Button variant="outline" onClick={() => setShowStatusDialog(false)}>取消</Button>
                <Button disabled={batchLoading || selected.length === 0 || !batchStatus} onClick={async () => { await runBatch("UPDATE_STATUS", { status: batchStatus }); setShowStatusDialog(false); }}>确定</Button>
              </div>
            </div>
          </div>
        </div>
      )}
      {showDeleteConfirm && (
        <div className="fixed inset-0 z-50 bg-black/40 flex items-center justify-center">
          <div className="bg-background border rounded-lg shadow-lg w-[90%] max-w-sm">
            <div className="p-4 border-b">
              <div className="text-lg font-semibold text-red-600">确认删除</div>
              <div className="text-sm text-muted-foreground mt-1">将删除选中的 {selected.length} 项，此操作不可恢复。</div>
            </div>
            <div className="p-4 flex justify-end gap-2">
              <Button variant="outline" onClick={() => setShowDeleteConfirm(false)}>取消</Button>
              <Button className="bg-red-600 text-white hover:bg-red-700" disabled={batchLoading || selected.length === 0} onClick={async () => { await runBatch("DELETE"); setShowDeleteConfirm(false); }}>确定删除</Button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}