<script lang="ts" setup>
import { computed, onMounted, onUnmounted, ref } from 'vue';

import { VbenButton, VbenInputPassword } from '#/ui/composed';
import { Input } from '#/ui/input';
import { Label } from '#/ui/label';
import { authApi, type EmailRegistrationResult, type RegisterOptions } from '#/api/auth';
import { useUserStore } from '#/store';
import { isLoginEmail } from '#/utils/login-identifier';
import {
  passwordMeetsPolicy,
  passwordRuleStates,
  type PasswordPolicyView,
} from '#/utils/password-rules';
import { BusinessError } from '#/utils/business-error';
import { resolveHttpErrorMessage } from '#/utils/request';
import axios from 'axios';

defineOptions({ name: 'Register' });

const userStore = useUserStore();

const email = ref('');
const password = ref('');
const options = ref<RegisterOptions | null>(null);
const optionsError = ref('');
const formError = ref('');
const emailTouched = ref(false);
const passwordTouched = ref(false);
const pending = ref<EmailRegistrationResult | null>(null);
const submittedEmail = ref('');
const resendWait = ref(0);
let resendTimer = 0;

const policy = computed<PasswordPolicyView | null>(() => {
  if (!options.value) return null;
  return {
    minLength: options.value.minLength,
    maxLength: options.value.maxLength,
    requireLowercase: options.value.requireLowercase,
    requireDigit: options.value.requireDigit,
    requireSpecial: options.value.requireSpecial,
  };
});

const rules = computed(() =>
  policy.value ? passwordRuleStates(password.value, policy.value) : [],
);
const emailValid = computed(() => isLoginEmail(email.value));
const passwordValid = computed(() =>
  policy.value ? passwordMeetsPolicy(password.value, policy.value) : false,
);
const registrationOpen = computed(() => options.value?.registrationEnabled === true);
const canSubmit = computed(
  () =>
    registrationOpen.value &&
    emailValid.value &&
    passwordValid.value &&
    !userStore.loginLoading,
);
const emailError = computed(() => {
  if (!emailTouched.value || email.value.trim() === '') return '';
  return emailValid.value ? '' : '邮箱格式不正确';
});
const showPasswordError = computed(
  () => passwordTouched.value && policy.value != null && !passwordValid.value,
);

async function loadOptions() {
  optionsError.value = '';
  try {
    options.value = await authApi.registerOptions();
  } catch (error) {
    options.value = null;
    optionsError.value = messageOf(error);
  }
}

function messageOf(error: unknown): string {
  if (error instanceof BusinessError) return error.message || '注册失败，请稍后重试';
  if (axios.isAxiosError(error)) return resolveHttpErrorMessage(error);
  return '注册失败，请稍后重试';
}

function startResendWait(seconds: number) {
  window.clearInterval(resendTimer);
  resendWait.value = Math.max(0, seconds);
  if (resendWait.value === 0) return;
  resendTimer = window.setInterval(() => {
    resendWait.value -= 1;
    if (resendWait.value <= 0) window.clearInterval(resendTimer);
  }, 1000);
}

async function handleSubmit() {
  emailTouched.value = true;
  passwordTouched.value = true;
  formError.value = '';
  if (!canSubmit.value || userStore.loginLoading) return;
  try {
    const result = await userStore.register({
      email: email.value,
      password: password.value,
    });
    password.value = '';
    submittedEmail.value = email.value.trim();
    pending.value = result;
    startResendWait(result.resendAfterSeconds);
  } catch (error) {
    formError.value = messageOf(error);
  }
}

async function resend() {
  formError.value = '';
  if (!submittedEmail.value || resendWait.value > 0 || userStore.loginLoading) return;
  try {
    const result = await authApi.resendRegistration({ email: submittedEmail.value });
    pending.value = result;
    startResendWait(result.resendAfterSeconds);
  } catch (error) {
    formError.value = messageOf(error);
  }
}

onMounted(loadOptions);
onUnmounted(() => window.clearInterval(resendTimer));
</script>

