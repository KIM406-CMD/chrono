import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'
import { viteMockServe } from 'vite-plugin-mock'
import { fileURLToPath, URL } from 'node:url'

export default defineConfig(({ mode }) => ({
  plugins: [
    vue(),
    // Element Plus 按需引入，避免全量注册导致主包过大
    AutoImport({
      resolvers: [ElementPlusResolver()],
      dts: false,
    }),
    Components({
      resolvers: [ElementPlusResolver({ importStyle: false })],
      directives: true,
      dts: false,
    }),
    viteMockServe({
      mockPath: 'mock',
      // 仅 `vite --mode mock`（npm run dev:mock）时启用，普通 dev 仍走后端代理
      enable: mode === 'mock',
      ignore: /^\/_/,
    }),
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url)),
    },
  },
  build: {
    // ECharts 仅在报表页懒加载，单独 chunk 约 552 kB（gzip 约 188 kB）
    chunkSizeWarningLimit: 600,
  },
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
      // 聊天 WebSocket：浏览器连接同源 /chat/{userId}，由 Vite 转发到后端
      '/chat': {
        target: 'ws://localhost:8080',
        ws: true,
        changeOrigin: true,
      },
      // 本地图片上传模式的静态资源
      '/uploads': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
}))