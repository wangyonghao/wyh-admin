<script setup lang="ts">
import type {
  MailConfig,
  LoginConfig,
  RegisterConfig,
  SecurityConfig,
  SiteConfig,
  SmsConfig,
  StorageConfig,
} from '#/api/system/config';

import { nextTick, onMounted, onUnmounted, ref, watch } from 'vue';

import { ColPage } from '@vben/common-ui';
import { useVbenDrawer } from '#/components/popup/drawer';
import { IconifyIcon } from '@vben/icons';

import { configApi } from '#/api/system';
import { Badge } from '#/ui/badge';
import { Button } from '#/ui/button';
import {
  Card,
  CardContent,
  CardFooter,
  CardHeader,
  CardTitle,
} from '#/ui/card';
import { Input } from '#/ui/input';
import { Separator } from '#/ui/separator';
import { toast } from '#/ui-patterns/toast';

import { useConfigForms } from './use-config-forms';

// ==================== 配置类型定义 ====================
interface ConfigItem {
  key: string;
  label: string;
  icon: string;
  permission?: string;
}

const configList: ConfigItem[] = [
  { key: 'site', label: '站点配置', icon: 'lucide:globe' },
  { key: 'login', label: '登录配置', icon: 'lucide:log-in' },
  { key: 'register', label: '注册配置', icon: 'lucide:user-plus' },
  {
    key: 'email',
    label: '邮件配置',
    icon: 'lucide:mail',
    permission: 'system:config:edit',
  },
  {
    key: 'sms',
    label: '短信配置',
    icon: 'lucide:message-square',
    permission: 'system:config:edit',
  },
  {
    key: 'storage',
    label: '存储配置',
    icon: 'lucide:hard-drive',
    permission: 'system:config:edit',
  },
  { key: 'security', label: '安全配置', icon: 'lucide:shield' },
];

// ==================== 当前选中的配置 ====================
const selectedConfigKey = ref<string>('site');
const loading = ref(false);
const saving = ref(false);
const [EmailDrawer, emailDrawerApi] = useVbenDrawer({
  class: 'w-[600px]',
  title: '邮件配置',
  showCancelButton: false,
  showConfirmButton: false,
});
const verifyingEmail = ref(false);
const emailVerified = ref(false);
const verificationCode = ref('');
const sendingVerificationCode = ref(false);
const verificationCodeSent = ref(false);
const countdown = ref(0);
let countdownTimer: NodeJS.Timeout | null = null;

// ==================== 监听验证码输入，自动验证 ====================
watch(verificationCode, (newValue) => {
  if (newValue.length === 6 && !emailVerified.value) {
    handleVerifyCode();
  }
});

// ==================== 各配置表单数据 ====================
const siteForm = ref<SiteConfig>({
  siteName: '',
  siteLogo: '',
  siteCopyright: '',
  siteIcp: '',
});

const loginForm = ref<LoginConfig>({
  captchaErrorThreshold: 2,
  captchaType: 'graphic',
  maxRetry: 5,
  lockTime: 30,
  sessionTimeout: 30,
});

const registerForm = ref<RegisterConfig>({
  enabled: true,
  verifyEmail: false,
  verifyPhone: false,
  defaultRoleId: '',
});

const emailForm = ref<MailConfig>({
  host: '',
  port: 465,
  username: '',
  password: '',
  from: '',
  sslEnabled: true,
});

const smsForm = ref<SmsConfig>({
  provider: 'aliyun',
  accessKey: '',
  secretKey: '',
  signName: '',
});

const storageForm = ref<StorageConfig>({
  type: 'local',
  endpoint: '',
  accessKey: '',
  secretKey: '',
  bucket: '',
});

const securityForm = ref<SecurityConfig>({
  passwordMinLength: 8,
  passwordRequireUppercase: true,
  passwordRequireLowercase: true,
  passwordRequireNumber: true,
  passwordRequireSpecial: false,
  passwordExpireDays: 90,
  passwordAllowContainUsername: false,
  passwordRepetitionTimes: 3,
});

// ==================== 选项数据 ====================
const captchaTypeOptions = [
  { label: '图形验证码', value: 'graphic' },
  { label: '行为验证码', value: 'behavior' },
];

