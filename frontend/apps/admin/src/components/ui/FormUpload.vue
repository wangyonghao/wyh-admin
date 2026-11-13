<script lang="ts" setup>
import { computed, useTemplateRef } from 'vue';

import { Button } from '#/ui/button';

import {
  displayUploadFiles,
  nextUploadList,
  removeUploadFile,
  type FormUploadFile,
} from './upload-value';

defineOptions({
  name: 'FormUpload',
  inheritAttrs: false,
});

const props = defineProps<{
  accept?: string;
  disabled?: boolean;
  fileList?: FormUploadFile[] | null;
  max?: number;
  multiple?: boolean;
}>();

const emit = defineEmits<{
  'update:fileList': [value: FormUploadFile[] | null];
}>();

const inputRef = useTemplateRef<globalThis.HTMLInputElement>('inputRef');
const files = computed(() => displayUploadFiles(props.fileList));

function openPicker() {
  inputRef.value?.click();
}

function onChange(event: globalThis.Event) {
  const input = event.target;
  if (!(input instanceof globalThis.HTMLInputElement)) return;
  const picked = [...(input.files ?? [])].map((file) => ({
    file,
    name: file.name,
  }));
  emit(
    'update:fileList',
    nextUploadList(props.fileList, picked, {
      max: props.max,
      multiple: props.multiple,
    }),
  );
  input.value = '';
}

function remove(id: string) {
  emit('update:fileList', removeUploadFile(props.fileList, id));
}
</script>

<template>
  <div class="flex flex-col gap-2">
    <input
      ref="inputRef"
      type="file"
      class="sr-only"
      :accept="accept"
      :multiple="multiple"
      :disabled="disabled"
      @change="onChange"
    >
    <div>
      <Button
        type="button"
        variant="outline"
        :disabled="disabled"
        @click="openPicker"
      >
        选择文件
      </Button>
    </div>
    <ul
      v-if="files.length"
      class="flex flex-col gap-1"
    >
      <li
        v-for="file in files"
        :key="file.id"
        class="flex items-center gap-2 text-sm"
      >
        <span class="min-w-0 flex-1 truncate">{{ file.name }}</span>
        <Button
          type="button"
          variant="link"
          size="sm"
          class="text-destructive h-auto px-1"
          :disabled="disabled"
          @click="remove(file.id)"
        >
          移除
        </Button>
      </li>
    </ul>
  </div>
</template>
