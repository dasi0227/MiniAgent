<script setup>
import { onMounted, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { queryChatMcps, studioCreate, studioGenerate, studioUpdate } from '../request/api';
import { normalizeError } from '../request/request';
import AppFooter from './AppFooter.vue';

const router = useRouter();
const loading = ref(false);
const saveLoading = ref(false);
const saveMode = ref('create');
const message = ref('');
const mcpList = ref([]);
const currentAgentId = ref('');

const form = reactive({
    taskPrompt: '',
    strategy: 'react',
    mcpIdList: []
});

const generated = reactive({
    agentName: '',
    agentType: 'react',
    agentDesc: '',
    flowPrompt: ''
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

const loadMcps = async () => {
    const resp = await queryChatMcps();
    const list = pickData(resp) || [];
    mcpList.value = Array.isArray(list) ? list : [];
};

const doGenerate = async () => {
    if (!form.taskPrompt.trim()) {
        message.value = '请输入任务描述';
        return;
    }
    loading.value = true;
    message.value = '';
    try {
        const resp = await studioGenerate({
            taskPrompt: form.taskPrompt,
            strategy: form.strategy,
            mcpIdList: form.mcpIdList
        });
        const data = pickData(resp) || {};
        generated.agentName = data.agentName || '';
        generated.agentType = data.agentType || form.strategy;
        generated.agentDesc = data.agentDesc || '';
        generated.flowPrompt = data.flowPrompt || '';
        message.value = '已生成草稿，可直接保存';
    } catch (error) {
        message.value = normalizeError(error).message || '生成失败';
    } finally {
        loading.value = false;
    }
};

const doSave = async () => {
    if (!generated.agentName.trim()) {
        message.value = '请先生成或填写 MiniAgent 名称';
        return;
    }
    saveLoading.value = true;
    message.value = '';
    try {
        if (saveMode.value === 'update' && currentAgentId.value) {
            await studioUpdate({
                agentId: currentAgentId.value,
                agentName: generated.agentName,
                agentDesc: generated.agentDesc,
                flowPrompt: generated.flowPrompt
            });
            message.value = '更新成功';
        } else {
            const resp = await studioCreate({
                agentName: generated.agentName,
                agentType: generated.agentType || form.strategy,
                agentDesc: generated.agentDesc,
                flowPrompt: generated.flowPrompt,
                mcpIdList: form.mcpIdList
            });
            const data = pickData(resp) || {};
            currentAgentId.value = data.agentId || '';
            saveMode.value = 'update';
            message.value = '创建成功';
        }
    } catch (error) {
        message.value = normalizeError(error).message || '保存失败';
    } finally {
        saveLoading.value = false;
    }
};

const goRepository = () => {
    router.push('/repository');
};

onMounted(async () => {
    await loadMcps();
});
</script>

<template>
    <section class="grid h-screen grid-rows-[1fr_var(--footer-height)] bg-white">
        <div class="overflow-y-auto py-[24px] pl-[24px] pr-[calc(24px+var(--scrollbar-w))]">
            <div class="mx-auto max-w-[1100px] space-y-[20px]">
                <div class="flex items-center justify-between gap-[12px]">
                    <h1 class="text-[24px] font-bold text-[var(--text-primary)]">MiniAgent Studio</h1>
                    <button
                        class="rounded-[10px] border border-[var(--border-color)] bg-white px-[12px] py-[8px] text-[13px] font-semibold text-[#334155] transition hover:border-[var(--accent-color)] hover:text-[var(--accent-color)]"
                        @click="goRepository"
                    >
                        我的仓库
                    </button>
                </div>

                <div class="space-y-[12px] rounded-[16px] bg-[#f8fafc] p-[16px]">
                    <div class="grid gap-[12px] md:grid-cols-[1fr_180px]">
                        <textarea
                            v-model="form.taskPrompt"
                            class="w-full min-h-[140px] rounded-[14px] border border-[rgba(148,163,184,0.35)] bg-white px-[12px] py-[12px] text-[14px] outline-none focus:border-[var(--accent-color)]"
                            placeholder="请输入你的 MiniAgent 需求"
                        ></textarea>
                        <div class="space-y-[10px]">
                            <select
                                v-model="form.strategy"
                                class="w-full rounded-[12px] border border-[rgba(148,163,184,0.35)] bg-white px-[10px] py-[10px] text-[14px] outline-none focus:border-[var(--accent-color)]"
                            >
                                <option value="step">step</option>
                                <option value="loop">loop</option>
                                <option value="react">react</option>
                            </select>
                            <button
                                class="w-full rounded-[12px] border border-[var(--accent-color)] bg-[var(--accent-color)] px-[12px] py-[10px] text-white font-semibold disabled:opacity-70"
                                :disabled="loading"
                                @click="doGenerate"
                            >
                                {{ loading ? '生成中...' : '一键生成' }}
                            </button>
                        </div>
                    </div>
                    <div>
                        <div class="mb-[8px] text-[13px] text-[var(--text-secondary)]">MCP 工具</div>
                        <div class="flex flex-wrap gap-[8px]">
                            <label
                                v-for="item in mcpList"
                                :key="item.mcpId"
                                class="inline-flex items-center gap-[6px] rounded-[999px] border border-[rgba(148,163,184,0.4)] bg-white px-[10px] py-[5px] text-[12px]"
                            >
                                <input v-model="form.mcpIdList" type="checkbox" :value="item.mcpId" />
                                <span>{{ item.mcpName }}</span>
                                <span class="text-[var(--text-secondary)]">({{ item.sourceType || 'system' }})</span>
                            </label>
                        </div>
                    </div>
                </div>

                <div class="space-y-[10px] border-t border-[var(--border-color)] pt-[16px]">
                    <div class="text-[14px] font-semibold text-[var(--text-secondary)]">MiniAgent 草稿</div>
                    <div class="grid gap-[10px] md:grid-cols-2">
                        <input
                            v-model="generated.agentName"
                            class="rounded-[12px] border border-[rgba(148,163,184,0.35)] bg-white px-[10px] py-[10px] text-[14px] outline-none focus:border-[var(--accent-color)]"
                            placeholder="MiniAgent 名称"
                        />
                        <input
                            v-model="generated.agentDesc"
                            class="rounded-[12px] border border-[rgba(148,163,184,0.35)] bg-white px-[10px] py-[10px] text-[14px] outline-none focus:border-[var(--accent-color)]"
                            placeholder="MiniAgent 描述"
                        />
                    </div>
                    <textarea
                        v-model="generated.flowPrompt"
                        class="w-full min-h-[160px] rounded-[14px] border border-[rgba(148,163,184,0.35)] bg-white px-[10px] py-[10px] text-[14px] outline-none focus:border-[var(--accent-color)]"
                        placeholder="Flow Prompt"
                    ></textarea>
                    <div class="flex justify-end">
                        <button
                            class="rounded-[12px] border border-[var(--accent-color)] bg-[var(--accent-color)] px-[14px] py-[10px] text-white font-semibold disabled:opacity-70"
                            :disabled="saveLoading"
                            @click="doSave"
                        >
                            {{ saveLoading ? '保存中...' : saveMode === 'update' ? '更新 MiniAgent' : '保存私有 MiniAgent' }}
                        </button>
                    </div>
                    <div v-if="message" class="text-[13px] text-[var(--text-secondary)]">{{ message }}</div>
                </div>
            </div>
        </div>

        <AppFooter />
    </section>
</template>
