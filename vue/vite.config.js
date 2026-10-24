import { fileURLToPath, URL } from 'node:url'
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
// Load global UI styles once, avoiding dependency rediscovery on navigation.
export default defineConfig({
  plugins: [vue()],
  server: { proxy: { '/api': { target: 'http://127.0.0.1:9090', changeOrigin: true, rewrite: path => path.replace(/^\/api/, '') } } },
  resolve: { alias: { '@': fileURLToPath(new URL('./src', import.meta.url)) } },
  optimizeDeps: { include: ['element-plus', '@element-plus/icons-vue', 'dayjs', 'html2canvas', 'echarts/core', 'echarts/charts', 'echarts/components', 'echarts/renderers'] }
})
