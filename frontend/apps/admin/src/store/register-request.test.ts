import { readFileSync } from 'node:fs';
import { dirname, resolve } from 'node:path';
import { fileURLToPath } from 'node:url';

import { describe, expect, it } from 'vitest';

import { toRegisterRequest } from './register-request';

const adminSrc = resolve(dirname(fileURLToPath(import.meta.url)), '..');

function source(relativePath: string) {
  return readFileSync(resolve(adminSrc, relativePath), 'utf8');
}

describe('toRegisterRequest', () => {
  it('submits the original password and the trimmed email', () => {
    const password = 'sample-pass-1a';
    const body = toRegisterRequest({
      email: ' user@example.com ',
      password,
    });

    expect(body).toEqual({ email: 'user@example.com', password });
    expect(body.password).toBe(password);
    expect(body).not.toHaveProperty('username');
  });

  it('leaves a long opaque string unchanged', () => {
    const opaque = 'A'.repeat(88);
    expect(toRegisterRequest({ email: 'user@example.com', password: opaque }).password).toBe(
      opaque,
    );
  });
});

describe('registration call scope', () => {
  it('store register uses the plaintext helper and does not call encryptByRsa', () => {
    const userStore = source('store/user.ts');
    const registerFn = userStore.slice(
      userStore.indexOf('async function register'),
      userStore.indexOf('async function socialLogin'),
    );
    const loginFn = userStore.slice(
      userStore.indexOf('async function login'),
      userStore.indexOf('async function register'),
    );

    expect(registerFn).toContain('toRegisterRequest(params)');
    expect(registerFn).not.toContain('completeLogin');
    expect(registerFn).not.toContain('encryptByRsa');
    expect(loginFn).not.toContain('encryptByRsa');
    expect(userStore).not.toContain("from '#/utils/crypto'");
  });

  it('the self-register page collects email and does not encrypt', () => {
    const page = source('views/_core/authentication/register.vue');
    expect(page).toContain('userStore.register(');
    expect(page).toContain('email');
    expect(page).not.toContain('邮箱暂不验证');
    expect(page).not.toContain('encryptByRsa');
    expect(page).not.toContain('USERNAME_WRITE_PATTERN');
  });

  it('keeps the shared RSA helper and the non-register callers', () => {
    expect(source('utils/crypto.ts')).toContain('export function encryptByRsa');
    expect(source('views/user/profile/components/security-settings.vue')).toContain(
      'encryptByRsa(',
    );
    expect(source('views/tenant/management/modules/form.vue')).toContain(
      'encryptByRsa(',
    );
    expect(source('views/_core/authentication/login.vue')).not.toContain(
      'encryptByRsa',
    );
  });
});
