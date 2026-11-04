<script setup lang="ts">
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type {
  LoginLogPageQuery,
  LoginLogQuery,
  LoginLogResult,
} from '#/api/system/login-log';

import { ref } from 'vue';

import { toast } from '#/ui-patterns/toast';
import FormSelect from '#/adapter/component/FormSelect.vue';
import { useVbenVxeGrid } from '#/adapter/vxe-table';
import { loginLogApi } from '#/api/system/login-log';
import { $t } from '#/locales';
import { useUserStore } from '#/store/user';
import { Badge } from '#/ui/badge';
import { Button } from '#/ui/button';
import { DatePicker } from '#/ui/date-picker';

const userStore = useUserStore();

const filters = ref({
  loginStatus: null as 'SUCCESS' | 'FAILURE' | null,
  dateRange: null as [number, number] | null,
});

const statusOptions = [
  { label: $t('page.profile.logs.success'), value: 'SUCCESS' },
  { label: $t('page.profile.logs.failure'), value: 'FAILURE' },
];

const [Grid, gridApi] = useVbenVxeGrid({
  showSearchForm: false,
  gridOptions: {
    columns: [
      {
        field: 'loginStatus',
        title: $t('page.profile.logs.operationType'),
        width: 100,
        slots: { default: 'status' },
      },
      {
        field: 'loginTime',
        title: $t('page.profile.logs.operationTime'),
        minWidth: 180,
      },
      {
        field: 'ipAddress',
        title: $t('page.profile.logs.ipAddress'),
        minWidth: 140,
      },
      {
        field: 'location',
        title: $t('page.profile.logs.location'),
        minWidth: 150,
      },
      {
        field: 'device',
        title: $t('page.profile.logs.device'),
        minWidth: 180,
        slots: { default: 'device' },
      },
      {
        field: 'failureReason',
        title: $t('page.profile.logs.operationResult'),
        minWidth: 200,
        slots: { default: 'result' },
      },
    ],
    height: 480,
    pagerConfig: {
      pageSize: 10,
      pageSizes: [10, 20, 50, 100],
    },
    proxyConfig: {
      ajax: {
        query: async ({ page }) => {
          const params: LoginLogPageQuery = {
            username: userStore.user?.username,
            page: page.currentPage,
            pageSize: page.pageSize,
          };
          if (filters.value.loginStatus) {
            params.loginStatus = filters.value.loginStatus;
          }
          if (filters.value.dateRange) {
            params.loginTimeStart = new Date(
              filters.value.dateRange[0],
            ).toISOString();
            params.loginTimeEnd = new Date(
              filters.value.dateRange[1],
            ).toISOString();
          }
          const res = await loginLogApi.listLoginLog(params);
          return { records: res.records ?? [], total: res.total ?? 0 };
        },
      },
    },
    toolbarConfig: { enabled: false },
  } as VxeTableGridOptions<LoginLogResult>,
});

function search() {
  gridApi.reload();
}

function reset() {
  filters.value = {
    loginStatus: null,
    dateRange: null,
  };
  gridApi.reload();
}

async function handleExport() {
  try {
    const params: LoginLogQuery = {
      username: userStore.user?.username,
    };
    if (filters.value.loginStatus) {
      params.loginStatus = filters.value.loginStatus;
    }
    if (filters.value.dateRange) {
      params.loginTimeStart = new Date(filters.value.dateRange[0]).toISOString();
      params.loginTimeEnd = new Date(filters.value.dateRange[1]).toISOString();
    }
    await loginLogApi.exportLoginLog(params);
    toast.success($t('page.profile.logs.exportSuccess'));
  } catch (error) {
    console.error('导出失败:', error);
  }
}
</script>

<template>
  <div class="max-w-full">
    <div class="mb-4 flex flex-wrap items-center gap-4">
      <FormSelect
        v-model:value="filters.loginStatus"
        :options="statusOptions"
        :placeholder="$t('page.profile.logs.all')"
        style="width: 150px"
        clearable
      />

      <DatePicker
        v-model:value="filters.dateRange"
        type="daterange"
        :placeholder="$t('page.profile.logs.operationTime')"
        class="w-[300px]"
        clearable
      />

      <Button
        type="button"
        @click="search"
      >
        {{ $t('page.profile.logs.filter') }}
      </Button>
      <Button
        type="button"
        variant="outline"
        @click="reset"
      >
        {{ $t('common.reset') }}
      </Button>
      <Button
        type="button"
        variant="outline"
        @click="handleExport"
      >
        {{ $t('page.profile.logs.export') }}
      </Button>
    </div>

    <Grid>
      <template #status="{ row }">
        <Badge :variant="row.loginStatus === 'SUCCESS' ? 'success' : 'destructive'">
          {{
            row.loginStatus === 'SUCCESS'
              ? $t('page.profile.logs.login')
              : $t('page.profile.logs.failure')
          }}
        </Badge>
      </template>
      <template #device="{ row }">
        {{ row.browser }} / {{ row.os }}
      </template>
      <template #result="{ row }">
        {{ row.failureReason || '-' }}
      </template>
    </Grid>
  </div>
</template>
