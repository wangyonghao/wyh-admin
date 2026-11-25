<script setup lang="ts">
import type { CatalogNode, MenuScope, RoleMenus } from '#/api/system/menu-catalog';
import type { MatrixPackage } from '#/api/tenant/package-catalog';

import { computed } from 'vue';

import { IconifyIcon } from '@vben/icons';

import { VbenSelect } from '#/ui/composed/select';

import { visibleTree } from './catalog';

const props = defineProps<{
  nodes: CatalogNode[];
  packages: MatrixPackage[];
  roles: RoleMenus[];
  scope: MenuScope;
}>();

const roleId = defineModel<string>('roleId');
const packageIds = defineModel<string[]>('packageIds', { default: () => [] });

const roleOptions = computed(() =>
  props.roles
    .filter((role) => role.id)
    .map((role) => ({ label: role.name, value: role.id })),
);

const tree = computed(() => {
  if (props.scope === 'TENANT') {
    if (packageIds.value.length === 0) return visibleTree(props.nodes);
    const allowed = new Set<string>();
    for (const pkg of props.packages) {
      if (packageIds.value.includes(pkg.id)) {
        for (const id of pkg.menuIds) allowed.add(id);
      }
    }
    return visibleTree(props.nodes, allowed);
  }
  const role = props.roles.find((item) => item.id === roleId.value);
  return visibleTree(props.nodes, role ? new Set(role.menuIds) : undefined);
});

const packageLabel = (pkg: MatrixPackage) => {
  if (pkg.lifecycle === 'DELISTED') return `${pkg.name}（已下架）`;
  if (pkg.lifecycle === 'DRAFT') return `${pkg.name}（未发布）`;
  return pkg.name;
};

function togglePackage(id: string) {
  const current = packageIds.value ?? [];
  packageIds.value = current.includes(id)
    ? current.filter((item) => item !== id)
    : [...current, id];
}

function iconOf(icon?: string) {
  if (!icon) return 'lucide:file';
  return icon.includes(':') ? icon : `lucide:${icon}`;
}
</script>

<template>
  <section class="flex min-h-0 flex-col rounded-xl border border-border bg-card">
    <header class="border-b border-border px-4 py-3">
      <h2 class="text-sm font-semibold">预览</h2>
    </header>
    <div class="grid gap-3 overflow-auto p-3">
      <VbenSelect
        v-if="scope === 'PLATFORM'"
        v-model="roleId"
        allow-clear
        :options="roleOptions"
        placeholder="按角色预览"
      />
      <div v-else class="flex flex-wrap gap-2">
        <button
          v-for="pkg in packages"
          :key="pkg.id"
          :class="packageIds?.includes(pkg.id) ? 'border-primary text-primary' : 'border-border'"
          class="rounded-full border px-2 py-0.5 text-xs"
          type="button"
          @click="togglePackage(pkg.id)"
        >
          {{ packageLabel(pkg) }}
        </button>
      </div>
      <div class="rounded-lg bg-muted/60 p-2">
        <div
          v-for="row in tree"
          :key="row.node.id"
          class="flex items-center gap-2 rounded-md px-2 py-1.5 text-sm"
          :style="{ paddingLeft: `${row.depth * 14 + 8}px` }"
        >
          <IconifyIcon :icon="iconOf(row.node.icon)" class="size-4" />
          <span>{{ row.node.name }}</span>
        </div>
        <p v-if="tree.length === 0" class="px-2 py-6 text-center text-xs text-muted-foreground">
          当前条件下没有可见菜单
        </p>
      </div>
      <p class="text-xs text-muted-foreground">停用项和没有可见子菜单的目录不会出现。</p>
    </div>
  </section>
</template>
