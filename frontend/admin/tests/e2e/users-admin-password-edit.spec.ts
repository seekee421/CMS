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

test.describe("Users - Admin Edit Password", () => {
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
            { id: 301, username: "charlie", roles: [{ name: "ROLE_USER" }], email: "charlie@example.com" },
          ],
          totalElements: 1,
          totalPages: 1,
          size: 10,
          number: 0,
          first: true,
          last: true,
          empty: false,
        }),
      });
    });

    await page.route("**/api/users/301/password", async (route) => {
      const json = await route.request().postDataJSON?.();
      expect(json?.newPassword?.length).toBeGreaterThanOrEqual(8);
      await route.fulfill({ status: 200, contentType: "application/json", body: JSON.stringify({ message: "密码已更新" }) });
    });
  });

  test("admin edits user password with confirm success", async ({ page }) => {
    await page.goto("/admin/users");
    await page.locator('[data-testid="user-action-edit-password-301"]').click();
    await page.locator('[data-testid="password-edit-new-input"]').fill("StrongP@ssw0rd");
    await page.locator('[data-testid="password-edit-confirm-input"]').fill("StrongP@ssw0rd");
    await page.locator('[data-testid="password-edit-save-button"]').click();
    const toast = page.locator('[data-testid="users-toast-msg"]');
    await expect(toast).toContainText("密码已更新");
  });
});