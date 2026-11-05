<script setup lang="ts">
import { computed } from 'vue';
import { Badge } from '#/ui/badge';
import { badgeVariantForTag } from '#/ui/badge/variant';
import { Separator } from '#/ui/separator';
import { VbenAvatar } from '#/ui/composed';
import { $t } from '#/locales';
import type { UserProfile } from '#/api/auth';
import { resolveFilePreviewUrl } from '#/api/system/file';

interface Props {
  userInfo?: UserProfile;
}

const props = defineProps<Props>();

// 性别显示
const genderText = computed(() => {
  if (!props.userInfo) return '';
  switch (props.userInfo.gender) {
    case 1:
      return $t('page.profile.basic.male');
    case 2:
      return $t('page.profile.basic.female');
    default:
      return $t('page.profile.basic.unknown');
  }
});

// 账号状态
const accountStatus = computed(() => {
  return {
    text: $t('page.profile.summary.active'),
    type: 'success' as const,
  };
});

// 安全等级（根据绑定情况计算）
const securityLevel = computed(() => {
  if (!props.userInfo)
    return { text: $t('page.profile.summary.low'), type: 'error' as const };

  let score = 0;
  if (props.userInfo.phone) score += 1;
  if (props.userInfo.email) score += 1;

  if (score >= 2) {
    return { text: $t('page.profile.summary.high'), type: 'success' as const };
  } else if (score === 1) {
    return {
      text: $t('page.profile.summary.medium'),
      type: 'warning' as const,
    };
  }
  return { text: $t('page.profile.summary.low'), type: 'error' as const };
});

// 手机号脱敏
const maskedPhone = computed(() => {
  if (!props.userInfo?.phone) return $t('page.profile.summary.phoneUnbound');
  const phone = props.userInfo.phone;
  return phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2');
});

// 邮箱脱敏
const maskedEmail = computed(() => {
  if (!props.userInfo?.email) return $t('page.profile.summary.emailUnbound');
  const email = props.userInfo.email;
  const [name = '', domain = ''] = email.split('@');
  if (name.length <= 2) return email;
  return `${name.slice(0, 2)}***@${domain}`;
});
</script>

<template>
  <div class="profile-summary-card rounded-xl bg-card p-4 shadow-sm">
    <div class="text-center">
      <!-- 头像 -->
      <VbenAvatar
        :src="resolveFilePreviewUrl(userInfo?.avatar ?? undefined) ?? ''"
        :alt="userInfo?.displayName ?? 'User'"
        :size="100"
        class="mb-4 mx-auto"
      />

      <!-- 显示名称 -->
      <h3 class="text-xl font-semibold mb-2">
        {{ userInfo?.displayName || '-' }}
      </h3>

      <!-- 用户名 -->
      <p class="text-sm text-muted-foreground">
        @{{ userInfo?.username || '-' }}
      </p>

      <!-- 账号状态 -->
      <div class="mt-4">
        <Badge :variant="badgeVariantForTag(accountStatus.type) || 'secondary'">
          {{ accountStatus.text }}
        </Badge>
      </div>
    </div>

    <Separator class="my-4" />

    <!-- 基本信息 -->
    <div class="flex flex-col gap-3">
      <div class="info-item">
        <span class="info-label">{{ $t('page.profile.basic.gender') }}</span>
        <span class="info-value">{{ genderText }}</span>
      </div>

      <div class="info-item">
        <span class="info-label">{{ $t('page.profile.basic.department') }}</span>
        <span class="info-value">{{ userInfo?.departmentName || '-' }}</span>
      </div>

      <div class="info-item">
        <span class="info-label">{{ $t('page.profile.basic.phone') }}</span>
        <span class="info-value">{{ maskedPhone }}</span>
      </div>

      <div class="info-item">
        <span class="info-label">{{ $t('page.profile.basic.email') }}</span>
        <span class="info-value">{{ maskedEmail }}</span>
      </div>

      <div class="info-item">
        <span class="info-label">{{
          $t('page.profile.basic.registrationDate')
        }}</span>
        <span class="info-value">{{ userInfo?.registrationDate || '-' }}</span>
      </div>
    </div>

    <Separator class="my-4" />

    <!-- 安全等级 -->
    <div class="security-level">
      <div class="flex items-center justify-between mb-2">
        <span class="text-sm text-gray-600">{{
          $t('page.profile.summary.securityLevel')
        }}</span>
        <Badge :variant="badgeVariantForTag(securityLevel.type) || 'secondary'">
          {{ securityLevel.text }}
        </Badge>
      </div>

      <!-- 绑定状态 -->
      <div class="mt-3 flex flex-col gap-2">
        <div class="flex items-center text-xs">
          <span :class="userInfo?.phone ? 'text-green-600' : 'text-gray-400'">
            <span
              class="i-carbon-checkmark-filled mr-1"
              v-if="userInfo?.phone"
            />
            <span class="i-carbon-close-filled mr-1" v-else />
            {{
              userInfo?.phone
                ? $t('page.profile.summary.phoneBound')
                : $t('page.profile.summary.phoneUnbound')
            }}
          </span>
        </div>
        <div class="flex items-center text-xs">
          <span :class="userInfo?.email ? 'text-green-600' : 'text-gray-400'">
            <span
              class="i-carbon-checkmark-filled mr-1"
              v-if="userInfo?.email"
            />
            <span class="i-carbon-close-filled mr-1" v-else />
            {{
              userInfo?.email
                ? $t('page.profile.summary.emailBound')
                : $t('page.profile.summary.emailUnbound')
            }}
          </span>
        </div>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.profile-summary-card {
  position: sticky;
  top: 80px;
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 14px;

  .info-label {
    color: var(--vben-text-secondary-color);
  }

  .info-value {
    font-weight: 500;
    color: var(--vben-text-primary-color);
  }
}

.security-level {
  padding: 12px;
  background: var(--vben-background-color-deep);
  border-radius: 8px;
}

// 响应式：移动端取消 sticky
@media (max-width: 1024px) {
  .profile-summary-card {
    position: static;
  }
}
</style>
