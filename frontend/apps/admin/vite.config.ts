import type { Plugin, PluginOption, UserConfig } from 'vite';

import fs from 'node:fs';
import fsp from 'node:fs/promises';
import path from 'node:path';
import process from 'node:process';

import viteVueI18nPlugin from '@intlify/unplugin-vue-i18n/vite';
import tailwindcss from '@tailwindcss/vite';
import viteVue from '@vitejs/plugin-vue';
import viteVueJsx from '@vitejs/plugin-vue-jsx';
import archiver from 'archiver';
import { minify } from 'html-minifier-terser';
import { defineConfig, loadEnv } from 'vite';
import { lazyImport, VxeResolver } from 'vite-plugin-lazy-import';

import { createTideUiAliases, createVbenAliases } from './vben.aliases.mts';

function readEnvFlag(env: Record<string, string>, key: string) {
  return env[key] === 'true';
}

function viteTailwindReferencePlugin(): Plugin {
  const referenceLine = '@reference "@vben/tailwind-config/theme";\n';
  return {
    enforce: 'pre',
    name: 'vite:tailwind-reference',
    transform(code, id) {
      if (!id.includes('.vue') || !id.includes('type=style')) {
        return null;
      }
      if (code.includes('@reference') || !code.includes('@apply')) {
        return null;
      }
      return { code: referenceLine + code, map: null };
    },
  };
}

function viteHtmlMinPlugin(): PluginOption {
  return {
    name: 'vite:html-minify',
    transformIndexHtml: {
      order: 'post',
      async handler(html, ctx) {
        if (!ctx.bundle) {
          return html;
        }
        return await minify(html, {
          collapseWhitespace: true,
          minifyCSS: true,
          minifyJS: true,
          removeComments: true,
          removeRedundantAttributes: true,
          removeScriptTypeAttributes: true,
          removeStyleLinkTypeAttributes: true,
          useShortDoctype: true,
        });
      },
    },
  };
}

function viteArchiverPlugin(): PluginOption {
  return {
    apply: 'build',
    closeBundle: {
      order: 'post',
      handler() {
        setTimeout(async () => {
          const zipPath = path.join(process.cwd(), 'dist.zip');
          await new Promise<void>((resolve, reject) => {
            const output = fs.createWriteStream(zipPath);
            const archive = archiver('zip', { zlib: { level: 9 } });
            output.on('close', () => {
              console.log(
                `ZIP file created: ${zipPath} (${archive.pointer()} total bytes)`,
              );
              resolve();
            });
            archive.on('error', reject);
            archive.pipe(output);
            archive.directory('dist', false);
            archive.finalize();
          });
          console.log(`Folder has been zipped to: ${zipPath}`);
        }, 0);
      },
    },
    enforce: 'post',
    name: 'vite:archiver',
  };
}

async function viteInjectAppLoadingPlugin(
  isBuild: boolean,
  env: Record<string, string>,
): Promise<PluginOption | undefined> {
  const loadingPath = path.join(process.cwd(), 'loading.html');
  if (!fs.existsSync(loadingPath)) {
    return;
  }
  const loadingHtml = await fsp.readFile(loadingPath, 'utf8');
  const pkg = JSON.parse(
    await fsp.readFile(path.join(process.cwd(), 'package.json'), 'utf8'),
  ) as { version?: string };
  const cacheName = `'${env.VITE_APP_NAMESPACE}-${pkg.version ?? '0'}-${isBuild ? 'prod' : 'dev'}-preferences-theme'`;
  const injectScript = `
  <script data-app-loading="inject-js">
  var theme = localStorage.getItem(${cacheName});
  document.documentElement.classList.toggle('dark', /dark/.test(theme));
  </script>
`;
  return {
    enforce: 'pre',
    name: 'vite:inject-app-loading',
    transformIndexHtml: {
      order: 'pre',
      handler(html) {
        return html.replace(/<body\s*>/, `<body>${injectScript}${loadingHtml}`);
      },
    },
  };
}

export default defineConfig(async ({ command, mode }) => {
  const root = process.cwd();
  const env = loadEnv(mode, root);
  const isBuild = command === 'build';
  const port = Number(env.VITE_PORT) || 5888;
  const base = env.VITE_BASE || '/';
  const injectLoading = env.VITE_INJECT_APP_LOADING !== 'false';
  const enableArchiver = readEnvFlag(env, 'VITE_ARCHIVER');

  const plugins: PluginOption[] = [
    viteVue({ script: { defineModel: true } }),
    viteVueJsx(),
    viteTailwindReferencePlugin(),
    tailwindcss(),
    viteVueI18nPlugin({ fullInstall: true, runtimeOnly: true }),
    lazyImport({
      resolvers: [
        VxeResolver({ libraryName: 'vxe-table' }),
        VxeResolver({ libraryName: 'vxe-pc-ui' }),
      ],
    }),
    viteHtmlMinPlugin(),
  ];

  if (injectLoading) {
    const loadingPlugin = await viteInjectAppLoadingPlugin(isBuild, env);
    if (loadingPlugin) {
      plugins.push(loadingPlugin);
    }
  }
  if (enableArchiver) {
    plugins.push(viteArchiverPlugin());
  }

  const config: UserConfig = {
    base,
    build: {
      chunkSizeWarningLimit: 2000,
      reportCompressedSize: false,
      rolldownOptions: {
        output: {
          assetFileNames: '[ext]/[name]-[hash].[ext]',
          chunkFileNames: 'js/[name]-[hash].js',
          entryFileNames: 'jse/index-[name]-[hash].js',
          minify: isBuild
            ? { compress: { dropDebugger: true } }
            : false,
        },
      },
      sourcemap: false,
      target: 'es2015',
    },
    plugins,
    resolve: {
      alias: [
        ...createTideUiAliases(),
        ...Object.entries(createVbenAliases()).map(([find, replacement]) => ({
          find,
          replacement,
        })),
      ],
    },
    server: {
      fs: {
        // packages/ and vendor/ live above this app.
        allow: ['../..'],
      },
      host: true,
      port,
      proxy: {
        [env.VITE_API_PREFIX]: {
          changeOrigin: true,
          rewrite: (p) =>
            p.replace(new RegExp(`^${env.VITE_API_PREFIX}`), ''),
          target: env.VITE_GLOB_API_URL,
          ws: true,
        },
      },
      warmup: {
        clientFiles: [
          './index.html',
          './src/bootstrap.ts',
          './src/{views,layouts,router,store,api,adapter}/*',
        ],
      },
    },
  };

  return config;
});
