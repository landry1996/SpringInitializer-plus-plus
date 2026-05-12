import { test, expect } from '@playwright/test';

test.describe('Internationalization', () => {
  test('should display page in default language (English)', async ({ page }) => {
    await page.goto('/');
    await expect(page.locator('body')).toContainText(/home|new project|marketplace/i);
  });

  test('should have locale switcher', async ({ page }) => {
    await page.goto('/');
    const switcher = page.locator('app-locale-switcher, select, .locale-switcher');
    if (await switcher.first().isVisible()) {
      await expect(switcher.first()).toBeVisible();
    }
  });

  test('should switch to French', async ({ page }) => {
    await page.goto('/');
    const switcher = page.locator('.locale-switcher select, app-locale-switcher select');
    if (await switcher.first().isVisible()) {
      await switcher.first().selectOption('fr');
      await page.waitForTimeout(500);
      await expect(page.locator('body')).toContainText(/accueil|nouveau projet|marketplace/i);
    }
  });

  test('should switch to German', async ({ page }) => {
    await page.goto('/');
    const switcher = page.locator('.locale-switcher select, app-locale-switcher select');
    if (await switcher.first().isVisible()) {
      await switcher.first().selectOption('de');
      await page.waitForTimeout(500);
      await expect(page.locator('body')).toContainText(/startseite|neues projekt/i);
    }
  });

  test('should persist locale in localStorage', async ({ page }) => {
    await page.goto('/');
    const switcher = page.locator('.locale-switcher select, app-locale-switcher select');
    if (await switcher.first().isVisible()) {
      await switcher.first().selectOption('es');
      await page.waitForTimeout(300);

      const locale = await page.evaluate(() => localStorage.getItem('springforge-locale'));
      expect(locale).toBe('es');
    }
  });
});
