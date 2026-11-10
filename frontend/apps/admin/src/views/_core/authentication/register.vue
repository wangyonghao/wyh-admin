<script lang="ts" setup>
import { computed, onMounted, ref } from 'vue';

import { VbenButton } from '#/ui/composed';
import { Input } from '#/ui/input';
import { Label } from '#/ui/label';
import { authApi, type RegisterOptions } from '#/api/auth';
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
const showPassword = ref(false);
const options = ref<RegisterOptions | null>(null);
const optionsError = ref('');
const formError = ref('');
const emailTouched = ref(false);

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

async function handleSubmit() {
  emailTouched.value = true;
  formError.value = '';
  if (!canSubmit.value || userStore.loginLoading) return;
  try {
    await userStore.register({
      email: email.value,
      password: password.value,
    });
  } catch (error) {
    formError.value = messageOf(error);
  }
}

onMounted(loadOptions);
</script>

<template>
  <div class="mx-auto w-full max-w-md">
    <h1 class="text-foreground text-3xl font-bold tracking-tight">
      创建账号
    </h1>
    <p class="text-muted-foreground mt-2 text-sm">
      使用邮箱和密码注册。邮箱暂不验证。
    </p>

    <p
      v-if="optionsError"
      class="text-destructive mt-6 text-sm"
      role="alert"
    >
      {{ optionsError }}
    </p>
    <p
      v-else-if="options && !registrationOpen"
      class="text-destructive mt-6 text-sm"
      role="status"
    >
      系统未开放注册
    </p>

    <form
      class="mt-6 space-y-4"
      @submit.prevent="handleSubmit"
    >
      <div class="flex flex-col gap-2">
        <Label for="register-email">邮箱</Label>
        <Input
          id="register-email"
          v-model="email"
          type="email"
          inputmode="email"
          autocomplete="email"
          placeholder="name@example.com"
          :aria-invalid="emailError ? 'true' : 'false'"
          aria-describedby="register-email-error"
          :class="emailError ? 'border-destructive' : ''"
          @blur="emailTouched = true"
        />
        <p
          id="register-email-error"
          class="text-destructive min-h-5 text-sm"
          role="alert"
        >
          {{ emailError }}
        </p>
      </div>

      <div class="flex flex-col gap-2">
        <Label for="register-password">密码</Label>
        <div class="relative">
          <Input
            id="register-password"
            v-model="password"
            :type="showPassword ? 'text' : 'password'"
            autocomplete="new-password"
            class="pr-12"
            :aria-invalid="password && !passwordValid ? 'true' : 'false'"
            aria-describedby="register-password-rules"
          />
          <button
            type="button"
            class="text-muted-foreground hover:text-foreground absolute inset-y-0 right-0 px-3 text-sm"
            :aria-pressed="showPassword"
            :aria-label="showPassword ? '隐藏密码' : '显示密码'"
            @click="showPassword = !showPassword"
          >
            {{ showPassword ? '隐藏' : '显示' }}
          </button>
        </div>
        <ul
          id="register-password-rules"
          class="space-y-1 text-sm"
        >
          <li
            v-for="rule in rules"
            :key="rule.id"
            class="flex items-center gap-2"
            :class="rule.met ? 'text-primary' : 'text-muted-foreground'"
          >
            <span aria-hidden="true">{{ rule.met ? '✓' : '○' }}</span>
            <span class="sr-only">{{ rule.met ? '已满足' : '未满足' }}</span>
            <span>{{ rule.label }}</span>
          </li>
        </ul>
      </div>

      <p
        v-if="formError"
        class="text-destructive text-sm"
        role="alert"
      >
        {{ formError }}
      </p>

      <VbenButton
        type="submit"
        class="w-full"
        :loading="userStore.loginLoading"
        :disabled="!canSubmit"
        aria-label="注册"
      >
        注册
      </VbenButton>
    </form>

    <p class="text-muted-foreground mt-4 text-center text-sm">
      已有账号？
      <router-link
        class="text-primary underline-offset-4 hover:underline"
        to="/auth/login"
      >
        去登录
      </router-link>
    </p>
  </div>
</template>
