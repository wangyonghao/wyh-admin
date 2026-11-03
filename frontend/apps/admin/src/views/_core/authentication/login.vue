<script lang="ts" setup>
import type { ImageCaptchaResp } from '#/api';

import { computed, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';

import { AuthenticationThirdPartyLogin, z } from '@vben/common-ui';
import { $t } from '@vben/locales';

import {
  Input,
  VbenButton,
  VbenInputCaptcha,
  VbenInputPassword,
} from '@vben-core/shadcn-ui';

import { captchaApi } from '#/api';
import { useUserStore } from '#/store';
import { Label } from '#/ui/label';

defineOptions({ name: 'Login' });

const userStore = useUserStore();
const router = useRouter();

// 状态
const captchaInfo = ref<ImageCaptchaResp>({
  uuid: '',
  img: '',
  expireTime: 0,
  isEnabled: false,
});
/** 密码错误达到阈值后由 NEED_CAPTCHA 强制展示验证码 */
const forceShowCaptcha = ref(false);
const username = ref('admin');
const password = ref('admin123');
const captcha = ref('');
const loginLoading = ref(false);

const showCaptcha = computed(
  () => captchaInfo.value.isEnabled || forceShowCaptcha.value,
);

// Validation schema using zod
const loginSchema = z.object({
  username: z.string().min(1, $t('authentication.usernameTip')),
  password: z.string().min(1, $t('authentication.passwordTip')),
  captcha: z.string().optional(),
});

// Validation state
const validationErrors = ref<Record<string, string>>({});

// Validate form
const validateField = (field: string, value: string) => {
  try {
    if (field === 'username') {
      loginSchema.shape.username.parse(value);
    } else if (field === 'password') {
      loginSchema.shape.password.parse(value);
    } else if (field === 'captcha' && showCaptcha.value) {
      z.string().min(1, $t('authentication.verifyRequiredTip')).parse(value);
    }
    validationErrors.value[field] = '';
  } catch (error) {
    if (error instanceof z.ZodError) {
      validationErrors.value[field] = error.errors[0]?.message || '';
    }
  }
};

// Validate entire form
const validateForm = () => {
  const formData = {
    username: username.value,
    password: password.value,
    captcha: captcha.value,
  };

  try {
    // Validate basic fields
    loginSchema.parse(formData);

    // Validate captcha if enabled
    if (showCaptcha.value) {
      z.string()
        .min(1, $t('authentication.verifyRequiredTip'))
        .parse(captcha.value);
    }

    validationErrors.value = {};
    return true;
  } catch (error) {
    if (error instanceof z.ZodError) {
      const errors: Record<string, string> = {};
      error.errors.forEach((err) => {
        if (err.path[0]) {
          errors[err.path[0] as string] = err.message;
        }
      });
      validationErrors.value = errors;
    }
    return false;
  }
};

// Get captcha
const getCaptcha = async () => {
  try {
    const res = await captchaApi.getImage();
    const { uuid, img, expireTime, isEnabled } = res;
    captchaInfo.value = {
      uuid: uuid || '',
      img: img || '',
      expireTime: expireTime || 0,
      isEnabled: !!isEnabled,
    };
    captcha.value = '';
    // 始终开启时同步强制展示标志，避免后续逻辑不一致
    if (isEnabled) {
      forceShowCaptcha.value = true;
    }
  } catch (error) {
    console.error('Failed to get captcha:', error);
  }
};

// Submit handler
const handleSubmit = async (event?: Event) => {
  event?.preventDefault();

  if (!validateForm()) {
    return;
  }

  try {
    const loginData = {
      username: username.value,
      password: password.value,
      captchaUuid: captchaInfo.value.uuid,
      captchaChallenge: captcha.value,
    };

    loginLoading.value = true;
    // Call auth login
    await userStore.login(loginData);
  } catch (error: any) {
    console.error('Login failed:', error);
    if (error?.code === 'NEED_CAPTCHA') {
      forceShowCaptcha.value = true;
      await getCaptcha();
      return;
    }
    // Reset captcha on failure
    if (showCaptcha.value) {
      getCaptcha();
    }
  } finally {
    loginLoading.value = false;
  }
};

// Navigation
const handleGo = (path: string) => {
  router.push(path);
};

// Computed properties for validation
const usernameError = computed(() => validationErrors.value.username);
const passwordError = computed(() => validationErrors.value.password);
const captchaError = computed(() => validationErrors.value.captcha);

onMounted(() => {
  getCaptcha();
});
</script>

<template>
  <div>
    <!-- Login Form -->
    <div @keydown.enter.prevent="handleSubmit">
      <!-- Title Section -->
      <div class="mx-auto mb-7 w-full max-w-md">
        <h2
          class="text-foreground mb-3 text-3xl font-bold leading-9 tracking-tight lg:text-4xl"
        >
          {{ $t('authentication.welcomeBack') }} 👋🏻
        </h2>
        <p class="text-muted-foreground lg:text-md text-sm">
          {{ $t('authentication.loginSubtitle') }}
        </p>
      </div>

      <!-- Form Section -->
      <form @submit="handleSubmit">
        <!-- Username Field -->
        <div class="mb-4 flex flex-col gap-2">
          <Label for="username">{{ $t('authentication.username') }}</Label>
          <Input
            id="username"
            v-model="username"
            :placeholder="$t('authentication.usernameTip')"
            :class="{ 'border-red-500': usernameError }"
            @blur="validateField('username', username)"
          />
          <div v-if="usernameError" class="text-sm text-red-500">
            {{ usernameError }}
          </div>
        </div>

        <!-- Password Field -->
        <div class="mb-4 flex flex-col gap-2">
          <div class="flex items-center justify-between">
            <Label for="password">{{ $t('authentication.password') }}</Label>
            <span
              class="text-muted-foreground hover:text-foreground cursor-pointer text-sm font-normal leading-none"
              @click="handleGo('/auth/forget-password')"
            >
              {{ $t('authentication.forgetPassword') }}
            </span>
          </div>
          <VbenInputPassword
            id="password"
            v-model="password"
            :placeholder="$t('authentication.passwordTip')"
            :class="{ 'border-red-500': passwordError }"
            @blur="validateField('password', password)"
          />
          <div v-if="passwordError" class="text-sm text-red-500">
            {{ passwordError }}
          </div>
        </div>

        <!-- Captcha Field (conditional) -->
        <div v-if="showCaptcha" class="mb-4 flex flex-col gap-2">
          <Label for="code">{{ $t('authentication.code') }}</Label>
          <VbenInputCaptcha
            v-model="captcha"
            :captcha="captchaInfo.img"
            :expire-time="captchaInfo.expireTime"
            :placeholder="$t('authentication.code')"
            :class="{
              'border-red-500': captchaError,
              'focus:border-primary': !captchaError,
            }"
            @captcha-click="getCaptcha"
            @blur="validateField('captcha', captcha)"
          />
          <div v-if="captchaError" class="text-sm text-red-500">
            {{ captchaError }}
          </div>
        </div>
      </form>

      <!-- Submit Button -->
      <VbenButton
        :class="{
          'cursor-wait': loginLoading,
        }"
        :loading="loginLoading"
        aria-label="login"
        class="mt-4 w-full"
        @click="handleSubmit"
      >
        {{ $t('common.login') }}
      </VbenButton>

      <!-- Alternative Login Methods -->
      <div class="mb-2 mt-4 flex items-center justify-between">
        <VbenButton
          class="w-1/2"
          variant="outline"
          @click="handleGo('/auth/code-login')"
        >
          {{ $t('authentication.mobileLogin') }}
        </VbenButton>
        <VbenButton
          class="ml-4 w-1/2"
          variant="outline"
          @click="handleGo('/auth/qrcode-login')"
        >
          {{ $t('authentication.qrcodeLogin') }}
        </VbenButton>
      </div>

      <!-- 第三方登录 -->
      <AuthenticationThirdPartyLogin />

      <!-- Register Link -->
      <div class="mt-3 text-center text-sm">
        {{ $t('authentication.accountTip') }}
        <span
          class="text-foreground cursor-pointer text-sm font-normal underline"
          @click="handleGo('/auth/register')"
        >
          {{ $t('authentication.createAccount') }}
        </span>
      </div>
      <p
        class="text-muted-foreground mx-auto mt-6 text-center text-xs leading-5"
      >
        <i18n-t keypath="authentication.legalConsent" scope="global" tag="span">
          <template #terms>
            <a href="/terms" class="underline underline-offset-2">
              {{ $t('authentication.termsOfService') }}
            </a>
          </template>
          <template #privacy>
            <a href="/privacy" class="underline underline-offset-2">
              {{ $t('authentication.privacyPolicyLink') }}
            </a>
          </template>
        </i18n-t>
      </p>
    </div>
</div>
</template>
