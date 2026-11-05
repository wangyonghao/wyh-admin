<script lang="ts" setup>
import type { ColPageProps } from './types';

import { computed, ref, useAttrs, useSlots } from 'vue';

import {
  ResizableHandle,
  ResizablePanel,
  ResizablePanelGroup,
} from '#/ui/resizable';

import Page from '../page/page.vue';

import { resolveSplitPanes } from './split-size';

defineOptions({
  name: 'ColPage',
  inheritAttrs: false,
});

const props = withDefaults(defineProps<ColPageProps>(), {
  leftWidth: 30,
  rightWidth: 70,
  resizable: true,
  leftSizeUnit: '%',
  rightSizeUnit: '%',
});

const attrs = useAttrs();

const delegatedProps = computed(() => {
  const {
    leftWidth: _leftWidth,
    leftMinWidth: _leftMinWidth,
    leftMaxWidth: _leftMaxWidth,
    leftCollapsedWidth: _leftCollapsedWidth,
    leftCollapsible: _leftCollapsible,
    leftSizeUnit: _leftSizeUnit,
    rightWidth: _rightWidth,
    rightMinWidth: _rightMinWidth,
    rightMaxWidth: _rightMaxWidth,
    rightCollapsedWidth: _rightCollapsedWidth,
    rightCollapsible: _rightCollapsible,
    rightSizeUnit: _rightSizeUnit,
    resizable: _resizable,
    splitLine: _splitLine,
    splitHandle: _splitHandle,
    ...pageProps
  } = props;
  return pageProps;
});

const panes = computed(() =>
  resolveSplitPanes({
    left: {
      width: props.leftWidth,
      minWidth: props.leftMinWidth,
      maxWidth: props.leftMaxWidth,
      collapsedWidth: props.leftCollapsedWidth,
      sizeUnit: props.leftSizeUnit,
    },
    right: {
      width: props.rightWidth,
      minWidth: props.rightMinWidth,
      maxWidth: props.rightMaxWidth,
      collapsedWidth: props.rightCollapsedWidth,
      sizeUnit: props.rightSizeUnit,
    },
  }),
);

const slots = useSlots();

const delegatedSlots = computed(() => {
  const resultSlots: string[] = [];

  for (const key of Object.keys(slots)) {
    if (!['default', 'left'].includes(key)) {
      resultSlots.push(key);
    }
  }
  return resultSlots;
});

const leftPanelRef = ref<InstanceType<typeof ResizablePanel>>();

function expandLeft() {
  leftPanelRef.value?.expand();
}

function collapseLeft() {
  leftPanelRef.value?.collapse();
}

defineExpose({
  expandLeft,
  collapseLeft,
});
</script>
<template>
  <Page v-bind="{ ...delegatedProps, ...attrs }">
    <!-- 继承默认的slot -->
    <template
      v-for="slotName in delegatedSlots"
      :key="slotName"
      #[slotName]="slotProps"
    >
      <slot :name="slotName" v-bind="slotProps"></slot>
    </template>

    <ResizablePanelGroup class="h-full w-full" direction="horizontal">
      <ResizablePanel
        ref="leftPanelRef"
        :collapsed-size="panes.left.collapsedSize"
        :collapsible="leftCollapsible"
        :default-size="panes.left.defaultSize"
        :max-size="panes.left.maxSize"
        :min-size="panes.left.minSize"
        :size-unit="panes.left.sizeUnit"
      >
        <template #default="slotProps">
          <slot
            name="left"
            v-bind="{
              ...slotProps,
              expand: expandLeft,
              collapse: collapseLeft,
            }"
          ></slot>
        </template>
      </ResizablePanel>
      <ResizableHandle
        v-if="resizable"
        :style="{ backgroundColor: splitLine ? undefined : 'transparent' }"
        :with-handle="splitHandle"
      />
      <ResizablePanel
        :collapsed-size="panes.right.collapsedSize"
        :collapsible="rightCollapsible"
        :default-size="panes.right.defaultSize"
        :max-size="panes.right.maxSize"
        :min-size="panes.right.minSize"
        :size-unit="panes.right.sizeUnit"
      >
        <template #default>
          <slot></slot>
        </template>
      </ResizablePanel>
    </ResizablePanelGroup>
  </Page>
</template>
