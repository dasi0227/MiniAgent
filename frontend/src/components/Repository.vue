<script setup>
import { onMounted, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { repoAdd, repoList, repoRemove } from '../request/api';
import { normalizeError } from '../request/request';
import { useWelcomeLaunchStore } from '../router/pinia';
import AppFooter from './AppFooter.vue';

const router = useRouter();
const welcomeLaunchStore = useWelcomeLaunchStore();
const loading = ref(false);
const message = ref('');
const repoItems = ref([]);
const form = reactive({
    agentId: ''
});

const pickData = (resp) => {
    if (resp && typeof resp === 'object' && Object.prototype.hasOwnProperty.call(resp, 'code')) {
        if (resp.code !== 200) {
            throw new Error(resp.info || '操作失败');
        }
        return resp.data;
    }
    return resp?.data ?? resp?.result ?? resp;
};

const loadRepo = async () => {
    loading.value = true;
    try {
        const resp = await repoList();
        const list = pickData(resp) || [];
        repoItems.value = Array.isArray(list) ? list : [];
    } catch (error) {
        message.value = normalizeError(error).message || '获取仓库失败';
    } finally {
        loading.value = false;
    }
};

const doAdd = async () => {
    if (!form.agentId.trim()) {
        return;
    }
    try {
        await repoAdd({ agentId: form.agentId.trim() });
        form.agentId = '';
        message.value = '添加成功';
        await loadRepo();
    } catch (error) {
        message.value = normalizeError(error).message || '添加失败';
    }
};

const doRemove = async (agentId) => {
    try {
        await repoRemove({ agentId });
        await loadRepo();
    } catch (error) {
        message.value = normalizeError(error).message || '移除失败';
    }
};

const useAgent = (item) => {
    welcomeLaunchStore.setTask({
        type: 'work',
        prompt: '请根据当前任务执行。',
        sessionTitle: item.agentName || '仓库任务',
        agentId: item.agentId
    });
    router.push('/work');
};

onMounted(async () => {
    await loadRepo();
});
</script>

<template>
    <section class="grid h-screen grid-rows-[1fr_var(--footer-height)] bg-[var(--bg-page)]">
        <div class="overflow-y-auto py-[24px] pl-[24px] pr-[calc(24px+var(--scrollbar-w))]">
            <div class="mx-auto max-w-[980px] space-y-[16px]">
                <h1 class="text-[24px] font-bold">Agent Repository</h1>

                <div class="rounded-[14px] border border-[var(--border-color)] bg-white p-[14px]">
                    <div class="mb-[8px] text-[16px] font-semibold">添加 Agent</div>
                    <div class="flex gap-[8px]">
                        <input v-model="form.agentId" class="flex-1 rounded-[10px] border border-[var(--border-color)] px-[10px] py-[10px] text-[14px]" placeholder="输入 agentId" />
                        <button class="rounded-[10px] border border-[var(--accent-color)] bg-[var(--accent-color)] px-[14px] py-[10px] text-white font-semibold" @click="doAdd">添加</button>
                    </div>
                </div>

                <div v-if="message" class="text-[13px] text-[var(--text-secondary)]">{{ message }}</div>

                <div class="rounded-[14px] border border-[var(--border-color)] bg-white p-[14px]">
                    <div class="mb-[10px] text-[16px] font-semibold">我的仓库</div>
                    <div v-if="loading" class="text-[13px] text-[var(--text-secondary)]">加载中...</div>
                    <div v-else-if="repoItems.length === 0" class="text-[13px] text-[var(--text-secondary)]">暂无数据</div>
                    <div class="space-y-[8px]">
                        <div v-for="item in repoItems" :key="item.repoId" class="rounded-[10px] border border-[var(--border-color)] p-[10px]">
                            <div class="flex items-center justify-between gap-[12px]">
                                <div>
                                    <div class="font-semibold">{{ item.agentName }}</div>
                                    <div class="text-[12px] text-[var(--text-secondary)]">{{ item.agentId }} · {{ item.sourceType }}</div>
                                    <div class="text-[12px] text-[var(--text-secondary)]">{{ item.agentDesc }}</div>
                                </div>
                                <div class="flex gap-[8px]">
                                    <button class="rounded-[8px] border border-[var(--border-color)] px-[10px] py-[6px] text-[12px]" @click="useAgent(item)">使用</button>
                                    <button class="rounded-[8px] border border-[var(--border-color)] px-[10px] py-[6px] text-[12px]" @click="doRemove(item.agentId)">移除</button>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <AppFooter inner-class="max-w-[980px] pl-[24px] pr-[calc(24px+var(--scrollbar-w))]" />
    </section>
</template>
