<script setup lang="ts">
import type { AppRecord } from '../types';

import { computed, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue';

import { useUserStore } from '#/store/user';

import { $t } from '@vben/locales';

import { Switch } from '@vben-core/shadcn-ui';

import { Button } from '#/ui/button';
import { Input } from '#/ui/input';
import { toast } from '#/ui-patterns/toast';

import { z } from 'zod';

import { useMenuState } from '../composables/useMenuState';

const userStore = useUserStore();
const state = useMenuState();

const errors = ref<Record<string, string>>({});
const snapshot = ref('');
const model = reactive({
  code: '',
  entry: '',
  icon: '',
  name: '',
  status: 'enabled' as AppRecord['status'],
});

const inputClass =
  'h-8 rounded-none border-0 border-b border-border bg-transparent px-0 shadow-none focus-visible:rounded-md focus-visible:border focus-visible:px-2 focus-visible:ring-0';

const canDelete = computed(
  () =>
    userStore.hasPermission('system:menu:delete') ||
    userStore.hasPermission('*:*:*'),
);

const schema = computed(() =>
  z.object({
    code: z.string().trim(),
    entry: z.string(),
    icon: z.string(),
    name: z.string().trim().min(1, $t('appMenu.nameRequired')),
    status: z.enum(['enabled', 'disabled']),
  }),
);

function loadFromApp() {
  const app = state.selectedApp.value;
  if (!app) return;
  Object.assign(model, {
    code: app.code,
    entry: app.entry,
    icon: app.icon,
    name: app.name,
    status: app.status,
  });
  snapshot.value = JSON.stringify(model);
  errors.value = {};
  state.setDirty(false);
}

watch(
  () => {
    const app = state.selectedApp.value;
    if (!app) return '';
    return [
      app.id,
      app.code,
      app.entry,
      app.icon,
      app.name,
      app.status,
      state.formEpoch.value,
    ].join('\u0000');
  },
  () => {
    if (state.dirty.value) return;
    loadFromApp();
  },
  { immediate: true },
);

watch(
  model,
  () => {
    state.setDirty(JSON.stringify(model) !== snapshot.value);
  },
  { deep: true },
);

async function save() {
  const app = state.selectedApp.value;
  const parsed = schema.value.safeParse(model);
  errors.value = {};
  if (!parsed.success || !app) {
    for (const issue of parsed.success ? [] : parsed.error.issues) {
      const key = String(issue.path[0] ?? '');
      if (key && !errors.value[key]) errors.value[key] = issue.message;
    }
    return false;
  }
  const result = await state.upsertApp({
    ...parsed.data,
    id: app.id,
    sort: app.sort,
  });
  if (!result.ok) {
    if (result.error === 'code') errors.value.code = $t('appMenu.appCodeTaken');
    return false;
  }
  snapshot.value = JSON.stringify(model);
  state.setDirty(false);
  toast.success($t('appMenu.saved'));
  return true;
}

onMounted(() => {
  state.registerSave(save);
});
onBeforeUnmount(() => state.registerSave(null));
</script>

<template>
  <div class="flex h-full min-h-0 flex-col">
    <div class="min-h-0 flex-1 overflow-auto px-4 py-3">
      <div class="flex min-w-0 items-baseline gap-2">
        <h2 class="min-w-0 truncate text-base font-medium">
          {{ state.selectedApp.value?.name }}
        </h2>
        <span class="shrink-0 text-xs text-muted-foreground">
          {{ $t('appMenu.app') }}
        </span>
      </div>
      <p
        v-if="state.selectedApp.value?.entry"
        class="mt-1 max-w-xl truncate font-mono text-xs text-muted-foreground"
      >
        {{ state.selectedApp.value.entry }}
      </p>
      <form
        class="mt-6 max-w-xl"
        @submit.prevent="save"
      >
        <div class="space-y-5">
          <label class="grid gap-1.5">
            <span class="text-sm text-muted-foreground">{{ $t('appMenu.appName') }}</span>
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
          <label class="grid gap-1.5">
            <span class="text-sm text-muted-foreground">{{ $t('appMenu.appCode') }}</span>
            <span>
              <Input
                v-model="model.code"
                :class="[inputClass, 'font-mono']"
              />
              <p
                v-if="errors.code"
                class="mt-1 text-xs text-destructive"
              >{{ errors.code }}</p>
            </span>
          </label>
          <label class="grid gap-1.5">
            <span class="text-sm text-muted-foreground">{{ $t('appMenu.appIcon') }}</span>
            <Input
              v-model="model.icon"
              :class="inputClass"
            />
          </label>
          <label class="grid gap-1.5">
            <span class="text-sm text-muted-foreground">{{ $t('appMenu.appEntry') }}</span>
            <Input
              v-model="model.entry"
              :class="[inputClass, 'font-mono']"
            />
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
          v-if="canDelete"
          class="mt-10 flex items-center gap-3"
        >
          <Button
            type="button"
            variant="outline"
            class="border-destructive bg-transparent text-destructive shadow-none hover:bg-destructive/10 hover:text-destructive"
            @click="state.askRemoveApp(state.selectedApp.value?.id ?? '')"
          >
            {{ $t('appMenu.delete') }}
          </Button>
          <span class="text-sm text-muted-foreground">
            {{ $t('appMenu.deleteCascade') }}
          </span>
        </div>
      </form>
    </div>
    <div
      v-if="state.dirty.value"
      class="sticky bottom-0 border-t border-border bg-background px-4 py-3"
    >
      <Button
        type="button"
        class="shadow-none"
        @click="save"
      >
        {{ $t('appMenu.save') }}
      </Button>
    </div>
  </div>
</template>
