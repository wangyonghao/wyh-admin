import { describe, expect, it } from 'vitest';

import { toPasswordLoginFields } from './login-identifier';

describe('toPasswordLoginFields', () => {
  it('sends an email on the existing email field', () => {
    expect(toPasswordLoginFields(' user@example.com ')).toEqual({
      email: 'user@example.com',
    });
  });

  it('keeps a short username on the username field', () => {
    expect(toPasswordLoginFields('abc')).toEqual({ username: 'abc' });
    expect(toPasswordLoginFields(' admin ')).toEqual({ username: ' admin ' });
  });
});
