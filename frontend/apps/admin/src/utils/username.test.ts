import { readFileSync } from 'node:fs';
import { dirname, resolve } from 'node:path';
import { fileURLToPath } from 'node:url';

import { describe, expect, it } from 'vitest';

import {
  acceptsLoginUsername,
  acceptsWriteUsername,
} from './username';

const adminSrc = resolve(dirname(fileURLToPath(import.meta.url)), '..');

function source(relativePath: string) {
  return readFileSync(resolve(adminSrc, relativePath), 'utf8');
}

function sample(length: number) {
  return `a${'b'.repeat(length - 1)}`;
}

describe('write username length', () => {
  it('rejects 3 and 65, accepts 4 and 64', () => {
    expect(acceptsWriteUsername(sample(3))).toBe(false);
    expect(acceptsWriteUsername(sample(4))).toBe(true);
    expect(acceptsWriteUsername(sample(64))).toBe(true);
    expect(acceptsWriteUsername(sample(65))).toBe(false);
  });

  it('rejects null, blank, and empty', () => {
    expect(acceptsWriteUsername(null)).toBe(false);
    expect(acceptsWriteUsername(undefined)).toBe(false);
    expect(acceptsWriteUsername('')).toBe(false);
    expect(acceptsWriteUsername('   ')).toBe(false);
  });

  it('keeps the existing character class', () => {
    expect(acceptsWriteUsername('ab_d')).toBe(true);
    expect(acceptsWriteUsername('Abcd')).toBe(true);
    expect(acceptsWriteUsername('1abc')).toBe(false);
    expect(acceptsWriteUsername('ab-d')).toBe(false);
  });
});

describe('login username compatibility', () => {
  it('accepts a 3-character username and rejects only empty values', () => {
    expect(acceptsLoginUsername(sample(3))).toBe(true);
    expect(acceptsLoginUsername('a')).toBe(true);
    expect(acceptsLoginUsername('   ')).toBe(true);
    expect(acceptsLoginUsername('')).toBe(false);
    expect(acceptsLoginUsername(null)).toBe(false);
    expect(acceptsLoginUsername(undefined)).toBe(false);
  });
});

describe('username form wiring', () => {
  it('uses the shared write pattern on user create and tenant admin', () => {
    expect(source('views/system/user/components/user-edit-drawer.vue')).toContain(
      'USERNAME_WRITE_PATTERN',
    );
    expect(source('views/tenant/management/data.ts')).toContain(
      'USERNAME_WRITE_PATTERN',
    );
  });

  it('keeps login and user edit off the 4-character minimum', () => {
    const login = source('views/_core/authentication/login.vue');
    const editor = source('views/system/user/components/user-edit-drawer.vue');
    expect(login).toContain('acceptsLoginUsername');
    expect(login).not.toContain('USERNAME_WRITE_PATTERN');
    expect(editor).toContain('acceptsLoginUsername');
    expect(editor).toContain('isUpdate.value');
  });
});
