import { Card, CardHeader, CardTitle, CardContent, CardDescription } from "@/components/ui/card";

export default function DashboardPage() {
  return (
    <div className="grid gap-6">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-semibold">仪表板</h1>
      </div>
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        <Card>
          <CardHeader>
            <CardTitle>文档总量</CardTitle>
            <CardDescription>系统内已发布的文档数量</CardDescription>
          </CardHeader>
          <CardContent>
            <div className="text-3xl font-bold">128</div>
            <div className="text-xs text-muted-foreground mt-1">较上周 +12%</div>
          </CardContent>
        </Card>
        <Card>
          <CardHeader>
            <CardTitle>今日访问</CardTitle>
            <CardDescription>今日文档浏览总次数</CardDescription>
          </CardHeader>
          <CardContent>
            <div className="text-3xl font-bold">3,245</div>
            <div className="text-xs text-muted-foreground mt-1">较昨日 +5%</div>
          </CardContent>
        </Card>
        <Card>
          <CardHeader>
            <CardTitle>下载次数</CardTitle>
            <CardDescription>累计文档下载次数</CardDescription>
          </CardHeader>
          <CardContent>
            <div className="text-3xl font-bold">876</div>
            <div className="text-xs text-muted-foreground mt-1">较上月 +8%</div>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}