const smsProviderOptions = [
  { label: '阿里云', value: 'aliyun' },
  { label: '腾讯云', value: 'tencent' },
];

const storageTypeOptions = [
  { label: '本地存储', value: 'local' },
  { label: '阿里云OSS', value: 'oss' },
  { label: 'Amazon S3', value: 's3' },
];

const emailProtectionOptions = [
  { label: 'SSL', value: true },
  { label: 'STARTTLS', value: false },
];

const {
  EmailForm,
  LoginForm,
  RegisterForm,
  SecurityForm,
  SiteForm,
  SmsForm,
  StorageForm,
  apply: applyConfigForm,
} = useConfigForms({
  email: emailForm,
  emailVerified,
  login: loginForm,
  register: registerForm,
  security: securityForm,
  site: siteForm,
  sms: smsForm,
  storage: storageForm,
  captchaTypeOptions,
  emailProtectionOptions,
  smsProviderOptions,
  storageTypeOptions,
});

// ==================== 加载配置数据 ====================
async function loadConfig(configKey: string) {
  loading.value = true;
  try {
    switch (configKey) {
      case 'site': {
        const data = await configApi.getSiteConfig();
        siteForm.value = data;
        break;
      }
      case 'login': {
        const data = await configApi.getLoginConfig();
        loginForm.value = {
          ...data,
          captchaErrorThreshold:
            data.captchaErrorThreshold ??
            (data.captchaEnabled === false ? -1 : 2),
        };
        break;
      }
      case 'register': {
        const data = await configApi.getRegisterConfig();
        registerForm.value = data;
        break;
      }
      case 'email': {
        const data = await configApi.getEmailConfig();
        emailForm.value = data;
        // 默认状态为未验证
        emailVerified.value = false;
        break;
      }
      case 'sms': {
        const data = await configApi.getSmsConfig();
        smsForm.value = data;
        break;
      }
      case 'storage': {
        const data = await configApi.getStorageConfig();
        storageForm.value = data;
        break;
      }
      case 'security': {
        const data = await configApi.getSecurityConfig();
        securityForm.value = data;
        break;
      }
    }
  } finally {
    loading.value = false;
  }
  await nextTick();
  // 邮件表单在抽屉里，未打开时还没挂载；setValues 会一直等到挂载。
  if (configKey === 'email') return;
  const current =
    configKey === 'site'
      ? siteForm.value
      : configKey === 'login'
        ? loginForm.value
        : configKey === 'register'
          ? registerForm.value
          : configKey === 'sms'
            ? smsForm.value
            : configKey === 'storage'
              ? storageForm.value
              : securityForm.value;
  await applyConfigForm(configKey, current);
}

// ==================== 保存配置 ====================
async function handleSave() {
  saving.value = true;
  try {
    switch (selectedConfigKey.value) {
      case 'site': {
        await configApi.updateSiteConfig(siteForm.value);
        break;
      }
      case 'login': {
        await configApi.updateLoginConfig(loginForm.value);
        break;
      }
      case 'register': {
        await configApi.updateRegisterConfig(registerForm.value);
        break;
      }
      case 'email': {
        await configApi.updateEmailConfig(emailForm.value);
        break;
      }
      case 'sms': {
        await configApi.updateSmsConfig(smsForm.value);
        break;
      }
      case 'storage': {
        await configApi.updateStorageConfig(storageForm.value);
        break;
      }
      case 'security': {
        await configApi.updateSecurityConfig(securityForm.value);
        break;
      }
    }
    toast.success('保存成功');
  } finally {
    saving.value = false;
  }
}

// ==================== 重置表单 ====================
function handleReset() {
  loadConfig(selectedConfigKey.value);
}

// ==================== 切换配置类型 ====================
function handleSelectConfig(configKey: string) {
  selectedConfigKey.value = configKey;
  loadConfig(configKey);
}

// ==================== 初始化 ====================
onMounted(() => {
  loadConfig(selectedConfigKey.value);
});

// ==================== 清理定时器 ====================
onUnmounted(() => {
  if (countdownTimer) {
    clearInterval(countdownTimer);
    countdownTimer = null;
  }
});

