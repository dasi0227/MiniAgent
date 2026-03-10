<script setup>
import { computed, onMounted, reactive, ref } from 'vue';
import { queryChatMcps, queryModelList, workspaceAgentCreate } from '../request/api';
import { notifyAppError } from '../request/request';
import { useSettingsStore } from '../router/pinia';
import { getStudioSelectionTone } from '../utils/StrategyTone';
import Footer from './Footer.vue';

const settingsStore = useSettingsStore();
const isDarkTheme = computed(() => settingsStore.theme === 'dark');
const loading = ref(false);
const message = ref('');
const mcpList = ref([]);
const modelList = ref([]);

const form = reactive({
    taskPrompt: '',
    strategy: 'react',
    mcpIdList: [],
    modelId: '',
    agentName: ''
});

const selectedModelName = computed(() => {
    const matched = modelList.value.find((item) => item?.modelId === form.modelId);
    return (matched?.modelName || '').trim();
});

const resolveStudioToneStyle = (kind) => {
    const tone = getStudioSelectionTone(kind, isDarkTheme.value);
    return {
        '--studio-active-border': tone.activeBorder,
        '--studio-active-bg': tone.activeBg,
        '--studio-active-text': tone.activeText,
        '--studio-hover-border': tone.hoverBorder,
        '--studio-hover-bg': tone.hoverBg
    };
};

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
    const resp = await queryModelList();
    const list = pickData(resp) || [];
    modelList.value = Array.isArray(list) ? list : [];
    if (!form.modelId && modelList.value.length > 0) {
        form.modelId = modelList.value[0].modelId || '';
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
    if (!form.agentName.trim()) {
        message.value = '请输入 MiniAgent 名称';
        return;
    }
    if (!form.modelId.trim()) {
        message.value = '请选择模型';
        return;
    }
    if (!selectedModelName.value) {
        message.value = '模型信息缺失，请重新选择模型';
        return;
    }
    if (!form.taskPrompt.trim()) {
        message.value = '请输入任务描述';
        return;
    }
    loading.value = true;
    message.value = '';
    try {
        await workspaceAgentCreate({
            modelId: form.modelId.trim(),
            modelName: selectedModelName.value,
            agentName: form.agentName.trim(),
            agentDesc: form.taskPrompt.trim(),
            strategy: form.strategy,
            mcpIdSet: [...new Set(form.mcpIdList)]
        });
        message.value = '创建成功';
    } catch (error) {
        notifyAppError(error, '创建失败');
    } finally {
        loading.value = false;
    }
};

onMounted(async () => {
    try {
        await Promise.all([loadModels(), loadMcps()]);
    } catch (error) {
        notifyAppError(error, '初始化失败');
    }
});
</script>

