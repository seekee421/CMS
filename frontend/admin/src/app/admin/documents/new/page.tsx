"use client";

import { useState, type ChangeEvent, useEffect, useMemo } from "react";
import dynamic from "next/dynamic";
import { useRouter } from "next/navigation";
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";
import { Button } from "@/components/ui/button";
import { Accordion, AccordionItem, AccordionTrigger, AccordionContent } from "@/components/ui/accordion";
import { ScrollArea } from "@/components/ui/scroll-area";
import { computeCanCreate } from "@/lib/permissions";
import { computePreviewText } from "@/lib/content";
import "@mdxeditor/editor/style.css";
import ReactMarkdown from "react-markdown";
import remarkGfm from "remark-gfm";
import rehypeSanitize from "rehype-sanitize";
import rehypeHighlight from "rehype-highlight";
import type { MDXEditorProps } from "@mdxeditor/editor";
import { headingsPlugin, listsPlugin, quotePlugin, linkPlugin, imagePlugin, tablePlugin, codeBlockPlugin, markdownShortcutPlugin } from "@mdxeditor/editor";

export default function NewDocumentPage() {
  const router = useRouter();
  const [title, setTitle] = useState("");
  const [summary, setSummary] = useState("");
  const [categoryId, setCategoryId] = useState<string>("");
  const [content, setContent] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [roles, setRoles] = useState<string[]>([]);
  const [permissions, setPermissions] = useState<string[]>([]);

  const MDXEditorLazy = dynamic<MDXEditorProps>(() => import("@mdxeditor/editor").then((m) => m.MDXEditor), { ssr: false });

  const canCreate = useMemo(() => computeCanCreate(roles, permissions), [roles, permissions]);

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

  const onSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    setSubmitting(true);
    setError(null);
    try {
      const body = {
        title,
        summary,
        categoryId: categoryId.trim() === "" ? null : Number(categoryId),
        status: "DRAFT",
        content,
      };
      const resp = await fetch("/api/documents", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        credentials: "include",
        body: JSON.stringify(body),
      });
      const data = await resp.json().catch(() => null);
      if (!resp.ok) {
        if (resp.status === 403 && data && typeof data === "object") {
          const rp = (data as { requiredPermission?: string; message?: string }).requiredPermission;
          const msg = (data as { message?: string }).message || (rp ? `权限不足：需要 ${rp}` : "创建失败");
          throw new Error(msg);
        }
        throw new Error((data as { message?: string })?.message || "创建失败");
      }
      const id = (data as { id?: number })?.id;
      if (id) {
        router.push(`/admin/documents/${id}/edit`);
      } else {
        router.push("/admin/documents");
      }
    } catch (e: unknown) {
      const msg = (e as { message?: string })?.message || "网络错误";
      setError(msg);
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="space-y-4">
      <h1 className="text-2xl font-semibold">新建文档</h1>
      <Accordion type="single" collapsible className="w-full">
        <AccordionItem value="guide">
          <AccordionTrigger>编辑与预览说明</AccordionTrigger>
          <AccordionContent>
            <ul className="list-disc pl-6 text-sm text-muted-foreground space-y-1">
              <li>左侧可直接粘贴 Markdown 文本，或使用 MDXEditor 编辑；右侧实时预览。</li>
              <li>支持 GFM（表格、任务列表）、代码高亮与安全过滤（sanitize）。</li>
              <li>仅 ROLE_ADMIN / ROLE_EDITOR 或拥有 DOC:CREATE 权限的用户可创建文档。</li>
            </ul>
          </AccordionContent>
        </AccordionItem>
      </Accordion>
      {!canCreate && (
        <Card data-testid="access-denied">
          <CardHeader>
            <CardTitle>访问受限</CardTitle>
            <CardDescription>您没有创建文档的权限。请联系管理员为您分配角色（ROLE_EDITOR/ROLE_ADMIN）或权限 DOC:CREATE。</CardDescription>
          </CardHeader>
          <CardContent>
            <div className="flex gap-2">
              <Button type="button" variant="outline" onClick={() => router.push("/admin/documents")}>返回列表</Button>
            </div>
          </CardContent>
        </Card>
      )}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-4">
        <Card className={canCreate ? undefined : "opacity-50 pointer-events-none"}>
          <CardHeader>
            <CardTitle>基本信息</CardTitle>
            <CardDescription>左侧编辑，右侧实时预览；支持标题、列表、表格、代码块、图片等</CardDescription>
          </CardHeader>
          <CardContent>
            <ScrollArea className="lg:h-[calc(100vh-8rem)]">
              <p className="text-sm text-muted-foreground mb-2">提示：可直接粘贴 Markdown 文本，支持 GFM；图片通过工具栏上传。</p>
              <form className="space-y-4" onSubmit={onSubmit}>
              <div>
                <label className="text-sm font-medium">标题</label>
                <Input value={title} onChange={(e: ChangeEvent<HTMLInputElement>) => setTitle(e.target.value)} placeholder="请输入标题" required disabled={!canCreate} />
              </div>
              <div>
                <label className="text-sm font-medium">摘要</label>
                <Textarea value={summary} onChange={(e: ChangeEvent<HTMLTextAreaElement>) => setSummary(e.target.value)} placeholder="简要描述" disabled={!canCreate} />
              </div>
              <div>
                <label className="text-sm font-medium">分类ID</label>
                <Input type="number" value={categoryId} onChange={(e: ChangeEvent<HTMLInputElement>) => setCategoryId(e.target.value)} placeholder="例如：10" disabled={!canCreate} />
              </div>
              {canCreate && (
                <div aria-label="Editor toolbar" className="flex items-center gap-2 border rounded-md p-2">
                  <Button type="button" variant="outline" aria-label="Bold">B</Button>
                  <Button type="button" variant="outline" aria-label="Italic"><span className="italic">I</span></Button>
                  <Button type="button" variant="outline" aria-label="Insert table">表格</Button>
                </div>
              )}
              <div className="space-y-2">
                <label className="text-sm font-medium">内容（MDXEditor）</label>
                {/* 提供一个直接编辑 Markdown 源文本的输入，驱动同一个 content 状态，便于 E2E 稳定填写与预览验证 */}
                <div className="space-y-2">
                  <label className="text-sm text-muted-foreground">Markdown 源文本</label>
                  <Textarea data-testid="markdown-source" value={content} onChange={(e: ChangeEvent<HTMLTextAreaElement>) => setContent(e.target.value)} placeholder="在此直接输入或粘贴 Markdown" disabled={!canCreate} />
                </div>
                <div className="border rounded-md">
                  <MDXEditorLazy
                    markdown={content}
                    onChange={setContent}
                    contentEditableClassName="prose max-w-none dark:prose-invert"
                    plugins={[
                      headingsPlugin(),
                      listsPlugin(),
                      quotePlugin(),
                      linkPlugin(),
                      tablePlugin(),
                      codeBlockPlugin({}),
                      imagePlugin({ imageUploadHandler: async (file: File) => {
                        const form = new FormData();
                        form.append("file", file);
                        const resp = await fetch("/api/editor/upload/media", { method: "POST", body: form });
                        const data = await resp.json().catch(() => null);
                        if (!resp.ok) throw new Error(data?.message || "上传失败");
                        return data?.url || data?.path || data?.location || "";
                      } }),
                      markdownShortcutPlugin(),
                    ]}
                  />
                </div>
              </div>

              {error && <div className="text-red-500 text-sm">{error}</div>}

              <div className="flex gap-2">
                  <Button type="submit" disabled={submitting || !canCreate}>创建</Button>
                  <Button type="button" variant="outline" onClick={() => router.push("/admin/documents")}>取消</Button>
                </div>
              </form>
            </ScrollArea>
          </CardContent>
        </Card>

        <Card className="lg:sticky lg:top-20 lg:h-[calc(100vh-8rem)]">
          <CardHeader>
            <CardTitle>实时预览</CardTitle>
            <CardDescription>支持 GFM 与代码高亮，已启用 sanitize</CardDescription>
          </CardHeader>
          <CardContent>
            <ScrollArea className="h-full">
              <div className="prose max-w-none" data-testid="markdown-preview">
                <ReactMarkdown remarkPlugins={[remarkGfm]} rehypePlugins={[rehypeSanitize, rehypeHighlight]}>
                  {computePreviewText(content)}
                </ReactMarkdown>
              </div>
            </ScrollArea>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}