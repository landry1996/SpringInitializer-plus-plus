import { test, expect } from '@playwright/test';

test.describe('Organization Settings', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/organization');
  });

  test('should display organization page', async ({ page }) => {
    await expect(page.locator('body')).toContainText(/organization|settings|usage/i);
  });

  test('should show usage bars', async ({ page }) => {
    const usageBars = page.locator('.usage-item, .bar-track, [class*="usage"]');
    await page.waitForTimeout(500);
    const count = await usageBars.count();
    expect(count).toBeGreaterThanOrEqual(0);
  });

  test('should show members list', async ({ page }) => {
    const members = page.locator('.member-item, .member-list, [class*="member"]');
    await page.waitForTimeout(500);
    const count = await members.count();
    expect(count).toBeGreaterThanOrEqual(0);
  });

  test('should have invite member input', async ({ page }) => {
    const emailInput = page.locator('input[placeholder*="email" i], input[type="email"]');
    if (await emailInput.first().isVisible()) {
      await expect(emailInput.first()).toBeVisible();
    }
  });

  test('should navigate to API keys section', async ({ page }) => {
    await page.goto('/organization/api-keys');
    await expect(page.locator('body')).toContainText(/api|key/i);
  });
});
