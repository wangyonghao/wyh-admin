<script setup lang="ts">
import type { MatrixMenu, MatrixPackage, PackageKind } from '#/api/tenant/package-catalog';

import { computed, onMounted, ref } from 'vue';

import { Page } from '@vben/common-ui';

import { packageCatalogApi } from '#/api/tenant/package-catalog';
import { Button } from '#/ui/button';
import { Checkbox } from '#/ui/checkbox';
import { Input } from '#/ui/input';
import { VbenSelect } from '#/ui/composed/select';
import { toast } from '#/ui-patterns/toast';
import { isBusinessError } from '#/utils/business-error';

import { checkState, differs, matrixRows, previewGroups, togglePages } from './matrix';

const menus = ref<MatrixMenu[]>([]);
const packages = ref<MatrixPackage[]>([]);
const onlyDiff = ref(false);
const baseId = ref('');
const previewIds = ref<string[]>([]);
const busyId = ref('');
const dialog = ref<{ kind: PackageKind; mode: 'copy' | 'create' | 'rename'; packageId?: string; title: string } | null>(null);
const dialogName = ref('');
const publishTarget = ref<MatrixPackage | null>(null);

const pageIds = computed(() => new Set(menus.value.filter((menu) => menu.type === 2).map((menu) => menu.id)));
const requiredIds = computed(() => new Set(menus.value.filter((menu) => menu.required).map((menu) => menu.id)));
const rows = computed(() => matrixRows(menus.value));
const visibleRows = computed(() => {
  if (!onlyDiff.value || !baseId.value) return rows.value;
  return rows.value.filter((row) => differs(row.pageIds, baseId.value, packages.value));
});
const baseOptions = computed(() => packages.value.map((item) => ({ label: item.name, value: item.id })));
const groups = computed(() => previewGroups(menus.value, packages.value, previewIds.value));

function fail(error: unknown) {
  if (isBusinessError(error)) toast.error(error.message || '操作失败');
}

async function load() {
  const matrix = await packageCatalogApi.matrix();
  menus.value = matrix.menus;
  packages.value = matrix.packages;
  if (!packages.value.some((item) => item.id === baseId.value)) {
    baseId.value = packages.value[0]?.id ?? '';
  }
  if (previewIds.value.length === 0 && baseId.value) previewIds.value = [baseId.value];
}

function selection(pkg: MatrixPackage) {
  return new Set(pkg.menuIds.filter((id) => pageIds.value.has(id)));
}

function stateOf(rowPageIds: string[], pkg: MatrixPackage) {
  return checkState(rowPageIds, selection(pkg), requiredIds.value);
}

async function toggle(rowPageIds: string[], pkg: MatrixPackage) {
  if (pkg.lifecycle === 'DELISTED') {
    toast.error('已下架套餐不能修改菜单');
    return;
  }
  const next = togglePages(rowPageIds, selection(pkg), requiredIds.value);
  busyId.value = pkg.id;
  try {
    const saved = await packageCatalogApi.saveMenus(pkg.id, pkg.version, [...next]);
    pkg.menuIds = saved.menuIds;
    pkg.version = saved.version;
    pkg.dirty = saved.dirty;
  } catch (error) {
    fail(error);
    await load();
  } finally {
    busyId.value = '';
  }
}

async function run(id: string, action: () => Promise<unknown>) {
  busyId.value = id;
  try {
    await action();
    await load();
  } catch (error) {
    fail(error);
  } finally {
    busyId.value = '';
  }
}

function openCreate() {
  dialog.value = { kind: 'MAIN', mode: 'create', title: '新建套餐' };
  dialogName.value = '';
}

function openCopy() {
  const base = packages.value.find((item) => item.id === baseId.value);
  if (!base) return;
  dialog.value = { kind: base.kind, mode: 'copy', packageId: base.id, title: '复制为草稿套餐' };
  dialogName.value = `${base.name} 副本`;
}

function openRename(pkg: MatrixPackage) {
  dialog.value = { kind: pkg.kind, mode: 'rename', packageId: pkg.id, title: '修改名称' };
  dialogName.value = pkg.name;
}

async function submitDialog() {
  const current = dialog.value;
  if (!current || !dialogName.value.trim()) return;
  busyId.value = current.packageId ?? 'new';
  try {
    if (current.mode === 'create') await packageCatalogApi.create(dialogName.value.trim(), current.kind);
    if (current.mode === 'copy' && current.packageId) {
      await packageCatalogApi.copy(current.packageId, dialogName.value.trim(), current.kind);
    }
    if (current.mode === 'rename' && current.packageId) {
      const pkg = packages.value.find((item) => item.id === current.packageId);
      await packageCatalogApi.rename(current.packageId, pkg?.version ?? 0, dialogName.value.trim());
    }
    dialog.value = null;
    await load();
  } catch (error) {
    fail(error);
  } finally {
    busyId.value = '';
  }
}

