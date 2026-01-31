import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'   // ✅ 新增

export default defineConfig({
  plugins: [vue()],

  // ✅ 关键：配置 @ 别名
  resolve: {
    alias: {
      '@': path.resolve(__dirname, 'src')
    }
  },

  server: {
    host: true,
    allowedHosts: [
      'dhy721.dpdns.org',
      'localhost',
      '127.0.0.1'
    ],
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, '')
      }
    }
  }
})