<template>
  <div>
    <div class="mx-auto mb-7 w-full max-w-md">
      <h2 class="text-foreground mb-3 text-3xl leading-9 tracking-tight lg:text-4xl">
        创建账号
      </h2>
      <p class="text-muted-foreground lg:text-md text-sm">
        使用邮箱和密码注册。我们会发送验证链接，确认后才能登录。
        <template v-if="options">
          链接 {{ options.verificationTtlMinutes }} 分钟内有效，重发至少间隔
          {{ options.resendIntervalSeconds }} 秒。
        </template>
      </p>
      <p v-if="optionsError" class="mt-3 text-sm text-red-500" role="alert">
        {{ optionsError }}
      </p>
      <p
        v-else-if="options && !registrationOpen"
        class="mt-3 text-sm text-red-500"
        role="status"
      >
        系统未开放注册
      </p>
    </div>

    <div v-if="pending" class="flex flex-col gap-4" role="status">
      <p class="text-foreground text-sm">
        {{
          pending.delivered
            ? '验证邮件已发送。请打开邮件中的链接，并在页面上点击确认。打开链接本身不会完成注册。'
            : '还没到重发时间，这次没有新发邮件。'
        }}
      </p>
      <p class="text-muted-foreground text-sm">{{ submittedEmail }}</p>
      <p v-if="formError" class="text-sm text-red-500" role="alert">{{ formError }}</p>
      <VbenButton
        type="button"
        class="w-full font-normal"
        :disabled="resendWait > 0 || userStore.loginLoading"
        :loading="userStore.loginLoading"
        aria-label="重发验证邮件"
        @click="resend"
      >
        {{ resendWait > 0 ? `重发验证邮件（${resendWait} 秒）` : '重发验证邮件' }}
      </VbenButton>
    </div>

    <form v-else class="flex flex-col gap-4" @submit.prevent="handleSubmit">
      <div class="flex flex-col gap-2">
        <Label for="register-email" class="font-normal">邮箱</Label>
        <Input
          id="register-email"
          v-model="email"
          type="email"
          inputmode="email"
          autocomplete="email"
          placeholder="name@example.com"
          class="h-9"
          :aria-invalid="emailError ? 'true' : 'false'"
          :aria-describedby="emailError ? 'register-email-error' : undefined"
          :class="{ 'border-red-500': emailError }"
          @blur="emailTouched = true"
        />
        <div
          v-if="emailError"
          id="register-email-error"
          class="text-sm text-red-500"
          role="alert"
        >
          {{ emailError }}
        </div>
      </div>

      <div class="flex flex-col gap-2">
        <Label for="register-password" class="font-normal">密码</Label>
        <VbenInputPassword
          id="register-password"
          v-model="password"
          autocomplete="new-password"
          class="h-9"
          :aria-invalid="showPasswordError ? 'true' : 'false'"
          aria-describedby="register-password-rules"
          :class="{ 'border-red-500': showPasswordError }"
          @blur="passwordTouched = true"
        />
        <ul id="register-password-rules" class="space-y-1 text-sm">
          <li
            v-for="rule in rules"
            :key="rule.id"
            class="flex items-center gap-2"
            :class="
              rule.met
                ? 'text-primary'
                : showPasswordError
                  ? 'text-red-500'
                  : 'text-muted-foreground'
            "
          >
            <span aria-hidden="true">{{ rule.met ? '✓' : '○' }}</span>
            <span class="sr-only">{{ rule.met ? '已满足' : '未满足' }}</span>
            <span>{{ rule.label }}</span>
          </li>
        </ul>
      </div>

      <div v-if="formError" class="text-sm text-red-500" role="alert">
        {{ formError }}
      </div>

      <VbenButton
        type="submit"
        class="mb-6 w-full font-normal"
        :class="{ 'cursor-wait': userStore.loginLoading }"
        :loading="userStore.loginLoading"
        :disabled="!canSubmit"
        aria-label="注册"
      >
        注册
      </VbenButton>
    </form>

    <div class="mt-3 text-center text-sm">
      已有账号？
      <router-link class="text-foreground font-normal underline" to="/auth/login">
        去登录
      </router-link>
    </div>
  </div>
</template>
