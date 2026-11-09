import { expect, test } from '@playwright/test'

test('游客可以浏览首页并进入商品详情', async ({ page }) => {
  await page.goto('/home')

  await expect(page.getByText('网上商城').first()).toBeVisible()
  await expect(page.getByText('热销推荐')).toBeVisible()

  const firstCard = page.locator('.product-card').first()
  await expect(firstCard).toBeVisible()
  await firstCard.click()

  await expect(page).toHaveURL(/\/product\/\d+/)
  await expect(page.getByText('加入购物车')).toBeVisible()
})

test('领券中心可以打开', async ({ page }) => {
  await page.goto('/coupons')
  await expect(page.getByText('领券中心')).toBeVisible()
})
