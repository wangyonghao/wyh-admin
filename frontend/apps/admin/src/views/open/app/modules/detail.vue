<script lang="ts" setup>
import type { OpenAppApi } from '#/api/open/app';

import { computed, ref } from 'vue';

import { useVbenDrawer } from '#/components/popup/drawer';
import { $t } from '@vben/locales';

import { Badge } from '#/ui/badge';
import { Button } from '#/ui/button';
import { toast } from '#/ui-patterns/toast';

const appData = ref<OpenAppApi.AppResp>();

const [Drawer, drawerApi] = useVbenDrawer({
  onOpenChange(isOpen) {
    if (isOpen) {
      const data = drawerApi.getData<OpenAppApi.AppResp>();
      if (data) {
        appData.value = data;
      }
    }
  },
  showConfirmButton: false,
  cancelText: $t('common.cancel'),
});

const getDrawerTitle = computed(() => {
  return $t('open.app.expireTime');
});

// 复制到剪贴板
const copyToClipboard = async (text: string) => {
  try {
    await navigator.clipboard.writeText(text);
    toast.success('复制成功');
  } catch (error) {
    console.error('复制失败:', error);
    toast.error('复制失败');
  }
};
</script>

<template>
  <Drawer :title="getDrawerTitle">
    <div class="p-4">
      <dl class="grid grid-cols-2 gap-x-6 gap-y-4 text-sm">
        <div>
          <dt class="text-muted-foreground">ID</dt>
          <dd class="mt-1">{{ appData?.id }}</dd>
        </div>
        <div>
          <dt class="text-muted-foreground">{{ $t('open.app.name') }}</dt>
          <dd class="mt-1">{{ appData?.name }}</dd>
        </div>
        <div class="col-span-2">
          <dt class="text-muted-foreground">{{ $t('open.app.accessKey') }}</dt>
          <dd class="mt-1">
            <span class="font-mono text-sm">{{ appData?.accessKey }}</span>
            <Button
              v-if="appData?.accessKey"
              type="button"
              size="sm"
              class="ml-2"
              @click="copyToClipboard(appData.accessKey)"
            >
              {{ $t('open.app.copy') }}
            </Button>
          </dd>
        </div>
        <div>
          <dt class="text-muted-foreground">{{ $t('open.app.status') }}</dt>
          <dd class="mt-1">
            <Badge v-if="appData?.status === 1" variant="success">启用</Badge>
            <Badge v-else variant="destructive">禁用</Badge>
          </dd>
        </div>
        <div>
          <dt class="text-muted-foreground">{{ $t('open.app.expireTime') }}</dt>
          <dd class="mt-1">{{ appData?.expireTime }}</dd>
        </div>
        <div>
          <dt class="text-muted-foreground">{{ $t('open.app.createUser') }}</dt>
          <dd class="mt-1">{{ appData?.createUserString }}</dd>
        </div>
        <div>
          <dt class="text-muted-foreground">{{ $t('open.app.createTime') }}</dt>
          <dd class="mt-1">{{ appData?.createTime }}</dd>
        </div>
        <div>
          <dt class="text-muted-foreground">{{ $t('open.app.updateUser') }}</dt>
          <dd class="mt-1">{{ appData?.updateUserString }}</dd>
        </div>
        <div>
          <dt class="text-muted-foreground">{{ $t('open.app.updateTime') }}</dt>
          <dd class="mt-1">{{ appData?.updateTime }}</dd>
        </div>
        <div class="col-span-2">
          <dt class="text-muted-foreground">{{ $t('open.app.description') }}</dt>
          <dd class="mt-1">{{ appData?.description }}</dd>
        </div>
      </dl>
    </div>
  </Drawer>
</template>

<style scoped lang="scss"></style>
