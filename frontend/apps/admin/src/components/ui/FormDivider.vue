<script lang="ts" setup>
import { Separator } from '#/ui/separator';

import { dividerClass, dividerOrientation } from './divider-props';

defineOptions({
  name: 'FormDivider',
  inheritAttrs: false,
});

defineProps<{
  dashed?: boolean;
  title?: string;
  vertical?: boolean;
}>();
</script>

<template>
  <div
    v-if="!vertical && (title || $slots.default)"
    class="flex w-full items-center gap-3"
  >
    <Separator
      :orientation="dividerOrientation(vertical)"
      :class="dividerClass(vertical, dashed)"
    />
    <span class="text-muted-foreground shrink-0 text-sm">
      <slot>{{ title }}</slot>
    </span>
    <Separator
      :orientation="dividerOrientation(vertical)"
      :class="dividerClass(vertical, dashed)"
    />
  </div>
  <Separator
    v-else
    :orientation="dividerOrientation(vertical)"
    :class="dividerClass(vertical, dashed)"
  />
</template>