function confirmPublish() {
  const pkg = publishTarget.value;
  if (!pkg) return;
  publishTarget.value = null;
  void run(pkg.id, () => packageCatalogApi.publish(pkg.id, pkg.version));
}

function askPublish(pkg: MatrixPackage) {
  if (pkg.lifecycle === 'DRAFT' && !pkg.everPublished) {
    publishTarget.value = pkg;
    return;
  }
  void run(pkg.id, () => packageCatalogApi.publish(pkg.id, pkg.version));
}

function allRequired(pageIds: string[]) {
  return pageIds.length > 0 && pageIds.every((id) => requiredIds.value.has(id));
}

function lifecycleText(lifecycle: MatrixPackage['lifecycle']) {
  if (lifecycle === 'PUBLISHED') return '已发布';
  if (lifecycle === 'DELISTED') return '已下架';
  return '未发布';
}

function chip(pkg: MatrixPackage) {
  if (pkg.lifecycle === 'DELISTED') return `${pkg.name}（已下架）`;
  if (pkg.lifecycle === 'DRAFT') return `${pkg.name}（未发布）`;
  return pkg.name;
}

function togglePreview(id: string) {
  previewIds.value = previewIds.value.includes(id)
    ? previewIds.value.filter((item) => item !== id)
    : [...previewIds.value, id];
}

onMounted(() => {
  load().catch(fail);
});
</script>

