"use client";

import { useEffect, useState, useMemo, useCallback, useRef } from "react";
import dynamic from "next/dynamic";
import { useParams, useRouter } from "next/navigation";
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";
import { Button } from "@/components/ui/button";
import { DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuTrigger } from "@/components/ui/dropdown-menu";
import { Accordion, AccordionItem, AccordionTrigger, AccordionContent } from "@/components/ui/accordion";
import { ScrollArea } from "@/components/ui/scroll-area";
import "@mdxeditor/editor/style.css";
import ReactMarkdown from "react-markdown";
import remarkGfm from "remark-gfm";
import rehypeSanitize from "rehype-sanitize";
import rehypeHighlight from "rehype-highlight";
import type { MDXEditorProps } from "@mdxeditor/editor";
import { headingsPlugin, listsPlugin, quotePlugin, linkPlugin, imagePlugin, tablePlugin, codeBlockPlugin, markdownShortcutPlugin, toolbarPlugin, diffSourcePlugin, DiffSourceToggleWrapper, UndoRedo, BoldItalicUnderlineToggles, CodeToggle, ListsToggle, CreateLink, InsertTable, InsertImage, InsertCodeBlock, BlockTypeSelect, Separator, InsertThematicBreak } from "@mdxeditor/editor";
import { getJSON, putJSON, postJSON } from "@/lib/http";
import { hasRole } from "@/lib/permissions";
import { computePreviewText } from "@/lib/content";
let Diff: { (a: string, b: string): Array<[number, string]>; DELETE: number; INSERT: number; EQUAL: number } | null = null;
if (typeof window !== "undefined") {
  // 动态导入确保仅在客户端加载，避免 SSR 编译报错
  // eslint-disable-next-line @typescript-eslint/no-var-requires
  const mod = require("fast-diff");
  Diff = (mod?.default ?? mod) as any;
}
 
 function renderInlineWordDiff(base: string, compare: string) {
  if (!Diff) {
    // 降级：直接显示 compare 文本
    return <span>{compare}</span>;
  }
  const result = Diff(base || "", compare || "");
  return (
    <span>
      {result.map(([op, text]: [number, string], idx: number) => {
        if (op === (Diff as any).INSERT) return <span key={idx} className="bg-green-200">{text}</span>;
        if (op === (Diff as any).DELETE) return <span key={idx} className="bg-red-200 line-through">{text}</span>;
        return <span key={idx}>{text}</span>;
      })}
    </span>
  );
}

const MDXEditorLazy = dynamic<MDXEditorProps>(() => import("@mdxeditor/editor").then((m) => m.MDXEditor), { ssr: false });

interface DocumentDetail { id: number; title: string; summary?: string; content?: string; categoryId?: number; }
interface DocumentVersion { id: number; tag?: string; createdAt?: string; content?: string }

