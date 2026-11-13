import { describe, expect, it } from 'vitest';

import {
  commitFormNumber,
  displayFormNumber,
  numberBound,
  numberStep,
} from './number-value';

describe('form number empty value', () => {
  it('renders null as empty and keeps zero', () => {
    expect(displayFormNumber(null)).toBeUndefined();
    expect(displayFormNumber(undefined)).toBeUndefined();
    expect(displayFormNumber('')).toBeUndefined();
    expect(displayFormNumber(0)).toBe(0);
    expect(displayFormNumber('2.5')).toBe(2.5);
    expect(displayFormNumber(Number.NaN)).toBeUndefined();
  });

  it('writes null when cleared and writes the number otherwise', () => {
    expect(commitFormNumber(null)).toBeNull();
    expect(commitFormNumber(undefined)).toBeNull();
    expect(commitFormNumber('')).toBeNull();
    expect(commitFormNumber(0)).toBe(0);
    expect(commitFormNumber(3)).toBe(3);
  });

  it('derives a step from precision only when step is absent', () => {
    expect(numberStep(0.5, 2)).toBe(0.5);
    expect(numberStep(undefined, 2)).toBe(0.01);
    expect(numberStep(undefined, undefined)).toBeUndefined();
    expect(numberBound('1')).toBeUndefined();
    expect(numberBound(1)).toBe(1);
  });
});
