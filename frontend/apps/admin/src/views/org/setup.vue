<script lang="ts" setup>
import { onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';

import { VbenButton } from '#/ui/composed';

import { orgApi, type OrgInviteResult } from '#/api/org';
import { Input } from '#/ui/input';
import { toast } from '#/ui-patterns/toast';
import { useUserStore } from '#/store';

defineOptions({ name: 'OrgSetup' });

const router = useRouter();
const userStore = useUserStore();

const tab = ref<'create' | 'apply' | 'invite'>('create');
const creating = ref(false);
const applying = ref(false);
const orgName = ref('');
const orgDescription = ref('');
const applyCode = ref('');
const applyMessage = ref('');
const pendingInvites = ref<OrgInviteResult[]>([]);
const inviteTokens = ref<Record<string, string>>({});

async function refresh() {
  const session = await orgApi.session();
  if (!session.needsOrganization && (session.organizations?.length || 0) > 0) {
    await router.replace(userStore.user?.homePath || '/dashboard');
    return;
  }
  pendingInvites.value = session.pendingInvites || [];
}

async function handleCreate() {
  if (!orgName.value.trim()) {
    toast.error('请输入组织名称');
    return;
  }
  creating.value = true;
  try {
    await orgApi.create({
      name: orgName.value.trim(),
      description: orgDescription.value.trim() || undefined,
    });
    toast.success('组织创建成功');
    await userStore.fetchAuthInfo();
    await router.replace('/dashboard');
  } finally {
    creating.value = false;
  }
}

async function handleApply() {
  if (!applyCode.value.trim()) {
    toast.error('请输入组织编码');
    return;
  }
  applying.value = true;
  try {
    await orgApi.apply({
      code: applyCode.value.trim(),
      message: applyMessage.value.trim() || undefined,
    });
    toast.success('已提交申请，请等待管理员审核');
    applyCode.value = '';
    applyMessage.value = '';
  } finally {
    applying.value = false;
  }
}

async function acceptInvite(invite: OrgInviteResult) {
  const token = inviteTokens.value[String(invite.id)];
  if (!token) {
    toast.error('请从邀请邮件中的链接进入以接受邀请');
    return;
  }
  await orgApi.acceptInvite(token);
  toast.success('已加入组织');
  await userStore.fetchAuthInfo();
  await router.replace('/dashboard');
}

defineExpose({ acceptInvite });

onMounted(refresh);
</script>

<template>
  <div class="mx-auto max-w-xl px-4 py-10">
    <h1 class="text-foreground mb-2 text-2xl font-bold">加入或创建组织</h1>
    <p class="text-muted-foreground mb-8 text-sm">
      你还没有所属组织。请创建组织、申请加入，或处理收到的邀请。
    </p>

    <div class="mb-6 flex gap-2">
      <VbenButton
        :variant="tab === 'create' ? 'default' : 'outline'"
        @click="tab = 'create'"
      >
        创建组织
      </VbenButton>
      <VbenButton
        :variant="tab === 'apply' ? 'default' : 'outline'"
        @click="tab = 'apply'"
      >
        申请加入
      </VbenButton>
      <VbenButton
        :variant="tab === 'invite' ? 'default' : 'outline'"
        @click="tab = 'invite'"
      >
        待处理邀请
      </VbenButton>
    </div>

    <div v-if="tab === 'create'" class="space-y-4 rounded-lg border p-4">
      <Input v-model="orgName" placeholder="组织名称" />
      <Input v-model="orgDescription" placeholder="描述（可选）" />
      <VbenButton :loading="creating" class="w-full" @click="handleCreate">
        创建并进入
      </VbenButton>
    </div>

    <div v-else-if="tab === 'apply'" class="space-y-4 rounded-lg border p-4">
      <Input v-model="applyCode" placeholder="组织编码" />
      <Input v-model="applyMessage" placeholder="申请说明（可选）" />
      <VbenButton :loading="applying" class="w-full" @click="handleApply">
        提交申请
      </VbenButton>
    </div>

    <div v-else class="space-y-3 rounded-lg border p-4">
      <p v-if="!pendingInvites.length" class="text-muted-foreground text-sm">
        暂无待处理邀请。请打开邮件中的邀请链接完成确认。
      </p>
      <div
        v-for="invite in pendingInvites"
        :key="String(invite.id)"
        class="flex items-center justify-between gap-3 rounded border p-3"
      >
        <div>
          <div class="font-medium">{{ invite.orgName || invite.tenantId }}</div>
          <div class="text-muted-foreground text-xs">
            编码 {{ invite.orgCode || '-' }} · 过期
            {{ invite.expireTime || '-' }}
          </div>
        </div>
        <VbenButton size="sm" @click="router.push(`/org/invite?from=list`)">
          去处理
        </VbenButton>
      </div>
    </div>
  </div>
</template>
