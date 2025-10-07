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

test.describe("Users - Reset Password Mail", () => {
  test.beforeEach(async ({ page }) => {
    await page.context().addCookies([buildSessionCookie(["ROLE_ADMIN"])]);
    await page.route("**/api/auth/me", async (route) => {
      await route.fulfill({ status: 200, contentType: "application/json", body: JSON.stringify({ roles: ["ROLE_ADMIN"] }) });
    });
    await page.route("**/api/users?**", async (route) => {
      await route.fulfill({
        status: 200,
        contentType: "application/json",
        body: JSON.stringify({
          content: [
            { id: 101, username: "doc_tester", email: "doc.tester@example.com", roles: [{ name: "ROLE_USER" }] },
            { id: 102, username: "no_email_user", roles: [{ name: "ROLE_USER" }] },
          ],
          totalElements: 2,
          totalPages: 1,
          size: 10,
          number: 0,
          first: true,
          last: true,
          empty: false,
        }),
      });
    });
    await page.route("**/api/auth/password-reset", async (route) => {
      const json = await route.request().postDataJSON?.();
      if (!json?.email) {
        await route.fulfill({ status: 400, contentType: "application/json", body: JSON.stringify({ message: "邮箱必填" }) });
        return;
      }
      await route.fulfill({ status: 200, contentType: "application/json", body: JSON.stringify({ message: "重置邮件已发送" }) });
    });
  });

  test("click reset on user with email shows success toast", async ({ page }) => {
    await page.goto("/admin/users");
    await page.locator('[data-testid="user-reset-101"]').click();
    const toast = page.locator('[data-testid="users-toast-msg"]');
    await expect(toast).toContainText("重置邮件已发送");
  });

  test("click reset on user without email shows warning toast", async ({ page }) => {
    await page.goto("/admin/users");
    await page.locator('[data-testid="user-reset-102"]').click();
    const toast = page.locator('[data-testid="users-toast-msg"]');
    await expect(toast).toContainText("该用户没有邮箱，无法发送重置邮件");
  });
});