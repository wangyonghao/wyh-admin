<script lang="ts" setup>
import { ref } from 'vue';

import { VbenButton } from '@vben-core/shadcn-ui';

import { orgApi } from '#/api/org';
import { Input } from '#/ui/input';
import { toast } from '#/ui-patterns/toast';

defineOptions({ name: 'OrgInviteAdmin' });

const email = ref('');
const loading = ref(false);
const lastLink = ref('');

async function send() {
  if (!email.value.trim()) {
    toast.error('请输入邮箱');
    return;
  }
  loading.value = true;
  try {
    const res = await orgApi.invite(email.value.trim());
    toast.success('邀请邮件已发送');
    if (res.token) {
      lastLink.value = `${location.origin}/org/invite?token=${res.token}`;
    }
    email.value = '';
  } finally {
    loading.value = false;
  }
}
</script>

<template>
  <div class="p-6">
    <h1 class="mb-2 text-xl font-semibold">邀请成员</h1>
    <p class="text-muted-foreground mb-6 text-sm">
      向邮箱发送加入当前组织的邀请链接。被邀请人注册/登录后可确认或拒绝。
    </p>
    <div class="flex max-w-lg gap-3">
      <Input v-model="email" class="flex-1" placeholder="成员邮箱" />
      <VbenButton :loading="loading" @click="send">发送邀请</VbenButton>
    </div>
    <p v-if="lastLink" class="text-muted-foreground mt-4 break-all text-xs">
      开发调试链接：{{ lastLink }}
    </p>
  </div>
</template>
