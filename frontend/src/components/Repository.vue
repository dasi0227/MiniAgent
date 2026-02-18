<script setup>
import { computed, onMounted, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { plazaPublish, repoList, repoRemove, studioListMine } from '../request/api';
import { normalizeError } from '../request/request';
import { useWelcomeLaunchStore } from '../router/pinia';
import AppFooter from './AppFooter.vue';

const router = useRouter();
const welcomeLaunchStore = useWelcomeLaunchStore();
const loading = ref(false);
const message = ref('');
const repoItems = ref([]);
const mineAgents = ref([]);
const publishForm = reactive({
    agentId: '',
    plazaTitle: '',
    plazaDesc: ''
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

const loadMine = async () => {
    try {
        const resp = await studioListMine();
        const list = pickData(resp) || [];
        mineAgents.value = Array.isArray(list) ? list : [];
        if (!publishForm.agentId && mineAgents.value.length > 0) {
            publishForm.agentId = mineAgents.value[0].agentId;
        }
    } catch (error) {
        message.value = normalizeError(error).message || '获取我的 MiniAgent 失败';
    }
};

const doPublish = async () => {
    if (!publishForm.agentId || !publishForm.plazaTitle.trim()) {
        message.value = '请选择 MiniAgent 并填写展示标题';
        return;
    }
    try {
        await plazaPublish({
            agentId: publishForm.agentId,
            plazaTitle: publishForm.plazaTitle,
            plazaDesc: publishForm.plazaDesc
        });
        publishForm.plazaTitle = '';
        publishForm.plazaDesc = '';
        message.value = '发布成功';
    } catch (error) {
        message.value = normalizeError(error).message || '发布失败';
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

const addedItems = computed(() =>
    repoItems.value.filter((item) => `${item?.sourceType || ''}`.toLowerCase() !== 'mine')
);

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
    await Promise.all([loadRepo(), loadMine()]);
});
</script>

<template>
    <section class="grid h-screen grid-rows-[1fr_var(--footer-height)] bg-[var(--bg-page)]">
        <div class="overflow-y-auto py-[24px] pl-[24px] pr-[calc(24px+var(--scrollbar-w))]">
            <div class="mx-auto max-w-[980px] space-y-[16px]">
                <h1 class="text-[24px] font-bold">MiniAgent Repository</h1>

                <div class="rounded-[14px] border border-[var(--border-color)] bg-white p-[14px]">
                    <div class="mb-[10px] text-[16px] font-semibold">发布我的 MiniAgent</div>
                    <div v-if="mineAgents.length === 0" class="text-[13px] text-[var(--text-secondary)]">暂无可发布的 MiniAgent</div>
                    <div v-else class="grid gap-[10px] md:grid-cols-[220px_1fr_1fr_auto]">
                        <select v-model="publishForm.agentId" class="rounded-[10px] border border-[var(--border-color)] px-[10px] py-[10px] text-[14px]">
                            <option v-for="item in mineAgents" :key="item.agentId" :value="item.agentId">{{ item.agentName }}</option>
                        </select>
                        <input v-model="publishForm.plazaTitle" class="rounded-[10px] border border-[var(--border-color)] px-[10px] py-[10px] text-[14px]" placeholder="展示标题" />
                        <input v-model="publishForm.plazaDesc" class="rounded-[10px] border border-[var(--border-color)] px-[10px] py-[10px] text-[14px]" placeholder="展示描述" />
                        <button class="rounded-[10px] border border-[var(--accent-color)] bg-[var(--accent-color)] px-[14px] py-[10px] text-white font-semibold" @click="doPublish">发布</button>
                    </div>
                </div>

                <div class="rounded-[14px] border border-[var(--border-color)] bg-white p-[14px]">
                    <div class="mb-[10px] text-[16px] font-semibold">我创建的 MiniAgent</div>
                    <div v-if="mineAgents.length === 0" class="text-[13px] text-[var(--text-secondary)]">暂无数据</div>
                    <div class="space-y-[8px]">
                        <div v-for="item in mineAgents" :key="item.agentId" class="rounded-[10px] border border-[var(--border-color)] p-[10px]">
                            <div class="flex items-center justify-between gap-[12px]">
                                <div>
                                    <div class="font-semibold">{{ item.agentName }} ({{ item.agentType }})</div>
                                    <div class="text-[12px] text-[var(--text-secondary)]">{{ item.agentId }}</div>
                                    <div class="text-[12px] text-[var(--text-secondary)]">{{ item.agentDesc }}</div>
                                </div>
                                <div class="flex gap-[8px]">
                                    <button class="rounded-[8px] border border-[var(--border-color)] px-[10px] py-[6px] text-[12px]" @click="useAgent(item)">使用</button>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <div v-if="message" class="text-[13px] text-[var(--text-secondary)]">{{ message }}</div>

                <div class="rounded-[14px] border border-[var(--border-color)] bg-white p-[14px]">
                    <div class="mb-[10px] text-[16px] font-semibold">我添加的 MiniAgent</div>
                    <div v-if="loading" class="text-[13px] text-[var(--text-secondary)]">加载中...</div>
                    <div v-else-if="addedItems.length === 0" class="text-[13px] text-[var(--text-secondary)]">暂无数据</div>
                    <div class="space-y-[8px]">
                        <div v-for="item in addedItems" :key="item.repoId" class="rounded-[10px] border border-[var(--border-color)] p-[10px]">
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

        <AppFooter />
    </section>
</template>
