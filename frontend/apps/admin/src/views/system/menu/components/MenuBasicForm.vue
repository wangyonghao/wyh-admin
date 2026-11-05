<script setup lang="ts">
import type { BasicFormValues } from '../types';

import { computed, reactive, ref, watch } from 'vue';

import { $t } from '@vben/locales';

import { Switch } from '@vben-core/shadcn-ui';

import { Input } from '#/ui/input';
import { Textarea } from '#/ui/textarea';
import { toast } from '#/ui-patterns/toast';

import { z } from 'zod';

import { pathTaken } from '../composables/usePermCode';
import { useMenuState } from '../composables/useMenuState';

const state = useMenuState();
const more = ref(false);
const errors = ref<Record<string, string>>({});
const snapshot = ref('');

const model = reactive<BasicFormValues>({
  cache: false,
  component: '',
  external: false,
  icon: '',
  name: '',
  path: '',
  permission: '',
  remark: '',
  sort: 0,
  status: 'enabled',
  type: 'function',
  visible: true,
});

const inputClass =
  'h-8 rounded-none border-0 border-b border-border bg-transparent px-0 shadow-none focus-visible:rounded-md focus-visible:border focus-visible:px-2 focus-visible:ring-0';

function loadFromNode() {
  const node = state.selectedNode.value;
  if (!node || node.type === 'button') return;
  Object.assign(model, state.valuesFromNode(node));
  snapshot.value = JSON.stringify(model);
  errors.value = {};
  more.value = false;
  state.setDirty(false);
}

watch(
  () => [state.selectedNodeId.value, state.formEpoch.value],
  () => loadFromNode(),
  { immediate: true },
);

watch(
  model,
  () => {
    state.setDirty(JSON.stringify(model) !== snapshot.value);
  },
  { deep: true },
);

const isFolder = computed(() => model.type === 'folder');

const schema = computed(() => {
  const name = z.string().trim().min(1, $t('appMenu.nameRequired'));
  if (isFolder.value) return z.object({ name });
  const appId = state.selectedAppId.value ?? '';
  const id = state.selectedNodeId.value ?? undefined;
  return z.object({
    name,
    path: z
      .string()
      .trim()
      .min(1, $t('appMenu.pathRequired'))
      .refine((value) => !pathTaken(state.nodes.value, appId, value, id), {
        message: $t('appMenu.pathTaken'),
      }),
    permission: z.string().refine(
      (value) => !state.permissionTaken(value, id),
      { message: $t('appMenu.permissionTaken') },
    ),
  });
});

async function save() {
  const parsed = schema.value.safeParse(model);
  errors.value = {};
  if (!parsed.success) {
    for (const issue of parsed.error.issues) {
      const key = String(issue.path[0] ?? '');
      if (key && !errors.value[key]) errors.value[key] = issue.message;
    }
    return false;
  }
  const id = state.selectedNodeId.value;
  if (!id) return false;
  const ok = await state.saveBasic(id, { ...model });
  if (!ok) return false;
  snapshot.value = JSON.stringify(model);
  state.setDirty(false);
  toast.success($t('appMenu.saved'));
  return true;
}

defineExpose({ reset: loadFromNode, save });
</script>

