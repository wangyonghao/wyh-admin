import { describe, expect, it } from 'vitest';

import { readRegistrationToken } from './registration-link';

describe('readRegistrationToken', () => {
  it('reads the fragment token and asks to clear it', () => {
    expect(readRegistrationToken('#t=abc_def-123')).toEqual({
      token: 'abc_def-123',
      shouldClear: true,
    });
  });

  it('returns an empty token when the fragment has none', () => {
    expect(readRegistrationToken('')).toEqual({ token: '', shouldClear: false });
    expect(readRegistrationToken('#other=1')).toEqual({ token: '', shouldClear: false });
  });
});
