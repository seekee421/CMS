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

// 未登录访问 /admin 应重定向到登录页
test("unauthenticated user is redirected to /login with redirect param", async ({ page }) => {
  await page.context().clearCookies();
  const resp = await page.goto("/admin");
  expect(resp?.status()).toBeLessThan(400); // Next.js 会进行客户端或服务端重定向
  await expect(page).toHaveURL(/\/login\?redirect=\/admin/);
});

// 非管理员（ROLE_EDITOR）在菜单看不到“用户管理”，访问 /admin/users 会被重定向到仪表板
test("ROLE_EDITOR cannot access /admin/users and is redirected to dashboard", async ({ page }) => {
  await page.context().addCookies([buildSessionCookie(["ROLE_EDITOR"])]);
  await page.goto("/admin");
  // 侧边栏不应出现“用户管理”入口
  const userMenu = page.locator("nav", { hasText: "用户管理" });
  await expect(userMenu).toHaveCount(0);
  // 直接访问 /admin/users 将被 UsersPage 的前端路由守卫重定向到 /admin/dashboard
  await page.goto("/admin/users");
  await expect(page).toHaveURL(/\/admin\/dashboard/);
});

// 管理员（ROLE_ADMIN）可访问 /admin/users
test("ROLE_ADMIN can access /admin/users and see Users page", async ({ page }) => {
  await page.context().addCookies([buildSessionCookie(["ROLE_ADMIN"])]);
  await page.goto("/admin/users");
  await expect(page).toHaveURL(/\/admin\/users/);
  // 页面标题存在
  await expect(page.locator("h1:text('用户管理')")).toHaveCount(1);
});