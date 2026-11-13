<script setup lang="ts">
import type { UserResp } from '#/api/system/user';

import { ref, watch } from 'vue';

import { useVbenDrawer } from '#/components/popup/drawer';
import { IconifyIcon } from '@vben/icons';

import { userApi } from '#/api/system/user';
import { Badge } from '#/ui/badge';
import { badgeVariantForTag } from '#/ui/badge/variant';
import { Button } from '#/ui/button';
import { toast } from '#/ui-patterns/toast';

interface Props {
  visible: boolean;
  userId?: string;
}

interface Emits {
  (e: 'update:visible', value: boolean): void;
  (e: 'edit', user: UserResp): void;
}

const props = defineProps<Props>();
const emit = defineEmits<Emits>();


const detailData = ref<null | UserResp>(null);
const detailLoading = ref(false);

const [Drawer, drawerApi] = useVbenDrawer({
  class: 'w-[600px]',
  title: '用户详情',
  onOpenChange(isOpen) {
    if (!isOpen) emit('update:visible', false);
  },
});

watch(
  () => props.visible,
  async (open) => {
    if (open) {
      drawerApi.open();
      if (props.userId) await loadUserDetail();
    } else {
      drawerApi.close();
    }
  },
);

async function loadUserDetail() {
  if (!props.userId) return;

  detailLoading.value = true;
  try {
    const res = await userApi.detail(props.userId);
    detailData.value = res;
  } catch (error) {
    console.error('加载用户详情失败:', error);
    toast.error('加载用户详情失败');
    handleClose();
  } finally {
    detailLoading.value = false;
  }
}

function getGenderLabel(gender?: number) {
  const genderMap: Record<number, string> = {
    0: '未知',
    1: '男',
    2: '女',
  };
  return genderMap[gender ?? 0] || '未知';
}

function getStatusLabel(status?: number) {
  const statusMap: Record<number, string> = {
    0: '禁用',
    1: '启用',
    2: '待审核',
    3: '审核拒绝',
  };
  return statusMap[status ?? 1] || '未知';
}

function getStatusType(
  status?: number,
): 'error' | 'info' | 'success' | 'warning' {
  const statusTypeMap: Record<
    number,
    'error' | 'info' | 'success' | 'warning'
  > = {
    0: 'error',
    1: 'success',
    2: 'warning',
    3: 'error',
  };
  return statusTypeMap[status ?? 1] || 'info';
}

function handleClose() {
  drawerApi.close();
}

function handleEdit() {
  if (detailData.value) {
    emit('edit', detailData.value);
    handleClose();
  }
}
</script>

<template>
  <Drawer>
    <div
      v-if="detailLoading"
      class="flex items-center justify-center py-20"
    >
      <IconifyIcon
        icon="lucide:loader-2"
        class="size-10 animate-spin"
      />
    </div>
    <dl
      v-else-if="detailData"
      class="grid grid-cols-2 gap-x-6 gap-y-4 text-sm"
    >
      <div>
        <dt class="text-muted-foreground">用户名</dt>
        <dd class="mt-1">{{ detailData.username || '-' }}</dd>
      </div>
      <div>
        <dt class="text-muted-foreground">显示名称</dt>
        <dd class="mt-1">{{ detailData.displayName || '-' }}</dd>
      </div>
      <div>
        <dt class="text-muted-foreground">性别</dt>
        <dd class="mt-1">{{ getGenderLabel(detailData.gender) }}</dd>
      </div>
      <div>
        <dt class="text-muted-foreground">邮箱</dt>
        <dd class="mt-1">{{ detailData.email || '-' }}</dd>
      </div>
      <div>
        <dt class="text-muted-foreground">手机号</dt>
        <dd class="mt-1">{{ detailData.phone || '-' }}</dd>
      </div>
      <div>
        <dt class="text-muted-foreground">部门</dt>
        <dd class="mt-1">{{ detailData.departmentName || '-' }}</dd>
      </div>
      <div>
        <dt class="text-muted-foreground">角色</dt>
        <dd class="mt-1">{{ detailData.roleNames || '-' }}</dd>
      </div>
      <div>
        <dt class="text-muted-foreground">状态</dt>
        <dd class="mt-1">
          <Badge
            :variant="
              badgeVariantForTag(getStatusType(detailData.status)) ||
                'secondary'
            "
          >
            {{ getStatusLabel(detailData.status) }}
          </Badge>
        </dd>
      </div>
      <div class="col-span-2">
        <dt class="text-muted-foreground">描述</dt>
        <dd class="mt-1">{{ detailData.description || '-' }}</dd>
      </div>
      <div>
        <dt class="text-muted-foreground">创建时间</dt>
        <dd class="mt-1">{{ detailData.createTime || '-' }}</dd>
      </div>
      <div>
        <dt class="text-muted-foreground">更新时间</dt>
        <dd class="mt-1">{{ detailData.updateTime || '-' }}</dd>
      </div>
    </dl>

    <template #footer>
      <div class="flex w-full justify-end gap-2">
        <Button
          type="button"
          variant="outline"
          @click="handleClose"
        >
          关闭
        </Button>
        <Button
          type="button"
          @click="handleEdit"
        >
          <IconifyIcon
            icon="lucide:pencil"
            class="mr-1 size-4"
          />
          编辑
        </Button>
      </div>
    </template>
  </Drawer>
</template>
