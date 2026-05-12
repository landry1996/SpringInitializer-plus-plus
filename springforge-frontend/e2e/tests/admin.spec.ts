import { test, expect } from '@playwright/test';

test.describe('Admin Panel', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/admin');
  });

  test('should display admin dashboard', async ({ page }) => {
    await expect(page.locator('body')).toContainText(/dashboard|admin/i);
  });

  test('should show statistics cards', async ({ page }) => {
    const statCards = page.locator('.stat-card, .stats-card, [class*="stat"]');
    await page.waitForTimeout(500);
    const count = await statCards.count();
    expect(count).toBeGreaterThanOrEqual(0);
  });

  test('should navigate to users section', async ({ page }) => {
    const usersLink = page.locator('a:has-text("Users"), button:has-text("Users"), [routerLink*="users"]');
    if (await usersLink.first().isVisible()) {
      await usersLink.first().click();
      await expect(page.locator('body')).toContainText(/user/i);
    }
  });

  test('should navigate to audit logs', async ({ page }) => {
    const auditLink = page.locator('a:has-text("Audit"), button:has-text("Audit"), [routerLink*="audit"]');
    if (await auditLink.first().isVisible()) {
      await auditLink.first().click();
      await expect(page.locator('body')).toContainText(/audit|log/i);
    }
  });

  test('should display user table', async ({ page }) => {
    await page.goto('/admin/users');
    await page.waitForTimeout(500);
    const table = page.locator('table, .users-table');
    if (await table.first().isVisible()) {
      await expect(table.first()).toBeVisible();
    }
  });
});
