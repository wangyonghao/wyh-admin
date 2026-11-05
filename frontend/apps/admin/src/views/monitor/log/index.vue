<script setup lang="ts">
import { Page } from '@vben/common-ui';

import { Tabs, TabsContent, TabsList, TabsTrigger } from '#/ui/tabs';

import LoginLogList from './login-log.vue';
import OperationLogList from './operation-log.vue';
import SmsLogList from './sms-log.vue';
</script>
<template>
  <Page auto-content-height>
    <Tabs default-value="login" class="flex h-full flex-col">
      <TabsList class="w-fit">
        <TabsTrigger value="login">
          {{ $t('monitor.loginLog.title') }}
        </TabsTrigger>
        <TabsTrigger value="operation">
          {{ $t('monitor.operationLog.title') }}
        </TabsTrigger>
        <TabsTrigger value="sms">
          {{ $t('system.smsLog.listTitle') }}
        </TabsTrigger>
      </TabsList>
      <TabsContent value="login" class="mt-2 min-h-0 flex-1">
        <LoginLogList />
      </TabsContent>
      <TabsContent value="operation" class="mt-2 min-h-0 flex-1">
        <OperationLogList />
      </TabsContent>
      <TabsContent value="sms" class="mt-2 min-h-0 flex-1">
        <SmsLogList />
      </TabsContent>
    </Tabs>
  </Page>
</template>
