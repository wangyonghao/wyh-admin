<script setup lang="ts">
/* global HTMLElement, crypto, navigator */
import type { MenuRecord, MenuStatus } from '../types';

import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue';

import { useUserStore } from '#/store/user';

import { Ellipsis, GripVertical, Plus } from '@vben/icons';
import { $t } from '@vben/locales';

import { useSortable } from '@vben-core/composables';
import { Switch } from '@vben-core/shadcn-ui';

import { Button } from '#/ui/button';
import { Checkbox } from '#/ui/checkbox';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from '#/ui/dropdown-menu';
import { Input } from '#/ui/input';
import {
  Sheet,
  SheetContent,
  SheetHeader,
  SheetTitle,
} from '#/ui/sheet';
import { Textarea } from '#/ui/textarea';
import { Tooltip, TooltipContent, TooltipTrigger } from '#/ui/tooltip';

import { toast } from '#/ui-patterns/toast';

import { z } from 'zod';

import { useMenuState } from '../composables/useMenuState';
import { joinPerm, permissionTaken } from '../composables/usePermCode';
import ButtonTemplateDialog from './ButtonTemplateDialog.vue';

const props = defineProps<{ coarse: boolean }>();

const userStore = useUserStore();
const state = useMenuState();
const bodyRef = ref<HTMLElement | null>(null);
const selected = ref<string[]>([]);
const templateOpen = ref(false);
const creating = ref(false);
const sheetId = ref<null | string>(null);
const sheetApis = ref('');
const sheetRemark = ref('');
let sortable: { destroy: () => void } | null = null;

const draft = ref({
  action: '',
  name: '',
  permissionManual: false,
  status: 'enabled' as MenuStatus,
});
const draftError = ref('');

const parent = computed(() => state.selectedNode.value);
const locked = computed(() =>
  parent.value ? state.effectivelyDisabled(parent.value) : false,
);
const prefix = computed(
  () => parent.value?.permission || state.selectedApp.value?.code || '',
);
const rows = computed(() =>
  parent.value ? state.buttonsOf(parent.value.id) : [],
);
const sheetRow = computed(() => rows.value.find((row) => row.id === sheetId.value) ?? null);
const canUpdate = computed(
  () =>
    userStore.hasPermission('system:menu:update') ||
    userStore.hasPermission('*:*:*'),
);
const canCreate = computed(
  () =>
    userStore.hasPermission('system:menu:create') ||
    userStore.hasPermission('*:*:*'),
);
const canDelete = computed(
  () =>
    userStore.hasPermission('system:menu:delete') ||
    userStore.hasPermission('*:*:*'),
);

const rowSchema = z.object({
  name: z.string().trim().min(1),
  permission: z.string().trim().min(1),
});

function write(next: MenuRecord[]) {
  if (!parent.value) return Promise.resolve(false);
  return state.replaceButtons(
    parent.value.id,
    next.map((row, index) => ({ ...row, sort: (index + 1) * 10 })),
  );
}

function draftPermission() {
  if (draft.value.permissionManual) return draft.value.action;
  return joinPerm(prefix.value, draft.value.action);
}

function startCreate() {
  creating.value = true;
  draftError.value = '';
  draft.value = { action: '', name: '', permissionManual: false, status: 'enabled' };
}

async function commitDraft() {
  if (!parent.value) return;
  const permission = draft.value.permissionManual
    ? draft.value.action.trim()
    : joinPerm(prefix.value, draft.value.action);
  const parsed = rowSchema.safeParse({ name: draft.value.name, permission });
  if (!parsed.success) {
    draftError.value = $t('appMenu.nameRequired');
    return;
  }
  if (permissionTaken(state.nodes.value, parent.value.appId, permission)) {
    draftError.value = $t('appMenu.permissionTaken');
    return;
  }
  const created: MenuRecord = {
    apis: [],
    appId: parent.value.appId,
    cache: false,
    component: '',
    external: false,
    icon: '',
    id: crypto.randomUUID(),
    name: parsed.data.name,
    parentId: parent.value.id,
    path: '',
    permission,
    remark: '',
    sort: 0,
    status: draft.value.status,
    type: 'button',
    visible: true,
  };
  const ok = await write([created, ...rows.value]);
  if (!ok) return;
  creating.value = false;
  toast.success($t('appMenu.saved'));
}

