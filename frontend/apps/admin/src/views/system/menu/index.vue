<script setup lang="ts">
import { computed, onMounted } from 'vue';
import { onBeforeRouteUpdate, useRoute, useRouter } from 'vue-router';

import { Page } from '@vben/common-ui';
import { $t } from '@vben/locales';

import { useBreakpoints, useMediaQuery } from '@vueuse/core';

import {
  AlertDialog,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogTitle,
} from '#/ui/alert-dialog';
import { Button } from '#/ui/button';
import {
  ResizableHandle,
  ResizablePanel,
  ResizablePanelGroup,
} from '#/ui/resizable';
import { TooltipProvider } from '#/ui/tooltip';

import DeleteAppDialog from './components/DeleteAppDialog.vue';
import DetailPane from './components/DetailPane.vue';
import MenuTree from './components/MenuTree.vue';
import { useMenuState } from './composables/useMenuState';

const route = useRoute();
const router = useRouter();
const state = useMenuState();

const breakpoints = useBreakpoints({ tablet: 768 });
const tablet = breakpoints.greaterOrEqual('tablet');
const coarse = useMediaQuery('(pointer: coarse)');
const mobileDetail = computed(
  () => !tablet.value && route.query.view === 'detail',
);

onMounted(async () => {
  await state.load();
  const appId = typeof route.query.app === 'string' ? route.query.app : '';
  const nodeId = typeof route.query.node === 'string' ? route.query.node : '';
  if (nodeId) await state.selectNode(nodeId);
  else if (appId) await state.selectApp(appId);
});

onBeforeRouteUpdate(async () => {
  if (!state.dirty.value) return true;
  const choice = await state.guardDirty();
  return choice !== 'cancel';
});

function enterDetail() {
  if (tablet.value) return;
  void router.push({
    query: {
      ...route.query,
      app: state.selectedAppId.value ?? undefined,
      node: state.selectedNodeId.value ?? undefined,
      view: 'detail',
    },
  });
}

function backToTree() {
  void router.push({
    query: {
      app: state.selectedAppId.value ?? undefined,
      node: state.selectedNodeId.value ?? undefined,
    },
  });
}
</script>

<template>
  <Page
    auto-content-height
    content-class="!p-0 overflow-hidden"
  >
    <TooltipProvider :delay-duration="300">
      <div class="flex h-full min-h-0 flex-col">
        <div
          v-if="mobileDetail"
          class="flex items-center border-b border-border px-2"
        >
          <button
            type="button"
            class="px-2 py-3 text-sm text-foreground/80 transition-colors duration-150 hover:text-foreground"
            @click="backToTree"
          >
            {{ $t('appMenu.back') }}
          </button>
        </div>

        <ResizablePanelGroup
          v-if="tablet"
          class="min-h-0 flex-1"
          direction="horizontal"
        >
          <ResizablePanel
            :default-size="28"
            :max-size="42"
            :min-size="18"
          >
            <MenuTree :coarse="coarse" />
          </ResizablePanel>
          <ResizableHandle />
          <ResizablePanel
            :default-size="72"
            :min-size="40"
          >
            <DetailPane :coarse="coarse" />
          </ResizablePanel>
        </ResizablePanelGroup>

        <div
          v-else
          class="min-h-0 flex-1"
        >
          <MenuTree
            v-if="!mobileDetail"
            :coarse="true"
            @open="enterDetail"
          />
          <DetailPane
            v-else
            :coarse="true"
          />
        </div>
      </div>

      <AlertDialog
        :open="state.confirmOpen.value"
        @update:open="(open) => !open && state.settleDirty('cancel')"
      >
        <AlertDialogContent class="shadow-none">
          <AlertDialogTitle class="text-base font-medium">
            {{ $t('appMenu.dirtyTitle') }}
          </AlertDialogTitle>
          <AlertDialogDescription>{{ $t('appMenu.dirtyBody') }}</AlertDialogDescription>
          <div class="mt-4 flex justify-end gap-2">
            <Button
              type="button"
              variant="ghost"
              class="shadow-none"
              @click="state.settleDirty('cancel')"
            >
              {{ $t('appMenu.dirtyCancel') }}
            </Button>
            <Button
              type="button"
              variant="ghost"
              class="shadow-none"
              @click="state.settleDirty('discard')"
            >
              {{ $t('appMenu.dirtyDiscard') }}
            </Button>
            <Button
              type="button"
              class="shadow-none"
              @click="state.settleDirty('save')"
            >
              {{ $t('appMenu.dirtySave') }}
            </Button>
          </div>
        </AlertDialogContent>
      </AlertDialog>

      <AlertDialog
        :open="state.prefixOpen.value"
        @update:open="(open) => !open && state.settlePrefix('abort')"
      >
        <AlertDialogContent class="shadow-none">
          <AlertDialogTitle class="text-base font-medium">
            {{ $t('appMenu.prefixTitle') }}
          </AlertDialogTitle>
          <AlertDialogDescription>
            {{
              $t('appMenu.prefixBody', {
                from: state.prefixPair.value.from,
                to: state.prefixPair.value.to,
              })
            }}
          </AlertDialogDescription>
          <div class="mt-4 flex justify-end gap-2">
            <Button
              type="button"
              variant="ghost"
              class="shadow-none"
              @click="state.settlePrefix('abort')"
            >
              {{ $t('appMenu.cancel') }}
            </Button>
            <Button
              type="button"
              variant="ghost"
              class="shadow-none"
              @click="state.settlePrefix('keep')"
            >
              {{ $t('appMenu.prefixKeep') }}
            </Button>
            <Button
              type="button"
              class="shadow-none"
              @click="state.settlePrefix('sync')"
            >
              {{ $t('appMenu.prefixSync') }}
            </Button>
          </div>
        </AlertDialogContent>
      </AlertDialog>

      <DeleteAppDialog />
    </TooltipProvider>
  </Page>
</template>
