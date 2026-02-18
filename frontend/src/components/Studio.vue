<script setup>
import { onMounted, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { queryChatMcps, queryChatModels, studioGenerate } from '../request/api';
import { normalizeError } from '../request/request';
import AppFooter from './AppFooter.vue';

const router = useRouter();
const loading = ref(false);
const message = ref('');
const mcpList = ref([]);
const apiList = ref([]);
const showMcpSelector = ref(false);

const form = reactive({
    taskPrompt: '',
    strategy: 'react',
    mcpIdList: [],
    apiId: ''
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

const loadModels = async () => {
    const resp = await queryChatModels();
    const list = pickData(resp) || [];
    apiList.value = Array.isArray(list) ? list : [];
    if (!form.apiId && apiList.value.length > 0) {
        form.apiId = apiList.value[0].clientId || '';
    }
};

const loadMcps = async () => {
    const resp = await queryChatMcps();
    const list = pickData(resp) || [];
    mcpList.value = Array.isArray(list) ? list : [];
};

const toggleMcp = (mcpId) => {
    const current = new Set(form.mcpIdList);
    if (current.has(mcpId)) {
        current.delete(mcpId);
    } else {
        current.add(mcpId);
    }
    form.mcpIdList = [...current];
};

const doGenerate = async () => {
    if (!form.taskPrompt.trim()) {
        message.value = '请输入任务描述';
        return;
    }
    loading.value = true;
    message.value = '';
    try {
        await studioGenerate({
            taskPrompt: form.taskPrompt,
            strategy: form.strategy,
            mcpIdList: form.mcpIdList
        });
        message.value = '已完成生成';
    } catch (error) {
        message.value = normalizeError(error).message || '生成失败';
    } finally {
        loading.value = false;
    }
};

const goRepository = () => {
    router.push('/repository');
};

onMounted(async () => {
    try {
        await Promise.all([loadModels(), loadMcps()]);
    } catch (error) {
        message.value = normalizeError(error).message || '初始化失败';
    }
});
</script>

<template>
    <section class="grid h-screen grid-rows-[1fr_var(--footer-height)] bg-white">
        <div class="overflow-y-auto py-[24px] pl-[24px] pr-[calc(24px+var(--scrollbar-w))]">
            <div class="mx-auto max-w-[1100px] space-y-[16px]">
                <div class="flex items-center justify-between gap-[12px]">
                    <h1 class="text-[24px] font-bold text-[var(--text-primary)]">MiniAgent Studio</h1>
                    <button
                        class="rounded-[10px] border border-[var(--border-color)] bg-white px-[12px] py-[8px] text-[13px] font-semibold text-[#334155] transition hover:border-[var(--accent-color)] hover:text-[var(--accent-color)]"
                        @click="goRepository"
                    >
                        我的仓库
                    </button>
                </div>

                <div class="space-y-[12px] rounded-[16px] border border-[var(--border-color)] bg-[#fcfdff] p-[16px]">
                    <div class="grid gap-[10px] lg:grid-cols-[auto_auto_1fr] lg:items-center">
                        <div class="text-[14px] font-semibold text-[var(--text-secondary)]">API 选择</div>
                        <select
                            v-model="form.apiId"
                            class="min-w-[180px] rounded-[10px] border border-[var(--border-color)] bg-white px-[10px] py-[8px] text-[14px] outline-none focus:border-[var(--accent-color)]"
                        >
                            <option v-for="item in apiList" :key="item.clientId" :value="item.clientId">
                                {{ item.clientName || item.clientId }}
                            </option>
                        </select>
                        <div></div>

                        <div class="text-[14px] font-semibold text-[var(--text-secondary)]">执行策略</div>
                        <div class="flex flex-wrap gap-[8px] lg:col-span-2">
                            <button
                                v-for="strategy in ['step', 'loop', 'react']"
                                :key="strategy"
                                class="min-w-[90px] rounded-[10px] border px-[14px] py-[8px] text-[13px] font-semibold transition"
                                :class="
                                    form.strategy === strategy
                                        ? 'border-[var(--accent-color)] bg-[rgba(59,130,246,0.08)] text-[var(--accent-color)]'
                                        : 'border-[var(--border-color)] bg-white text-[#334155] hover:bg-[#f8fafc]'
                                "
                                @click="form.strategy = strategy"
                            >
                                {{ strategy }}
                            </button>
                        </div>

                        <div class="text-[14px] font-semibold text-[var(--text-secondary)]">MCP 工具</div>
                        <div class="flex flex-wrap items-center gap-[8px] lg:col-span-2">
                            <button
                                v-for="mcpId in form.mcpIdList.slice(0, 4)"
                                :key="mcpId"
                                class="rounded-[999px] border border-[#16a34a] bg-[#eafcef] px-[10px] py-[5px] text-[12px] font-semibold text-[#166534]"
                                @click="toggleMcp(mcpId)"
                            >
                                {{ mcpId }}
                            </button>
                            <button
                                class="grid h-[32px] w-[32px] place-items-center rounded-full border border-[#16a34a] bg-[#eafcef] text-[18px] leading-none text-[#166534] transition hover:scale-[1.03]"
                                @click="showMcpSelector = !showMcpSelector"
                            >
                                +
                            </button>
                        </div>
                    </div>

                    <div v-if="showMcpSelector" class="flex flex-wrap gap-[8px] rounded-[12px] border border-[var(--border-color)] bg-white p-[10px]">
                        <button
                            v-for="item in mcpList"
                            :key="item.mcpId"
                            class="rounded-[999px] border px-[10px] py-[5px] text-[12px] transition"
                            :class="
                                form.mcpIdList.includes(item.mcpId)
                                    ? 'border-[#16a34a] bg-[#dcfce7] text-[#166534]'
                                    : 'border-[var(--border-color)] bg-white text-[var(--text-secondary)] hover:border-[#16a34a]'
                            "
                            @click="toggleMcp(item.mcpId)"
                        >
                            {{ item.mcpName }} ({{ item.sourceType || 'system' }})
                        </button>
                    </div>

                    <div class="grid gap-[12px] lg:grid-cols-[1fr_auto] lg:items-end">
                        <textarea
                            v-model="form.taskPrompt"
                            class="min-h-[220px] w-full rounded-[18px] border border-[var(--border-color)] bg-white px-[14px] py-[14px] text-[16px] leading-[1.45] outline-none focus:border-[var(--accent-color)] placeholder:text-[#94a3b8]"
                            placeholder="请输入你的 MiniAgent 需求"
                        ></textarea>
                        <button
                            class="h-[84px] rounded-[20px] border border-[var(--border-color)] bg-white px-[22px] text-[46px] font-bold leading-none text-[#1f2a44] transition hover:border-[var(--accent-color)] hover:text-[var(--accent-color)] disabled:opacity-70"
                            :disabled="loading"
                            @click="doGenerate"
                        >
                            {{ loading ? '...' : '一键生成' }}
                        </button>
                    </div>
                </div>

                <div v-if="message" class="text-[13px] text-[var(--text-secondary)]">{{ message }}</div>
            </div>
        </div>

        <AppFooter />
    </section>
</template>
