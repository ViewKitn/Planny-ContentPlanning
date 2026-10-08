import { defineConfig } from 'vite';
import vue from '@vitejs/plugin-vue';
export default defineConfig({ plugins:[vue()], server:{port:5173, strictPort:true, proxy:{'/api':process.env.PLANNY_API_TARGET||'http://127.0.0.1:18080'}}, preview:{port:4173,proxy:{'/api':'http://127.0.0.1:18080'}} });
