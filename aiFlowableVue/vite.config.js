import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  server: {
    port: 3000,
    proxy: {
      // 配置代理，将 /api 路径代理到后端服务
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        secure: false,
        timeout: 60000, // 超时时间设置为60秒
        rewrite: (path) => path,
        configure: (proxy, options) => {
          // 记录代理请求日志
          proxy.on('proxyReq', (proxyReq, req, res) => {
            console.log(`[Proxy] ${req.method} ${req.url} -> ${options.target}${req.url}`);
          });
          
          // 记录代理响应日志
          proxy.on('proxyRes', (proxyRes, req, res) => {
            console.log(`[Proxy] ${req.method} ${req.url} <- ${proxyRes.statusCode}`);
          });
          
          // 记录代理错误
          proxy.on('error', (err, req, res) => {
            console.error(`[Proxy Error] ${err.message}`);
            res.statusCode = 502;
            res.end('Proxy error: ' + err.message);
          });
        }
      }
    }
  }
})