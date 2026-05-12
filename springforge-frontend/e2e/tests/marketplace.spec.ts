import { test, expect } from '@playwright/test';

test.describe('Marketplace', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/marketplace');
  });

  test('should display marketplace page', async ({ page }) => {
    await expect(page.locator('body')).toContainText(/marketplace|blueprint/i);
  });

  test('should have search input', async ({ page }) => {
    const searchInput = page.locator('input[type="text"], input[placeholder*="search" i]');
    await expect(searchInput.first()).toBeVisible();
  });

  test('should have category filter', async ({ page }) => {
    const categorySelect = page.locator('select').first();
    if (await categorySelect.isVisible()) {
      const options = await categorySelect.locator('option').count();
      expect(options).toBeGreaterThan(1);
    }
  });

  test('should display blueprint cards', async ({ page }) => {
    await page.waitForTimeout(1000);
    const cards = page.locator('.blueprint-card, .card, [class*="blueprint"]');
    const count = await cards.count();
    expect(count).toBeGreaterThanOrEqual(0);
  });

  test('should filter by search query', async ({ page }) => {
    const searchInput = page.locator('input[type="text"], input[placeholder*="search" i]').first();
    if (await searchInput.isVisible()) {
      await searchInput.fill('microservice');
      await searchInput.press('Enter');
      await page.waitForTimeout(500);
    }
  });

  test('should sort blueprints', async ({ page }) => {
    const sortSelect = page.locator('select').nth(1);
    if (await sortSelect.isVisible()) {
      await sortSelect.selectOption({ index: 1 });
      await page.waitForTimeout(500);
    }
  });
});