function patch(id: string, partial: Partial<MenuRecord>) {
  const prev = rows.value.map((row) => ({ ...row }));
  const next = prev.map((row) => (row.id === id ? { ...row, ...partial } : row));
  const target = next.find((row) => row.id === id);
  if (
    target?.permission &&
    permissionTaken(state.nodes.value, target.appId, target.permission, id)
  ) {
    toast.warning($t('appMenu.permissionTaken'));
    return;
  }
  write(next);
}

function toggleStatus(row: MenuRecord) {
  const prev = row.status;
  const next = prev === 'enabled' ? 'disabled' : 'enabled';
  try {
    patch(row.id, { status: next });
  } catch {
    patch(row.id, { status: prev });
    toast.error($t('appMenu.loadFailed'));
  }
}

async function removeIds(ids: string[]) {
  const ok = await write(rows.value.filter((row) => !ids.includes(row.id)));
  if (!ok) return;
  selected.value = selected.value.filter((id) => !ids.includes(id));
  toast.success($t('appMenu.removed'));
}

function bulkStatus(status: MenuStatus) {
  write(
    rows.value.map((row) =>
      selected.value.includes(row.id) ? { ...row, status } : row,
    ),
  );
  selected.value = [];
}

async function applyTemplate(actions: string[]) {
  if (!parent.value) return;
  const existing = new Set(rows.value.map((row) => row.permission));
  const created: MenuRecord[] = [];
  for (const action of actions) {
    const permission = joinPerm(prefix.value, action);
    if (existing.has(permission)) continue;
    created.push({
      apis: [],
      appId: parent.value.appId,
      cache: false,
      component: '',
      external: false,
      icon: '',
      id: crypto.randomUUID(),
      name: $t(`appMenu.action.${action}`),
      parentId: parent.value.id,
      path: '',
      permission,
      remark: '',
      sort: 0,
      status: 'enabled',
      type: 'button',
      visible: true,
    });
  }
  if (created.length === 0) return;
  const ok = await write([...created, ...rows.value]);
  if (!ok) return;
  toast.success($t('appMenu.templateApplied', { count: created.length }));
}

async function copyCode(code: string) {
  await navigator.clipboard.writeText(code);
  toast.success($t('appMenu.copied'));
}

function openSheet(row: MenuRecord) {
  sheetId.value = row.id;
  sheetApis.value = row.apis.join('\n');
  sheetRemark.value = row.remark;
}

function saveSheet() {
  if (!sheetId.value) return;
  patch(sheetId.value, {
    apis: sheetApis.value
      .split('\n')
      .map((line) => line.trim())
      .filter(Boolean),
    remark: sheetRemark.value,
  });
  sheetId.value = null;
}

function toggleAll(checked: boolean | 'indeterminate') {
  selected.value = checked === true ? rows.value.map((row) => row.id) : [];
}

function restoreDom(evt: { from: HTMLElement; item: HTMLElement; oldIndex?: number }) {
  if (evt.oldIndex == null) return;
  evt.item.remove();
  evt.from.insertBefore(evt.item, evt.from.children[evt.oldIndex] ?? null);
}

async function mountSortable() {
  sortable?.destroy();
  sortable = null;
  if (props.coarse || !bodyRef.value) return;
  const { initializeSortable } = useSortable(bodyRef.value, {
    animation: 0,
    delay: 0,
    handle: '.drag-handle',
    onEnd: (evt) => {
      const ids = [...bodyRef.value!.querySelectorAll<HTMLElement>('[data-id]')].map(
        (el) => el.dataset.id ?? '',
      );
      restoreDom(evt as { from: HTMLElement; item: HTMLElement; oldIndex?: number });
      const order = new Map(ids.map((id, index) => [id, index]));
      write([...rows.value].sort((a, b) => (order.get(a.id) ?? 0) - (order.get(b.id) ?? 0)));
    },
  });
  sortable = await initializeSortable();
}

onMounted(() => nextTick(mountSortable));
watch(() => [rows.value.map((row) => row.id).join(), props.coarse], () => nextTick(mountSortable));
onBeforeUnmount(() => sortable?.destroy());
</script>

