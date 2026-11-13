<script setup lang="ts">
import { ref, computed } from 'vue';

import { useVbenModal } from '#/components/popup/modal';

import { VbenAvatar } from '#/ui/composed';
import FormRadioGroup from '#/components/ui/FormRadioGroup.vue';
import { Button } from '#/ui/button';
import { Input } from '#/ui/input';
import { Label } from '#/ui/label';
import { FileUpload } from '#/ui/upload';
import { $t } from '#/locales';
import { useUserStore } from '#/store/user';
import { toast } from '#/ui-patterns/toast';
import { userProfileApi } from '#/api/system/user-profile';
import { resolveFilePreviewUrl } from '#/api/system/file';

const userStore = useUserStore();

// 编辑状态
const isEditing = ref(false);

// 表单数据
const formData = ref({
  displayName: '',
  gender: 0 as 0 | 1 | 2,
});

// 头像上传
const uploadingAvatar = ref(false);

const [AvatarModal, avatarModalApi] = useVbenModal({
  footer: false,
  centered: true,
});

// 初始化表单数据
const initFormData = () => {
  if (userStore.user) {
    formData.value = {
      displayName: userStore.user.displayName,
      gender: userStore.user.gender,
    };
  }
};

// 开始编辑
const handleEdit = () => {
  initFormData();
  isEditing.value = true;
};

// 取消编辑
const handleCancel = () => {
  isEditing.value = false;
  initFormData();
};

// 保存修改
const handleSave = async () => {
  try {
    await userProfileApi.updateBaseInfo(formData.value);
    toast.success($t('page.profile.basic.editSuccess'));
    isEditing.value = false;
    // 刷新用户信息
    await userStore.fetchAuthInfo();
  } catch (error) {
    console.error('更新用户信息失败:', error);
  }
};

// 上传头像
const handleAvatarUpload = async (files: File[]) => {
  const avatarFile = files[0];
  if (!avatarFile) return;
  try {
    uploadingAvatar.value = true;
    const body = new FormData();
    body.append('avatarFile', avatarFile);

    await userProfileApi.uploadAvatar(body);
    toast.success($t('page.profile.basic.uploadSuccess'));
    avatarModalApi.close();

    // 刷新用户信息
    await userStore.fetchAuthInfo();
  } catch (error) {
    console.error('上传头像失败:', error);
  } finally {
    uploadingAvatar.value = false;
  }
};

function onGender(value: null | boolean | number | string) {
  if (value === 0 || value === 1 || value === 2) {
    formData.value.gender = value;
  }
}

// 性别选项
const genderOptions = [
  { label: $t('page.profile.basic.unknown'), value: 0 },
  { label: $t('page.profile.basic.male'), value: 1 },
  { label: $t('page.profile.basic.female'), value: 2 },
];

// 用户信息（只读）
const userInfo = computed(() => userStore.user);
</script>

<template>
  <div class="basic-info">
    <div class="flex items-center justify-between mb-6">
      <h3 class="text-lg font-semibold">
        {{ $t('page.profile.tabs.basic') }}
      </h3>
      <Button
        v-if="!isEditing"
        type="button"
        @click="handleEdit"
      >
        {{ $t('page.profile.basic.edit') }}
      </Button>
    </div>

    <!-- 头像上传区域 -->
    <div class="avatar-section mb-8 p-6 bg-gray-50 dark:bg-gray-800 rounded-lg">
      <div class="flex items-center gap-6">
        <!-- 头像 -->
        <VbenAvatar
          :src="resolveFilePreviewUrl(userInfo?.avatar ?? undefined) ?? ''"
          :alt="userInfo?.displayName ?? 'User'"
          :size="100"
          class="mb-4 mx-auto"
          @click="avatarModalApi.open()"
        />
        <div class="flex-1">
          <h4 class="font-medium mb-2">
            {{ $t('page.profile.basic.avatar') }}
          </h4>
          <p class="text-sm text-gray-500 mb-3">
            {{ $t('page.profile.basic.uploadTip') }}
          </p>
          <Button
            type="button"
            size="sm"
            variant="outline"
            @click="avatarModalApi.open()"
          >
            {{ $t('page.profile.basic.changeAvatar') }}
          </Button>
        </div>
      </div>
    </div>

    <div class="grid max-w-xl gap-4">
      <div class="grid grid-cols-[120px_minmax(0,1fr)] items-center gap-3">
        <Label>{{ $t('page.profile.basic.username') }}</Label>
        <Input
          :model-value="userInfo?.username ?? ''"
          disabled
        />
      </div>
      <div class="grid grid-cols-[120px_minmax(0,1fr)] items-center gap-3">
        <Label>{{ $t('page.profile.basic.displayName') }}</Label>
        <Input
          v-model="formData.displayName"
          :disabled="!isEditing"
          :placeholder="$t('page.profile.basic.displayName')"
        />
      </div>
      <div class="grid grid-cols-[120px_minmax(0,1fr)] items-center gap-3">
        <Label>{{ $t('page.profile.basic.gender') }}</Label>
        <FormRadioGroup
          :value="formData.gender"
          :options="genderOptions"
          :disabled="!isEditing"
          @update:value="onGender"
        />
      </div>
      <div class="grid grid-cols-[120px_minmax(0,1fr)] items-center gap-3">
        <Label>{{ $t('page.profile.basic.phone') }}</Label>
        <Input
          :model-value="userInfo?.phone ?? ''"
          disabled
        />
      </div>
      <div class="grid grid-cols-[120px_minmax(0,1fr)] items-center gap-3">
        <Label>{{ $t('page.profile.basic.email') }}</Label>
        <Input
          :model-value="userInfo?.email ?? ''"
          disabled
        />
      </div>
      <div class="grid grid-cols-[120px_minmax(0,1fr)] items-center gap-3">
        <Label>{{ $t('page.profile.basic.dept') }}</Label>
        <Input
          :model-value="userInfo?.departmentName ?? ''"
          disabled
        />
      </div>
      <div class="grid grid-cols-[120px_minmax(0,1fr)] items-center gap-3">
        <Label>{{ $t('page.profile.basic.registrationDate') }}</Label>
        <Input
          :model-value="userInfo?.registrationDate ?? ''"
          disabled
        />
      </div>
      <div
        v-if="isEditing"
        class="flex items-center gap-2 pl-[132px]"
      >
        <Button
          type="button"
          @click="handleSave"
        >
          {{ $t('page.profile.basic.save') }}
        </Button>
        <Button
          type="button"
          variant="outline"
          @click="handleCancel"
        >
          {{ $t('page.profile.basic.cancel') }}
        </Button>
      </div>
    </div>

    <AvatarModal
      class="w-[500px]"
      :title="$t('page.profile.basic.uploadAvatar')"
    >
      <div class="text-center">
        <FileUpload
          :max="1"
          accept="image/png,image/jpeg,image/jpg"
          :disabled="uploadingAvatar"
          @select="handleAvatarUpload"
        >
          <Button
            type="button"
            :loading="uploadingAvatar"
          >
            {{ $t('page.profile.basic.uploadAvatar') }}
          </Button>
        </FileUpload>
        <p class="mt-4 text-sm text-gray-500">
          {{ $t('page.profile.basic.uploadTip') }}
        </p>
      </div>
    </AvatarModal>
  </div>
</template>

<style lang="scss" scoped>
.basic-info {
  max-width: 800px;
}

.avatar-section {
  transition: all 0.3s ease;

  &:hover {
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  }
}
</style>
