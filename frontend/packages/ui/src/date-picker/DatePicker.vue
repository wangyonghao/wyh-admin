<script lang="ts" setup>
import type { DatePart, DatePickerKind } from './date-value';

import { computed, ref, useAttrs } from 'vue';

import { IconifyIcon } from '@wyh/icons';

import { Button } from '../component/shadcn-vue/ui/button';
import {
  Popover,
  PopoverContent,
  PopoverTrigger,
} from '../component/shadcn-vue/ui/popover';

import {
  applyTime,
  buildMonthCells,
  commitDateSelection,
  compareDay,
  displayDateLabel,
  hasTimeKind,
  isDateEmpty,
  isRangeKind,
  readDateRange,
  resolveDateKind,
  sameDay,
  shiftMonth,
  startOfDay,
  timeText,
} from './date-value';

defineOptions({
  name: 'DatePicker',
  inheritAttrs: false,
});

const props = defineProps<{
  clearable?: boolean;
  disabled?: boolean;
  format?: string;
  placeholder?: string;
  showTime?: boolean;
  type?: DatePickerKind | string;
  value?: unknown;
  valueFormat?: string;
}>();

const emit = defineEmits<{
  'update:value': [
    value: null | number | string | [number, number] | [string, string],
  ];
}>();

const attrs = useAttrs();
const open = ref(false);
const viewYear = ref(new Date().getFullYear());
const viewMonth = ref(new Date().getMonth() + 1);
const draftStart = ref<DatePart | null>(null);
const draftEnd = ref<DatePart | null>(null);

const kind = computed(() => resolveDateKind(props.type, props.showTime));
const range = computed(() => isRangeKind(kind.value));
const withTime = computed(() => hasTimeKind(kind.value));
const label = computed(() =>
  displayDateLabel(props.value, kind.value, props.format, props.valueFormat),
);
const empty = computed(() => isDateEmpty(props.value));
const cells = computed(() => buildMonthCells(viewYear.value, viewMonth.value));
const weekdays = ['一', '二', '三', '四', '五', '六', '日'];

const triggerLook =
  'border-input bg-background ring-offset-background focus-visible:ring-ring flex h-10 w-full items-center justify-between rounded-md border px-3 py-2 text-sm shadow-xs focus-visible:ring-1 focus-visible:outline-hidden disabled:cursor-not-allowed disabled:opacity-50';

function syncDraft() {
  const current = readDateRange(props.value, props.valueFormat);
  draftStart.value = current.start;
  draftEnd.value = range.value ? current.end : null;
  const anchor = current.start ?? timestampPart(Date.now());
  if (anchor) {
    viewYear.value = anchor.year;
    viewMonth.value = anchor.month;
  }
}

function timestampPart(value: number): DatePart | null {
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return null;
  return {
    year: date.getFullYear(),
    month: date.getMonth() + 1,
    day: date.getDate(),
    hour: date.getHours(),
    minute: date.getMinutes(),
    second: date.getSeconds(),
  };
}

function onOpen(next: boolean) {
  open.value = next;
  if (next) syncDraft();
}

function moveMonth(delta: number) {
  const next = shiftMonth(viewYear.value, viewMonth.value, delta);
  viewYear.value = next.year;
  viewMonth.value = next.month;
}

function cellPart(cell: { day: number; month: number; year: number }): DatePart {
  const previous = draftStart.value;
  return {
    year: cell.year,
    month: cell.month,
    day: cell.day,
    hour: withTime.value ? (previous?.hour ?? 0) : 0,
    minute: withTime.value ? (previous?.minute ?? 0) : 0,
    second: withTime.value ? (previous?.second ?? 0) : 0,
  };
}

function finish(close: boolean) {
  const next = commitDateSelection(
    kind.value,
    draftStart.value,
    draftEnd.value,
    props.valueFormat,
  );
  // 范围只点了一端时不把已有值写成 null，清除走单独的按钮。
  if (next != null) emit('update:value', next);
  if (close) open.value = false;
}

function pick(cell: { day: number; month: number; year: number }) {
  if (props.disabled) return;
  const part = cellPart(cell);
  if (!range.value) {
    draftStart.value = withTime.value ? part : startOfDay(part);
    if (!withTime.value) finish(true);
    return;
  }
  if (!draftStart.value || draftEnd.value) {
    draftStart.value = withTime.value ? part : startOfDay(part);
    draftEnd.value = null;
    return;
  }
  draftEnd.value = withTime.value ? part : startOfDay(part);
  if (!withTime.value) finish(true);
}

function inRange(cell: { day: number; month: number; year: number }): boolean {
  if (!draftStart.value || !draftEnd.value) return false;
  const part = cellPart(cell);
  const afterStart = compareDay(part, draftStart.value) >= 0;
  const beforeEnd = compareDay(part, draftEnd.value) <= 0;
  return afterStart && beforeEnd;
}

