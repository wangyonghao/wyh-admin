import { describe, expect, it } from 'vitest';

import { resolveSplitPanes } from './split-size';

describe('col page split size', () => {
  it('keeps percent panes unchanged', () => {
    expect(
      resolveSplitPanes({
        left: { width: 30, minWidth: 20, maxWidth: 35 },
        right: { width: 70 },
      }),
    ).toEqual({
      left: {
        collapsedSize: undefined,
        defaultSize: 30,
        maxSize: 35,
        minSize: 20,
        sizeUnit: '%',
      },
      right: {
        collapsedSize: undefined,
        defaultSize: 70,
        maxSize: undefined,
        minSize: undefined,
        sizeUnit: '%',
      },
    });
  });

  it('lets a percent pane fill beside a 200–320px pane', () => {
    expect(
      resolveSplitPanes({
        left: {
          width: 200,
          minWidth: 200,
          maxWidth: 320,
          sizeUnit: 'px',
        },
        right: { width: 70, sizeUnit: '%' },
      }),
    ).toEqual({
      left: {
        collapsedSize: undefined,
        defaultSize: 200,
        maxSize: 320,
        minSize: 200,
        sizeUnit: 'px',
      },
      right: {
        collapsedSize: undefined,
        defaultSize: undefined,
        maxSize: undefined,
        minSize: undefined,
        sizeUnit: '%',
      },
    });
  });

  it('keeps both defaults when both panes are pixels', () => {
    const panes = resolveSplitPanes({
      left: { width: 200, sizeUnit: 'px' },
      right: { width: 480, sizeUnit: 'px' },
    });
    expect(panes.left.defaultSize).toBe(200);
    expect(panes.right.defaultSize).toBe(480);
    expect(panes.left.sizeUnit).toBe('px');
    expect(panes.right.sizeUnit).toBe('px');
  });
});
