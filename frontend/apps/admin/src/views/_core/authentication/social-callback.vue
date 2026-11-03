<script lang="ts" setup>
import { onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import { LOGIN_PATH } from '@vben/constants';

import { useUserStore } from '#/store';
import { toast } from '#/ui-patterns/toast';

defineOptions({ name: 'SocialCallback' });

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();

const loading = ref(true);
const errorMsg = ref('');

onMounted(async () => {
  // 对齐 GitHub OAuth App：/auth/github/callback；其它 provider 仍可读 params/query
  const pathProvider = route.path.includes('/auth/github/callback')
    ? 'github'
    : '';
  const provider = String(
    pathProvider || route.params.provider || route.query.provider || '',
  ).toLowerCase();
  const code = String(route.query.code || '');
  const state = String(route.query.state || '');

  if (!provider || !code || !state) {
    loading.value = false;
    errorMsg.value = '缺少授权参数，请重新发起登录';
    toast.error(errorMsg.value);
    setTimeout(() => router.replace(LOGIN_PATH), 1500);
    return;
  }

  try {
    await userStore.socialLogin({ provider, code, state });
  } catch (error: any) {
    loading.value = false;
    errorMsg.value = error?.message || '第三方登录失败';
    toast.error(errorMsg.value);
    setTimeout(() => router.replace(LOGIN_PATH), 2000);
  }
});
</script>

<template>
  <div class="flex min-h-screen items-center justify-center">
    <div class="text-center text-sm text-muted-foreground">
      <p v-if="loading">正在完成第三方登录…</p>
      <p v-else class="text-destructive">{{ errorMsg }}</p>
    </div>
  </div>
</template>
