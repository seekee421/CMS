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

test.describe("Users - Edit Roles & Batch Assign", () => {
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
            { id: 201, username: "alice", roles: [{ name: "ROLE_USER" }] },
            { id: 202, username: "bob", roles: [{ name: "ROLE_EDITOR" }] },
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

    await page.route("**/api/users/201/roles", async (route) => {
      const json = await route.request().postDataJSON?.();
      expect(json?.roles).toEqual(["ROLE_EDITOR", "ROLE_USER"]);
      await route.fulfill({ status: 200, contentType: "application/json", body: JSON.stringify({ message: "角色已更新" }) });
    });

    await page.route("**/api/users/batch/roles", async (route) => {
      const json = await route.request().postDataJSON?.();
      expect(json?.userIds).toEqual([201, 202]);
      expect(json?.roles).toEqual(["ROLE_ADMIN"]);
      await route.fulfill({ status: 200, contentType: "application/json", body: JSON.stringify({ message: "批量角色已分配" }) });
    });
  });

  test("edit single user's roles and see success toast", async ({ page }) => {
    await page.goto("/admin/users");
    await page.locator('[data-testid="user-action-edit-roles-201"]').click();
    await page.locator('[data-testid="role-edit-role-ROLE_USER"]').check();
    await page.locator('[data-testid="role-edit-role-ROLE_EDITOR"]').check();
    await page.locator('[data-testid="role-edit-save-button"]').click();
    const toast = page.locator('[data-testid="users-toast-msg"]');
    await expect(toast).toContainText("角色已更新");
  });

  test("batch assign roles for selected users", async ({ page }) => {
    await page.goto("/admin/users");
    await page.locator('[data-testid="user-select-201"]').check();
    await page.locator('[data-testid="user-select-202"]').check();
    await page.locator('[data-testid="batch-role-ROLE_ADMIN"]').check();
    await page.locator('[data-testid="batch-assign-submit-button"]').click();
    const toast = page.locator('[data-testid="users-toast-msg"]');
    await expect(toast).toContainText("批量角色已分配");
  });
});