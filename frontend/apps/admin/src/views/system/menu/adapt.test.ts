import { describe, expect, it } from 'vitest';

import { splitMenus } from './adapt';

describe('splitMenus', () => {
  it('keeps a module and the menus under it', () => {
    const split = splitMenus([
      {
        icon: 'lucide:settings-2',
        id: 1000,
        name: '平台域',
        parentId: 0,
        path: '/system',
        permission: 'platform',
        sort: 1,
        status: 1,
        type: 1,
      },
      {
        id: 1010,
        name: '用户管理',
        parentId: 1000,
        path: '/system/user',
        sort: 1,
        status: 1,
        type: 2,
      },
      {
        id: 1011,
        name: '列表',
        parentId: 1010,
        permission: 'system:user:list',
        sort: 1,
        status: 1,
        type: 3,
      },
    ]);

    expect(split.apps.map((app) => app.name)).toEqual(['平台域']);
    expect(split.nodes.filter((node) => node.type !== 'button').map((node) => node.name)).toEqual([
      '用户管理',
    ]);
    expect(split.nodes.find((node) => node.name === '用户管理')).toMatchObject({
      appId: '1000',
      parentId: null,
      type: 'function',
    });
  });

  it('flattens a nested tree and a data wrapper', () => {
    const split = splitMenus({
      data: [
        {
          children: [
            {
              id: 2,
              name: '租户设置',
              type: 'DIR',
            },
          ],
          id: 1,
          name: '平台域',
          parentId: 0,
          type: { value: 1 },
        },
      ],
    });

    expect(split.apps.map((app) => app.name)).toEqual(['平台域']);
    expect(split.nodes.map((node) => node.name)).toEqual(['租户设置']);
    expect(split.nodes[0]).toMatchObject({ appId: '1', parentId: null, type: 'folder' });
  });
});
