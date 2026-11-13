import { describe, expect, it } from 'vitest';

import {
  commitUploadFiles,
  displayUploadFiles,
  nextUploadList,
  removeUploadFile,
} from './upload-value';

describe('form upload empty value', () => {
  it('shows null as an empty list and writes null when nothing is left', () => {
    expect(displayUploadFiles(null)).toEqual([]);
    expect(displayUploadFiles(undefined)).toEqual([]);
    expect(displayUploadFiles('file.txt')).toEqual([]);
    expect(commitUploadFiles([])).toBeNull();
  });

  it('keeps named files and drops entries without a name', () => {
    expect(
      displayUploadFiles([
        { id: 7, name: 'a.txt', url: '/a' },
        { id: 'b', name: '   ' },
        { name: 'c.txt' },
      ]),
    ).toEqual([
      { id: '7', name: 'a.txt', url: '/a' },
      { id: 'c.txt', name: 'c.txt' },
    ]);
  });

  it('appends files, caps at max, and replaces the list when multiple is off', () => {
    const first = nextUploadList(null, [{ name: 'a.txt' }, { name: 'b.txt' }], {
      max: 2,
      multiple: true,
    });
    expect(first).toHaveLength(2);
    expect(first?.map((file) => file.name)).toEqual(['a.txt', 'b.txt']);

    const capped = nextUploadList(first, [{ name: 'c.txt' }], {
      max: 2,
      multiple: true,
    });
    expect(capped?.map((file) => file.name)).toEqual(['a.txt', 'b.txt']);

    const replaced = nextUploadList(
      first,
      [{ name: 'd.txt' }, { name: 'e.txt' }],
      {
        multiple: false,
      },
    );
    expect(replaced).toHaveLength(1);
    expect(replaced?.[0]?.name).toBe('d.txt');
  });

  it('writes null after the last file is removed', () => {
    const files = nextUploadList(null, [{ name: 'a.txt' }]);
    expect(files).toHaveLength(1);
    expect(removeUploadFile(files, files?.[0]?.id ?? '')).toBeNull();
    expect(removeUploadFile(null, 'missing')).toBeNull();
  });
});
