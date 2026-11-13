<script setup lang="ts">
import { ref, watch } from 'vue';

import { toast } from '#/ui-patterns/toast';
import { fileApi, resolveFilePreviewUrl, toFileId } from '#/api/system/file';
import { Button } from '#/ui/button';
import { FileUpload } from '#/ui/upload';

const props = defineProps<{
  /** 头像/图片对应的 fileId */
  modelValue?: string | null;
}>();

const emit = defineEmits<{
  (e: 'update:modelValue', value: string | undefined): void;
}>();

const previewUrl = ref<string>();

watch(
  () => props.modelValue,
  (val) => {
    const fileId = toFileId(val);
    previewUrl.value =
      fileId == null ? undefined : resolveFilePreviewUrl(fileId);
  },
  { immediate: true },
);

async function handleUpload(files: File[]) {
  const file = files[0];
  if (!file) return;
  try {
    const result = await fileApi.upload(file);
    emit('update:modelValue', result.fileId);
  } catch (error) {
    console.error('上传失败', error);
    toast.error('上传失败');
  }
}

function handleRemove() {
  emit('update:modelValue', undefined);
}
</script>

<template>
  <div class="image-upload">
    <div v-if="previewUrl" class="relative size-[100px]">
      <img
        :src="previewUrl"
        alt=""
        class="size-[100px] rounded-md object-cover"
      />
      <Button
        type="button"
        variant="outline"
        size="sm"
        class="mt-2"
        @click="handleRemove"
      >
        移除
      </Button>
    </div>
    <FileUpload v-else accept="image/*" :max="1" @select="handleUpload">
      <button
        type="button"
        class="flex size-[100px] items-center justify-center rounded-md border border-dashed border-border text-sm text-muted-foreground"
      >
        上传图片
      </button>
    </FileUpload>
  </div>
</template>
