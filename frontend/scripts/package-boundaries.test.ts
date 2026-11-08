import { readdirSync, readFileSync, statSync } from 'node:fs';
import path from 'node:path';

import { describe, expect, it } from 'vitest';

const frontendRoot = path.resolve(import.meta.dirname, '..');

const sourceExtensions = new Set([
  '.css',
  '.js',
  '.json',
  '.mjs',
  '.mts',
  '.scss',
  '.ts',
  '.tsx',
  '.vue',
  '.yaml',
  '.yml',
]);

const skippedDirectories = new Set([
  '.git',
  '.turbo',
  'coverage',
  'dist',
  'node_modules',
]);

/** 实践笔记需要写出旧路径，门禁不扫这篇。 */
const allowedOldPathFiles = new Set([
  path.normalize('docs/architecture/frontend-packages.md'),
]);

const removedSpecifiers = [
  '@wyh/layout-ui',
  '@wyh/menu-ui',
  '@wyh/tabs-ui',
  '@wyh/typings',
  'packages/core',
  'packages/popup-ui',
  'packages/shell',
  '@wyh/popup-ui',
];

const packageImportBans: Array<{ from: string; specifiers: string[] }> = [
  {
    from: 'packages/ui',
    specifiers: [
      '@vben/',
      '@wyh/form-ui',
      '#/components/popup',
      '@wyh/shared/global-state',
      'apps/admin',
    ],
  },
  {
    from: 'packages/ui-patterns',
    specifiers: [
      '@vben/',
      '@wyh/form-ui',
      '#/components/popup',
      '@wyh/shared/global-state',
      'apps/admin',
    ],
  },
  {
    from: 'packages/shared',
    specifiers: [
      '@vben/',
      '@wyh/form-ui',
      '#/components/popup',
      '@wyh/ui',
      '@wyh/ui-patterns',
      'apps/',
    ],
  },
  {
    from: 'packages/composables',
    specifiers: [
      '@vben/',
      '@wyh/form-ui',
      '#/components/popup',
      '@wyh/ui',
      '@wyh/ui-patterns',
      'apps/',
    ],
  },
  {
    from: 'packages/icons',
    specifiers: [
      '@vben/',
      '@wyh/form-ui',
      '#/components/popup',
      '@wyh/ui',
      '@wyh/ui-patterns',
      'apps/',
    ],
  },
  {
    from: 'packages/design',
    specifiers: [
      '@vben/',
      '@wyh/form-ui',
      '#/components/popup',
      '@wyh/ui',
      '@wyh/ui-patterns',
      'apps/',
    ],
  },
  {
    from: 'packages/form-ui',
    specifiers: ['@vben/', '#/components/popup', 'apps/'],
  },
];

function walk(directory: string, files: string[] = []) {
  for (const entry of readdirSync(directory)) {
    if (skippedDirectories.has(entry)) {
      continue;
    }
    const fullPath = path.join(directory, entry);
    const stats = statSync(fullPath);
    if (stats.isDirectory()) {
      walk(fullPath, files);
      continue;
    }
    if (sourceExtensions.has(path.extname(entry))) {
      files.push(fullPath);
    }
  }
  return files;
}

function relative(file: string) {
  return path.relative(frontendRoot, file);
}

function importSpecifiers(source: string) {
  const specifiers: string[] = [];
  const pattern = /(?:import|export)\s+(?:type\s+)?(?:[^'";]*?\s+from\s+)?['"]([^'"]+)['"]/g;
  for (const match of source.matchAll(pattern)) {
    const specifier = match[1];
    if (specifier) {
      specifiers.push(specifier);
    }
  }
  return specifiers;
}

describe('frontend package boundaries', () => {
  const files = walk(frontendRoot);

  it('does not keep the old core/shell directories', () => {
    expect(files.some((file) => relative(file).startsWith('packages/core'))).toBe(
      false,
    );
    expect(files.some((file) => relative(file).startsWith('packages/shell'))).toBe(
      false,
    );
  });

  it('does not leave removed package paths in source', () => {
    const hits: string[] = [];
    for (const file of files) {
      const rel = relative(file);
      if (
        rel === 'pnpm-lock.yaml' ||
        rel.endsWith('package-boundaries.test.ts') ||
        allowedOldPathFiles.has(path.normalize(rel))
      ) {
        continue;
      }
      const source = readFileSync(file, 'utf8');
      for (const specifier of removedSpecifiers) {
        if (source.includes(specifier)) {
          hits.push(`${rel}: ${specifier}`);
        }
      }
    }
    expect(hits).toEqual([]);
  });

  it('keeps shared and ui from depending on the app shell or each other upward', () => {
    const hits: string[] = [];
    for (const rule of packageImportBans) {
      const prefix = `${rule.from}${path.sep}`;
      for (const file of files) {
        const rel = relative(file);
        if (!rel.startsWith(prefix) || rel.endsWith('package.json')) {
          continue;
        }
        const specifiers = importSpecifiers(readFileSync(file, 'utf8'));
        for (const specifier of specifiers) {
          // Tailwind 入口仍在应用的 theme.css。design / 原子用 @reference 指向它，不把它当成脚本依赖。
          if (
            specifier === '@vben/tailwind-config' ||
            specifier === '@vben/tailwind-config/theme'
          ) {
            continue;
          }
          if (rule.specifiers.some((banned) => specifier.includes(banned))) {
            hits.push(`${rel} -> ${specifier}`);
          }
        }
      }
    }
    expect(hits).toEqual([]);
  });
});
