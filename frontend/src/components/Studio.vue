<script setup>
import { onMounted, reactive, ref } from 'vue';
import { queryChatMcps, studioCreate, studioDetail, studioGenerate, studioListMine, studioUpdate } from '../request/api';
import { normalizeError } from '../request/request';
import AppFooter from './AppFooter.vue';

const loading = ref(false);
const saveLoading = ref(false);
const saveMode = ref('create');
const message = ref('');
const mineList = ref([]);
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

const loadMine = async () => {
    const resp = await studioListMine();
    mineList.value = pickData(resp) || [];
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
        message.value = '请先生成或填写 Agent 名称';
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
        await loadMine();
    } catch (error) {
        message.value = normalizeError(error).message || '保存失败';
    } finally {
        saveLoading.value = false;
    }
};

const openMine = async (agentId) => {
    currentAgentId.value = agentId;
    saveMode.value = 'update';
    message.value = '';
    try {
        const resp = await studioDetail(agentId);
        const data = pickData(resp) || {};
        generated.agentName = data.agentName || '';
        generated.agentType = data.agentType || 'react';
        generated.agentDesc = data.agentDesc || '';
        generated.flowPrompt = data.flowPrompt || generated.flowPrompt;
    } catch (error) {
        message.value = normalizeError(error).message || '加载详情失败';
    }
};

onMounted(async () => {
    await Promise.all([loadMine(), loadMcps()]);
});
</script>

<template>
    <section class="grid h-screen grid-rows-[1fr_var(--footer-height)] bg-[var(--bg-page)]">
        <div class="overflow-y-auto py-[24px] pl-[24px] pr-[calc(24px+var(--scrollbar-w))]">
            <div class="mx-auto max-w-[980px] space-y-[16px]">
                <h1 class="text-[24px] font-bold text-[var(--text-primary)]">Agent Studio</h1>

                <div class="rounded-[14px] border border-[var(--border-color)] bg-white p-[14px] space-y-[10px]">
                    <div class="grid gap-[10px] md:grid-cols-[1fr_160px]">
                        <textarea
                            v-model="form.taskPrompt"
                            class="w-full min-h-[110px] rounded-[10px] border border-[var(--border-color)] px-[10px] py-[10px] text-[14px]"
                            placeholder="输入你想创建的 MiniAgent 任务描述"
                        ></textarea>
                        <div class="space-y-[10px]">
                            <select v-model="form.strategy" class="w-full rounded-[10px] border border-[var(--border-color)] px-[10px] py-[10px] text-[14px]">
                                <option value="step">step</option>
                                <option value="loop">loop</option>
                                <option value="react">react</option>
                            </select>
                            <button
                                class="w-full rounded-[10px] border border-[var(--accent-color)] bg-[var(--accent-color)] px-[12px] py-[10px] text-white font-semibold disabled:opacity-70"
                                :disabled="loading"
                                @click="doGenerate"
                            >
                                {{ loading ? '生成中...' : '生成草稿' }}
                            </button>
                        </div>
                    </div>
                    <div>
                        <div class="mb-[6px] text-[13px] text-[var(--text-secondary)]">MCP 选择</div>
                        <div class="flex flex-wrap gap-[8px]">
                            <label v-for="item in mcpList" :key="item.mcpId" class="inline-flex items-center gap-[6px] rounded-[999px] border border-[var(--border-color)] px-[10px] py-[4px] text-[12px]">
                                <input v-model="form.mcpIdList" type="checkbox" :value="item.mcpId" />
                                <span>{{ item.mcpName }}</span>
                                <span class="text-[var(--text-secondary)]">({{ item.sourceType || 'system' }})</span>
                            </label>
                        </div>
                    </div>
                </div>

                <div class="rounded-[14px] border border-[var(--border-color)] bg-white p-[14px] space-y-[10px]">
                    <div class="grid gap-[10px] md:grid-cols-2">
                        <input v-model="generated.agentName" class="rounded-[10px] border border-[var(--border-color)] px-[10px] py-[10px] text-[14px]" placeholder="Agent 名称" />
                        <input v-model="generated.agentDesc" class="rounded-[10px] border border-[var(--border-color)] px-[10px] py-[10px] text-[14px]" placeholder="Agent 描述" />
                    </div>
                    <textarea
                        v-model="generated.flowPrompt"
                        class="w-full min-h-[140px] rounded-[10px] border border-[var(--border-color)] px-[10px] py-[10px] text-[14px]"
                        placeholder="Flow Prompt"
                    ></textarea>
                    <div class="flex justify-end">
                        <button
                            class="rounded-[10px] border border-[var(--accent-color)] bg-[var(--accent-color)] px-[14px] py-[10px] text-white font-semibold disabled:opacity-70"
                            :disabled="saveLoading"
                            @click="doSave"
                        >
                            {{ saveLoading ? '保存中...' : saveMode === 'update' ? '更新 Agent' : '保存私有 Agent' }}
                        </button>
                    </div>
                    <div v-if="message" class="text-[13px] text-[var(--text-secondary)]">{{ message }}</div>
                </div>

                <div class="rounded-[14px] border border-[var(--border-color)] bg-white p-[14px]">
                    <div class="mb-[10px] text-[16px] font-semibold">我的 Agent</div>
                    <div v-if="mineList.length === 0" class="text-[13px] text-[var(--text-secondary)]">暂无数据</div>
                    <div class="space-y-[8px]">
                        <button
                            v-for="item in mineList"
                            :key="item.agentId"
                            class="w-full rounded-[10px] border border-[var(--border-color)] px-[12px] py-[10px] text-left hover:bg-[#f7f9fc]"
                            @click="openMine(item.agentId)"
                        >
                            <div class="font-semibold">{{ item.agentName }} ({{ item.agentType }})</div>
                            <div class="text-[12px] text-[var(--text-secondary)]">{{ item.agentId }}</div>
                        </button>
                    </div>
                </div>
            </div>
        </div>

        <AppFooter inner-class="max-w-[980px] pl-[24px] pr-[calc(24px+var(--scrollbar-w))]" />
    </section>
</template>
