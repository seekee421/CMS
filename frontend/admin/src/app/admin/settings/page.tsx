"use client";

import { useState } from "react";
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Checkbox } from "@/components/ui/checkbox";

export default function SettingsPage() {
  const [siteTitle, setSiteTitle] = useState("CMS 文档中心");
  const [siteDesc, setSiteDesc] = useState("企业文档与知识库管理平台");
  const [i18nEnabled, setI18nEnabled] = useState(false);
  const [defaultLang, setDefaultLang] = useState("zh-CN");
  const [message, setMessage] = useState<string | null>(null);

  const submit = () => {
    setMessage("已提交保存请求（占位，后续联调后端设置接口）");
    setTimeout(() => setMessage(null), 3000);
  };

  return (
    <div className="space-y-4">
      <h1 className="text-2xl font-semibold">系统设置</h1>
      <p className="text-muted-foreground text-sm">系统配置与多语言开关基础表单，后续联调设置接口与 Docusaurus 门户对齐。</p>

      <Card>
        <CardHeader>
          <CardTitle>基础信息</CardTitle>
          <CardDescription>站点标题与简要描述</CardDescription>
        </CardHeader>
        <CardContent>
          <div className="space-y-3">
            <div className="grid gap-2">
              <label htmlFor="siteTitle" className="text-sm font-medium">站点标题</label>
              <Input id="siteTitle" value={siteTitle} onChange={(e) => setSiteTitle(e.target.value)} />
            </div>
            <div className="grid gap-2">
              <label htmlFor="siteDesc" className="text-sm font-medium">站点描述</label>
              <Input id="siteDesc" value={siteDesc} onChange={(e) => setSiteDesc(e.target.value)} />
            </div>
          </div>
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle>国际化</CardTitle>
          <CardDescription>开启多语言与默认语言设置</CardDescription>
        </CardHeader>
        <CardContent>
          <div className="space-y-3">
            <div className="flex items-center justify-between">
              <div>
                <div className="text-sm font-medium">启用多语言</div>
                <div className="text-xs text-muted-foreground">启用后将开放语言切换入口与多语言内容配置</div>
              </div>
              <Checkbox checked={i18nEnabled} onChange={() => setI18nEnabled(!i18nEnabled)} />
            </div>
            <div className="grid gap-2">
              <label htmlFor="defaultLang" className="text-sm font-medium">默认语言</label>
              <Input id="defaultLang" value={defaultLang} onChange={(e) => setDefaultLang(e.target.value)} placeholder="例如 zh-CN / en-US" />
            </div>
          </div>
        </CardContent>
      </Card>

      <div className="flex items-center gap-3">
        <Button onClick={submit}>保存设置（占位）</Button>
        <Button variant="outline" onClick={() => setMessage("已恢复默认设置（占位）")}>恢复默认</Button>
      </div>
      {message && <div className="text-xs text-muted-foreground">{message}</div>}
    </div>
  );
}