function isSelected(cell: { day: number; month: number; year: number }): boolean {
  const part = cellPart(cell);
  if (draftStart.value && sameDay(part, draftStart.value)) return true;
  return !!(draftEnd.value && sameDay(part, draftEnd.value));
}

function onTime(which: 'end' | 'start', event: unknown) {
  const target = (event as { target?: { value?: string } | null }).target;
  const text = target?.value ?? '';
  const current = which === 'start' ? draftStart.value : draftEnd.value;
  if (!current) return;
  const next = applyTime(current, text.length === 5 ? `${text}:00` : text);
  if (!next) return;
  if (which === 'start') draftStart.value = next;
  else draftEnd.value = next;
}

function clear() {
  draftStart.value = null;
  draftEnd.value = null;
  emit('update:value', null);
  open.value = false;
}
</script>

<template>
  <div
    class="min-w-0"
    :class="attrs.class || 'w-full'"
    :style="attrs.style"
  >
    <Popover
      :open="open"
      @update:open="onOpen"
    >
      <PopoverTrigger as-child>
        <button
          type="button"
          :disabled="disabled"
          :class="triggerLook"
        >
          <span
            class="line-clamp-1 flex-auto text-left"
            :class="label ? '' : 'text-muted-foreground'"
          >
            {{ label || placeholder }}
          </span>
          <span
            v-if="clearable && !empty"
            class="mr-1 inline-flex size-4 shrink-0 cursor-pointer items-center opacity-50 hover:opacity-100"
            @pointerdown.stop
            @click.stop.prevent="clear"
          >
            <IconifyIcon
              icon="lucide:x"
              class="size-4"
            />
          </span>
          <IconifyIcon
            icon="lucide:calendar"
            class="size-4 shrink-0 opacity-50"
          />
        </button>
      </PopoverTrigger>
      <!-- Naive 弹层 z-index 更高，下拉挂到 body 后抬一层才能盖住抽屉和对话框。 -->
      <PopoverContent
        align="start"
        class="z-[4000] w-auto p-3"
      >
        <div class="w-[252px]">
          <div class="mb-2 flex items-center justify-between">
            <Button
              type="button"
              variant="ghost"
              size="icon"
              class="size-7"
              @click="moveMonth(-1)"
            >
              <IconifyIcon
                icon="lucide:chevron-left"
                class="size-4"
              />
            </Button>
            <span class="text-sm font-medium">{{ viewYear }}年{{ viewMonth }}月</span>
            <Button
              type="button"
              variant="ghost"
              size="icon"
              class="size-7"
              @click="moveMonth(1)"
            >
              <IconifyIcon
                icon="lucide:chevron-right"
                class="size-4"
              />
            </Button>
          </div>
          <div class="text-muted-foreground grid grid-cols-7 text-center text-xs">
            <span
              v-for="day in weekdays"
              :key="day"
              class="py-1"
            >{{ day }}</span>
          </div>
          <div class="grid grid-cols-7">
            <button
              v-for="(cell, index) in cells"
              :key="`${cell.year}-${cell.month}-${cell.day}-${index}`"
              type="button"
              class="mx-auto flex size-8 items-center justify-center rounded-md text-sm"
              :class="[
                cell.outside ? 'text-muted-foreground' : '',
                isSelected(cell)
                  ? 'bg-primary text-primary-foreground'
                  : inRange(cell)
                    ? 'bg-accent'
                    : 'hover:bg-accent',
              ]"
              @click="pick(cell)"
            >
              {{ cell.day }}
            </button>
          </div>
          <div
            v-if="withTime"
            class="mt-3 space-y-2"
          >
            <label class="flex items-center gap-2 text-sm">
              <span class="text-muted-foreground w-8 shrink-0">
                {{ range ? '开始' : '时间' }}
              </span>
              <input
                type="time"
                step="1"
                class="border-input bg-background h-8 flex-1 rounded-md border px-2 text-sm"
                :value="timeText(draftStart)"
                @change="onTime('start', $event)"
              >
            </label>
            <label
              v-if="range"
              class="flex items-center gap-2 text-sm"
            >
              <span class="text-muted-foreground w-8 shrink-0">结束</span>
              <input
                type="time"
                step="1"
                class="border-input bg-background h-8 flex-1 rounded-md border px-2 text-sm"
                :value="timeText(draftEnd)"
                @change="onTime('end', $event)"
              >
            </label>
          </div>
          <div class="mt-3 flex justify-end gap-2">
            <Button
              v-if="clearable"
              type="button"
              variant="ghost"
              size="sm"
              @click="clear"
            >
              清除
            </Button>
            <Button
              type="button"
              size="sm"
              @click="finish(true)"
            >
              确定
            </Button>
          </div>
        </div>
      </PopoverContent>
    </Popover>
  </div>
</template>
