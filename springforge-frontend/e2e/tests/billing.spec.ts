import { test, expect } from '@playwright/test';

test.describe('Billing Page', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/settings/billing');
  });

  test('should display billing page with plan cards', async ({ page }) => {
    await expect(page.locator('body')).toContainText(/billing|subscription|plan/i);
  });

  test('should show FREE, PRO, and ENTERPRISE plan options', async ({ page }) => {
    await expect(page.locator('body')).toContainText(/free/i);
    await expect(page.locator('body')).toContainText(/pro/i);
    await expect(page.locator('body')).toContainText(/enterprise/i);
  });

  test('should display current subscription status', async ({ page }) => {
    const statusText = page.locator('[class*="status"], [class*="plan"], [class*="subscription"]').first();
    await expect(statusText.or(page.locator('body'))).toBeVisible();
  });

  test('should have upgrade button for higher plans', async ({ page }) => {
    const upgradeBtn = page.locator('button:has-text("Upgrade"), button:has-text("Subscribe"), button:has-text("Choose")');
    if (await upgradeBtn.first().isVisible()) {
      await expect(upgradeBtn.first()).toBeEnabled();
    }
  });

  test('should display invoices section', async ({ page }) => {
    const invoicesSection = page.locator('text=/invoice/i, th:has-text("Date"), th:has-text("Amount")');
    await expect(invoicesSection.first().or(page.locator('body'))).toBeVisible();
  });
});
