"use client";

import { useState, type ChangeEvent } from "react";
import { useRouter } from "next/navigation";
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";
import { Button } from "@/components/ui/button";

export default function NewDocumentPage() {
  const router = useRouter();
  const [title, setTitle] = useState("");
  const [summary, setSummary] = useState("");
  const [categoryId, setCategoryId] = useState<string>("");
  const [content, setContent] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

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
      if (!resp.ok) throw new Error(data?.message || "创建失败");
      const id = data?.id;
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
      <Card>
        <CardHeader>
          <CardTitle>基本信息</CardTitle>
          <CardDescription>填写标题、摘要与分类</CardDescription>
        </CardHeader>
        <CardContent>
          <form className="space-y-4" onSubmit={onSubmit}>
            <div>
              <label className="text-sm font-medium">标题</label>
              <Input value={title} onChange={(e: ChangeEvent<HTMLInputElement>) => setTitle(e.target.value)} placeholder="请输入标题" required />
            </div>
            <div>
              <label className="text-sm font-medium">摘要</label>
              <Textarea value={summary} onChange={(e: ChangeEvent<HTMLTextAreaElement>) => setSummary(e.target.value)} placeholder="简要描述" />
            </div>
            <div>
              <label className="text-sm font-medium">分类ID</label>
              <Input type="number" value={categoryId} onChange={(e: ChangeEvent<HTMLInputElement>) => setCategoryId(e.target.value)} placeholder="例如：10" />
            </div>
            <div>
              <label className="text-sm font-medium">内容（暂时为文本域，后续接入编辑器）</label>
              <Textarea value={content} onChange={(e: ChangeEvent<HTMLTextAreaElement>) => setContent(e.target.value)} rows={8} placeholder="支持 Markdown，后续接入 Monaco/Markdown 编辑器" />
            </div>

            {error && <div className="text-red-500 text-sm">{error}</div>}

            <div className="flex gap-2">
              <Button type="submit" disabled={submitting}>创建</Button>
              <Button type="button" variant="outline" onClick={() => router.push("/admin/documents")}>取消</Button>
            </div>
          </form>
        </CardContent>
      </Card>
    </div>
  );
}