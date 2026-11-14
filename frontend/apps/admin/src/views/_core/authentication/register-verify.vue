<script lang="ts" setup>
import { onMounted, ref } from 'vue';

import { VbenButton } from '#/ui/composed';
import { authApi } from '#/api/auth';

import { readRegistrationToken } from '#/utils/registration-link';

defineOptions({ name: 'RegisterVerify' });

type Phase = 'confirm' | 'working' | 'success' | 'already' | 'invalid';

const phase = ref<Phase>('confirm');
let token = '';

onMounted(() => {
  const referrer = document.createElement('meta');
  referrer.name = 'referrer';
  referrer.content = 'no-referrer';
  document.head.appendChild(referrer);
  const read = readRegistrationToken(window.location.hash);
  token = read.token;
  if (read.shouldClear) {
    window.history.replaceState(null, '', window.location.pathname + window.location.search);
  }
  if (!token) phase.value = 'invalid';
});

async function confirm() {
  if (!token || phase.value === 'working') return;
  phase.value = 'working';
  try {
    const result = await authApi.confirmRegistration({ token });
    token = '';
    phase.value = result.status === 'ALREADY_VERIFIED' ? 'already' : 'success';
  } catch {
    token = '';
    phase.value = 'invalid';
  }
}
</script>

<template>
  <div class="mx-auto w-full max-w-md">
    <h2 class="text-foreground mb-3 text-3xl leading-9 tracking-tight">确认注册</h2>

    <div v-if="phase === 'confirm' || phase === 'working'" class="flex flex-col gap-4">
      <p class="text-muted-foreground text-sm">
        请点击下面的按钮完成注册。打开邮件链接不会自动确认，这样可以避免邮件扫描把链接用掉。
      </p>
      <VbenButton
        type="button"
        class="w-full font-normal"
        :loading="phase === 'working'"
        :disabled="phase === 'working'"
        aria-label="确认完成注册"
        @click="confirm"
      >
        确认完成注册
      </VbenButton>
    </div>

    <div v-else-if="phase === 'success'" class="flex flex-col gap-4" role="status">
      <p class="text-foreground text-sm">注册已完成。请使用邮箱和密码登录。</p>
      <router-link class="text-foreground text-sm font-normal underline" to="/auth/login">
        去登录
      </router-link>
    </div>

    <div v-else-if="phase === 'already'" class="flex flex-col gap-4" role="status">
      <p class="text-foreground text-sm">这个链接已经确认过。请直接登录，不需要再次注册。</p>
      <router-link class="text-foreground text-sm font-normal underline" to="/auth/login">
        去登录
      </router-link>
    </div>

    <div v-else class="flex flex-col gap-4" role="alert">
      <p class="text-sm text-red-500">链接无效或已过期。请回到注册页重新发送验证邮件。</p>
      <router-link class="text-foreground text-sm font-normal underline" to="/auth/register">
        返回注册
      </router-link>
    </div>
  </div>
</template>
