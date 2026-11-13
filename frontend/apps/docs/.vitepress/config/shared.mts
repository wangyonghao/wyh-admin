import type { PwaOptions } from '@vite-pwa/vitepress';
import type { HeadConfig } from 'vitepress';

import { resolve } from 'node:path';
import { fileURLToPath } from 'node:url';

import { lazyImport, VxeResolver } from 'vite-plugin-lazy-import';

async function viteVxeTableImportsPlugin() {
  return [
    lazyImport({
      resolvers: [
        VxeResolver({ libraryName: 'vxe-table' }),
        VxeResolver({ libraryName: 'vxe-pc-ui' }),
      ],
    }),
  ];
}

import {
  GitChangelog,
  GitChangelogMarkdownSection,
} from '@nolebase/vitepress-plugin-git-changelog/vite';
import tailwindcss from '@tailwindcss/vite';
import { defineConfig, postcssIsolateStyles } from 'vitepress';
import {
  groupIconMdPlugin,
  groupIconVitePlugin,
} from 'vitepress-plugin-group-icons';

import { installDocsSsrDom, uninstallDocsSsrDom } from '../ssr-dom';
import {
  createPopupAliases,
  createVbenAliases,
} from '../../../admin/vben.aliases.mts';
import { demoPreviewPlugin } from './plugins/demo-preview';
import { search as zhSearch } from './zh.mts';

export const shared = defineConfig({
  appearance: 'dark',
  buildEnd() {
    uninstallDocsSsrDom();
  },
  head: head(),
  markdown: {
    preConfig(md) {
      md.use(demoPreviewPlugin);
      md.use(groupIconMdPlugin);
    },
  },
  pwa: pwa(),
  srcDir: 'src',
  themeConfig: {
    i18nRouting: true,
    logo: '/logo.svg',
    search: {
      options: {
        locales: {
          ...zhSearch,
        },
      },
      provider: 'local',
    },
    siteTitle: 'wyh-admin',
    socialLinks: [
      { icon: 'github', link: 'https://github.com/vbenjs/vue-vben-admin' },
    ],
  },
  title: 'wyh-admin',
  vite: {
    build: {
      chunkSizeWarningLimit: Infinity,
      minify: 'terser',
    },
    css: {
      postcss: {
        plugins: [postcssIsolateStyles({ includeFiles: [/vp-doc\.css/] })],
      },
      preprocessorOptions: {
        scss: {
          api: 'modern',
        },
      },
    },
    json: {
      stringify: true,
    },
    plugins: [
      (() => {
        let ssrBuild = false;
        return {
          name: 'tide-docs-ssr-dom',
          apply: 'build' as const,
          configResolved(config: { build: { ssr?: boolean | string } }) {
            ssrBuild = Boolean(config.build.ssr);
          },
          closeBundle: {
            order: 'post' as const,
            sequential: true,
            handler() {
              // 客户端 bundle（含 PWA）先完成。只在 SSR bundle 结束、
              // 开始预渲染页面之前补 DOM，避免 service worker 拿到 http location。
              if (ssrBuild) installDocsSsrDom();
            },
          },
        };
      })(),
      tailwindcss(),
      GitChangelog({
        mapAuthors: [
          {
            mapByNameAliases: ['Vben'],
            name: 'vben',
            username: 'anncwb',
          },
          {
            name: 'vince',
            username: 'vince292007',
          },
          {
            name: 'Li Kui',
            username: 'likui628',
          },
        ],
        repoURL: () => 'https://github.com/vbenjs/vue-vben-admin',
      }),
      GitChangelogMarkdownSection(),
      groupIconVitePlugin(),
      await viteVxeTableImportsPlugin(),
    ],
    resolve: {
      alias: {
        ...createPopupAliases(),
        ...createVbenAliases(),
        'vue-json-viewer': fileURLToPath(
          new URL('../shims/vue-json-viewer.ts', import.meta.url),
        ),
      },
    },
    server: {
      fs: {
        allow: ['../../..'],
      },
      host: true,
      port: 6173,
    },

    ssr: {
      external: ['@vue/repl'],
    },
  },
});

function head(): HeadConfig[] {
  return [
    ['meta', { content: 'Vbenjs Team', name: 'author' }],
    [
      'meta',
      {
        content: 'vben, vitejs, vite, shacdn-ui, vue',
        name: 'keywords',
      },
    ],
    ['link', { href: '/favicon.ico', rel: 'icon', type: 'image/svg+xml' }],
    [
      'meta',
      {
        content:
          'width=device-width,initial-scale=1,minimum-scale=1.0,maximum-scale=1.0,user-scalable=no',
        name: 'viewport',
      },
    ],
    ['meta', { content: 'vben admin docs', name: 'keywords' }],
    ['link', { href: '/favicon.ico', rel: 'icon' }],
    // [
    //   'script',
    //   {
    //     src: 'https://cdn.tailwindcss.com',
    //   },
    // ],
  ];
}

function pwa(): PwaOptions {
  return {
    includeManifestIcons: false,
    manifest: {
      description:
        'Vben Admin is a modern admin dashboard template based on Vue 3. ',
      icons: [
        {
          sizes: '192x192',
          src: 'https://unpkg.com/@vbenjs/static-source@0.1.7/source/pwa-icon-192.png',
          type: 'image/png',
        },
        {
          sizes: '512x512',
          src: 'https://unpkg.com/@vbenjs/static-source@0.1.7/source/pwa-icon-512.png',
          type: 'image/png',
        },
      ],
      id: '/',
      name: 'Vben Admin Doc',
      short_name: 'vben_admin_doc',
      theme_color: '#ffffff',
    },
    outDir: resolve(process.cwd(), '.vitepress/dist'),
    registerType: 'autoUpdate',
    workbox: {
      globPatterns: ['**/*.{css,js,html,svg,png,ico,txt,woff2}'],
      maximumFileSizeToCacheInBytes: 5 * 1024 * 1024,
    },
  };
}
