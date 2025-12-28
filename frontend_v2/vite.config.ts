
import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        secure: false,
      }
    }
  },
  define: {
    // 确保前端代码中访问 process.env.API_KEY 时不会报错
    // Vite 在构建时会自动替换这些标记
    'process.env.API_KEY': JSON.stringify(process.env.API_KEY),
    'process.env': {} 
  }
});
