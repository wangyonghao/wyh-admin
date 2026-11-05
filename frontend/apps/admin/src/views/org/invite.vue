<script lang="ts" setup>
import { onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import { VbenButton } from '#/ui/composed';

import { orgApi, type OrgInviteResult } from '#/api/org';
import { toast } from '#/ui-patterns/toast';
import { useUserStore } from '#/store';

defineOptions({ name: 'OrgInvite' });

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();

const loading = ref(true);
const acting = ref(false);
const invite = ref<OrgInviteResult | null>(null);
const token = ref('');
const error = ref('');

async function load() {
  token.value = String(route.query.token || '');
  if (!token.value) {
    error.value = '缺少邀请令牌，请从邮件链接进入';
    loading.value = false;
    return;
  }
  if (!userStore.token) {
    await router.replace({
      path: '/auth/login',
      query: { redirect: encodeURIComponent(route.fullPath) },
    });
    return;
  }
  try {
    invite.value = await orgApi.previewInvite(token.value);
  } catch (e: any) {
    error.value = e?.message || '邀请无效或已过期';
  } finally {
    loading.value = false;
  }
}

async function accept() {
  acting.value = true;
  try {
    await orgApi.acceptInvite(token.value);
    toast.success('已接受邀请');
    await userStore.fetchAuthInfo();
    await router.replace('/dashboard');
  } finally {
    acting.value = false;
  }
}

async function reject() {
  acting.value = true;
  try {
    await orgApi.rejectInvite(token.value);
    toast.success('已拒绝邀请');
    await router.replace('/org/setup');
  } finally {
    acting.value = false;
  }
}

onMounted(load);
</script>

<template>
  <div class="mx-auto max-w-lg px-4 py-10">
    <h1 class="text-foreground mb-2 text-2xl font-bold">组织邀请</h1>
    <div v-if="loading" class="text-muted-foreground text-sm">加载中…</div>
    <div v-else-if="error" class="text-sm text-red-500">{{ error }}</div>
    <div v-else-if="invite" class="space-y-4 rounded-lg border p-4">
      <p>
        你被邀请加入组织
        <span class="font-semibold">{{ invite.orgName || invite.tenantId }}</span>
      </p>
      <p class="text-muted-foreground text-sm">
        组织编码：{{ invite.orgCode || '-' }}<br />
        邀请邮箱：{{ invite.email }}<br />
        过期时间：{{ invite.expireTime || '-' }}
      </p>
      <div class="flex gap-3">
        <VbenButton :loading="acting" class="flex-1" @click="accept">
          接受邀请
        </VbenButton>
        <VbenButton
          :loading="acting"
          class="flex-1"
          variant="outline"
          @click="reject"
        >
          拒绝
        </VbenButton>
      </div>
    </div>
  </div>
</template>