export default function DocumentEditEnhancedPage() {
  const { id } = useParams() as { id: string };
  const router = useRouter();
  const [title, setTitle] = useState("");
  const [summary, setSummary] = useState("");
  const [content, setContent] = useState("");
  const [categoryId, setCategoryId] = useState<number | "">("");
  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [message, setMessage] = useState<string | null>(null);
  const [roles, setRoles] = useState<string[]>([]);
  const [hasDraft, setHasDraft] = useState(false);
  const [dirty, setDirty] = useState(false);
  const fileInputRef = useRef<HTMLInputElement | null>(null);

  // 版本与对比相关状态
  const [versions, setVersions] = useState<DocumentVersion[]>([]);
  const [versionsLoading, setVersionsLoading] = useState(false);
  const [startDate, setStartDate] = useState<string>("");
  const [endDate, setEndDate] = useState<string>("");
  const [tagQuery, setTagQuery] = useState<string>("");
  const [baseVersionId, setBaseVersionId] = useState<number | null>(null);
  const [compareVersionId, setCompareVersionId] = useState<number | null>(null);
  const [diffMode, setDiffMode] = useState<"side-by-side" | "inline">("side-by-side");
  const [diffError, setDiffError] = useState<string | null>(null);
  const [diffContent, setDiffContent] = useState<{ base?: string; compare?: string }>({});

  const canEdit = useMemo(() => hasRole(roles, "ROLE_ADMIN") || hasRole(roles, "ROLE_EDITOR"), [roles]);

  useEffect(() => { (async () => {
    try {
      const data = await getJSON<{ roles?: string[] }>("/api/auth/me");
      const rs: string[] = Array.isArray(data?.roles) ? data.roles : [];
      setRoles(rs);
      if (rs.length && !hasRole(rs, "ROLE_ADMIN") && !hasRole(rs, "ROLE_EDITOR")) {
        setError("无权限编辑此文档");
        router.replace(`/admin/documents/${id}`);
      }
    } catch { setRoles([]); }
  })(); }, [id, router]);

  useEffect(() => { const fetchDetail = async () => {
    setLoading(true); setError(null);
    try {
      const data = await getJSON<DocumentDetail>(`/api/documents/${id}`);
      setTitle(data.title || ""); setSummary(data.summary || ""); setContent(data.content || ""); setCategoryId((data.categoryId ?? "") as number | "");
    } catch (e: unknown) { setError((e as { message?: string })?.message || "网络错误"); } finally { setLoading(false); }
  }; if (id) fetchDetail(); }, [id]);

  // 拉取版本列表（支持时间范围与标签过滤）
  useEffect(() => {
    const fetchVersions = async () => {
      if (!id) return;
      setVersionsLoading(true);
      const params = new URLSearchParams();
      if (startDate) params.set("start", startDate);
      if (endDate) params.set("end", endDate);
      if (tagQuery) params.set("tag", tagQuery);
      try {
        const data = await getJSON<DocumentVersion[]>(`/api/documents/${id}/versions${params.toString() ? `?${params.toString()}` : ""}`);
        setVersions(Array.isArray(data) ? data : []);
      } catch {
        setVersions([]);
      } finally {
        setVersionsLoading(false);
      }
    };
    fetchVersions();
  }, [id, startDate, endDate, tagQuery]);

  // 本地筛选（双重保障）
  const filteredVersions: DocumentVersion[] = useMemo(() => {
    return versions.filter((v: DocumentVersion) => {
      const inTag = tagQuery ? (v.tag || "").toLowerCase().includes(tagQuery.toLowerCase()) : true;
      const inStart = startDate ? (v.createdAt ? new Date(v.createdAt) >= new Date(startDate) : false) : true;
      const inEnd = endDate ? (v.createdAt ? new Date(v.createdAt) <= new Date(endDate) : false) : true;
      return inTag && inStart && inEnd;
    });
  }, [versions, tagQuery, startDate, endDate]);

  useEffect(() => { const key = `docDraft:${id}`; try { const raw = localStorage.getItem(key); if (raw) setHasDraft(true); } catch {} }, [id]);
  useEffect(() => { setDirty(true); const key = `docDraft:${id}`; const t = setTimeout(() => { try { const p = { title, summary, content, categoryId }; localStorage.setItem(key, JSON.stringify(p)); } catch {} }, 800); return () => clearTimeout(t); }, [title, summary, content, categoryId, id]);
  useEffect(() => { const onBeforeUnload = (e: BeforeUnloadEvent) => { if (dirty) { e.preventDefault(); e.returnValue = ""; } }; window.addEventListener("beforeunload", onBeforeUnload); return () => window.removeEventListener("beforeunload", onBeforeUnload); }, [dirty]);

  const onSubmit = async (e: React.FormEvent<HTMLFormElement>) => { e.preventDefault(); setSaving(true); setError(null); setMessage(null);
    try {
      const body = { title, summary, content, categoryId: categoryId === "" ? null : Number(categoryId) };
      await putJSON(`/api/documents/${id}`, body);
      setMessage("保存成功"); setDirty(false); try { localStorage.removeItem(`docDraft:${id}`); } catch {}
    } catch (e: unknown) { setError((e as { message?: string })?.message || "网络错误"); }
    finally { setSaving(false); }
  };

  const imageUploadHandler = useCallback(async (file: File): Promise<string> => { const form = new FormData(); form.append("file", file); if (id) form.append("documentId", String(id)); const data = await postJSON<any>("/api/editor/upload/media", form); return data?.url || data?.path || data?.location || ""; }, [id]);

  // 获取指定版本内容
  const getVersionContent = useCallback(async (versionId: number) => {
    try {
      const v = await getJSON<DocumentVersion>(`/api/documents/${id}/versions/${versionId}`);
      return v?.content || "";
    } catch {
      return "";
    }
  }, [id]);

  // 简易行级差异比较
  const computeLineDiff = useCallback((a: string, b: string) => {
    const aLines = (a || "").split(/\r?\n/);
    const bLines = (b || "").split(/\r?\n/);
    const maxLen = Math.max(aLines.length, bLines.length);
    const result: Array<{ type: "same" | "added" | "removed" | "changed"; old?: string; text: string }> = [];
    for (let i = 0; i < maxLen; i++) {
      const la = aLines[i];
      const lb = bLines[i];
      if (la === undefined && lb !== undefined) {
        result.push({ type: "added", text: lb });
      } else if (la !== undefined && lb === undefined) {
        result.push({ type: "removed", text: la });
      } else if (la === lb) {
        result.push({ type: "same", text: lb ?? "" });
      } else {
        result.push({ type: "changed", old: la ?? "", text: lb ?? "" });
      }
    }
    return result;
  }, []);

  // 执行比较
  const handleCompare = useCallback(async () => {
    setDiffError(null);
    setDiffContent({});
    if (!baseVersionId || !compareVersionId) {
      setDiffError("请选择两个版本进行比较");
      return;
    }
    const [base, compare] = await Promise.all([
      getVersionContent(baseVersionId),
      getVersionContent(compareVersionId),
    ]);
    setDiffContent({ base, compare });
  }, [baseVersionId, compareVersionId, getVersionContent]);

  const handleImportClick = () => fileInputRef.current?.click();
  const handleFileChange: React.ChangeEventHandler<HTMLInputElement> = async (e) => { const file = e.target.files?.[0]; if (!file) return; const text = await file.text(); setContent(text); };
  const handleExport = () => { const blob = new Blob([content || ""], { type: "text/markdown;charset=utf-8" }); const url = URL.createObjectURL(blob); const a = document.createElement("a"); a.href = url; a.download = `${title || "document"}.md`; a.click(); URL.revokeObjectURL(url); };

  const wordCount = useMemo(() => (content || "").trim().split(/\s+/).filter(Boolean).length, [content]);
  const readingMinutes = useMemo(() => Math.max(1, Math.ceil(wordCount / 200)), [wordCount]);
  const previewText = useMemo(() => computePreviewText(content || ""), [content]);

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-semibold">编辑文档（增强版）</h1>
        <div className="flex gap-2">
          <Button variant="outline" onClick={() => router.push(`/admin/documents/${id}`)}>返回预览</Button>
          <Button variant="outline" onClick={handleImportClick}>导入 Markdown</Button>
          <Button variant="outline" onClick={handleExport}>导出 Markdown</Button>
          <input ref={fileInputRef} type="file" accept=".md,text/markdown" className="hidden" onChange={handleFileChange} />
        </div>
      </div>

      <Accordion type="single" collapsible className="w-full">
        <AccordionItem value="instructions">
          <AccordionTrigger>编辑与预览使用说明</AccordionTrigger>
          <AccordionContent>
            <ul className="list-disc pl-5 space-y-1 text-sm text-muted-foreground">
              <li>左侧为编辑区，支持标题、列表、表格、代码块、图片；支持快捷键与 Markdown 语法。</li>
              <li>右侧为实时预览，支持 GFM 与代码高亮，已启用内容安全过滤。</li>
              <li>粘贴或拖拽图片自动上传；大文件建议分片上传（后端支持后启用）。</li>
              <li>自动保存本地草稿，刷新或异常关闭也能恢复。</li>
            </ul>
          </AccordionContent>
        </AccordionItem>
      </Accordion>

      {hasDraft && (
        <div className="rounded-md border border-yellow-200 bg-yellow-50 p-3 text-sm text-yellow-800 flex items-center justify-between">
          <span>检测到未保存草稿，是否恢复？</span>
          <div className="flex gap-2">
            <Button size="sm" variant="outline" onClick={() => { try { const raw = localStorage.getItem(`docDraft:${id}`); if (raw) { const d = JSON.parse(raw) as { title?: string; summary?: string; content?: string; categoryId?: number | "" }; setTitle(d.title ?? title); setSummary(d.summary ?? summary); setContent(d.content ?? content); setCategoryId((d.categoryId ?? categoryId) as number | ""); setHasDraft(false); } } catch {} }}>恢复草稿</Button>
            <Button size="sm" variant="ghost" onClick={() => { try { localStorage.removeItem(`docDraft:${id}`); } catch {}; setHasDraft(false); }}>忽略</Button>
          </div>
        </div>
      )}

      {loading && <div className="text-sm text-muted-foreground">加载中...</div>}
      {error && <div className="text-red-500 text-sm">{error}</div>}

      {!loading && !error && canEdit && (
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-4">
          <Card className="lg:col-span-1">
            <CardHeader>
              <CardTitle>基本信息</CardTitle>
              <CardDescription>修改标题、摘要、分类与内容</CardDescription>
            </CardHeader>
            <CardContent>
              <ScrollArea className="h-[calc(100vh-320px)] pr-2">
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
                    <div className="flex items-center justify-between">
                      <label className="text-sm font-medium">内容（MDXEditor）</label>
                      <div className="flex items-center gap-2">
                        <div className="text-xs text-muted-foreground">{wordCount} 字，预计 {readingMinutes} 分钟阅读</div>
                        {/* 快捷命令菜单 */}
                        <DropdownMenu>
                          <DropdownMenuTrigger asChild>
                            <Button type="button" size="sm" variant="outline">快捷命令</Button>
                          </DropdownMenuTrigger>
                          <DropdownMenuContent align="end" className="w-44">
                            <DropdownMenuItem onClick={() => setContent((prev) => `# 标题\n\n${prev}`)}>插入标题</DropdownMenuItem>
                            <DropdownMenuItem onClick={() => setContent((prev) => `- 列表项 1\n- 列表项 2\n\n${prev}`)}>插入列表</DropdownMenuItem>
                            <DropdownMenuItem onClick={() => setContent((prev) => `${prev}\n\n| 列1 | 列2 |\n| --- | --- |\n| 值A | 值B |\n`)}>插入表格</DropdownMenuItem>
                            <DropdownMenuItem onClick={() => setContent((prev) => `${prev}\n\n\`\`\`ts\n// 代码示例\nconsole.log('hello');\n\`\`\`\n`)}>插入代码块</DropdownMenuItem>
                            <DropdownMenuItem onClick={() => setContent((prev) => `${prev}\n\n![图片说明](https://placehold.co/600x400)\n`)}>插入图片</DropdownMenuItem>
                          </DropdownMenuContent>
                        </DropdownMenu>
                      </div>
                    </div>
                    <div className="border rounded-md">
                      <MDXEditorLazy
                        markdown={content}
                        onChange={setContent}
                        plugins={[
                          toolbarPlugin({
                            toolbarContents: () => (
                              <DiffSourceToggleWrapper>
                                <UndoRedo />
                                <Separator />
                                <BoldItalicUnderlineToggles />
                                <CodeToggle />
                                <ListsToggle />
                                <CreateLink />
                                <InsertTable />
                                <InsertImage />
                                <InsertCodeBlock />
                                <InsertThematicBreak />
                                <BlockTypeSelect />
                              </DiffSourceToggleWrapper>
                            ),
                          }),
                          diffSourcePlugin({ diffMarkdown: (diffContent.base ?? undefined) }),
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
              </ScrollArea>
            </CardContent>
          </Card>

          <div className="lg:col-span-1 lg:sticky lg:top-20 space-y-4">
            <Card>
              <CardHeader>
                <CardTitle>实时预览</CardTitle>
                <CardDescription>支持 GFM 与代码高亮，已启用 sanitize</CardDescription>
              </CardHeader>
              <CardContent>
                <ScrollArea className="h-[calc(100vh-320px)] pr-2">
                  <div className="prose max-w-none">
                    <ReactMarkdown remarkPlugins={[remarkGfm]} rehypePlugins={[rehypeSanitize, rehypeHighlight]}>
                      {previewText || "暂无内容"}
                    </ReactMarkdown>
                  </div>
                </ScrollArea>
              </CardContent>
            </Card>

            {/* 版本历史与差异比较 */}
            <Card>
              <CardHeader>
                <CardTitle>版本历史与差异比较</CardTitle>
                <CardDescription>支持侧边分栏 diff 与内联高亮 diff；可按时间范围与标签过滤</CardDescription>
              </CardHeader>
              <CardContent>
                <div className="space-y-3">
                  {/* 过滤条件 */}
                  <div className="grid grid-cols-1 md:grid-cols-4 gap-2">
                    <div>
                      <label className="text-xs text-muted-foreground">开始日期</label>
                      <Input type="date" value={startDate} onChange={(e) => setStartDate(e.target.value)} />
                    </div>
                    <div>
                      <label className="text-xs text-muted-foreground">结束日期</label>
                      <Input type="date" value={endDate} onChange={(e) => setEndDate(e.target.value)} />
                    </div>
                    <div>
                      <label className="text-xs text-muted-foreground">标签（版本）</label>
                      <Input placeholder="如 v1.0 / draft" value={tagQuery} onChange={(e) => setTagQuery(e.target.value)} />
                    </div>
                    <div>
                      <label className="text-xs text-muted-foreground">对比模式</label>
                      <select className="w-full h-9 rounded-md border px-2 text-sm" value={diffMode} onChange={(e) => setDiffMode(e.target.value as any)} data-testid="diff-mode-select">
                        <option value="side-by-side">侧边分栏</option>
                        <option value="inline">内联高亮</option>
                      </select>
                    </div>
                  </div>

                  {/* 版本列表与选择 */}
                  <div className="rounded-md border">
                    <div className="flex items-center justify-between px-3 py-2">
                      <div className="text-sm text-muted-foreground">共 {filteredVersions.length} 个版本{versionsLoading ? "（加载中）" : ""}</div>
                      <Button size="sm" variant="outline" onClick={() => { /* 重新拉取 */ setStartDate(startDate); }}>刷新</Button>
                    </div>
                    <div className="max-h-48 overflow-auto divide-y">
                      {filteredVersions.length === 0 && (
                        <div className="p-3 text-sm text-muted-foreground">暂无版本记录</div>
                      )}
                      {filteredVersions.map((v: DocumentVersion) => (
                        <div key={v.id} className="p-3 flex items-center gap-3">
                          <div className="flex-1">
                            <div className="text-sm font-medium">版本 #{v.id} {v.tag ? `(${v.tag})` : ""}</div>
                            <div className="text-xs text-muted-foreground">{v.createdAt || "时间未知"}</div>
                          </div>
                          <div className="flex items-center gap-2">
                            <label className="text-xs">基准</label>
                            <input type="radio" name="baseVersion" checked={baseVersionId === v.id} onChange={() => setBaseVersionId(v.id)} />
                            <label className="text-xs ml-3">对比</label>
                            <input type="radio" name="compareVersion" checked={compareVersionId === v.id} onChange={() => setCompareVersionId(v.id)} />
                          </div>
                        </div>
                      ))}
                    </div>
                  </div>

                  <div className="flex items-center gap-2">
                    <Button size="sm" onClick={handleCompare} data-testid="compare-button">比较</Button>
                    {diffError && <span className="text-xs text-red-500">{diffError}</span>}
                  </div>

                  {/* 差异结果展示 */}
                  {diffContent.base !== undefined && diffContent.compare !== undefined && (
                    <div className="mt-2">
                      {diffMode === "side-by-side" ? (
                        <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
                          <div className="rounded-md border">
                            <div className="px-3 py-2 text-xs text-muted-foreground">基准版本</div>
                            <ScrollArea className="h-64">
                              <pre className="px-3 py-2 text-sm whitespace-pre-wrap">
                                {computeLineDiff(diffContent.base || "", diffContent.base || "").map((d, i) => (
                                  <div key={i} className={d.type === "added" ? "bg-green-50 text-green-800" : d.type === "removed" ? "bg-red-50 text-red-800 line-through" : d.type === "changed" ? "bg-yellow-50 text-yellow-800" : ""}>{d.text}</div>
                                ))}
                              </pre>
                            </ScrollArea>
                          </div>
                          <div className="rounded-md border">
                            <div className="px-3 py-2 text-xs text-muted-foreground">对比版本</div>
                            <ScrollArea className="h-64">
                              <pre className="px-3 py-2 text-sm whitespace-pre-wrap">
                                {computeLineDiff(diffContent.base || "", diffContent.compare || "").map((d, i) => (
                                  <div key={i} className={d.type === "added" ? "bg-green-50 text-green-800" : d.type === "removed" ? "bg-red-50 text-red-800 line-through" : d.type === "changed" ? "bg-yellow-50 text-yellow-800" : ""}>{d.text}</div>
                                ))}
                              </pre>
                            </ScrollArea>
                          </div>
                        </div>
                      ) : (
                        <div className="rounded-md border">
                          <div data-testid="inline-diff-container" className="px-3 py-2 text-xs text-muted-foreground">内联高亮（以对比版本为准显示变更）</div>
                          <ScrollArea className="h-64">
                            <pre className="px-3 py-2 text-sm whitespace-pre-wrap">
                              {computeLineDiff(diffContent.base || "", diffContent.compare || "").map((d, i) => (
                                <div key={i} data-testid={`inline-diff-line-${i}`} className={d.type === "added" ? "bg-green-50 text-green-800" : d.type === "removed" ? "bg-red-50 text-red-800 line-through" : d.type === "changed" ? "bg-yellow-50 text-yellow-800" : ""}>
                                  {d.type === "changed" && d.old ? (<span className="mr-2 line-through opacity-70">{d.old}</span>) : null}
                                  {renderInlineWordDiff(d.old ?? "", d.text)}
                                </div>
                              ))}
                            </pre>
                          </ScrollArea>
                        </div>
                      )}
                    </div>
                  )}
                </div>
              </CardContent>
            </Card>
          </div>
        </div>
      )}

      {!loading && !error && !canEdit && (
        <Card>
          <CardHeader>
            <CardTitle>实时预览</CardTitle>
            <CardDescription>支持 GFM（表格、任务列表）与代码高亮，已启用 sanitize</CardDescription>
          </CardHeader>
          <CardContent>
            <div className="prose max-w-none">
              <ReactMarkdown remarkPlugins={[remarkGfm]} rehypePlugins={[rehypeSanitize, rehypeHighlight]}>
                {previewText || "暂无内容"}
              </ReactMarkdown>
            </div>
          </CardContent>
        </Card>
      )}
    </div>
  );
}