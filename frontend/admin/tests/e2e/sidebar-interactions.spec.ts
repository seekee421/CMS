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

// 标题点击与箭头展开分离；UI 层按角色过滤

test.describe("Sidebar interactions and role-based visibility", () => {
  test("ROLE_EDITOR: clicking group title navigates to defaultHref; arrow toggles children", async ({ page }) => {
    await page.context().addCookies([buildSessionCookie(["ROLE_EDITOR"])]);
    await page.goto("/admin");

    // 标题点击跳到默认页（文档管理 -> /admin/documents）
    await page.getByRole("link", { name: "文档管理" }).click();
    await expect(page).toHaveURL(/\/admin\/documents/);

    // 返回 /admin 以测试箭头展开
    await page.goto("/admin");

    // 箭头展开文档管理分组，不触发导航
    await page.locator('[data-testid="menu-toggle-文档管理"]').first().click();
    await expect(page.locator('[data-testid="menu-children-文档管理"] a:text("文档列表")')).toBeVisible();

    // 再次点击箭头进行折叠
    await page.locator('[data-testid="menu-toggle-文档管理"]').first().click();
    await expect(page.locator('[data-testid="menu-children-文档管理"] a:text("文档列表")')).toHaveCount(0);

    // UI 层过滤：ROLE_EDITOR 不应看到“用户管理”分组标题
    await expect(page.getByRole("link", { name: "用户管理" })).toHaveCount(0);
  });

  test("ROLE_ADMIN: can see 用户管理 and navigate to /admin/users via title", async ({ page }) => {
    await page.context().addCookies([buildSessionCookie(["ROLE_ADMIN"])]);
    await page.goto("/admin");
    await expect(page.getByRole("link", { name: "用户管理" })).toBeVisible();
    await page.getByRole("link", { name: "用户管理" }).click();
    await expect(page).toHaveURL(/\/admin\/users/);
  });
});