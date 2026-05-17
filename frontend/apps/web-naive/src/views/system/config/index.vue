<script setup lang="ts">
import type {
  EmailConfig,
  LoginConfig,
  RegisterConfig,
  SecurityConfig,
  SiteConfig,
  SmsConfig,
  StorageConfig,
} from '#/api/system/config';

import { onMounted, ref, onUnmounted, watch } from 'vue';

import { IconifyIcon } from '@vben/icons';
import { Page } from '@vben/common-ui';

import {
  NButton,
  NCard,
  NCheckbox,
  NForm,
  NFormItem,
  NInput,
  NInputNumber,
  NSelect,
  NSpace,
  NSplit,
  NDrawer,
  NDrawerContent,
  NAlert,
  NTag,
  NDivider,
  useDialog,
  useMessage,
} from 'naive-ui';

import { configApi } from '#/api/system';

const message = useMessage();
const dialog = useDialog();

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
const sendingTestEmail = ref(false);
const showEmailDrawer = ref(false);
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
  captchaEnabled: true,
  captchaType: 'graphic',
  maxRetry: 5,
  lockTime: 30,
});

const registerForm = ref<RegisterConfig>({
  enabled: true,
  verifyEmail: false,
  verifyPhone: false,
  defaultRoleId: '',
});