<template>
    <section class="grid h-screen grid-rows-[1fr_var(--footer-height)] bg-white">
        <div class="overflow-y-auto py-[24px] pl-[24px] pr-[calc(24px+var(--scrollbar-w))]">
            <div class="mx-auto max-w-[1100px] space-y-[16px]">
                <div class="flex items-center justify-between gap-[12px]">
                    <h1 class="text-[24px] font-bold text-[var(--text-primary)]">MiniAgent Studio</h1>
                </div>

                <div class="space-y-[18px] rounded-[16px] bg-white p-[18px]">
                    <div class="flex min-h-[50px] items-center gap-[12px]">
                        <div class="flex h-[44px] w-[140px] shrink-0 items-center text-[16px] font-semibold tracking-[0.02em] text-[var(--text-secondary)]">MiniAgent 名称</div>
                        <div class="min-w-0 flex-1">
                            <input
                                v-model="form.agentName"
                                class="h-[44px] w-full rounded-[10px] border border-[var(--border-color)] bg-white px-[12px] text-[15px] text-[var(--text-primary)] outline-none transition focus:border-[var(--accent-color)]"
                                placeholder="请输入 MiniAgent 名称"
                            />
                        </div>
                    </div>

                    <div class="flex min-h-[50px] items-center gap-[12px]">
                        <div class="flex h-[44px] w-[140px] shrink-0 items-center text-[16px] font-semibold tracking-[0.02em] text-[var(--text-secondary)]">模型选择</div>
                        <div class="min-w-0 flex-1">
                            <div class="no-scrollbar flex items-center gap-[8px] overflow-x-auto pb-[4px]">
                                <button
                                    v-for="item in modelList"
                                    :key="item.modelId"
                                    class="h-[44px] shrink-0 whitespace-nowrap rounded-[999px] border px-[15px] text-[16px] font-semibold transition"
                                    :style="resolveStudioToneStyle('model')"
                                    :class="
                                        form.modelId === item.modelId
                                            ? 'border-[var(--studio-active-border)] bg-[var(--studio-active-bg)] text-[var(--studio-active-text)]'
                                            : 'border-[var(--border-color)] bg-white text-[#475569] hover:border-[var(--studio-hover-border)] hover:bg-[var(--studio-hover-bg)]'
                                    "
                                    @click="
                                        form.modelId = item.modelId || '';
                                    "
                                >
                                    {{ item.modelName || item.modelId }}
                                </button>
                                <div
                                    v-if="modelList.length === 0"
                                    class="inline-flex h-[44px] shrink-0 items-center rounded-[999px] border border-dashed border-[var(--border-color)] px-[13px] text-[14px] text-[var(--text-secondary)]"
                                >
                                    暂无可用模型
                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="flex min-h-[50px] items-center gap-[12px]">
                        <div class="flex h-[44px] w-[140px] shrink-0 items-center text-[16px] font-semibold tracking-[0.02em] text-[var(--text-secondary)]">工具选择</div>
                        <div class="min-w-0 flex-1">
                            <div class="no-scrollbar flex items-center gap-[8px] overflow-x-auto pb-[4px]">
                                <button
                                    v-for="item in mcpList"
                                    :key="item.mcpId"
                                    class="h-[44px] shrink-0 whitespace-nowrap rounded-[999px] border px-[15px] text-[16px] font-semibold transition"
                                    :style="resolveStudioToneStyle('tool')"
                                    :class="
                                        form.mcpIdList.includes(item.mcpId)
                                            ? 'border-[var(--studio-active-border)] bg-[var(--studio-active-bg)] text-[var(--studio-active-text)]'
                                            : 'border-[var(--border-color)] bg-white text-[#475569] hover:border-[var(--studio-hover-border)] hover:bg-[var(--studio-hover-bg)]'
                                    "
                                    @click="toggleMcp(item.mcpId)"
                                >
                                    {{ item.mcpName || item.mcpId }}
                                </button>
                                <div
                                    v-if="mcpList.length === 0"
                                    class="inline-flex h-[44px] shrink-0 items-center rounded-[999px] border border-dashed border-[var(--border-color)] px-[13px] text-[14px] text-[var(--text-secondary)]"
                                >
                                    暂无可用的自建 MCP
                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="flex min-h-[50px] items-center gap-[12px]">
                        <div class="flex h-[44px] w-[140px] shrink-0 items-center text-[16px] font-semibold tracking-[0.02em] text-[var(--text-secondary)]">执行策略</div>
                        <div class="min-w-0 flex-1">
                            <div class="flex items-center gap-[8px] overflow-x-auto pb-[4px]">
                                <button
                                    v-for="strategy in ['step', 'loop', 'react']"
                                    :key="strategy"
                                    class="h-[44px] min-w-[104px] shrink-0 rounded-[10px] border px-[17px] text-[16px] font-semibold transition"
                                    :style="resolveStudioToneStyle(strategy)"
                                    :class="
                                        form.strategy === strategy
                                            ? 'border-[var(--studio-active-border)] bg-[var(--studio-active-bg)] text-[var(--studio-active-text)]'
                                            : 'border-[var(--border-color)] bg-white text-[#334155] hover:border-[var(--studio-hover-border)] hover:bg-[var(--studio-hover-bg)]'
                                    "
                                    @click="form.strategy = strategy"
                                >
                                    {{ strategy }}
                                </button>
                            </div>
                        </div>
                    </div>

                    <div class="space-y-[12px]">
                        <textarea
                            v-model="form.taskPrompt"
                            class="h-[248px] w-full resize-none rounded-[16px] border border-[var(--border-color)] bg-white px-[16px] py-[16px] text-[16px] leading-[1.6] outline-none focus:border-[var(--accent-color)] placeholder:text-[16px] placeholder:leading-[1.7] placeholder:text-[#94a3b8]"
                            placeholder="描述你希望 MiniAgent 完成的任务目标、输入上下文、执行约束和产出格式。例如：每周一早上 9 点汇总上周投放数据，给出异常原因与优化建议，并输出可直接发送给团队的简报。"
                        ></textarea>
                        <div class="flex justify-center">
                            <button
                                class="h-[44px] rounded-[12px] border border-[var(--border-color)] bg-white px-[24px] text-[16px] font-semibold text-[#1f2a44] transition hover:border-[var(--accent-color)] hover:text-[var(--accent-color)] disabled:opacity-70"
                                :disabled="loading"
                                @click="doGenerate"
                            >
                                {{ loading ? '创建中...' : '一键创建' }}
                            </button>
                        </div>
                    </div>
                </div>

                <div v-if="message" class="text-[13px] text-[var(--text-secondary)]">{{ message }}</div>
            </div>
        </div>

        <Footer />
    </section>
</template>

<style scoped>
.no-scrollbar {
    -ms-overflow-style: none;
    scrollbar-width: none;
}

.no-scrollbar::-webkit-scrollbar {
    display: none;
}
</style>