<template>
  <Page auto-content-height>
    <div class="flex h-full min-h-0 flex-col gap-4">
      <header class="flex flex-wrap items-center justify-between gap-3">
        <label class="flex items-center gap-2 text-sm">
          <Checkbox :model-value="onlyDiff" @update:model-value="onlyDiff = Boolean($event)" />
          只看套餐差异
        </label>
        <div class="flex flex-wrap items-center gap-2">
          <div class="w-40">
            <VbenSelect v-model="baseId" :options="baseOptions" placeholder="选择基准套餐" />
          </div>
          <Button :disabled="!baseId" type="button" variant="outline" @click="openCopy">复制为草稿套餐</Button>
          <Button type="button" @click="openCreate">新建套餐</Button>
        </div>
      </header>

      <section class="min-h-0 flex-1 overflow-auto rounded-xl border border-border bg-card">
        <table class="w-full min-w-[760px] border-collapse text-sm">
          <thead>
            <tr class="border-b border-border">
              <th class="sticky left-0 z-10 bg-card px-4 py-3 text-left font-semibold">菜单</th>
              <th v-for="pkg in packages" :key="pkg.id" class="min-w-36 px-3 py-3 align-top font-normal">
                <div class="flex flex-col items-center gap-1">
                  <div class="font-medium">{{ pkg.name }}</div>
                  <div
                    :class="pkg.lifecycle === 'PUBLISHED' ? 'text-success' : pkg.lifecycle === 'DRAFT' ? 'text-primary' : 'text-muted-foreground'"
                    class="text-xs"
                  >
                    {{ lifecycleText(pkg.lifecycle) }}
                  </div>
                  <div class="text-xs text-muted-foreground">{{ pkg.tenantCount }} 家在用</div>
                  <div class="mt-1 flex flex-col gap-1">
                    <Button
                      v-if="pkg.lifecycle === 'DRAFT' || pkg.dirty"
                      :disabled="pkg.lifecycle === 'DELISTED'"
                      :loading="busyId === pkg.id"
                      size="sm"
                      type="button"
                      variant="outline"
                      @click="askPublish(pkg)"
                    >
                      发布
                    </Button>
                    <Button
                      v-if="pkg.dirty"
                      size="sm"
                      type="button"
                      variant="outline"
                      @click="run(pkg.id, () => packageCatalogApi.discard(pkg.id, pkg.version))"
                    >
                      放弃
                    </Button>
                    <Button
                      v-if="pkg.lifecycle === 'PUBLISHED'"
                      :disabled="pkg.dirty"
                      size="sm"
                      type="button"
                      variant="outline"
                      @click="run(pkg.id, () => packageCatalogApi.delist(pkg.id, pkg.version))"
                    >
                      下架
                    </Button>
                    <Button
                      v-if="pkg.lifecycle === 'DELISTED'"
                      size="sm"
                      type="button"
                      variant="outline"
                      @click="run(pkg.id, () => packageCatalogApi.relist(pkg.id, pkg.version))"
                    >
                      重新上架
                    </Button>
                    <template v-if="pkg.lifecycle === 'DRAFT' && !pkg.everPublished">
                      <Button size="sm" type="button" variant="outline" @click="openRename(pkg)">改名</Button>
                      <Button
                        size="sm"
                        type="button"
                        variant="outline"
                        @click="run(pkg.id, () => packageCatalogApi.remove(pkg.id))"
                      >
                        删除
                      </Button>
                    </template>
                  </div>
                </div>
              </th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="row in visibleRows" :key="row.menu.id" class="border-b border-border">
              <td class="sticky left-0 bg-card px-4 py-2.5">
                <div class="flex items-center gap-2" :style="{ paddingLeft: `${row.depth * 16}px` }">
                  <span>{{ row.menu.name }}</span>
                  <span
                    v-if="row.menu.required"
                    class="rounded bg-muted px-1.5 py-0.5 text-xs text-muted-foreground"
                  >
                    必含
                  </span>
                </div>
              </td>
              <td v-for="pkg in packages" :key="pkg.id" class="px-3 py-2 text-center">
                <Checkbox
                  :disabled="row.menu.required || allRequired(row.pageIds) || row.pageIds.length === 0 || pkg.lifecycle === 'DELISTED' || busyId === pkg.id"
                  :indeterminate="stateOf(row.pageIds, pkg) === 'indeterminate'"
                  :model-value="stateOf(row.pageIds, pkg) === 'indeterminate' ? 'indeterminate' : stateOf(row.pageIds, pkg)"
                  @update:model-value="toggle(row.pageIds, pkg)"
                />
              </td>
            </tr>
            <tr v-if="menus.length === 0">
              <td :colspan="packages.length + 1" class="px-4 py-10 text-center text-muted-foreground">
                租户菜单还没有已发布内容。先在菜单管理里维护并发布租户菜单。
              </td>
            </tr>
          </tbody>
        </table>
      </section>

      <section class="rounded-xl border border-border bg-card p-4">
        <h2 class="text-sm font-semibold">按套餐组合预览</h2>
        <div class="mt-3 flex flex-wrap gap-2">
          <button
            v-for="pkg in packages"
            :key="pkg.id"
            :class="previewIds.includes(pkg.id) ? 'border-foreground' : 'border-border text-muted-foreground'"
            class="rounded-full border bg-background px-3 py-1 text-sm"
            type="button"
            @click="togglePreview(pkg.id)"
          >
            {{ chip(pkg) }}
          </button>
        </div>
        <div class="mt-3 grid gap-3 sm:grid-cols-2 lg:grid-cols-4">
          <div v-for="group in groups" :key="group.title" class="rounded-lg bg-muted px-3 py-3 text-sm">
            <div class="font-medium">{{ group.title }}</div>
            <div v-for="page in group.pages" :key="page" class="text-muted-foreground">{{ page }}</div>
          </div>
        </div>
      </section>
    </div>

    <div
      v-if="publishTarget"
      class="fixed inset-0 z-50 flex items-center justify-center bg-overlay p-4"
      @click.self="publishTarget = null"
    >
      <div class="w-full max-w-sm rounded-xl border border-border bg-card p-5">
        <h3 class="text-base font-semibold">发布「{{ publishTarget.name }}」</h3>
        <p class="mt-2 text-sm text-muted-foreground">发布后名称不可修改。</p>
        <div class="mt-5 flex justify-end gap-2">
          <Button type="button" variant="outline" @click="publishTarget = null">取消</Button>
          <Button
            type="button"
            @click="confirmPublish"
          >
            发布
          </Button>
        </div>
      </div>
    </div>

    <div
      v-if="dialog"
      class="fixed inset-0 z-50 flex items-center justify-center bg-overlay p-4"
      @click.self="dialog = null"
    >
      <div class="w-full max-w-sm rounded-xl border border-border bg-card p-5">
        <h3 class="text-base font-semibold">{{ dialog.title }}</h3>
        <p v-if="dialog.mode !== 'rename'" class="mt-1 text-xs text-muted-foreground">发布后名称不能再改。</p>
        <div class="mt-4 grid gap-2">
          <Input v-model="dialogName" placeholder="套餐名称" />
          <VbenSelect
            v-if="dialog.mode !== 'rename'"
            v-model="dialog.kind"
            :options="[
              { label: '主套餐', value: 'MAIN' },
              { label: '增购包', value: 'ADDON' },
            ]"
          />
        </div>
        <div class="mt-4 flex justify-end gap-2">
          <Button type="button" variant="outline" @click="dialog = null">取消</Button>
          <Button type="button" @click="submitDialog">确定</Button>
        </div>
      </div>
    </div>
  </Page>
</template>
