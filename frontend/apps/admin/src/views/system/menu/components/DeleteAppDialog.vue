<script setup lang="ts">
import { computed, ref, watch } from 'vue';

import { $t } from '@vben/locales';

import {
  AlertDialog,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogTitle,
} from '#/ui/alert-dialog';
import { Button } from '#/ui/button';
import { Input } from '#/ui/input';

import { toast } from '#/ui-patterns/toast';


import { useMenuState } from '../composables/useMenuState';

const state = useMenuState();
const deleteInput = ref('');
const deleteError = ref('');

const app = computed(
  () => state.apps.value.find((item) => item.id === state.deleteAppId.value) ?? null,
);
const impact = computed(() => (app.value ? state.appImpact(app.value.id) : null));

watch(
  () => state.deleteAppId.value,
  () => {
    deleteInput.value = '';
    deleteError.value = '';
  },
);

function close() {
  state.deleteAppId.value = null;
}

async function confirmDelete() {
  if (!app.value) return;
  if (deleteInput.value.trim() !== app.value.name) {
    deleteError.value = $t('appMenu.nameMismatch');
    return;
  }
  const ok = await state.removeApp(app.value.id);
  if (!ok) return;
  close();
  toast.success($t('appMenu.removed'));
}
</script>

<template>
  <AlertDialog
    :open="!!state.deleteAppId.value"
    @update:open="(open) => !open && close()"
  >
    <AlertDialogContent class="shadow-none">
      <AlertDialogTitle class="text-base font-medium">
        {{ $t('appMenu.deleteAppTitle') }}
      </AlertDialogTitle>
      <AlertDialogDescription v-if="app && impact">
        {{
          $t('appMenu.deleteAppBody', {
            buttons: impact.buttons,
            menus: impact.menus,
            name: app.name,
          })
        }}
      </AlertDialogDescription>
      <Input
        v-model="deleteInput"
        :aria-label="$t('appMenu.confirmName')"
        :placeholder="app?.name"
        class="mt-3 h-8 shadow-none"
      />
      <p
        v-if="deleteError"
        class="mt-2 text-xs text-destructive"
      >
        {{ deleteError }}
      </p>
      <div class="mt-4 flex justify-end gap-2">
        <AlertDialogCancel type="button">
          {{ $t('appMenu.cancel') }}
        </AlertDialogCancel>
        <Button
          type="button"
          variant="ghost"
          class="text-destructive shadow-none"
          @click="confirmDelete"
        >
          {{ $t('appMenu.confirmDelete') }}
        </Button>
      </div>
    </AlertDialogContent>
  </AlertDialog>
</template>
