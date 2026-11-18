import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173, // Your local React development port
    proxy: {
      // Intercepts all requests starting with /api
      '/api': {
        target: 'http://localhost:8080', // Routes traffic to your local Spring Boot
        changeOrigin: true,
        // CRITICAL: Strips '/api' from the URL path before sending to Spring Boot
        // e.g., '/api/login' becomes 'http://localhost:8080/login'
        rewrite: (path) => path.replace(/^\/api/, ''), 
      },
    },
  },
});
