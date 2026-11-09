import { defineConfig, devices } from '@playwright/test'

export default defineConfig({
  testDir: './e2e',
  timeout: 30_000,
  fullyParallel: false,
  use: {
    baseURL: 'http://localhost:5180',
    trace: 'on-first-retry',
  },
  webServer: {
    command: 'npm run dev:mock -- --port 5180 --strictPort',
    url: 'http://localhost:5180',
    reuseExistingServer: false,
    timeout: 120_000,
  },
  projects: [
    {
      name: 'chromium',
      use: { ...devices['Desktop Chrome'] },
    },
  ],
})
