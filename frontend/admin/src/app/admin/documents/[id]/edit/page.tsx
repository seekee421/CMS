"use client";

import { useEffect, useState } from "react";
import { useParams, useRouter } from "next/navigation";
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";
import { Button } from "@/components/ui/button";

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

  useEffect(() => {
    const fetchDetail = async () => {
      setLoading(true);
      setError(null);
      try {
        const resp = await fetch(`/api/documents/${id}`);
        const data: DocumentDetail = await resp.json();
        if (!resp.ok) throw new Error((data as any)?.message || "加载失败");
        setTitle(data.title || "");
        setSummary(data.summary || "");
        setContent(data.content || "");
        setCategoryId((data.categoryId ?? "") as any);
      } catch (e: any) {
        setError(e?.message || "网络错误");
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
    } catch (e: any) {
      setError(e?.message || "网络错误");
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-semibold">编辑文档</h1>
        <Button variant="outline" onClick={() => router.push(`/admin/documents/${id}`)}>返回预览</Button>
      </div>

      {loading && <div className="text-sm text-muted-foreground">加载中...</div>}
      {error && <div className="text-red-500 text-sm">{error}</div>}

      {!loading && !error && (
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
              <div>
                <label className="text-sm font-medium">内容</label>
                <Textarea value={content} onChange={(e) => setContent(e.target.value)} rows={8} />
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
    </div>
  );
}