const emailForm = ref<EmailConfig>({
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
  sessionTimeout: 30,
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
        loginForm.value = data;
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
    message.success('保存成功');
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

// ==================== 发送测试邮件 ====================
async function handleSendTestEmail() {
  dialog.warning({
    title: '发送测试邮件',
    content: '系统将发送测试邮件到您的邮箱，确认继续吗？',
    positiveText: '确认',
    negativeText: '取消',
    onPositiveClick: async () => {
      sendingTestEmail.value = true;
      try {
        await configApi.sendTestEmail();
        message.success('测试邮件已发送，请查收您的邮箱');
      } catch (error: any) {
        message.error(error.message || '发送失败');
      } finally {
        sendingTestEmail.value = false;
      }
    },
  });
}

// ==================== 打开邮件配置抽屉 ====================
function handleOpenEmailDrawer() {
  showEmailDrawer.value = true;
  // 重置验证状态
  emailVerified.value = false;
  verificationCode.value = '';
  verificationCodeSent.value = false;
  countdown.value = 0;
  if (countdownTimer) {
    clearInterval(countdownTimer);
    countdownTimer = null;
  }
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
    message.warning('请先验证邮箱配置');
    return;
  }
  
  saving.value = true;
  try {
    await configApi.updateEmailConfig(emailForm.value);
    message.success('邮件配置保存成功');
    showEmailDrawer.value = false;
    // 重新加载配置
    await loadConfig('email');
  } finally {
    saving.value = false;
  }
}

// ==================== 发送验证码 ====================
async function handleSendVerificationCode() {
  if (!emailForm.value.username || !emailForm.value.password || !emailForm.value.host) {
    message.warning('请先完整填写邮件配置信息');
    return;
  }

  sendingVerificationCode.value = true;
  try {
    // TODO: 调用后端发送验证码接口
    // await configApi.sendEmailVerificationCode(emailForm.value);
    await configApi.sendTestEmail();
    message.success('验证码已发送到您的邮箱，请查收');
    verificationCodeSent.value = true;
    startCountdown();
  } catch (error: any) {
    message.error(error.message || '发送验证码失败，请检查邮件配置是否正确');
  } finally {
    sendingVerificationCode.value = false;
  }
}

// ==================== 验证验证码 ====================
async function handleVerifyCode() {
  if (!verificationCode.value) {
    message.warning('请输入验证码');
    return;
  }

  if (verificationCode.value.length !== 6) {
    message.warning('请输入6位验证码');
    return;
  }

  verifyingEmail.value = true;
  try {
    // TODO: 调用后端验证验证码接口
    // await configApi.verifyEmailCode(verificationCode.value);
    
    // 模拟验证（实际应该调用后端接口）
    await new Promise(resolve => setTimeout(resolve, 500));
    
    emailVerified.value = true;
    message.success('验证成功，现在可以保存配置了');
  } catch (error: any) {
    message.error(error.message || '验证码错误，请重新输入');
  } finally {
    verifyingEmail.value = false;
  }
}
</script>

<template>
  <Page class="h-full m-4 bg-background">
    <NSplit
      direction="horizontal"
      default-size="200px"
      min="200px"
      max="320px"
      :resizable="true"
      class="h-full"
    >
      <template #1>
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
            <IconifyIcon :icon="item.icon" class="text-lg" />
            <span>{{ item.label }}</span>
          </div>
        </div>
      </template>
      <template #2>
        <!-- 右侧配置表单 -->
        <div class="h-full bg-background p-4 overflow-auto">
          <NCard :title="configList.find((c) => c.key === selectedConfigKey)?.label" :loading="loading">
            <!-- 站点配置 -->
            <NForm
              v-if="selectedConfigKey === 'site'"
              :model="siteForm"
              label-placement="left"
              label-width="120"
            >
              <NFormItem label="站点名称" path="siteName">
                <NInput v-model:value="siteForm.siteName" placeholder="请输入站点名称" />
              </NFormItem>
              <NFormItem label="站点Logo" path="siteLogo">
                <NInput v-model:value="siteForm.siteLogo" placeholder="请输入Logo URL" />
              </NFormItem>
              <NFormItem label="版权信息" path="siteCopyright">
                <NInput v-model:value="siteForm.siteCopyright" placeholder="请输入版权信息" />
              </NFormItem>
              <NFormItem label="ICP备案号" path="siteIcp">
                <NInput v-model:value="siteForm.siteIcp" placeholder="请输入ICP备案号" />
              </NFormItem>
            </NForm>

            <!-- 登录配置 -->
            <NForm
              v-else-if="selectedConfigKey === 'login'"
              :model="loginForm"
              label-placement="left"
              label-width="120"
            >
              <NFormItem label="启用验证码" path="captchaEnabled">
                <NCheckbox v-model:checked="loginForm.captchaEnabled">启用</NCheckbox>
              </NFormItem>
              <NFormItem label="验证码类型" path="captchaType">
                <NSelect
                  v-model:value="loginForm.captchaType"
                  :options="captchaTypeOptions"
                  placeholder="请选择验证码类型"
                />
              </NFormItem>
              <NFormItem label="最大重试次数" path="maxRetry">
                <NInputNumber
                  v-model:value="loginForm.maxRetry"
                  :min="1"
                  :max="10"
                  placeholder="请输入最大重试次数"
                  class="w-full"
                />
              </NFormItem>
              <NFormItem label="锁定时间(分钟)" path="lockTime">
                <NInputNumber
                  v-model:value="loginForm.lockTime"
                  :min="1"
                  :max="1440"
                  placeholder="请输入锁定时间"
                  class="w-full"
                />
              </NFormItem>
            </NForm>

            <!-- 注册配置 -->
            <NForm
              v-else-if="selectedConfigKey === 'register'"
              :model="registerForm"
              label-placement="left"
              label-width="120"
            >
              <NFormItem label="开启注册" path="enabled">
                <NCheckbox v-model:checked="registerForm.enabled">启用</NCheckbox>
              </NFormItem>
              <NFormItem label="邮箱验证" path="verifyEmail">
                <NCheckbox v-model:checked="registerForm.verifyEmail">需要邮箱验证</NCheckbox>
              </NFormItem>
              <NFormItem label="手机验证" path="verifyPhone">
                <NCheckbox v-model:checked="registerForm.verifyPhone">需要手机验证</NCheckbox>
              </NFormItem>
              <NFormItem label="默认角色ID" path="defaultRoleId">
                <NInput v-model:value="registerForm.defaultRoleId" placeholder="请输入默认角色ID" />
              </NFormItem>
            </NForm>

            <!-- 邮件配置 -->
            <div v-else-if="selectedConfigKey === 'email'" class="space-y-4">
              <div class="flex items-center gap-3">
                <IconifyIcon icon="lucide:mail" class="text-2xl text-primary" />
                <div class="flex-1">
                  <div class="flex items-center gap-2">
                    <span class="text-sm text-muted-foreground">发件人：</span>
                    <span class="font-medium">{{ emailForm.from || '未设置' }}</span>
                  </div>
                  <div class="flex items-center gap-2 mt-1">
                    <span class="text-sm text-muted-foreground">邮箱地址：</span>
                    <span class="font-medium">{{ emailForm.username || '未配置' }}</span>
                    <NTag
                      v-if="emailForm.username"
                      :type="emailVerified ? 'success' : 'warning'"
                      size="small"
                    >
                      {{ emailVerified ? '已验证' : '未验证' }}
                    </NTag>
                  </div>
                </div>
                <NButton text type="primary" @click="handleOpenEmailDrawer">
                  更换
                </NButton>
              </div>
            </div>

            <!-- 短信配置 -->
            <NForm
              v-else-if="selectedConfigKey === 'sms'"
              :model="smsForm"
              label-placement="left"
              label-width="120"
            >
              <NFormItem label="服务商" path="provider">
                <NSelect
                  v-model:value="smsForm.provider"
                  :options="smsProviderOptions"
                  placeholder="请选择短信服务商"
                />
              </NFormItem>
              <NFormItem label="AccessKey" path="accessKey">
                <NInput
                  v-model:value="smsForm.accessKey"
                  type="password"
                  show-password-on="click"
                  placeholder="请输入AccessKey"
                />
              </NFormItem>
              <NFormItem label="SecretKey" path="secretKey">
                <NInput
                  v-model:value="smsForm.secretKey"
                  type="password"
                  show-password-on="click"
                  placeholder="请输入SecretKey"
                />
              </NFormItem>
              <NFormItem label="短信签名" path="signName">
                <NInput v-model:value="smsForm.signName" placeholder="请输入短信签名" />
              </NFormItem>
            </NForm>

            <!-- 存储配置 -->
            <NForm
              v-else-if="selectedConfigKey === 'storage'"
              :model="storageForm"
              label-placement="left"
              label-width="120"
            >
              <NFormItem label="存储类型" path="type">
                <NSelect
                  v-model:value="storageForm.type"
                  :options="storageTypeOptions"
                  placeholder="请选择存储类型"
                />
              </NFormItem>
              <NFormItem label="存储端点" path="endpoint">
                <NInput v-model:value="storageForm.endpoint" placeholder="请输入存储端点" />
              </NFormItem>
              <NFormItem label="AccessKey" path="accessKey">
                <NInput
                  v-model:value="storageForm.accessKey"
                  type="password"
                  show-password-on="click"
                  placeholder="请输入AccessKey"
                />
              </NFormItem>
              <NFormItem label="SecretKey" path="secretKey">
                <NInput
                  v-model:value="storageForm.secretKey"
                  type="password"
                  show-password-on="click"
                  placeholder="请输入SecretKey"
                />
              </NFormItem>
              <NFormItem label="存储桶名称" path="bucket">
                <NInput v-model:value="storageForm.bucket" placeholder="请输入存储桶名称" />
              </NFormItem>
            </NForm>

            <!-- 安全配置 -->
            <NForm
              v-else-if="selectedConfigKey === 'security'"
              :model="securityForm"
              label-placement="left"
              label-width="140"
            >
              <NFormItem label="密码最小长度" path="passwordMinLength">
                <NInputNumber
                  v-model:value="securityForm.passwordMinLength"
                  :min="6"
                  :max="32"
                  placeholder="请输入密码最小长度"
                  class="w-full"
                />
              </NFormItem>
              <NFormItem label="需要大写字母" path="passwordRequireUppercase">
                <NCheckbox v-model:checked="securityForm.passwordRequireUppercase">启用</NCheckbox>
              </NFormItem>
              <NFormItem label="需要小写字母" path="passwordRequireLowercase">
                <NCheckbox v-model:checked="securityForm.passwordRequireLowercase">启用</NCheckbox>
              </NFormItem>
              <NFormItem label="需要数字" path="passwordRequireNumber">
                <NCheckbox v-model:checked="securityForm.passwordRequireNumber">启用</NCheckbox>
              </NFormItem>
              <NFormItem label="需要特殊字符" path="passwordRequireSpecial">
                <NCheckbox v-model:checked="securityForm.passwordRequireSpecial">启用</NCheckbox>
              </NFormItem>
              <NFormItem label="会话超时时间(分钟)" path="sessionTimeout">
                <NInputNumber
                  v-model:value="securityForm.sessionTimeout"
                  :min="5"
                  :max="1440"
                  placeholder="请输入会话超时时间"
                  class="w-full"
                />
              </NFormItem>
            </NForm>

            <template #action>
              <NSpace justify="end">
                <NButton @click="handleReset">重置</NButton>
                <NButton type="primary" :loading="saving" @click="handleSave">
                  <template #icon><IconifyIcon icon="lucide:save" /></template>
                  保存
                </NButton>
              </NSpace>
            </template>
          </NCard>
        </div>
      </template>
    </NSplit>

    <!-- 邮件配置抽屉 -->
    <NDrawer v-model:show="showEmailDrawer" :width="600" placement="right">
      <NDrawerContent title="邮件配置" closable>
        <div class="space-y-6">
          <!-- 第一部分：邮件配置表单 -->
          <div>
            <div class="flex items-center gap-2 mb-4">
              <div class="flex items-center justify-center w-6 h-6 rounded-full bg-primary text-white text-sm font-bold">
                1
              </div>
              <span class="text-base font-medium">填写发件箱（SMTP）信息</span>
            </div>

            <NForm
              :model="emailForm"
              label-placement="top"
              require-mark-placement="left"
              :show-feedback="false"
              class="compact-form"
            >
              <NFormItem label="发件人名称" path="from">
                <NInput 
                  v-model:value="emailForm.from" 
                  placeholder="显示在邮件中的发件人名称"
                  :disabled="emailVerified"
                />
              </NFormItem>
              <NFormItem label="发件人邮箱" path="username" required>
                <NInput 
                  v-model:value="emailForm.username" 
                  placeholder="请输入发件人邮箱地址"
                  :disabled="emailVerified"
                />
              </NFormItem>
              <NFormItem label="邮箱密码" path="password" required>
                <NInput
                  v-model:value="emailForm.password"
                  type="password"
                  show-password-on="click"
                  placeholder="请输入邮箱授权码或密码"
                  :disabled="emailVerified"
                />
              </NFormItem>
              <NFormItem label="SMTP服务器" path="host" required>
                <NInput 
                  v-model:value="emailForm.host" 
                  placeholder="例如：smtp.qq.com"
                  :disabled="emailVerified"
                />
              </NFormItem>
              <NFormItem label="SMTP端口" path="port" required>
                <NInputNumber
                  v-model:value="emailForm.port"
                  :min="1"
                  :max="65535"
                  placeholder="例如：465"
                  class="w-full"
                  :disabled="emailVerified"
                />
              </NFormItem>
              <NFormItem label="加密方式" path="sslEnabled" required>
                <NSelect
                  v-model:value="emailForm.sslEnabled"
                  :options="emailProtectionOptions"
                  placeholder="请选择加密方式"
                  :disabled="emailVerified"
                />
              </NFormItem>
            </NForm>
          </div>

          <NDivider />

          <!-- 第二部分：验证邮箱 -->
          <div>
            <div class="flex items-center gap-2 mb-4">
              <div class="flex items-center justify-center w-6 h-6 rounded-full bg-primary text-white text-sm font-bold">
                2
              </div>
              <span class="text-base font-medium">发送测试邮件以验证配置是否正确</span>
            </div>

            <div class="space-y-4">
              <!-- 验证码输入框 -->
              <div class="flex items-center gap-3">
                <NInput
                  v-model:value="verificationCode"
                  placeholder="请输入6位验证码"
                  :maxlength="6"
                  :disabled="emailVerified"
                  @keyup.enter="handleVerifyCode"
                  style="width: 200px;"
                />
                <NButton
                  v-if="countdown === 0"
                  text
                  type="primary"
                  :loading="sendingVerificationCode"
                  :disabled="!emailForm.username || !emailForm.password || !emailForm.host || emailVerified"
                  @click="handleSendVerificationCode"
                >
                  {{ verificationCodeSent ? '重新发送验证码' : '给我发送验证码' }}
                </NButton>
                <span v-else class="text-sm text-muted-foreground">
                  {{ countdown }}秒后可重新发送
                </span>
              </div>

              <!-- 验证成功提示 -->
              <NAlert
                v-if="emailVerified"
                type="success"
                :show-icon="true"
              >
                <template #icon>
                  <IconifyIcon icon="lucide:check-circle" />
                </template>
                验证成功！邮件配置正确，现在可以保存配置了
              </NAlert>
            </div>
          </div>
        </div>

        <template #footer>
          <NSpace justify="end">
            <NButton @click="showEmailDrawer = false">取消</NButton>
            <NButton 
              type="primary" 
              :loading="saving" 
              :disabled="!emailVerified"
              @click="handleSaveEmailConfig"
            >
              <template #icon><IconifyIcon icon="lucide:save" /></template>
              保存配置
            </NButton>
          </NSpace>
        </template>
      </NDrawerContent>
    </NDrawer>
  </Page>
</template>

<style scoped>
.compact-form :deep(.n-form-item) {
  margin-bottom: 12px;
}

.compact-form :deep(.n-form-item:last-child) {
  margin-bottom: 0;
}
</style>
