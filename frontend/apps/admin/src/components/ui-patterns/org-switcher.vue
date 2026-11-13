<script lang="ts" setup>
import { computed, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';

import { ChevronDown } from '@vben/icons';

import { orgApi, type OrgResult } from '#/api/org';
import { toast } from '#/ui-patterns/toast';
import { useUserStore } from '#/store';

defineOptions({ name: 'OrgSwitcher' });

const router = useRouter();
const userStore = useUserStore();

const orgs = ref<OrgResult[]>([]);
const current = ref<OrgResult | null>(null);
const open = ref(false);
const loading = ref(false);

const showSwitcher = computed(() => orgs.value.length > 0);
const canSwitch = computed(() => orgs.value.length > 1);

async function load() {
  try {
    const session = await orgApi.session();
    orgs.value = session.organizations || [];
    current.value = session.current || orgs.value[0] || null;
    if (session.needsOrganization) {
      await router.replace('/org/setup');
    }
  } catch {
    // 未登录或接口失败时不打断布局
  }
}

async function switchTo(org: OrgResult) {
  if (String(org.id) === String(current.value?.id)) {
    open.value = false;
    return;
  }
  loading.value = true;
  try {
    await orgApi.switchOrg(org.id);
    current.value = org;
    open.value = false;
    toast.success(`已切换到「${org.name}」`);
    await userStore.fetchAuthInfo();
    // 菜单/权限可能随组织变化，刷新页面最稳妥
    window.location.reload();
  } finally {
    loading.value = false;
  }
}

onMounted(load);

defineExpose({ reload: load });
</script>

<template>
  <div v-if="showSwitcher" class="relative ml-2 flex min-w-0 items-center">
    <button
      type="button"
      class="text-foreground hover:bg-accent flex max-w-[220px] items-center gap-1 truncate rounded-md px-2 py-1 text-sm font-medium"
      :disabled="loading"
      @click="canSwitch ? (open = !open) : undefined"
    >
      <span class="truncate">{{ current?.name || '未选择组织' }}</span>
      <ChevronDown v-if="canSwitch" class="size-3.5 shrink-0 opacity-60" />
    </button>
    <div
      v-if="open && canSwitch"
      class="bg-popover text-popover-foreground absolute left-0 top-full z-50 mt-1 min-w-[200px] rounded-md border p-1 shadow-md"
    >
      <button
        v-for="org in orgs"
        :key="String(org.id)"
        type="button"
        class="hover:bg-accent flex w-full items-center justify-between rounded px-2 py-1.5 text-left text-sm"
        @click="switchTo(org)"
      >
        <span class="truncate">{{ org.name }}</span>
        <span
          v-if="String(org.id) === String(current?.id)"
          class="text-primary text-xs"
        >
          当前
        </span>
      </button>
    </div>
  </div>
</template>
