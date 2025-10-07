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

// 管理员邀请用户的流程：填写邮箱、勾选角色、提交并看到成功提示
// 通过拦截 /api/auth/me、/api/users 和 /api/users/invite，确保测试在无后端时也能稳定运行

test.describe("Users - Invite Flow", () => {
  test.beforeEach(async ({ page }) => {
    await page.context().addCookies([buildSessionCookie(["ROLE_ADMIN"])]);
    await page.route("**/api/auth/me", async (route) => {
      await route.fulfill({
        status: 200,
        contentType: "application/json",
        body: JSON.stringify({ roles: ["ROLE_ADMIN"] }),
      });
    });
    await page.route("**/api/users?**", async (route) => {
      await route.fulfill({
        status: 200,
        contentType: "application/json",
        body: JSON.stringify({
          content: [],
          totalElements: 0,
          totalPages: 0,
          size: 10,
          number: 0,
          first: true,
          last: true,
          empty: true,
        }),
      });
    });
    await page.route("**/api/users/invite", async (route) => {
      const json = await route.request().postDataJSON?.();
      expect(json?.email).toContain("@");
      expect(Array.isArray(json?.roles)).toBeTruthy();
      await route.fulfill({
        status: 200,
        contentType: "application/json",
        body: JSON.stringify({ message: "邀请已发送" }),
      });
    });
  });

  test("invite user with email and roles shows success toast", async ({ page }) => {
    await page.goto("/admin/users");

    await page.locator('[data-testid="invite-email-input"]').fill("new.user@example.com");
    await page.locator('[data-testid="invite-role-ROLE_USER"]').click();
    await page.locator('[data-testid="invite-role-ROLE_EDITOR"]').click();

    await page.locator('[data-testid="invite-submit-button"]').click();

    const toast = page.locator('[data-testid="users-toast-msg"]');
    await expect(toast).toContainText("邀请已发送");
  });
});