// ==================== 打开邮件配置抽屉 ====================
async function handleOpenEmailDrawer() {
  emailVerified.value = false;
  verificationCode.value = '';
  verificationCodeSent.value = false;
  countdown.value = 0;
  if (countdownTimer) {
    clearInterval(countdownTimer);
    countdownTimer = null;
  }
  emailDrawerApi.open();
  await nextTick();
  await applyConfigForm('email', emailForm.value);
}

// ==================== 开始倒计时 ====================
function startCountdown() {
  countdown.value = 30;
  countdownTimer = setInterval(() => {
    countdown.value--;
    if (countdown.value <= 0) {
      if (countdownTimer) {
        clearInterval(countdownTimer);
        countdownTimer = null;
      }
    }
  }, 1000);
}

// ==================== 保存邮件配置 ====================
async function handleSaveEmailConfig() {
  if (!emailVerified.value) {
    toast.warning('请先验证邮箱配置');
    return;
  }

  saving.value = true;
  try {
    await configApi.updateEmailConfig(emailForm.value);
    toast.success('邮件配置保存成功');
    emailDrawerApi.close();
    // 重新加载配置
    await loadConfig('email');
  } finally {
    saving.value = false;
  }
}

// ==================== 发送验证码 ====================
async function handleSendVerificationCode() {
  if (
    !emailForm.value.username ||
    !emailForm.value.password ||
    !emailForm.value.host
  ) {
    toast.warning('请先完整填写邮件配置信息');
    return;
  }

  sendingVerificationCode.value = true;
  try {
    // TODO: 调用后端发送验证码接口
    // await configApi.sendEmailVerificationCode(emailForm.value);
    await configApi.sendTestEmail();
    toast.success('验证码已发送到您的邮箱，请查收');
    verificationCodeSent.value = true;
    startCountdown();
  } catch (error: any) {
    toast.error(error.message || '发送验证码失败，请检查邮件配置是否正确');
  } finally {
    sendingVerificationCode.value = false;
  }
}

// ==================== 验证验证码 ====================
async function handleVerifyCode() {
  if (!verificationCode.value) {
    toast.warning('请输入验证码');
    return;
  }

  if (verificationCode.value.length !== 6) {
    toast.warning('请输入6位验证码');
    return;
  }

  verifyingEmail.value = true;
  try {
    // TODO: 调用后端验证验证码接口
    // await configApi.verifyEmailCode(verificationCode.value);

    // 模拟验证（实际应该调用后端接口）
    await new Promise((resolve) => setTimeout(resolve, 500));

    emailVerified.value = true;
    toast.success('验证成功，现在可以保存配置了');
  } catch (error: any) {
    toast.error(error.message || '验证码错误，请重新输入');
  } finally {
    verifyingEmail.value = false;
  }
}
</script>

