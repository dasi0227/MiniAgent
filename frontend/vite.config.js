import { defineConfig } from 'vite';
import vue from '@vitejs/plugin-vue';

// https://vite.dev/config/
export default defineConfig({
    // Serve the frontend under /miniagent/ on nginx.
    base: '/miniagent/',
    plugins: [vue()]
});
