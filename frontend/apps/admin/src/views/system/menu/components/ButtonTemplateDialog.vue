<script setup lang="ts">
import { ref } from 'vue';

import { $t } from '@vben/locales';

import { Button } from '#/ui/button';
import {
  Dialog,
  DialogContent,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '#/ui/dialog';

const open = defineModel<boolean>('open', { default: false });
const emit = defineEmits<{ apply: [actions: string[]] }>();

const templates = [
  { actions: ['list', 'create', 'update', 'delete'], id: 'crud', label: 'appMenu.templateCrud' },
  {
    actions: ['list', 'create', 'update', 'delete', 'import', 'export'],
    id: 'crud-io',
    label: 'appMenu.templateCrudIo',
  },
  {
    actions: ['submit', 'approve', 'reject'],
    id: 'audit',
    label: 'appMenu.templateAudit',
  },
] as const;

const current = ref<(typeof templates)[number]['id']>('crud');

function confirm() {
  const template = templates.find((item) => item.id === current.value);
  if (!template) return;
  emit('apply', [...template.actions]);
  open.value = false;
}
</script>

<template>
  <Dialog v-model:open="open">
    <DialogContent
      animation-type="scale"
      modal
      :open="open"
      class="left-1/2 top-1/2 w-[calc(100%-2rem)] max-w-md -translate-x-1/2 -translate-y-1/2 shadow-none"
    >
      <DialogHeader>
        <DialogTitle class="text-base font-medium">
          {{ $t('appMenu.templates') }}
        </DialogTitle>
      </DialogHeader>
      <div class="space-y-2">
        <button
          v-for="template in templates"
          :key="template.id"
          type="button"
          class="block w-full border-b border-transparent py-2 text-left text-sm transition-colors duration-150"
          :class="current === template.id ? 'border-foreground font-medium' : 'text-muted-foreground'"
          @click="current = template.id"
        >
          {{ $t(template.label) }}
        </button>
      </div>
      <DialogFooter>
        <Button
          type="button"
          variant="ghost"
          class="shadow-none"
          @click="open = false"
        >
          {{ $t('appMenu.cancel') }}
        </Button>
        <Button
          type="button"
          class="shadow-none"
          @click="confirm"
        >
          {{ $t('appMenu.save') }}
        </Button>
      </DialogFooter>
    </DialogContent>
  </Dialog>
</template>
