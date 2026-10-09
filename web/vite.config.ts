import { fileURLToPath } from "node:url";
import { defineConfig } from "vitest/config";
import { svelte } from "@sveltejs/vite-plugin-svelte";
import { VitePWA } from "vite-plugin-pwa";

// GitHub Pages: /imposteur/ (default). Dominio proprio: BASE_PATH=/
const base = process.env.BASE_PATH ?? "/imposteur/";
const parole = fileURLToPath(new URL("../app/src/main/assets/parole.json", import.meta.url));
const radiceRepo = fileURLToPath(new URL("..", import.meta.url));

export default defineConfig({
  base,
  plugins: [
    svelte(),
    VitePWA({
      registerType: "autoUpdate",
      includeAssets: ["icona.svg"],
      workbox: { globPatterns: ["**/*.{js,css,html,svg,png,json,ico,woff2}"] },
      manifest: {
        name: "Imposteur",
        short_name: "Imposteur",
        lang: "it",
        display: "standalone",
        start_url: base,
        scope: base,
        background_color: "#141218",
        theme_color: "#141218",
        icons: [
          { src: "icona-192.png", sizes: "192x192", type: "image/png" },
          { src: "icona-512.png", sizes: "512x512", type: "image/png" },
          { src: "icona-maskable-512.png", sizes: "512x512", type: "image/png", purpose: "maskable" },
        ],
      },
    }),
  ],
  resolve: { alias: { $parole: parole } },
  // parole.json sta fuori da web/ (app/src/main/assets)
  server: { fs: { allow: [radiceRepo] } },
  test: {
    environment: "node",
    include: ["src/**/*.test.ts"],
  },
});
