<script lang="ts" setup>
import { computed, onMounted, ref } from 'vue';

import { VbenButton, VbenInputPassword } from '#/ui/composed';
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
const options = ref<RegisterOptions | null>(null);
const optionsError = ref('');
const formError = ref('');
const emailTouched = ref(false);
const passwordTouched = ref(false);

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

async function handleSubmit() {
  emailTouched.value = true;
  passwordTouched.value = true;
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
  <div>
    <div class="mx-auto mb-7 w-full max-w-md">
      <h2 class="text-foreground mb-3 text-3xl leading-9 tracking-tight lg:text-4xl">
        创建账号
      </h2>
      <p class="text-muted-foreground lg:text-md text-sm">
        使用邮箱和密码注册。邮箱暂不验证。
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

    <form class="flex flex-col gap-4" @submit.prevent="handleSubmit">
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
