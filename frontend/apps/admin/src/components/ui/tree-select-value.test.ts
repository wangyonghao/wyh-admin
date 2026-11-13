import { describe, expect, it } from 'vitest';

import {
  commitTreeValue,
  filterRawTree,
  filterTreeNodes,
  findTreeLabel,
  isTreeSelectEmpty,
  normalizeTreeOptions,
  parentKeyIds,
  toggleTreeValue,
  treeKeyId,
  visibleTreeRows,
} from './tree-select-value';

const options = normalizeTreeOptions([
  {
    label: '顶级',
    key: 0,
    children: [
      { label: '系统', key: 'sys', children: [{ label: '用户', key: 'user' }] },
      { label: '停用', key: 'off', disabled: true },
    ],
  },
  { key: 'plain' },
  { label: '缺键' },
  null,
]);

describe('form tree select empty value', () => {
  it('renders null as unselected and keeps 0', () => {
    expect(isTreeSelectEmpty(null)).toBe(true);
    expect(isTreeSelectEmpty(undefined)).toBe(true);
    expect(isTreeSelectEmpty('')).toBe(true);
    expect(isTreeSelectEmpty([])).toBe(true);
    expect(isTreeSelectEmpty(0)).toBe(false);
    expect(findTreeLabel(options, 0)).toBe('顶级');
    expect(findTreeLabel(options, null)).toBeUndefined();
    expect(commitTreeValue(undefined)).toBeNull();
    expect(commitTreeValue('')).toBeNull();
    expect(commitTreeValue(0)).toBe(0);
    expect(commitTreeValue('sys')).toBe('sys');
  });

  it('reads value when keyField is missing and falls back to label', () => {
    const api = normalizeTreeOptions(
      [{ label: '菜单', value: 'path', children: [{ value: 1, name: '子级' }] }],
      'value',
      'name',
    );
    expect(api[0]?.key).toBe('path');
    expect(api[0]?.label).toBe('菜单');
    expect(api[0]?.children[0]).toMatchObject({ key: 1, label: '子级' });
    expect(findTreeLabel(options, 'plain')).toBe('plain');
  });

  it('filters to matches and their parents, and expands those parents', () => {
    const filtered = filterTreeNodes(options, '用户');
    expect(filtered.map((node) => node.key)).toEqual([0]);
    expect(filtered[0]?.children.map((node) => node.key)).toEqual(['sys']);
    expect(filtered[0]?.children[0]?.children.map((node) => node.key)).toEqual([
      'user',
    ]);
    const ids = new Set(parentKeyIds(filtered));
    expect(visibleTreeRows(filtered, ids).map((row) => row.key)).toEqual([
      0,
      'sys',
      'user',
    ]);
    expect(visibleTreeRows(options, new Set([treeKeyId(0)])).map((row) => row.key)).toEqual([
      0,
      'sys',
      'off',
      'plain',
    ]);
  });

  it('clears a multiple selection back to null', () => {
    expect(toggleTreeValue(null, 0, true)).toEqual([0]);
    expect(toggleTreeValue([0, 'sys'], 0, false)).toEqual(['sys']);
    expect(toggleTreeValue([0], 0, false)).toBeNull();
    expect(toggleTreeValue('sys', 0, true)).toEqual([0]);
  });

  it('filters a raw department tree by name and keeps ancestors', () => {
    const tree = filterRawTree(
      [
        {
          name: '总部',
          key: '1',
          children: [
            { name: '研发', key: '2', children: [] },
            { name: '市场', key: '3', children: [] },
          ],
        },
      ],
      '研发',
      'name',
      'children',
    );
    expect(tree).toEqual([
      {
        name: '总部',
        key: '1',
        children: [{ name: '研发', key: '2', children: [] }],
      },
    ]);
  });
});
