import { test, expect } from "@playwright/test";

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

// 未登录重定向 + 角色菜单过滤 + 增强编辑页版本比较基础导航
test("auth redirect and role-based menu filtering with enhanced page navigation", async ({ page }) => {
  // 未登录访问 /admin 重定向到 /login
  await page.context().clearCookies();
  await page.goto("/admin");
  await expect(page).toHaveURL(/\/login\?redirect=\/admin/);

  // 登录为 ROLE_EDITOR，菜单隐藏“用户管理”，但可进入文档管理
  await page.context().addCookies([buildSessionCookie(["ROLE_EDITOR"])]);
  await page.goto("/admin");
  await expect(page.getByRole("link", { name: "用户管理" })).toHaveCount(0);
  await page.getByRole("link", { name: "文档管理" }).click();
  await expect(page).toHaveURL(/\/admin\/documents/);

  // 切到 ROLE_ADMIN，进入增强编辑页（拦截 /api/auth/me 与 versions 列表）
  await page.context().clearCookies();
  await page.context().addCookies([buildSessionCookie(["ROLE_ADMIN"])]);

  await page.route("**/api/auth/me", async (route) => {
    await route.fulfill({ status: 200, contentType: "application/json", body: JSON.stringify({ roles: ["ROLE_ADMIN"] }) });
  });
  await page.route("**/api/documents/101/versions*", async (route) => {
    await route.fulfill({ status: 200, contentType: "application/json", body: JSON.stringify([]) });
  });

  await page.goto("/admin/documents/101/enhanced");
  await expect(page.getByText("编辑文档（增强版）")).toBeVisible();
});