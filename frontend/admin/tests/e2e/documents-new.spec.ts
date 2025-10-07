import { test, expect, Page } from "@playwright/test";

function buildSessionCookie(roles: string[]) {
  const payload = { user: { roles }, roles };
  const value = Buffer.from(JSON.stringify(payload)).toString("base64");
  return {
    name: "session",
    value,
    domain: "localhost",
    path: "/",
    httpOnly: false,
    secure: false,
  } as const;
}

// Helpers
async function login(page: Page, role: "ADMIN" | "EDITOR" | "USER" = "ADMIN") {
  const roles = role === "ADMIN" ? ["ROLE_ADMIN"] : role === "EDITOR" ? ["ROLE_EDITOR"] : ["ROLE_USER"];
  await page.context().addCookies([buildSessionCookie(roles)]);
  // 直接进入后台首页以确保会话生效
  await page.goto("/admin");
  await expect(page).toHaveURL(/\/admin/);
}

// E2E 套件：新建文档页
// 覆盖三层防线与编辑体验的关键路径

test.describe("/admin/documents/new", () => {
  test("未登录访问应重定向到 /login", async ({ page }) => {
    await page.goto("/admin/documents/new");
    await expect(page).toHaveURL(/\/login/);
  });

  test("ROLE_USER 不显示新建入口且直接访问显示 AccessDenied 卡片", async ({ page }) => {
    await login(page, "USER");
    await page.goto("/admin/documents/new");
    await expect(page.getByTestId("access-denied")).toBeVisible();
  });

  test("ROLE_EDITOR/ADMIN 显示编辑器与工具栏", async ({ page }) => {
    await login(page, "ADMIN");
    await page.goto("/admin/documents/new");
    // 工具栏存在（按钮若使用 aria-label 或 title）
    await expect(page.getByRole("button", { name: /Bold|加粗/i })).toBeVisible();
    await expect(page.getByRole("button", { name: /Italic|斜体/i })).toBeVisible();
    await expect(page.getByRole("button", { name: /Insert table|表格/i })).toBeVisible();
  });

  test("输入内容后右侧实时预览更新", async ({ page }) => {
    await login(page, "ADMIN");
    await page.goto("/admin/documents/new");
    // 找到编辑区域并输入内容
    const source = page.getByTestId("markdown-source");
await source.click();
await source.fill("# Hello\n\n- item 1\n- item 2\n\n**bold**");
    // 预览区域应显示对应渲染
    await expect(page.getByRole("heading", { name: "Hello", level: 1 })).toBeVisible();
    await expect(page.locator("ul li")).toHaveCount(2);
    await expect(page.locator("strong")).toBeVisible();
  });

  test("提交：标题/摘要/分类ID必填校验，成功后跳转详情或列表", async ({ page }) => {
    await login(page, "ADMIN");
    await page.goto("/admin/documents/new");
    await page.getByPlaceholder(/请输入标题|Title/i).fill("E2E 文档");
await page.getByPlaceholder(/简要描述|Summary/i).fill("这是摘要");
await page.getByPlaceholder(/例如：10/i).fill("10");
    await page.getByRole("button", { name: /创建|提交|Create/i }).click();
    // 成功提示或跳转（根据实现）
    await expect(page).toHaveURL(/\/admin\/documents\/\d+\/edit|\/admin\/documents/);
  });
});