"use client";

import { useEffect, useState } from "react";
import { getJSON } from "@/lib/http";
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from "@/components/ui/card";
import { Separator } from "@/components/ui/separator";

interface SystemStats {
  documentsCount: number;
  todayVisits: number;
  downloadsCount: number;
  documentsTrend?: string; // e.g. +12%
  visitsTrend?: string;
  downloadsTrend?: string;
}

export default function StatisticsPage() {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [stats, setStats] = useState<SystemStats | null>(null);

  const fetchStats = async () => {
    setLoading(true);
    setError(null);
    try {
      // 后端约定的系统统计接口
      const data = await getJSON<SystemStats>("/api/statistics/system");
      setStats(data);
    } catch (e: unknown) {
    const msg = (e as { message?: string })?.message || "加载失败";
    setError(msg);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchStats();
  }, []);

  return (
    <div className="space-y-4">
      <h1 className="text-2xl font-semibold">统计分析</h1>
      <p className="text-muted-foreground text-sm">
        展示系统关键指标（文档总量、今日访问、下载次数），后续将接入更多维度与图表。
      </p>
      <Separator />

      {loading && (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {Array.from({ length: 3 }).map((_, i) => (
            <div key={i} className="rounded-lg border p-6 animate-pulse">
              <div className="h-5 w-1/3 bg-muted mb-3" />
              <div className="h-8 w-2/3 bg-muted" />
            </div>
          ))}
        </div>
      )}

      {error && <div className="text-red-500 text-sm">{error}</div>}

      {!loading && !error && (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          <Card>
            <CardHeader>
              <CardTitle>文档总量</CardTitle>
              <CardDescription>系统内已发布和历史文档数量</CardDescription>
            </CardHeader>
            <CardContent>
              <div className="text-3xl font-bold">{stats?.documentsCount ?? "-"}</div>
              <div className="text-xs text-muted-foreground mt-2">较上月 {stats?.documentsTrend ?? "+0%"}</div>
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle>今日访问</CardTitle>
              <CardDescription>今日各文档总访问次数</CardDescription>
            </CardHeader>
            <CardContent>
              <div className="text-3xl font-bold">{stats?.todayVisits ?? "-"}</div>
              <div className="text-xs text-muted-foreground mt-2">较昨日 {stats?.visitsTrend ?? "+0%"}</div>
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle>下载次数</CardTitle>
              <CardDescription>累计文档下载次数</CardDescription>
            </CardHeader>
            <CardContent>
              <div className="text-3xl font-bold">{stats?.downloadsCount ?? "-"}</div>
              <div className="text-xs text-muted-foreground mt-2">较上周 {stats?.downloadsTrend ?? "+0%"}</div>
            </CardContent>
          </Card>
        </div>
      )}
    </div>
  );
}