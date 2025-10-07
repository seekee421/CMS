"use client";

import { useEffect, useState, useMemo, useCallback } from "react";
import dynamic from "next/dynamic";
import { useParams, useRouter } from "next/navigation";
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";
import { Button } from "@/components/ui/button";
import "@mdxeditor/editor/style.css";
import ReactMarkdown from "react-markdown";
import remarkGfm from "remark-gfm";
import rehypeSanitize from "rehype-sanitize";
import rehypeHighlight from "rehype-highlight";
import type { MDXEditorProps } from "@mdxeditor/editor";
import { headingsPlugin, listsPlugin, quotePlugin, linkPlugin, imagePlugin, tablePlugin, codeBlockPlugin, markdownShortcutPlugin } from "@mdxeditor/editor";

const MDXEditorLazy = dynamic<MDXEditorProps>(() => import("@mdxeditor/editor").then((m) => m.MDXEditor), { ssr: false });

interface DocumentDetail {
  id: number;
  title: string;
  summary?: string;
  content?: string;
  categoryId?: number;
}

export default function DocumentEditPage() {
  const params = useParams();
  const router = useRouter();
  const id = params?.id as string;

  const [title, setTitle] = useState("");
  const [summary, setSummary] = useState("");
  const [content, setContent] = useState("");
  const [categoryId, setCategoryId] = useState<number | "">("");
  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [message, setMessage] = useState<string | null>(null);
  const [roles, setRoles] = useState<string[]>([]);

  const canEdit = useMemo(() => roles.includes("ROLE_ADMIN") || roles.includes("ROLE_EDITOR"), [roles]);

  useEffect(() => {
    (async () => {
      try {
        const resp = await fetch("/api/auth/me");
        const data = await resp.json().catch(() => ({}));
        const rs: string[] = Array.isArray(data?.roles) ? data.roles : [];
        setRoles(rs);
        if (rs.length === 0) {
          // 未登录兜底由 middleware 处理
        } else if (!rs.includes("ROLE_ADMIN") && !rs.includes("ROLE_EDITOR")) {
          setError("无权限编辑此文档");
          router.replace(`/admin/documents/${id}`);
        }
      } catch {
        setRoles([]);
      }
    })();
  }, [id, router]);

  useEffect(() => {
    const fetchDetail = async () => {
      setLoading(true);
      setError(null);
      try {
        const resp = await fetch(`/api/documents/${id}`);
        const data: DocumentDetail = await resp.json();
        if (!resp.ok) throw new Error((data as { message?: string })?.message || "加载失败");
        setTitle(data.title || "");
        setSummary(data.summary || "");
        setContent(data.content || "");
        setCategoryId((data.categoryId ?? "") as number | "");
      } catch (e: unknown) {
        const msg = (e as { message?: string })?.message || "网络错误";
        setError(msg);
      } finally {
        setLoading(false);
      }
    };
    if (id) fetchDetail();
  }, [id]);

  const onSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    setSaving(true);
    setError(null);
    setMessage(null);
    try {
      const body = {
        title,
        summary,
        content,
        categoryId: categoryId === "" ? null : Number(categoryId),
      };
      const resp = await fetch(`/api/documents/${id}`, {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(body),
      });
      const data = await resp.json().catch(() => null);
      if (!resp.ok) throw new Error(data?.message || "保存失败");
      setMessage("保存成功");
    } catch (e: unknown) {
      const msg = (e as { message?: string })?.message || "网络错误";
      setError(msg);
    } finally {
      setSaving(false);
    }
  };

  const imageUploadHandler = useCallback(async (file: File): Promise<string> => {
    const form = new FormData();
    form.append("file", file);
    if (id) form.append("documentId", String(id));
    const resp = await fetch("/api/editor/upload/media", { method: "POST", body: form });
    const data = await resp.json().catch(() => null);
    if (!resp.ok) throw new Error(data?.message || "上传失败");
    return data?.url || data?.path || data?.location || "";
  }, [id]);

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-semibold">编辑文档</h1>
        <Button variant="outline" onClick={() => router.push(`/admin/documents/${id}`)}>返回预览</Button>
      </div>

      {loading && <div className="text-sm text-muted-foreground">加载中...</div>}
      {error && <div className="text-red-500 text-sm">{error}</div>}

      {!loading && !error && canEdit && (
        <Card>
          <CardHeader>
            <CardTitle>基本信息</CardTitle>
            <CardDescription>修改标题、摘要、分类与内容</CardDescription>
          </CardHeader>
          <CardContent>
            <form className="space-y-4" onSubmit={onSubmit}>
              <div>
                <label className="text-sm font-medium">标题</label>
                <Input value={title} onChange={(e) => setTitle(e.target.value)} required />
              </div>
              <div>
                <label className="text-sm font-medium">摘要</label>
                <Textarea value={summary} onChange={(e) => setSummary(e.target.value)} />
              </div>
              <div>
                <label className="text-sm font-medium">分类ID</label>
                <Input type="number" value={categoryId as number} onChange={(e) => setCategoryId(e.target.value === "" ? "" : Number(e.target.value))} />
              </div>
              <div className="space-y-2">
                <label className="text-sm font-medium">内容（MDXEditor）</label>
                <div className="border rounded-md">
                  <MDXEditorLazy
                    markdown={content}
                    onChange={setContent}
                    plugins={[
                      headingsPlugin(),
                      listsPlugin(),
                      quotePlugin(),
                      linkPlugin(),
                      tablePlugin(),
                      codeBlockPlugin({}),
                      imagePlugin({ imageUploadHandler }),
                      markdownShortcutPlugin(),
                    ]}
                  />
                </div>
              </div>
              {message && <div className="text-green-600 text-sm">{message}</div>}
              {error && <div className="text-red-500 text-sm">{error}</div>}
              <div className="flex gap-2">
                <Button type="submit" disabled={saving}>保存</Button>
                <Button type="button" variant="outline" onClick={() => router.push("/admin/documents")}>取消</Button>
              </div>
            </form>
          </CardContent>
        </Card>
      )}

      {!loading && !error && (
        <Card>
          <CardHeader>
            <CardTitle>实时预览</CardTitle>
            <CardDescription>支持 GFM（表格、任务列表）与代码高亮，已启用 sanitize</CardDescription>
          </CardHeader>
          <CardContent>
            <div className="prose max-w-none">
              <ReactMarkdown remarkPlugins={[remarkGfm]} rehypePlugins={[rehypeSanitize, rehypeHighlight]}>
                {content || "暂无内容"}
              </ReactMarkdown>
            </div>
          </CardContent>
        </Card>
      )}
    </div>
  );
}