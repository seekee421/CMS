"use client";
import { useState, useMemo } from "react";
import Link from "next/link";
import { ChevronDown, ChevronRight } from "lucide-react";
import { Button } from "./ui/button";
import { Separator } from "./ui/separator";
import { ScrollArea } from "./ui/scroll-area";

export type MenuItem = {
  label: string;
  href: string;
  roles: string[];
};

export type MenuGroup = {
  label: string;
  defaultHref: string; // 标题点击跳转的默认页
  roles: string[]; // 组本身需要的最低角色（如无则允许）
  children: MenuItem[];
};

function hasRole(roles: string[] | undefined, role: string) {
  return Array.isArray(roles) && roles.includes(role);
}

function canSee(roles: string[] | undefined, required: string[]) {
  if (!required || required.length === 0) return true;
  return required.some((r) => hasRole(roles, r));
}

export default function Sidebar({
  roles,
  groups,
}: {
  roles: string[];
  groups: MenuGroup[];
}) {
  const [expanded, setExpanded] = useState<Record<string, boolean>>({});

  const visibleGroups = useMemo(() => {
    return groups
      .map((g) => ({
        ...g,
        children: g.children.filter((c) => canSee(roles, c.roles)),
      }))
      .filter((g) => g.children.length > 0 && canSee(roles, g.roles));
  }, [groups, roles]);

  function toggle(label: string) {
    setExpanded((prev) => ({ ...prev, [label]: !prev[label] }));
  }

  return (
    <aside className="border-r bg-background p-4">
      <div className="text-sm text-muted-foreground">当前角色：{roles?.join(", ") || "未登录"}</div>
      <ScrollArea className="mt-3 h-[calc(100vh-120px)] pr-2">
        <nav className="flex flex-col gap-3">
          {visibleGroups.map((g) => {
            const isOpen = !!expanded[g.label];
            return (
              <div key={g.label} className="space-y-2">
                <div className="flex items-center justify-between gap-2">
                  {/* 标题点击跳默认页 */}
                  <Link className="text-base font-medium hover:text-foreground" href={g.defaultHref}>
                    {g.label}
                  </Link>
                  {/* 箭头控制展开，不触发导航 */}
                  <Button
                    variant="ghost"
                    size="icon"
                    onClick={() => toggle(g.label)}
                    aria-label={isOpen ? "折叠" : "展开"}
                  >
                    {isOpen ? <ChevronDown className="h-4 w-4" /> : <ChevronRight className="h-4 w-4" />}
                  </Button>
                </div>
                {isOpen && (
                  <div className="pl-2 flex flex-col gap-1">
                    {g.children.map((c) => (
                      <Link
                        key={c.href}
                        className="text-sm rounded-md px-2 py-1 hover:bg-muted hover:text-foreground"
                        href={c.href}
                      >
                        {c.label}
                      </Link>
                    ))}
                  </div>
                )}
                <Separator className="my-2" />
              </div>
            );
          })}
        </nav>
      </ScrollArea>
    </aside>
  );
}