<template>
  <form
    class="max-w-xl"
    @submit.prevent="save"
  >
    <div class="space-y-5">
      <label class="grid gap-1.5">
        <span class="text-sm text-muted-foreground">{{ $t('appMenu.name') }}</span>
        <span>
          <Input
            v-model="model.name"
            :class="inputClass"
          />
          <p
            v-if="errors.name"
            class="mt-1 text-xs text-destructive"
          >{{ errors.name }}</p>
        </span>
      </label>
      <template v-if="isFolder">
        <label class="grid gap-1.5">
          <span class="flex items-baseline gap-2">
            <span class="text-sm text-muted-foreground">{{ $t('appMenu.icon') }}</span>
            <a
              href="https://icon-sets.iconify.design/lucide"
              target="_blank"
              rel="noopener noreferrer"
              class="text-xs text-muted-foreground underline-offset-4 transition-colors duration-150 hover:text-foreground hover:underline"
            >
              {{ $t('appMenu.moreIcons') }}
            </a>
          </span>
          <Input
            v-model="model.icon"
            :class="inputClass"
          />
        </label>
        <label class="grid gap-1.5">
          <span class="text-sm text-muted-foreground">{{ $t('appMenu.visible') }}</span>
          <Switch v-model="model.visible" />
        </label>
      </template>
      <template v-else>
        <label class="grid gap-1.5">
          <span class="text-sm text-muted-foreground">{{ $t('appMenu.path') }}</span>
          <span>
            <Input
              v-model="model.path"
              :class="[inputClass, 'font-mono']"
            />
            <p
              v-if="errors.path"
              class="mt-1 text-xs text-destructive"
            >{{ errors.path }}</p>
          </span>
        </label>
        <label class="grid gap-1.5">
          <span class="text-sm text-muted-foreground">{{ $t('appMenu.component') }}</span>
          <Input
            v-model="model.component"
            :class="[inputClass, 'font-mono']"
          />
        </label>
        <label class="grid gap-1.5">
          <span class="text-sm text-muted-foreground">{{ $t('appMenu.permission') }}</span>
          <span>
            <Input
              v-model="model.permission"
              :class="[inputClass, 'font-mono']"
            />
            <p
              v-if="errors.permission"
              class="mt-1 text-xs text-destructive"
            >{{ errors.permission }}</p>
          </span>
        </label>
      </template>
    </div>

    <div
      v-if="!isFolder"
      class="mt-8 space-y-5"
    >
      <label class="grid gap-1.5">
        <span class="text-sm text-muted-foreground">{{ $t('appMenu.sort') }}</span>
        <Input
          v-model.number="model.sort"
          type="number"
          :class="inputClass"
        />
      </label>
      <label class="grid gap-1.5">
        <span class="text-sm text-muted-foreground">{{ $t('appMenu.visible') }}</span>
        <Switch v-model="model.visible" />
      </label>
      <label class="grid gap-1.5">
        <span class="text-sm text-muted-foreground">{{ $t('appMenu.status') }}</span>
        <Switch
          :model-value="model.status === 'enabled'"
          @update:model-value="model.status = $event ? 'enabled' : 'disabled'"
        />
      </label>
    </div>

    <div
      v-if="!isFolder"
      class="mt-8"
    >
      <button
        type="button"
        class="text-sm text-muted-foreground transition-colors duration-150 hover:text-foreground"
        @click="more = !more"
      >
        {{ $t('appMenu.moreSettings') }}
      </button>
      <div
        v-if="more"
        class="mt-5 space-y-5"
      >
        <label class="grid gap-1.5">
          <span class="flex items-baseline gap-2">
            <span class="text-sm text-muted-foreground">{{ $t('appMenu.icon') }}</span>
            <a
              href="https://icon-sets.iconify.design/lucide"
              target="_blank"
              rel="noopener noreferrer"
              class="text-xs text-muted-foreground underline-offset-4 transition-colors duration-150 hover:text-foreground hover:underline"
            >
              {{ $t('appMenu.moreIcons') }}
            </a>
          </span>
          <Input
            v-model="model.icon"
            :class="inputClass"
          />
        </label>
        <label class="grid gap-1.5">
          <span class="text-sm text-muted-foreground">{{ $t('appMenu.cache') }}</span>
          <Switch v-model="model.cache" />
        </label>
        <label class="grid gap-1.5">
          <span class="text-sm text-muted-foreground">{{ $t('appMenu.external') }}</span>
          <Switch v-model="model.external" />
        </label>
        <label class="grid gap-1.5">
          <span class="text-sm text-muted-foreground">{{ $t('appMenu.remark') }}</span>
          <Textarea
            v-model="model.remark"
            class="min-h-20 shadow-none"
          />
        </label>
      </div>
    </div>
  </form>
</template>
