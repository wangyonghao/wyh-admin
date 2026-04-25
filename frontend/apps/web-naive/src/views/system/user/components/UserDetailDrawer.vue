<script setup lang="ts">
import type { UserResp } from '#/api/system/user';

import { ref, watch } from 'vue';

import { IconifyIcon } from '@vben/icons';

import {
  NButton,
  NDescriptions,
  NDescriptionsItem,
  NDrawer,
  NDrawerContent,
  NIcon,
  NTag,
  useMessage,
} from 'naive-ui';

import { userApi } from '#/api/system/user';

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

const message = useMessage();

const detailData = ref<null | UserResp>(null);
const detailLoading = ref(false);

watch(
  () => props.visible,
  async (newVal) => {
    if (newVal && props.userId) {
      await loadUserDetail();
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
    message.error('加载用户详情失败');
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
  emit('update:visible', false);
}

function handleEdit() {
  if (detailData.value) {
    emit('edit', detailData.value);
    handleClose();
  }
}
</script>

<template>
  <NDrawer
    :show="visible"
    :width="600"
    placement="right"
    @update:show="handleClose"
  >
    <NDrawerContent title="用户详情" closable>
      <div v-if="detailLoading" class="flex items-center justify-center py-20">
        <NIcon size="40" class="animate-spin">
          <IconifyIcon icon="lucide:loader-2" />
        </NIcon>
      </div>
      <NDescriptions v-else-if="detailData" :column="2" label-placement="left">
        <NDescriptionsItem label="用户名">
          {{ detailData.username || '-' }}
        </NDescriptionsItem>
        <NDescriptionsItem label="昵称">
          {{ detailData.nickname || '-' }}
        </NDescriptionsItem>
        <NDescriptionsItem label="性别">
          {{ getGenderLabel(detailData.gender) }}
        </NDescriptionsItem>
        <NDescriptionsItem label="邮箱">
          {{ detailData.email || '-' }}
        </NDescriptionsItem>
        <NDescriptionsItem label="手机号">
          {{ detailData.phone || '-' }}
        </NDescriptionsItem>
        <NDescriptionsItem label="部门">
          {{ detailData.deptName || '-' }}
        </NDescriptionsItem>
        <NDescriptionsItem label="角色">
          {{ detailData.roleNames || '-' }}
        </NDescriptionsItem>
        <NDescriptionsItem label="状态">
          <NTag :type="getStatusType(detailData.status)" size="small">
            {{ getStatusLabel(detailData.status) }}
          </NTag>
        </NDescriptionsItem>
        <NDescriptionsItem label="描述" :span="2">
          {{ detailData.description || '-' }}
        </NDescriptionsItem>
        <NDescriptionsItem label="创建时间">
          {{ detailData.createTime || '-' }}
        </NDescriptionsItem>
        <NDescriptionsItem label="更新时间">
          {{ detailData.updateTime || '-' }}
        </NDescriptionsItem>
      </NDescriptions>

      <template #footer>
        <div class="flex justify-end gap-2">
          <NButton @click="handleClose"> 关闭 </NButton>
          <NButton type="primary" @click="handleEdit">
            <template #icon>
              <IconifyIcon icon="lucide:pencil" />
            </template>
            编辑
          </NButton>
        </div>
      </template>
    </NDrawerContent>
  </NDrawer>
</template>
