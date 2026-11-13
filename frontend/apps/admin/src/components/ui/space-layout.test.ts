import { describe, expect, it } from 'vitest';

import { spaceClass, spaceStyle } from './space-layout';

describe('form space layout', () => {
  it('uses a wrapping row and the medium gap when size is omitted', () => {
    expect(spaceClass()).toBe('flex flex-row flex-wrap');
    expect(spaceStyle(undefined)).toEqual({
      columnGap: '12px',
      rowGap: '12px',
    });
  });

  it('maps direction, alignment, and named or numeric gaps', () => {
    expect(
      spaceClass({
        align: 'center',
        inline: true,
        justify: 'space-between',
        vertical: true,
        wrap: false,
      }),
    ).toBe('inline-flex flex-col flex-nowrap items-center justify-between');
    expect(spaceStyle('small')).toEqual({ columnGap: '8px', rowGap: '8px' });
    expect(spaceStyle('large')).toEqual({ columnGap: '16px', rowGap: '16px' });
    expect(spaceStyle(4)).toEqual({ columnGap: '4px', rowGap: '4px' });
    expect(spaceStyle([2, 10])).toEqual({ columnGap: '2px', rowGap: '10px' });
  });
});
