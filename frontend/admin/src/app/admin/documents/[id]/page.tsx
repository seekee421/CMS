"use client";

import { useEffect, useState } from "react";
import { useParams, useRouter } from "next/navigation";
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from "@/components/ui/card";
import { Button } from "@/components/ui/button";

interface DocumentDetail {
  id: number;
  title: string;
  summary?: string;
  content?: string;
  categoryName?: string;
  updatedAt?: string;
}

export default function DocumentPreviewPage() {
  const params = useParams();
  const router = useRouter();
  const id = params?.id as string;
  const [doc, setDoc] = useState<DocumentDetail | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchDetail = async () => {
      setLoading(true);
      setError(null);
      try {
        const resp = await fetch(`/api/documents/${id}`);
        const data = await resp.json();
        if (!resp.ok) throw new Error(data?.message || "加载失败");
        setDoc(data ?? null);
      } catch (e: unknown) {
        const msg = (e as { message?: string })?.message || "网络错误";
        setError(msg);
      } finally {
        setLoading(false);
      }
    };
    if (id) fetchDetail();
  }, [id]);

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-semibold">文档预览</h1>
        <Button onClick={() => router.push(`/admin/documents/${id}/edit`)}>编辑</Button>
      </div>
      {loading && <div className="text-sm text-muted-foreground">加载中...</div>}
      {error && <div className="text-red-500 text-sm">{error}</div>}
      {!loading && !error && doc && (
        <Card>
          <CardHeader>
            <CardTitle>{doc.title}</CardTitle>
            <CardDescription>{doc.summary || "无摘要"}</CardDescription>
          </CardHeader>
          <CardContent>
            <div className="prose max-w-none">
              <pre className="whitespace-pre-wrap">{doc.content || "暂无内容"}</pre>
            </div>
          </CardContent>
        </Card>
      )}
    </div>
  );
}