<template>
  <div :class="locked ? 'opacity-60' : ''">
    <p
      v-if="locked"
      class="mb-3 text-xs text-muted-foreground"
    >
      {{ $t('appMenu.disabledHint') }}
    </p>

    <div
      v-if="selected.length > 0"
      class="mb-3 flex items-center gap-3 text-sm"
    >
      <span>{{ $t('appMenu.selected', { count: selected.length }) }}</span>
      <button
        v-if="canUpdate"
        type="button"
        class="text-foreground/80 hover:text-foreground"
        @click="bulkStatus('enabled')"
      >
        {{ $t('appMenu.bulkEnable') }}
      </button>
      <button
        v-if="canUpdate"
        type="button"
        class="text-foreground/80 hover:text-foreground"
        @click="bulkStatus('disabled')"
      >
        {{ $t('appMenu.bulkDisable') }}
      </button>
      <button
        v-if="canDelete"
        type="button"
        class="text-destructive"
        @click="removeIds(selected)"
      >
        {{ $t('appMenu.bulkDelete') }}
      </button>
    </div>
    <div
      v-else
      class="mb-3 flex items-center gap-4 text-sm"
    >
      <button
        v-if="canCreate"
        type="button"
        class="inline-flex items-center gap-1 text-foreground/80 transition-colors duration-150 hover:text-foreground"
        @click="startCreate"
      >
        <Plus
          class="size-4"
          :stroke-width="1.5"
        />
        {{ $t('appMenu.addButton') }}
      </button>
      <button
        v-if="canCreate"
        type="button"
        class="text-muted-foreground transition-colors duration-150 hover:text-foreground"
        @click="templateOpen = true"
      >
        {{ $t('appMenu.templates') }}
      </button>
    </div>

    <p
      v-if="rows.length === 0 && !creating"
      class="py-8 text-sm text-muted-foreground"
    >
      {{ $t('appMenu.buttonEmpty') }}
      <button
        type="button"
        class="ml-2 text-foreground underline-offset-4 hover:underline"
        @click="templateOpen = true"
      >
        {{ $t('appMenu.useTemplate') }}
      </button>
    </p>

    <div
      v-else-if="coarse"
      class="space-y-4"
    >
      <article
        v-if="creating"
        class="space-y-2 border-b border-border pb-3"
      >
        <Input
          v-model="draft.name"
          :placeholder="$t('appMenu.name')"
          class="h-8 shadow-none"
          @keydown.enter.prevent="commitDraft"
        />
        <Input
          v-model="draft.action"
          :placeholder="$t('appMenu.actionCode')"
          class="h-8 font-mono shadow-none"
          @keydown.enter.prevent="commitDraft"
        />
        <p class="truncate font-mono text-xs text-muted-foreground">
          {{ draftPermission() }}
        </p>
        <p
          v-if="draftError"
          class="text-xs text-destructive"
        >
          {{ draftError }}
        </p>
      </article>
      <article
        v-for="row in rows"
        :key="row.id"
        class="border-b border-border pb-3"
      >
        <div class="flex items-center justify-between gap-3">
          <p class="truncate text-sm">
            {{ row.name }}
          </p>
          <Switch
            :model-value="row.status === 'enabled'"
            @update:model-value="toggleStatus(row)"
          />
        </div>
        <button
          type="button"
          class="mt-1 max-w-full truncate font-mono text-xs text-muted-foreground"
          @click="copyCode(row.permission)"
        >
          {{ row.permission }}
        </button>
      </article>
    </div>

    <table
      v-else
      class="w-full text-sm"
    >
      <thead class="text-left text-xs text-muted-foreground">
        <tr class="border-b border-border">
          <th class="w-8 py-2 font-normal">
            <Checkbox
              :model-value="selected.length > 0 && selected.length === rows.length"
              @update:model-value="toggleAll"
            />
          </th>
          <th class="w-6 py-2 font-normal" />
          <th class="py-2 font-normal">
            {{ $t('appMenu.name') }}
          </th>
          <th class="py-2 font-normal">
            {{ $t('appMenu.permission') }}
          </th>
          <th class="py-2 font-normal">
            {{ $t('appMenu.apis') }}
          </th>
          <th class="py-2 font-normal">
            {{ $t('appMenu.status') }}
          </th>
          <th class="w-8 py-2 font-normal" />
        </tr>
      </thead>
      <tbody ref="bodyRef">
        <tr
          v-if="creating"
          class="border-b border-border"
        >
          <td />
          <td />
          <td class="py-2 pr-3">
            <Input
              v-model="draft.name"
              class="h-8 shadow-none"
              @keydown.enter.prevent="commitDraft"
            />
          </td>
          <td class="py-2 pr-3">
            <Input
              v-model="draft.action"
              class="h-8 font-mono shadow-none"
              @keydown.enter.prevent="commitDraft"
            />
            <p class="mt-1 truncate font-mono text-xs text-muted-foreground">
              {{ prefix }} + {{ draft.action || '…' }}
            </p>
            <p
              v-if="draftError"
              class="text-xs text-destructive"
            >
              {{ draftError }}
            </p>
          </td>
          <td />
          <td />
          <td />
        </tr>
        <tr
          v-for="row in rows"
          :key="row.id"
          :data-id="row.id"
          class="group border-b border-border"
        >
          <td class="py-2">
            <Checkbox
              :model-value="selected.includes(row.id)"
              @update:model-value="(checked) => {
                selected = checked === true
                  ? [...selected, row.id]
                  : selected.filter((id) => id !== row.id)
              }"
            />
          </td>
          <td>
            <button
              v-if="canUpdate"
              type="button"
              class="drag-handle inline-flex size-6 items-center justify-center text-muted-foreground opacity-0 group-hover:opacity-100"
              :aria-label="$t('appMenu.drag')"
            >
              <GripVertical
                class="size-4"
                :stroke-width="1.5"
              />
            </button>
          </td>
          <td class="py-2 pr-3">
            <input
              :value="row.name"
              class="w-full bg-transparent text-sm outline-none"
              :disabled="!canUpdate"
              @change="patch(row.id, { name: ($event.target as HTMLInputElement).value })"
              @keydown.enter.prevent="($event.target as HTMLInputElement).blur()"
            >
          </td>
          <td class="max-w-48 py-2 pr-3">
            <Tooltip>
              <TooltipTrigger as-child>
                <button
                  type="button"
                  class="block max-w-full truncate font-mono text-xs text-muted-foreground"
                  :aria-label="$t('appMenu.copyCode')"
                  @click="copyCode(row.permission)"
                >
                  {{ row.permission }}
                </button>
              </TooltipTrigger>
              <TooltipContent>{{ row.permission }}</TooltipContent>
            </Tooltip>
          </td>
          <td class="py-2 pr-3 text-xs text-muted-foreground">
            {{ row.apis.join(' · ') }}
          </td>
          <td class="py-2">
            <button
              type="button"
              class="inline-flex items-center gap-1.5 text-xs"
              @click="toggleStatus(row)"
            >
              <span
                class="size-1.5 rounded-full"
                :class="row.status === 'enabled' ? 'bg-foreground' : 'border border-muted-foreground'"
              />
              {{ row.status === 'enabled' ? $t('appMenu.enabled') : $t('appMenu.disabled') }}
            </button>
          </td>
          <td class="py-2">
            <DropdownMenu>
              <DropdownMenuTrigger as-child>
                <button
                  type="button"
                  class="inline-flex size-6 items-center justify-center text-muted-foreground opacity-0 group-hover:opacity-100 focus-visible:opacity-100"
                  :aria-label="$t('appMenu.more')"
                >
                  <Ellipsis
                    class="size-4"
                    :stroke-width="1.5"
                  />
                </button>
              </DropdownMenuTrigger>
              <DropdownMenuContent align="end">
                <DropdownMenuItem @click="openSheet(row)">
                  {{ $t('appMenu.more') }}
                </DropdownMenuItem>
                <DropdownMenuItem
                  v-if="canDelete"
                  class="text-destructive"
                  @click="removeIds([row.id])"
                >
                  {{ $t('appMenu.delete') }}
                </DropdownMenuItem>
              </DropdownMenuContent>
            </DropdownMenu>
          </td>
        </tr>
      </tbody>
    </table>

    <ButtonTemplateDialog
      v-model:open="templateOpen"
      @apply="applyTemplate"
    />

    <Sheet
      :open="!!sheetId"
      @update:open="(open) => !open && (sheetId = null)"
    >
      <SheetContent class="shadow-none">
        <SheetHeader>
          <SheetTitle class="text-base font-medium">
            {{ sheetRow?.name }}
          </SheetTitle>
        </SheetHeader>
        <label class="mt-6 block text-sm text-muted-foreground">
          {{ $t('appMenu.apis') }}
          <Textarea
            v-model="sheetApis"
            class="mt-2 min-h-24 shadow-none"
          />
        </label>
        <label class="mt-5 block text-sm text-muted-foreground">
          {{ $t('appMenu.remark') }}
          <Textarea
            v-model="sheetRemark"
            class="mt-2 min-h-20 shadow-none"
          />
        </label>
        <Button
          type="button"
          class="mt-6 shadow-none"
          @click="saveSheet"
        >
          {{ $t('appMenu.save') }}
        </Button>
      </SheetContent>
    </Sheet>
  </div>
</template>
