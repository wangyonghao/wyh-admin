<script setup lang="ts">
import type {
  JobHandlerOption,
  JobResp,
  ScheduleMode,
  ScheduleSpec,
} from '#/api/schedule';

import { computed, reactive, ref } from 'vue';

import { useVbenModal } from '#/components/popup/modal';

import FormInputNumber from '#/components/ui/FormInputNumber.vue';
import FormSelect from '#/components/ui/FormSelect.vue';
import FormTimePicker from '#/components/ui/FormTimePicker.vue';
import { Input } from '#/ui/input';
import { Label } from '#/ui/label';
import { Textarea } from '#/ui/textarea';

import { addJob, listJobHandlers, updateJob } from '#/api/schedule';
import { Checkbox } from '#/ui/checkbox';
import { isValueChecked, toggleCheckedValue } from '#/ui/checkbox/group';
import { toast } from '#/ui-patterns/toast';

const emits = defineEmits(['success']);
const dataId = ref('');
const handlers = ref<JobHandlerOption[]>([]);

const weekdayOptions = [
  { label: '周一', value: 1 },
  { label: '周二', value: 2 },
  { label: '周三', value: 3 },
  { label: '周四', value: 4 },
  { label: '周五', value: 5 },
  { label: '周六', value: 6 },
  { label: '周日', value: 7 },
];

const modeOptions = [
  { label: '每天', value: 'DAILY' },
  { label: '每周', value: 'WEEKLY' },
  { label: '每月', value: 'MONTHLY' },
  { label: '每隔一段时间', value: 'INTERVAL' },
  { label: '高级（Cron）', value: 'CRON' },
];

const form = reactive({
  name: '',
  handlerCode: '',
  remark: '',
  params: '',
  mode: 'DAILY' as ScheduleMode,
  hour: 9,
  minute: 0,
  daysOfWeek: [1] as number[],
  dayOfMonth: 1,
  interval: 5,
  intervalUnit: 'MINUTE' as 'HOUR' | 'MINUTE',
  cron: '0 0 9 * * ?',
});

const selectedHandler = computed(() =>
  handlers.value.find((item) => item.code === form.handlerCode),
);

const clockValue = computed({
  get() {
    const date = new Date();
    date.setHours(form.hour, form.minute, 0, 0);
    return date.getTime();
  },
  set(value: null | number | string) {
    if (typeof value !== 'number') return;
    const date = new Date(value);
    form.hour = date.getHours();
    form.minute = date.getMinutes();
  },
});

function buildSchedule(): ScheduleSpec {
  return {
    mode: form.mode,
    hour: form.hour,
    minute: form.minute,
    daysOfWeek: form.daysOfWeek,
    dayOfMonth: form.dayOfMonth,
    interval: form.interval,
    intervalUnit: form.intervalUnit,
    cron: form.cron,
  };
}

function applyRecord(record?: Partial<JobResp>) {
  form.name = record?.name ?? '';
  form.handlerCode = record?.handlerCode ?? '';
  form.remark = record?.remark ?? '';
  form.params = record?.params ?? '';
  const schedule = record?.schedule;
  form.mode = schedule?.mode ?? 'DAILY';
  form.hour = schedule?.hour ?? 9;
  form.minute = schedule?.minute ?? 0;
  form.daysOfWeek = schedule?.daysOfWeek?.length ? schedule.daysOfWeek : [1];
  form.dayOfMonth = schedule?.dayOfMonth ?? 1;
  form.interval = schedule?.interval ?? 5;
  form.intervalUnit = schedule?.intervalUnit ?? 'MINUTE';
  form.cron = schedule?.cron ?? record?.cron ?? '0 0 9 * * ?';
}

const isUpdate = computed(() => !!dataId.value);

const [Modal, drawerApi] = useVbenModal({
  async onConfirm() {
    if (!form.name.trim()) {
      toast.warning('请填写任务名称');
      return false;
    }
    if (!form.handlerCode) {
      toast.warning('请选择要执行的任务');
      return false;
    }
    drawerApi.lock();
    try {
      const payload = {
        name: form.name.trim(),
        handlerCode: form.handlerCode,
        schedule: buildSchedule(),
        params: form.params || undefined,
        remark: form.remark || undefined,
        enabled: false,
      };
      if (isUpdate.value) {
        await updateJob(payload, dataId.value);
        toast.success('修改成功');
      } else {
        await addJob(payload);
        toast.success('新增成功，默认已停止，可在列表中激活');
      }
      emits('success');
      drawerApi.close();
      return true;
    } finally {
      drawerApi.unlock();
    }
  },
  async onOpenChange(isOpen) {
    if (!isOpen) return;
    drawerApi.lock(true);
    try {
      if (handlers.value.length === 0) {
        handlers.value = await listJobHandlers();
      }
      const record = drawerApi.getData<Partial<JobResp>>();
      dataId.value = record?.id ? String(record.id) : '';
      applyRecord(record);
    } finally {
      drawerApi.unlock();
    }
  },
});
</script>

