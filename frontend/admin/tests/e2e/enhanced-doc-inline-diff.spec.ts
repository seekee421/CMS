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

// 增强编辑页：版本比较与内联词级高亮（使用网络拦截进行契约一致的 mock，不破坏后端权限）
test("Enhanced documents page renders inline word-level diff using fast-diff", async ({ page }) => {
  await page.context().addCookies([buildSessionCookie(["ROLE_ADMIN"])]);

  const baseContent = [
    "Hello world",
    "This is a test line.",
    "Another line.",
  ].join("\n");
  const compareContent = [
    "Hello brave new world",
    "This is a testing line!",
    "Another line added.",
  ].join("\n");

  // 统一契约的响应结构（与前端类型一致）：auth、detail 与 versions
  await page.route("**/api/auth/me", async (route) => {
    await route.fulfill({
      status: 200,
      contentType: "application/json",
      body: JSON.stringify({ roles: ["ROLE_ADMIN"], user: { id: 1, username: "admin" } }),
    });
  });

  await page.route("**/api/documents/101", async (route) => {
    await route.fulfill({
      status: 200,
      contentType: "application/json",
      body: JSON.stringify({ id: 101, title: "示例文档", content: baseContent, summary: "", categoryId: 1 }),
    });
  });

  // 注意：版本列表接口可能附带查询字符串，扩大匹配范围
  await page.route("**/api/documents/101/versions**", async (route) => {
    await route.fulfill({
      status: 200,
      contentType: "application/json",
      body: JSON.stringify([
        { id: 1, tag: "v1", createdAt: "2024-01-01T00:00:00Z", content: baseContent },
        { id: 2, tag: "v2", createdAt: "2024-02-01T00:00:00Z", content: compareContent },
      ]),
    });
  });

  // 若页面会拉取某一版本详情（例如选中对比版本），也进行拦截
  await page.route("**/api/documents/101/versions/1", async (route) => {
    await route.fulfill({ status: 200, contentType: "application/json", body: JSON.stringify({ id: 1, tag: "v1", content: baseContent }) });
  });
  await page.route("**/api/documents/101/versions/2", async (route) => {
    await route.fulfill({ status: 200, contentType: "application/json", body: JSON.stringify({ id: 2, tag: "v2", content: compareContent }) });
  });

  await page.goto("/admin/documents/101/enhanced");

  // 等待版本列表加载并选择基准与对比版本
  await page.locator('input[name="baseVersion"]').first().waitFor();
  await page.locator('input[name="baseVersion"]').first().check();
  await page.locator('input[name="compareVersion"]').nth(1).check();

  // 切换到“内联高亮”模式并触发比较
  await page.getByTestId('diff-mode-select').selectOption('inline');
  await page.getByTestId('compare-button').click();

  // 直接等待容器出现
  const container = page.getByTestId("inline-diff-container");
  await expect(container).toBeVisible();

  // 断言：至少一行出现词级插入/删除高亮
  const firstLine = page.getByTestId(/inline-diff-line-\d+/).first();
  await expect(firstLine).toBeVisible();

  // 插入（绿色）与删除（红色，带删除线）至少存在一个
  const inserted = page.locator("span.bg-green-200");
  const deleted = page.locator("span.bg-red-200.line-through");
  const insertedCount = await inserted.count();
  const deletedCount = await deleted.count();
  expect(insertedCount).toBeGreaterThan(0);
  expect(deletedCount).toBeGreaterThan(0);
});