<template>
  <ColPage
    class="m-4 h-full bg-background"
    :left-width="200"
    :left-min-width="200"
    :left-max-width="320"
    left-size-unit="px"
    resizable
    split-line
  >
    <template #left>
      <!-- 左侧配置列表 -->
      <div class="space-y-2">
        <div
          v-for="item in configList"
          :key="item.key"
          class="flex items-center gap-2 px-3 py-2 rounded cursor-pointer transition-colors"
          :class="
            selectedConfigKey === item.key
              ? 'bg-primary text-primary-foreground'
              : 'hover:bg-muted'
          "
          @click="handleSelectConfig(item.key)"
        >
          <IconifyIcon
            :icon="item.icon"
            class="text-lg"
          />
          <span>{{ item.label }}</span>
        </div>
      </div>
    </template>
    <!-- 右侧配置表单 -->
    <div class="h-full bg-background p-4 overflow-auto">
      <Card>
        <CardHeader>
          <CardTitle>
            {{ configList.find((c) => c.key === selectedConfigKey)?.label }}
          </CardTitle>
        </CardHeader>
        <CardContent>
          <p v-if="loading" class="text-sm text-muted-foreground">加载中…</p>
          <template v-else>
            <SiteForm v-if="selectedConfigKey === 'site'" />
            <LoginForm v-else-if="selectedConfigKey === 'login'" />
            <RegisterForm v-else-if="selectedConfigKey === 'register'" />
            <div
              v-else-if="selectedConfigKey === 'email'"
              class="space-y-4"
            >
              <div class="flex items-center gap-3">
                <IconifyIcon
                  icon="lucide:mail"
                  class="text-2xl text-primary"
                />
                <div class="flex-1">
                  <div class="flex items-center gap-2">
                    <span class="text-sm text-muted-foreground">发件人：</span>
                    <span class="font-medium">{{
                      emailForm.from || '未设置'
                    }}</span>
                  </div>
                  <div class="flex items-center gap-2 mt-1">
                    <span class="text-sm text-muted-foreground">邮箱地址：</span>
                    <span class="font-medium">{{
                      emailForm.username || '未配置'
                    }}</span>
                    <Badge
                      v-if="emailForm.username"
                      :variant="emailVerified ? 'success' : 'warning'"
                    >
                      {{ emailVerified ? '已验证' : '未验证' }}
                    </Badge>
                  </div>
                </div>
                <Button
                  type="button"
                  variant="link"
                  class="h-auto px-0"
                  @click="handleOpenEmailDrawer"
                >
                  更换
                </Button>
              </div>
            </div>
            <SmsForm v-else-if="selectedConfigKey === 'sms'" />
            <StorageForm v-else-if="selectedConfigKey === 'storage'" />
            <SecurityForm v-else-if="selectedConfigKey === 'security'" />
          </template>
        </CardContent>
        <CardFooter class="justify-end gap-2">
          <Button
            type="button"
            variant="outline"
            @click="handleReset"
          >
            重置
          </Button>
          <Button
            type="button"
            :loading="saving"
            @click="handleSave"
          >
            <IconifyIcon
              icon="lucide:save"
              class="mr-1 size-4"
            />
            保存
          </Button>
        </CardFooter>
      </Card>
    </div>

    <EmailDrawer>
      <div class="space-y-6">
        <div>
          <div class="flex items-center gap-2 mb-4">
            <div
              class="flex items-center justify-center w-6 h-6 rounded-full bg-primary text-white text-sm font-bold"
            >
              1
            </div>
            <span class="text-base font-medium">填写发件箱（SMTP）信息</span>
          </div>
          <EmailForm />
        </div>

        <Separator />

        <div>
          <div class="flex items-center gap-2 mb-4">
            <div
              class="flex items-center justify-center w-6 h-6 rounded-full bg-primary text-white text-sm font-bold"
            >
              2
            </div>
            <span class="text-base font-medium">发送测试邮件以验证配置是否正确</span>
          </div>

          <div class="space-y-4">
            <div class="flex items-center gap-3">
              <Input
                v-model="verificationCode"
                placeholder="请输入6位验证码"
                maxlength="6"
                class="w-[200px]"
                :disabled="emailVerified"
                @keyup.enter="handleVerifyCode"
              />
              <Button
                v-if="countdown === 0"
                type="button"
                variant="link"
                class="h-auto px-1"
                :loading="sendingVerificationCode"
                :disabled="
                  !emailForm.username ||
                    !emailForm.password ||
                    !emailForm.host ||
                    emailVerified
                "
                @click="handleSendVerificationCode"
              >
                {{
                  verificationCodeSent ? '重新发送验证码' : '给我发送验证码'
                }}
              </Button>
              <span
                v-else
                class="text-sm text-muted-foreground"
              >
                {{ countdown }}秒后可重新发送
              </span>
            </div>

            <div
              v-if="emailVerified"
              class="flex items-center gap-2 rounded-md border border-green-200 bg-green-50 px-3 py-2 text-sm text-green-800 dark:border-green-900 dark:bg-green-950 dark:text-green-200"
            >
              <IconifyIcon icon="lucide:check-circle" />
              验证成功！邮件配置正确，现在可以保存配置了
            </div>
          </div>
        </div>
      </div>

      <template #footer>
        <div class="flex justify-end gap-2">
          <Button
            type="button"
            variant="outline"
            @click="emailDrawerApi.close()"
          >
            取消
          </Button>
          <Button
            type="button"
            :loading="saving"
            :disabled="!emailVerified"
            @click="handleSaveEmailConfig"
          >
            <IconifyIcon
              icon="lucide:save"
              class="mr-1 size-4"
            />
            保存配置
          </Button>
        </div>
      </template>
    </EmailDrawer>
  </ColPage>
</template>
