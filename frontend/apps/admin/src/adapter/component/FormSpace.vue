<script lang="ts" setup>
import { cn } from '@wyh/ui/cn';

import { spaceClass, spaceStyle, type FormSpaceProps } from './space-layout';

defineOptions({
  name: 'FormSpace',
  inheritAttrs: false,
});

const props = defineProps<FormSpaceProps>();
</script>

<template>
  <div
    :class="cn(spaceClass(props))"
    :style="spaceStyle(props.size)"
  >
    <slot />
  </div>
</template>
