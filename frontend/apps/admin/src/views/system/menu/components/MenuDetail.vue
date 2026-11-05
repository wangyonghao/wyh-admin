<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue';

import { useUserStore } from '#/store/user';

import { $t } from '@vben/locales';

import {
  AlertDialog,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogTitle,
} from '#/ui/alert-dialog';
import { Button } from '#/ui/button';
import { Tabs, TabsList, TabsTrigger } from '#/ui/tabs';
import { Tooltip, TooltipContent, TooltipTrigger } from '#/ui/tooltip';

import { toast } from '#/ui-patterns/toast';


import { useMenuState } from '../composables/useMenuState';
import ButtonPermissionTable from './ButtonPermissionTable.vue';
import MenuBasicForm from './MenuBasicForm.vue';

defineProps<{ coarse: boolean }>();

const userStore = useUserStore();
const state = useMenuState();
const formRef = ref<InstanceType<typeof MenuBasicForm> | null>(null);
const tab = ref('basic');
const pending = ref(false);
const impact = computed(() =>
  node.value
    ? state.impactOf(node.value.id)
    : { buttons: 0, children: 0, roles: 0 },
);

const node = computed(() => state.selectedNode.value);
const buttonTotal = computed(() => (node.value ? state.buttonCount(node.value.id) : 0));
const showButtons = computed(() => node.value?.type === 'function');
const tabs = computed(() => {
  const list = ['basic'];
  if (showButtons.value) list.push('buttons');
  return list;
});
const canDelete = computed(
  () =>
    userStore.hasPermission('system:menu:delete') ||
    userStore.hasPermission('*:*:*'),
);

watch(
  () => node.value?.id,
  () => {
    if (!tabs.value.includes(tab.value)) tab.value = 'basic';
  },
);

onMounted(() => {
  state.registerSave(() => formRef.value?.save() ?? Promise.resolve(false));
});
onBeforeUnmount(() => state.registerSave(null));

function remove() {
  pending.value = true;
}

async function confirmRemove() {
  if (!node.value) return;
  const ok = await state.removeNode(node.value.id);
  if (!ok) return;
  pending.value = false;
  toast.success($t('appMenu.removed'));
}
</script>

<template>
  <div
    v-if="!node"
    class="px-4 py-6 text-sm text-muted-foreground"
  >
    {{ $t('appMenu.emptyDetail') }}
  </div>
  <div
    v-else
    class="flex h-full min-h-0 flex-col"
  >
    <div class="min-h-0 flex-1 overflow-auto px-4 py-3">
      <div class="flex min-w-0 items-baseline gap-2">
        <h2 class="min-w-0 truncate text-base font-medium">
          {{ node.name }}
        </h2>
        <span class="shrink-0 text-xs text-muted-foreground">
          {{ node.type === 'folder' ? $t('appMenu.directory') : $t('appMenu.menu') }}
        </span>
      </div>
      <Tooltip v-if="node.type !== 'folder' && node.path">
        <TooltipTrigger as-child>
          <p class="mt-1 max-w-xl truncate font-mono text-xs text-muted-foreground">
            {{ node.path }}
          </p>
        </TooltipTrigger>
        <TooltipContent>{{ node.path }}</TooltipContent>
      </Tooltip>

      <Tabs
        v-if="tabs.length > 1"
        v-model="tab"
        class="mt-6"
      >
        <TabsList class="h-auto justify-start gap-4 rounded-none bg-transparent p-0">
          <TabsTrigger
            value="basic"
            class="rounded-none border-b px-0 py-1 shadow-none data-[state=active]:border-foreground data-[state=active]:bg-transparent data-[state=active]:shadow-none"
            :class="tab === 'basic' ? 'border-foreground' : 'border-transparent text-muted-foreground'"
          >
            {{ $t('appMenu.basic') }}
          </TabsTrigger>
          <TabsTrigger
            v-if="showButtons"
            value="buttons"
            class="rounded-none border-b px-0 py-1 shadow-none data-[state=active]:border-foreground data-[state=active]:bg-transparent data-[state=active]:shadow-none"
            :class="tab === 'buttons' ? 'border-foreground' : 'border-transparent text-muted-foreground'"
          >
            {{ $t('appMenu.buttonTab', { count: buttonTotal }) }}
          </TabsTrigger>
        </TabsList>
      </Tabs>
      <div
        v-show="tab === 'basic'"
        class="mt-6"
      >
        <MenuBasicForm ref="formRef" />
        <div
          v-if="canDelete"
          class="mt-10 flex items-center gap-3"
        >
          <Button
            type="button"
            variant="outline"
            class="border-destructive bg-transparent text-destructive shadow-none hover:bg-destructive/10 hover:text-destructive"
            @click="remove"
          >
            {{ $t('appMenu.delete') }}
          </Button>
          <span class="text-sm text-muted-foreground">
            {{ $t('appMenu.deleteCascade') }}
          </span>
        </div>
      </div>
      <div
        v-if="showButtons"
        v-show="tab === 'buttons'"
        class="mt-6"
      >
        <ButtonPermissionTable :coarse="coarse" />
      </div>
    </div>
    <div
      v-if="state.dirty.value && tab === 'basic'"
      class="sticky bottom-0 border-t border-border bg-background px-4 py-3"
    >
      <Button
        type="button"
        class="shadow-none"
        @click="formRef?.save()"
      >
        {{ $t('appMenu.save') }}
      </Button>
    </div>
    <AlertDialog
      :open="pending"
      @update:open="pending = $event"
    >
      <AlertDialogContent class="shadow-none">
        <AlertDialogTitle class="text-base font-medium">
          {{ $t('appMenu.deleteMenuTitle', { name: node?.name ?? '' }) }}
        </AlertDialogTitle>
        <AlertDialogDescription>
          {{
            $t('appMenu.deleteMenuBody', {
              buttons: impact.buttons,
              children: impact.children,
            })
          }}
        </AlertDialogDescription>
        <div class="mt-4 flex justify-end gap-2">
          <Button
            type="button"
            variant="ghost"
            class="shadow-none"
            @click="pending = false"
          >
            {{ $t('appMenu.cancel') }}
          </Button>
          <Button
            type="button"
            variant="ghost"
            class="text-destructive shadow-none"
            @click="confirmRemove"
          >
            {{ $t('appMenu.delete') }}
          </Button>
        </div>
      </AlertDialogContent>
    </AlertDialog>
  </div>
</template>
