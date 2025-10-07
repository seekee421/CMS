"use client";

import { useState } from "react";
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Checkbox } from "@/components/ui/checkbox";

interface CategoryNode {
  id: number;
  name: string;
  children?: CategoryNode[];
}

const initialTree: CategoryNode[] = [
  { id: 1, name: "技术文档", children: [
    { id: 11, name: "前端" },
    { id: 12, name: "后端" },
  ]},
  { id: 2, name: "产品手册", children: [
    { id: 21, name: "安装指南" },
    { id: 22, name: "使用说明" },
  ]},
];

export default function CategoriesPage() {
  const [tree] = useState<CategoryNode[]>(initialTree);
  const [expanded, setExpanded] = useState<Record<number, boolean>>({ 1: true, 2: true });
  const [selectedIds, setSelectedIds] = useState<number[]>([]);
  const [newName, setNewName] = useState("");
  const [message, setMessage] = useState<string | null>(null);

  const toggleExpand = (id: number) => {
    setExpanded((prev) => ({ ...prev, [id]: !prev[id] }));
  };

  const toggleSelect = (id: number) => {
    setSelectedIds((prev) => (prev.includes(id) ? prev.filter((x) => x !== id) : [...prev, id]));
  };

  const renderNode = (node: CategoryNode, level = 0) => {
    const isExpanded = !!expanded[node.id];
    const hasChildren = (node.children || []).length > 0;
    return (
      <div key={node.id} className="pl-2">
        <div className="flex items-center gap-2 py-1">
          {hasChildren && (
            <Button variant="ghost" size="sm" onClick={() => toggleExpand(node.id)} aria-label={isExpanded ? "折叠" : "展开"}>
              {isExpanded ? "▾" : "▸"}
            </Button>
          )}
          {!hasChildren && <span className="inline-block w-4" />}
          <Checkbox checked={selectedIds.includes(node.id)} onChange={() => toggleSelect(node.id)} />
          <span className="text-sm">{node.name}</span>
        </div>
        {hasChildren && isExpanded && (
          <div className="pl-6">
            {node.children!.map((child) => renderNode(child, level + 1))}
          </div>
        )}
      </div>
    );
  };

  return (
    <div className="space-y-4">
      <h1 className="text-2xl font-semibold">分类管理</h1>
      <p className="text-muted-foreground text-sm">树形结构与拖拽排序将于后续接入，目前提供只读树与批量操作入口。</p>

      <Card>
        <CardHeader>
          <CardTitle>分类树</CardTitle>
          <CardDescription>点击箭头展开/折叠，勾选进行批量操作</CardDescription>
        </CardHeader>
        <CardContent>
          <div>
            {tree.map((n) => renderNode(n))}
            {tree.length === 0 && (
              <div className="text-sm text-muted-foreground">暂无分类</div>
            )}
          </div>
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle>新建分类</CardTitle>
          <CardDescription>输入名称后提交（占位，后续联调后端接口）</CardDescription>
        </CardHeader>
        <CardContent>
          <div className="flex flex-col md:flex-row md:items-center gap-3">
            <Input value={newName} onChange={(e) => setNewName(e.target.value)} placeholder="请输入分类名称" />
            <Button onClick={() => {
              if (!newName.trim()) { setMessage("请填写分类名称"); return; }
              setMessage(`已提交创建请求：${newName}`);
              setNewName("");
            }}>创建</Button>
          </div>
          {message && <div className="mt-2 text-xs text-muted-foreground">{message}</div>}
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle>批量操作</CardTitle>
          <CardDescription>后续将支持拖拽排序、批量移动与权限设置</CardDescription>
        </CardHeader>
        <CardContent>
          <div className="space-y-3">
            <div className="text-sm">已选择 {selectedIds.length} 个分类</div>
            <div className="flex items-center gap-3">
              <Button variant="outline" disabled={selectedIds.length === 0}>批量移动（占位）</Button>
              <Button variant="outline" disabled={selectedIds.length === 0}>权限设置（占位）</Button>
              <Button disabled>保存排序（占位）</Button>
            </div>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}