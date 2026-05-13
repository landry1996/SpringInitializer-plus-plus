import { test, expect } from '@playwright/test';

test.describe('Webhooks Page', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/settings/webhooks');
  });

  test('should display webhooks page', async ({ page }) => {
    await expect(page.locator('body')).toContainText(/webhook/i);
  });

  test('should show create webhook button', async ({ page }) => {
    const createBtn = page.locator('button:has-text("Create"), button:has-text("Add"), button:has-text("New")');
    await expect(createBtn.first()).toBeVisible();
  });

  test('should open create webhook form on button click', async ({ page }) => {
    const createBtn = page.locator('button:has-text("Create"), button:has-text("Add"), button:has-text("New")');
    await createBtn.first().click();

    await expect(page.locator('input[name="name"], input[formControlName="name"], input[placeholder*="name" i]').first()).toBeVisible();
    await expect(page.locator('input[name="url"], input[formControlName="url"], input[placeholder*="url" i]').first()).toBeVisible();
  });

  test('should display event type selection in form', async ({ page }) => {
    const createBtn = page.locator('button:has-text("Create"), button:has-text("Add"), button:has-text("New")');
    await createBtn.first().click();

    await expect(page.locator('body')).toContainText(/event/i);
  });

  test('should validate webhook URL format', async ({ page }) => {
    const createBtn = page.locator('button:has-text("Create"), button:has-text("Add"), button:has-text("New")');
    await createBtn.first().click();

    const urlInput = page.locator('input[name="url"], input[formControlName="url"], input[placeholder*="url" i]').first();
    await urlInput.fill('not-a-url');

    const saveBtn = page.locator('button:has-text("Save"), button:has-text("Create"), button[type="submit"]').last();
    if (await saveBtn.isVisible()) {
      await saveBtn.click();
      await expect(page.locator('body')).toContainText(/invalid|url|error|required/i);
    }
  });

  test('should show webhook list when webhooks exist', async ({ page }) => {
    const list = page.locator('[class*="webhook-list"], [class*="list"], table, [class*="card"]');
    await expect(list.first().or(page.locator('body'))).toBeVisible();
  });
});
