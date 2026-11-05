<script lang="ts" setup>
import type { DropdownMenuProps } from '@wyh/ui/composed';

import { LayoutGrid } from '@vben-core/icons';
import { VbenDropdownMenu } from '@wyh/ui/composed';

defineProps<DropdownMenuProps>();
</script>

<template>
  <VbenDropdownMenu :menus="menus" :modal="false">
    <div
      class="flex-center h-full cursor-pointer border-l border-border px-2 text-lg font-semibold text-muted-foreground hover:bg-muted hover:text-foreground"
    >
      <LayoutGrid class="size-4" />
    </div>
  </VbenDropdownMenu>
</template>
