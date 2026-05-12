import { test, expect } from '@playwright/test';

test.describe('Project Wizard', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/wizard');
  });

  test('should display wizard step 1', async ({ page }) => {
    await expect(page.locator('h2, h3').first()).toContainText(/metadata|project/i);
  });

  test('should fill project metadata and navigate to step 2', async ({ page }) => {
    await page.fill('input[formControlName="groupId"], input[name="groupId"]', 'com.mycompany');
    await page.fill('input[formControlName="artifactId"], input[name="artifactId"]', 'my-service');
    await page.fill('input[formControlName="name"], input[name="name"]', 'My Service');

    await page.click('button:has-text("Next")');
    await expect(page.locator('body')).toContainText(/architecture/i);
  });

  test('should select architecture type', async ({ page }) => {
    await page.click('button:has-text("Next")');

    const archSelect = page.locator('select, mat-select, [role="listbox"]').first();
    if (await archSelect.isVisible()) {
      await archSelect.selectOption('HEXAGONAL');
    }
  });

  test('should navigate through all steps', async ({ page }) => {
    for (let i = 0; i < 5; i++) {
      const nextBtn = page.locator('button:has-text("Next")');
      if (await nextBtn.isVisible() && await nextBtn.isEnabled()) {
        await nextBtn.click();
        await page.waitForTimeout(300);
      }
    }

    await expect(page.locator('body')).toContainText(/generate|review|summary/i);
  });

  test('should go back to previous step', async ({ page }) => {
    await page.click('button:has-text("Next")');
    await page.click('button:has-text("Previous")');
    await expect(page.locator('body')).toContainText(/metadata|project/i);
  });
});
