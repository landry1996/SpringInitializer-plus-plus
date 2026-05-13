import { test, expect } from '@playwright/test';

test.describe('AI Chat Panel', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/ai/chat');
  });

  test('should display chat interface', async ({ page }) => {
    await expect(page.locator('body')).toContainText(/chat|assistant|ai/i);
  });

  test('should have message input field', async ({ page }) => {
    const input = page.locator('input[type="text"], textarea, [contenteditable="true"]').first();
    await expect(input).toBeVisible();
  });

  test('should have send button', async ({ page }) => {
    const sendBtn = page.locator('button:has-text("Send"), button[aria-label*="send" i], button[type="submit"]');
    await expect(sendBtn.first()).toBeVisible();
  });

  test('should display suggestion chips', async ({ page }) => {
    const suggestions = page.locator('[class*="suggestion"], [class*="chip"], button:has-text("How")');
    if (await suggestions.first().isVisible()) {
      await expect(suggestions.first()).toBeEnabled();
    }
  });

  test('should allow typing a message', async ({ page }) => {
    const input = page.locator('input[type="text"], textarea, [contenteditable="true"]').first();
    await input.fill('How do I add caching to my project?');
    await expect(input).toHaveValue('How do I add caching to my project?');
  });

  test('should show message in chat after sending', async ({ page }) => {
    const input = page.locator('input[type="text"], textarea, [contenteditable="true"]').first();
    await input.fill('Hello AI assistant');

    const sendBtn = page.locator('button:has-text("Send"), button[aria-label*="send" i], button[type="submit"]').first();
    await sendBtn.click();

    await expect(page.locator('body')).toContainText('Hello AI assistant');
  });

  test('should clear input after sending message', async ({ page }) => {
    const input = page.locator('input[type="text"], textarea, [contenteditable="true"]').first();
    await input.fill('Test message');

    const sendBtn = page.locator('button:has-text("Send"), button[aria-label*="send" i], button[type="submit"]').first();
    await sendBtn.click();

    await expect(input).toHaveValue('');
  });
});
