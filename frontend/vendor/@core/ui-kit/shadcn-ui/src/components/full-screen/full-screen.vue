<script lang="ts" setup>
import { ref } from 'vue';

import { Maximize, Minimize } from '@vben-core/icons';
import { useEventListener } from '@vueuse/core';

import { VbenIconButton } from '../button';

defineOptions({ name: 'FullScreen' });

type FullscreenDocument = Document & {
  mozCancelFullScreen?: () => Promise<void> | void;
  mozFullScreenElement?: Element | null;
  msExitFullscreen?: () => Promise<void> | void;
  msFullscreenElement?: Element | null;
  webkitExitFullscreen?: () => Promise<void> | void;
  webkitFullscreenElement?: Element | null;
};

type FullscreenRoot = HTMLElement & {
  mozRequestFullScreen?: () => Promise<void> | void;
  msRequestFullscreen?: () => Promise<void> | void;
  webkitRequestFullscreen?: () => Promise<void> | void;
};

const isFullscreen = ref(false);

function activeFullscreenElement() {
  const doc = document as FullscreenDocument;
  return (
    document.fullscreenElement ||
    doc.webkitFullscreenElement ||
    doc.mozFullScreenElement ||
    doc.msFullscreenElement ||
    null
  );
}

function syncFullscreen() {
  isFullscreen.value = !!activeFullscreenElement();
}

// useFullscreen 用 webkitIsFullScreen 判断。标准 requestFullscreen 之后该标志仍可能为 false，
// toggle 会再次进入全屏，按钮看起来点了没有反应。
async function toggleFullscreen() {
  const doc = document as FullscreenDocument;
  const root = document.documentElement as FullscreenRoot;
  try {
    if (activeFullscreenElement()) {
      if (document.exitFullscreen) {
        await document.exitFullscreen();
      } else if (doc.webkitExitFullscreen) {
        await doc.webkitExitFullscreen();
      } else if (doc.mozCancelFullScreen) {
        await doc.mozCancelFullScreen();
      } else {
        await doc.msExitFullscreen?.();
      }
    } else if (root.requestFullscreen) {
      await root.requestFullscreen();
    } else if (root.webkitRequestFullscreen) {
      await root.webkitRequestFullscreen();
    } else if (root.mozRequestFullScreen) {
      await root.mozRequestFullScreen();
    } else {
      await root.msRequestFullscreen?.();
    }
  } catch {
    // 手势或策略被拒绝时，图标跟当前 document 状态走。
  }
  syncFullscreen();
}

syncFullscreen();
useEventListener(document, 'fullscreenchange', syncFullscreen);
useEventListener(document, 'webkitfullscreenchange', syncFullscreen);
useEventListener(document, 'mozfullscreenchange', syncFullscreen);
useEventListener(document, 'MSFullscreenChange', syncFullscreen);
</script>
<template>
  <VbenIconButton
    class="hover:animate-[shrink_0.3s_ease-in-out]"
    @click="toggleFullscreen"
  >
    <Minimize v-if="isFullscreen" class="text-foreground size-4" />
    <Maximize v-else class="text-foreground size-4" />
  </VbenIconButton>
</template>
