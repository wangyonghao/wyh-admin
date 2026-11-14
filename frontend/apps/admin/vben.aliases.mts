import { fileURLToPath } from 'node:url';

/** Absolute path under frontend/ (packages and vendor stay at the workspace root). */
export function frontendPath(...parts: string[]) {
  return fileURLToPath(new URL(`../../${parts.join('/')}`, import.meta.url));
}

/** Absolute path under frontend/apps/admin/. */
export function adminPath(...parts: string[]) {
  return fileURLToPath(new URL(`./${parts.join('/')}`, import.meta.url));
}

/**
 * `#/ui` and `#/ui-patterns` stay as app import paths for this release.
 * The source of truth is `@wyh/ui` and `@wyh/ui-patterns`.
 * `#/ui-patterns` and project-owned `#/ui` entries must be matched before
 * the shadcn-vue atoms.
 */
const ownedUiEntries = [
  'composed',
  'date-picker',
  'lib',
  'switch',
  'tree',
  'upload',
] as const;

export function createTideUiAliases() {
  const uiSrc = frontendPath('packages/ui/src');
  return [
    {
      find: /^#\/ui-patterns(?=\/|$)/,
      replacement: frontendPath('packages/ui-patterns/src'),
    },
    {
      find: /^#\/ui\/switch\/switch-root$/,
      replacement: frontendPath('packages/ui/src/switch/switch-root.vue'),
    },
    // Rolldown 不会把 replacement 里的 $1 换成捕获组，会去加载字面路径 src/$1。
    ...ownedUiEntries.flatMap((entry) => [
      {
        find: `#/ui/${entry}/`,
        replacement: `${uiSrc}/${entry}/`,
      },
      {
        find: `#/ui/${entry}`,
        replacement: `${uiSrc}/${entry}`,
      },
    ]),
    {
      find: /^#\/ui(?=\/|$)/,
      replacement: `${uiSrc}/component/shadcn-vue/ui`,
    },
  ];
}

/** Absolute path under frontend/apps/admin/src/components/vben/<pkg>/... */
export function vbenSrc(...parts: string[]) {
  return adminPath('src/components/vben', ...parts);
}

/**
 * Maps former @vben/* workspace packages to apps/admin/src/components/vben.
 * More-specific subpaths must come before the package root entry.
 */
export function createVbenAliases() {
  return {
    '@vben/tailwind-config/theme': adminPath('src/styles/theme.css'),
    '@vben/tailwind-config': adminPath('src/styles/theme.css'),

    '@vben/styles/naive': vbenSrc('styles/naive/index.css'),
    '@vben/styles': vbenSrc('styles/index.ts'),

    '@vben/plugins/echarts': vbenSrc('plugins/echarts/index.ts'),
    '@vben/plugins/tiptap': vbenSrc('plugins/tiptap/index.ts'),
    '@vben/plugins/vxe-table': vbenSrc('plugins/vxe-table/index.ts'),
    '@vben/plugins/motion': vbenSrc('plugins/motion/index.ts'),
    '@vben/plugins': vbenSrc('plugins/index.ts'),

    '@vben/common-ui/es/tippy': vbenSrc('common-ui/components/tippy/index.ts'),
    '@vben/common-ui/es/loading': vbenSrc(
      'common-ui/components/loading/index.ts',
    ),
    '@vben/common-ui': vbenSrc('common-ui/index.ts'),

    '@vben/types/global': vbenSrc('types/global.d.ts'),
    '@vben/types': vbenSrc('types/index.ts'),

    '@vben/constants': vbenSrc('constants/index.ts'),
    '@vben/utils': vbenSrc('utils/index.ts'),
    '@vben/icons': vbenSrc('icons/index.ts'),
    '@vben/stores': vbenSrc('stores/index.ts'),
    '@vben/preferences': vbenSrc('preferences/index.ts'),
    '@vben/locales': vbenSrc('locales/index.ts'),
    '@vben/hooks': vbenSrc('hooks/index.ts'),
    '@vben/layouts': vbenSrc('layouts/index.ts'),
  } as const;
}

/**
 * Docs `#/*` points at apps/docs/src/_env, so these explicit entries
 * let documentation demos import the admin popup runtime by subpath.
 * Admin already resolves them via `#/*` -> `./src/*`.
 */
export function createPopupAliases() {
  return {
    '#/components/popup/alert': adminPath(
      'src/components/popup/alert/index.ts',
    ),
    '#/components/popup/drawer': adminPath(
      'src/components/popup/drawer/index.ts',
    ),
    '#/components/popup/modal': adminPath(
      'src/components/popup/modal/index.ts',
    ),
  } as const;
}