<template>
  <Modal
    :title="isUpdate ? '编辑任务' : '新增任务'"
    class="w-[560px]"
  >
    <div class="grid gap-3 pt-2">
      <div class="grid grid-cols-[96px_minmax(0,1fr)] items-center gap-2">
        <Label>任务名称</Label>
        <Input
          v-model="form.name"
          placeholder="例如：每天发布公告"
        />
      </div>
      <div class="grid grid-cols-[96px_minmax(0,1fr)] items-center gap-2">
        <Label>执行任务</Label>
        <FormSelect
          v-model:value="form.handlerCode"
          filterable
          :options="
            handlers.map((item) => ({ label: item.name, value: item.code }))
          "
          placeholder="请选择已注册的任务"
        />
      </div>
      <p
        v-if="selectedHandler?.description"
        class="text-muted-foreground ml-24 text-sm"
      >
        {{ selectedHandler.description }}
      </p>
      <div class="grid grid-cols-[96px_minmax(0,1fr)] items-center gap-2">
        <Label>执行频率</Label>
        <FormSelect
          v-model:value="form.mode"
          :options="modeOptions"
        />
      </div>
      <div
        v-if="
          form.mode === 'DAILY' ||
            form.mode === 'WEEKLY' ||
            form.mode === 'MONTHLY'
        "
        class="grid grid-cols-[96px_minmax(0,1fr)] items-center gap-2"
      >
        <Label>时间</Label>
        <FormTimePicker
          v-model:value="clockValue"
          format="HH:mm"
        />
      </div>
      <div
        v-if="form.mode === 'WEEKLY'"
        class="grid grid-cols-[96px_minmax(0,1fr)] items-start gap-2"
      >
        <Label class="pt-1">星期</Label>
        <div class="flex flex-wrap items-center gap-x-4 gap-y-2">
          <label
            v-for="item in weekdayOptions"
            :key="item.value"
            class="inline-flex cursor-pointer items-center gap-2 text-sm"
          >
            <Checkbox
              :model-value="isValueChecked(form.daysOfWeek, item.value)"
              @update:model-value="
                (checked) =>
                  (form.daysOfWeek = toggleCheckedValue(
                    form.daysOfWeek,
                    item.value,
                    checked === true,
                  ))
              "
            />
            <span>{{ item.label }}</span>
          </label>
        </div>
      </div>
      <div
        v-if="form.mode === 'MONTHLY'"
        class="grid grid-cols-[96px_minmax(0,1fr)] items-center gap-2"
      >
        <Label>每月</Label>
        <div class="flex items-center gap-2">
          <FormInputNumber
            v-model:value="form.dayOfMonth"
            :min="1"
            :max="31"
          />
          <span>号</span>
        </div>
      </div>
      <div
        v-if="form.mode === 'INTERVAL'"
        class="grid grid-cols-[96px_minmax(0,1fr)] items-center gap-2"
      >
        <Label>间隔</Label>
        <div class="flex items-center gap-2">
          <FormInputNumber
            v-model:value="form.interval"
            :min="1"
            class="w-28"
          />
          <FormSelect
            v-model:value="form.intervalUnit"
            class="w-28"
            :options="[
              { label: '分钟', value: 'MINUTE' },
              { label: '小时', value: 'HOUR' },
            ]"
          />
        </div>
      </div>
      <div
        v-if="form.mode === 'CRON'"
        class="grid grid-cols-[96px_minmax(0,1fr)] items-center gap-2"
      >
        <Label>Cron</Label>
        <Input
          v-model="form.cron"
          placeholder="0 0 9 * * ?"
        />
      </div>
      <div class="grid grid-cols-[96px_minmax(0,1fr)] items-start gap-2">
        <Label class="pt-2">备注</Label>
        <Textarea
          v-model="form.remark"
          rows="2"
        />
      </div>
    </div>
  </Modal>
</template>
