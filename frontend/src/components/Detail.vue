<script setup>
import { computed, onMounted, reactive, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import {
    queryModelList,
    queryRoleMap,
    userApiList,
    userMcpList,
    workspaceAgentBaseUpdate,
    workspaceAgentDetail,
    workspaceAgentMcpUpdate,
    workspaceAgentModelUpdate,
    workspaceAgentSystemPromptUpdate,
    workspaceAgentUserPromptUpdate
} from '../request/api';
import { notifyAppError } from '../request/request';
import { useSettingsStore } from '../router/pinia';
import { getStrategyTone, normalizeStrategyType } from '../utils/StrategyTone';
import Footer from './Footer.vue';

const route = useRoute();
const router = useRouter();
const settingsStore = useSettingsStore();
const isDarkTheme = computed(() => settingsStore.theme === 'dark');

const detail = ref(null);
const roleMap = ref({});
const userApiOptions = ref([]);
const userMcpOptions = ref([]);
const modelCatalog = ref([]);

const agentId = computed(() => (route.params.agentId || '').toString().trim());

const strategyRoleOrder = {
    step: ['inspector', 'planner', 'runner', 'replier'],
    loop: ['analyzer', 'performer', 'supervisor', 'summarizer'],
    react: ['observer', 'reasoner', 'actor', 'evaluator']
};

const baseModal = reactive({
    open: false,
    saving: false,
    agentName: '',
    agentDesc: ''
});

const modelModal = reactive({
    open: false,
    saving: false,
    selectedApiId: '',
    options: []
});

const mcpEditModal = reactive({
    open: false,
    saving: false,
    selectedIds: []
});

const promptModal = reactive({
    open: false,
    mode: 'system',
    title: '',
    saving: false,
    value: '',
    promptId: '',
    flowId: null
});

const mcpInfoModal = reactive({
    open: false,
    title: '',
    rows: [],
    secretRows: []
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

const strategy = computed(() => normalizeStrategyType(detail.value?.agentType || 'react'));
const tone = computed(() => getStrategyTone(strategy.value, isDarkTheme.value));

const themeVars = computed(() => ({
    '--detail-page-bg': tone.value.sectionTintBg || 'var(--bg-page)',
    '--detail-card-bg': 'var(--surface-1)',
    '--detail-section-border': tone.value.detailSectionBorder || tone.value.sectionBorder || 'var(--border-color)',
    '--detail-divider': tone.value.detailDivider || tone.value.divider || 'var(--border-color)',
    '--detail-focus': tone.value.detailFocus || tone.value.focus || 'var(--accent-color)'
}));

const resolvedAgentType = computed(() => (detail.value?.agentType || '--').toString().toUpperCase());
const resolvedAgentName = computed(() => detail.value?.agentName || 'MiniAgent');

const resolvedCreateTime = computed(() => {
    const raw = detail.value?.createTime;
    if (!raw) return '--';
    const date = new Date(raw);
    if (Number.isNaN(date.getTime())) return '--';
    return date.toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
    });
});

const resolvedApiAddress = computed(() => {
    const base = (detail.value?.apiBaseUrl || '').trim();
    const completion = (detail.value?.apiCompletionUrl || '').trim();
    if (!base && !completion) return '--';
    if (!completion) return base;
    if (!base) return completion.startsWith('/') ? completion : `/${completion}`;
    if (/^https?:\/\//i.test(completion)) return completion;
    return `${base.replace(/\/+$/, '')}/${completion.replace(/^\/+/, '')}`;
});

const clientInfoList = computed(() => (Array.isArray(detail.value?.clientInfoList) ? detail.value.clientInfoList : []));
const mcpInfoList = computed(() => (Array.isArray(detail.value?.mcpInfoList) ? detail.value.mcpInfoList : []));

const clientIdList = computed(() => {
    const values = clientInfoList.value
        .map((item) => (item?.clientId || '').toString().trim())
        .filter(Boolean);
    return [...new Set(values)];
});

const orderedRoleRows = computed(() => {
    const roleKeys = strategyRoleOrder[strategy.value] || [];
    const byRole = new Map();
    clientInfoList.value.forEach((item) => {
        const key = (item?.clientRole || '').toString().trim().toLowerCase();
        if (key && !byRole.has(key)) {
            byRole.set(key, item);
        }
    });

    return roleKeys.map((roleKey, index) => {
        const linked = byRole.get(roleKey) || clientInfoList.value[index] || {};
        const roleRecord = roleMap.value?.[roleKey] || {};
        return {
            roleKey,
            roleName: (roleRecord.roleName || linked?.clientRole || roleKey || '--').toString().toUpperCase(),
            roleDesc: roleRecord.roleDesc || '暂无角色说明',
            systemPrompt: linked?.systemPrompt || '',
            flowPrompt: linked?.userPrompt || '',
            promptId: linked?.promptId || '',
            flowId: linked?.flowId,
            flowIndex: index + 1
        };
    });
});

const normalizeJsonObject = (raw) => {
    if (!raw) return {};
    if (typeof raw === 'object' && !Array.isArray(raw)) return raw;
    if (typeof raw === 'string') {
        try {
            const parsed = JSON.parse(raw);
            return parsed && typeof parsed === 'object' && !Array.isArray(parsed) ? parsed : {};
        } catch {
            return {};
        }
    }
    return {};
};

const parseSecretRows = (raw) => {
    const value = normalizeJsonObject(raw);
    return Object.entries(value).map(([key, val]) => ({ key, value: String(val ?? '') }));
};

const loadReferenceData = async () => {
    try {
        const [apiResp, mcpResp, modelResp] = await Promise.all([userApiList(''), userMcpList(''), queryModelList()]);
        const apiData = pickData(apiResp);
        const mcpData = pickData(mcpResp);
        const modelData = pickData(modelResp);
        userApiOptions.value = Array.isArray(apiData) ? apiData : [];
        userMcpOptions.value = Array.isArray(mcpData) ? mcpData : [];
        modelCatalog.value = Array.isArray(modelData) ? modelData : [];
    } catch (error) {
        userApiOptions.value = [];
        userMcpOptions.value = [];
        modelCatalog.value = [];
        notifyAppError(error, '加载可编辑配置失败');
    }
};

const loadDetail = async () => {
    if (!agentId.value) {
        notifyAppError(new Error('缺少 agentId'), '缺少 agentId');
        detail.value = null;
        roleMap.value = {};
        return;
    }
    try {
        const [detailResp, roleResp] = await Promise.all([
            workspaceAgentDetail({ agentId: agentId.value }),
            queryRoleMap()
        ]);
        detail.value = pickData(detailResp) || {};
        const roleData = pickData(roleResp);
        roleMap.value = roleData && typeof roleData === 'object' ? roleData : {};
    } catch (error) {
        detail.value = null;
        roleMap.value = {};
        notifyAppError(error, '加载智能体详情失败');
    }
};

const goBack = () => {
    if (window.history.length > 1) {
        router.back();
        return;
    }
    router.push('/repository');
};

const openBaseEdit = () => {
    if (!detail.value) return;
    baseModal.agentName = detail.value.agentName || '';
    baseModal.agentDesc = detail.value.agentDesc || '';
    baseModal.open = true;
};

const closeBaseEdit = () => {
    if (baseModal.saving) return;
    baseModal.open = false;
};

const saveBaseEdit = async () => {
    const name = baseModal.agentName.trim();
    const desc = baseModal.agentDesc.trim();
    if (!name || !desc) {
        notifyAppError(new Error('智能体名称和概述不能为空'), '智能体名称和概述不能为空');
        return;
    }
    baseModal.saving = true;
    try {
        await workspaceAgentBaseUpdate({
            agentId: agentId.value,
            agentName: name,
            agentDesc: desc
        });
        baseModal.open = false;
        await loadDetail();
    } catch (error) {
        notifyAppError(error, '更新基础信息失败');
    } finally {
        baseModal.saving = false;
    }
};

const joinApiAddress = (baseUrl, completionPath) => {
    const base = (baseUrl || '').trim();
    const completion = (completionPath || '').trim();
    if (!base && !completion) return '--';
    if (!completion) return base;
    if (!base) return completion.startsWith('/') ? completion : `/${completion}`;
    if (/^https?:\/\//i.test(completion)) return completion;
    return `${base.replace(/\/+$/, '')}/${completion.replace(/^\/+/, '')}`;
};

const resolveModelIdForApi = (apiItem) => {
    const apiId = (apiItem?.apiId || '').toString().trim();
    if (!apiId) return '';
    const candidates = modelCatalog.value.filter((item) => (item?.apiId || '').toString().trim() === apiId);
    if (!candidates.length) return '';
    const apiModelName = (apiItem?.modelName || '').toString().trim().toLowerCase();
    const exact = candidates.find((item) => (item?.modelName || '').toString().trim().toLowerCase() === apiModelName);
    return (exact?.modelId || candidates[0]?.modelId || '').toString().trim();
};

const buildModelOptions = () => {
    return userApiOptions.value.map((api) => {
        const modelId = resolveModelIdForApi(api);
        return {
            apiId: api.apiId,
            modelId,
            modelName: api.modelName || '--',
            modelType: api.modelType || '--',
            apiAddress: joinApiAddress(api.apiBaseUrl, api.apiCompletionPath),
            disabled: !modelId
        };
    });
};

const openModelEdit = async () => {
    if (!detail.value) return;
    if (!userApiOptions.value.length || !modelCatalog.value.length) {
        await loadReferenceData();
    }
    modelModal.options = buildModelOptions();
    const currentApiId = (detail.value.apiId || '').toString().trim();
    const exact = modelModal.options.find((item) => item.apiId === currentApiId && !item.disabled);
    const fallback = modelModal.options.find((item) => !item.disabled);
    modelModal.selectedApiId = (exact || fallback)?.apiId || '';
    modelModal.open = true;
};

const closeModelEdit = () => {
    if (modelModal.saving) return;
    modelModal.open = false;
};

const saveModelEdit = async () => {
    const selected = modelModal.options.find((item) => item.apiId === modelModal.selectedApiId);
    if (!selected || !selected.modelId) {
        notifyAppError(new Error('请选择可用的模型'), '请选择可用的模型');
        return;
    }
    if (!clientIdList.value.length) {
        notifyAppError(new Error('当前智能体缺少 client 信息'), '当前智能体缺少 client 信息');
        return;
    }
    modelModal.saving = true;
    try {
        await workspaceAgentModelUpdate({
            agentId: agentId.value,
            modelId: selected.modelId,
            clientIdList: clientIdList.value
        });
        modelModal.open = false;
        await loadDetail();
    } catch (error) {
        notifyAppError(error, '更新模型失败');
    } finally {
        modelModal.saving = false;
    }
};

const openMcpEdit = async () => {
    if (!detail.value) return;
    if (!userMcpOptions.value.length) {
        await loadReferenceData();
    }
    mcpEditModal.selectedIds = mcpInfoList.value
        .map((item) => (item?.mcpId || '').toString().trim())
        .filter(Boolean);
    mcpEditModal.open = true;
};

const closeMcpEdit = () => {
    if (mcpEditModal.saving) return;
    mcpEditModal.open = false;
};

const toggleMcpId = (mcpId) => {
    const set = new Set(mcpEditModal.selectedIds);
    if (set.has(mcpId)) {
        set.delete(mcpId);
    } else {
        set.add(mcpId);
    }
    mcpEditModal.selectedIds = [...set];
};

const saveMcpEdit = async () => {
    if (!clientIdList.value.length) {
        notifyAppError(new Error('当前智能体缺少 client 信息'), '当前智能体缺少 client 信息');
        return;
    }
    mcpEditModal.saving = true;
    try {
        await workspaceAgentMcpUpdate({
            agentId: agentId.value,
            clientIdList: clientIdList.value,
            mcpIdList: [...new Set(mcpEditModal.selectedIds)]
        });
        mcpEditModal.open = false;
        await loadDetail();
    } catch (error) {
        notifyAppError(error, '更新 MCP 失败');
    } finally {
        mcpEditModal.saving = false;
    }
};

const openPromptEdit = (row, mode) => {
    if (mode === 'system' && !row?.promptId) {
        notifyAppError(new Error('缺少 promptId，无法编辑'), '缺少 promptId，无法编辑');
        return;
    }
    if (mode === 'user' && (row?.flowId === undefined || row?.flowId === null)) {
        notifyAppError(new Error('缺少 flowId，无法编辑'), '缺少 flowId，无法编辑');
        return;
    }
    promptModal.mode = mode;
    promptModal.title = `${row?.roleName || '--'} · ${mode === 'system' ? 'SYSTEM PROMPT' : 'USER PROMPT'}`;
    promptModal.value = mode === 'system' ? row?.systemPrompt || '' : row?.flowPrompt || '';
    promptModal.promptId = row?.promptId || '';
    promptModal.flowId = row?.flowId ?? null;
    promptModal.open = true;
};

const closePromptEdit = () => {
    if (promptModal.saving) return;
    promptModal.open = false;
};

const savePromptEdit = async () => {
    const value = promptModal.value.trim();
    if (!value) {
        notifyAppError(new Error('Prompt 内容不能为空'), 'Prompt 内容不能为空');
        return;
    }
    promptModal.saving = true;
    try {
        if (promptModal.mode === 'system') {
            await workspaceAgentSystemPromptUpdate({
                agentId: agentId.value,
                promptId: promptModal.promptId,
                systemPrompt: value
            });
        } else {
            await workspaceAgentUserPromptUpdate({
                agentId: agentId.value,
                flowId: promptModal.flowId,
                userPrompt: value
            });
        }
        promptModal.open = false;
        await loadDetail();
    } catch (error) {
        notifyAppError(error, '更新 Prompt 失败');
    } finally {
        promptModal.saving = false;
    }
};

const openMcpInfo = (item) => {
    const config = normalizeJsonObject(item?.mcpParam);
    const mcpType = (item?.mcpType || '').toString().toLowerCase();
    const baseUri = config.baseUri || config.baseUrl || '--';
    const sseEndpoint = config.sseEndpoint || config.sseEndPoint || '--';
    const stdioCommand = config.command || config.cmd || '--';
    const stdioArgs = Array.isArray(config.args) ? config.args.join(' ') : '--';

    mcpInfoModal.title = `MCP · ${item?.mcpName || '--'}`;
    mcpInfoModal.rows = [
        { label: '使用描述', value: item?.mcpDesc || '暂无描述', multiline: true },
        ...(mcpType === 'stdio'
            ? [
                { label: '命令', value: stdioCommand || '--' },
                { label: '参数', value: stdioArgs || '--' }
            ]
            : [
                { label: '基础路径', value: baseUri || '--' },
                { label: 'SSE 端点', value: sseEndpoint || '--' }
            ])
    ];
    mcpInfoModal.secretRows = parseSecretRows(item?.mcpSecret);
    mcpInfoModal.open = true;
};

const closeMcpInfo = () => {
    mcpInfoModal.open = false;
};

onMounted(async () => {
    await Promise.all([loadDetail(), loadReferenceData()]);
});
</script>

<template>
    <section class="relative grid h-screen grid-rows-[1fr_var(--footer-height)] overflow-x-hidden" :style="themeVars">
        <div class="h-full overflow-y-auto overflow-x-hidden [scrollbar-gutter:stable] pt-[24px] pb-[24px] pl-[24px] pr-[calc(24px+var(--scrollbar-w))]">
            <div class="relative mx-auto max-w-[1180px]">
                <div v-if="detail" class="relative z-[1] space-y-[18px]">
                    <header class="relative">
                        <button
                            class="absolute left-0 top-1/2 z-[2] inline-flex h-[34px] w-[34px] -translate-y-1/2 items-center justify-center rounded-[10px] border border-[var(--detail-section-border)] bg-[var(--surface-1)] text-[var(--text-primary)] transition hover:border-[var(--detail-focus)]"
                            title="返回"
                            aria-label="返回"
                            @click="goBack"
                        >
                            <svg viewBox="0 0 20 20" class="h-[14px] w-[14px]" fill="currentColor" aria-hidden="true">
                                <path d="M11.5 4 5.5 10l6 6v-4h3v-4h-3V4z" />
                            </svg>
                        </button>
                        <div class="grid grid-cols-[1fr_auto_1fr] items-end gap-x-[24px] gap-y-[6px] pl-[52px] max-[980px]:grid-cols-1 max-[980px]:pl-0">
                            <span
                                class="inline-flex items-center rounded-full border px-[16px] py-[5px] text-[16px] font-bold tracking-[0.04em] justify-self-end max-[980px]:justify-self-center"
                                :style="{
                                    borderColor: tone.badgeBorder,
                                    backgroundColor: tone.badgeBg,
                                    color: tone.badgeText
                                }"
                            >
                                {{ resolvedAgentType }}
                            </span>
                            <div class="group relative justify-self-center">
                                <h1 class="text-center text-[42px] font-bold leading-[1.08] text-[var(--text-primary)] max-[980px]:text-[34px]">{{ resolvedAgentName }}</h1>
                                <button
                                    class="absolute -right-[38px] top-[6px] hidden h-[30px] w-[30px] items-center justify-center rounded-full border-[1.5px] border-[var(--detail-divider)] bg-[var(--surface-1)] text-[var(--text-secondary)] transition group-hover:inline-flex hover:border-[var(--detail-focus)] hover:text-[var(--text-primary)]"
                                    title="编辑智能体"
                                    @click="openBaseEdit"
                                >
                                    <svg viewBox="0 0 24 24" class="h-[16px] w-[16px]" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
                                        <path d="M12 20h9" />
                                        <path d="M16.5 3.5a2.1 2.1 0 0 1 3 3L7 19l-4 1 1-4 12.5-12.5z" />
                                    </svg>
                                </button>
                            </div>
                            <div class="flex flex-wrap items-end justify-self-start gap-x-[14px] text-[14px] text-[var(--text-secondary)] max-[980px]:justify-self-center">
                                <span>创建时间：{{ resolvedCreateTime }}</span>
                            </div>
                        </div>
                    </header>

                    <div class="h-px w-full bg-[var(--detail-divider)]" />

                    <section class="detail-section-panel space-y-[10px]">
                        <div class="flex items-center gap-[8px]">
                            <h2 class="detail-section-title text-[18px] font-semibold text-[var(--text-primary)]">智能体概述</h2>
                            <button
                                class="inline-flex h-[32px] w-[32px] items-center justify-center rounded-full border-[1.5px] border-[var(--detail-divider)] bg-[var(--surface-1)] text-[var(--text-secondary)] transition hover:border-[var(--detail-focus)] hover:text-[var(--text-primary)]"
                                title="编辑智能体"
                                @click="openBaseEdit"
                            >
                                <svg viewBox="0 0 24 24" class="h-[16px] w-[16px]" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
                                    <path d="M12 20h9" />
                                    <path d="M16.5 3.5a2.1 2.1 0 0 1 3 3L7 19l-4 1 1-4 12.5-12.5z" />
                                </svg>
                            </button>
                        </div>
                        <p class="whitespace-pre-wrap text-[15px] leading-[1.7] text-[var(--text-secondary)]">{{ detail.agentDesc || '暂无描述' }}</p>
                    </section>

                    <section class="grid items-stretch gap-[18px] min-[980px]:grid-cols-2">
                        <div class="detail-section-panel flex h-full flex-col space-y-[8px]">
                            <div class="flex items-center gap-[8px]">
                                <h2 class="detail-section-title text-[18px] font-semibold text-[var(--text-primary)]">模型信息</h2>
                                <button
                                    class="inline-flex h-[32px] w-[32px] items-center justify-center rounded-full border-[1.5px] border-[var(--detail-divider)] bg-[var(--surface-1)] text-[var(--text-secondary)] transition hover:border-[var(--detail-focus)] hover:text-[var(--text-primary)]"
                                    title="编辑模型"
                                    @click="openModelEdit"
                                >
                                    <svg viewBox="0 0 24 24" class="h-[16px] w-[16px]" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
                                        <path d="M12 20h9" />
                                        <path d="M16.5 3.5a2.1 2.1 0 0 1 3 3L7 19l-4 1 1-4 12.5-12.5z" />
                                    </svg>
                                </button>
                            </div>
                            <div class="flex h-full flex-col gap-[6px]">
                                <div class="detail-model-row"><span class="text-[var(--text-secondary)]">模型类别：</span>{{ detail.modelType || '--' }}</div>
                                <div class="detail-model-row"><span class="text-[var(--text-secondary)]">模型名称：</span>{{ detail.modelName || '--' }}</div>
                                <div class="detail-model-row"><span class="text-[var(--text-secondary)]">接口地址：</span>{{ resolvedApiAddress }}</div>
                            </div>
                        </div>

                        <div class="detail-section-panel flex h-full flex-col space-y-[8px]">
                            <div class="flex items-center gap-[8px]">
                                <h2 class="detail-section-title text-[18px] font-semibold text-[var(--text-primary)]">MCP 信息</h2>
                                <button
                                    class="inline-flex h-[32px] w-[32px] items-center justify-center rounded-full border-[1.5px] border-[var(--detail-divider)] bg-[var(--surface-1)] text-[var(--text-secondary)] transition hover:border-[var(--detail-focus)] hover:text-[var(--text-primary)]"
                                    title="编辑 MCP"
                                    @click="openMcpEdit"
                                >
                                    <svg viewBox="0 0 24 24" class="h-[16px] w-[16px]" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
                                        <path d="M12 20h9" />
                                        <path d="M16.5 3.5a2.1 2.1 0 0 1 3 3L7 19l-4 1 1-4 12.5-12.5z" />
                                    </svg>
                                </button>
                            </div>
                            <div v-if="mcpInfoList.length > 0" class="h-full max-h-[160px] w-full space-y-[6px] overflow-y-auto pr-[2px]">
                                <button
                                    v-for="(item, index) in mcpInfoList"
                                    :key="`${item.mcpId || item.mcpName || 'mcp'}-${index}`"
                                    class="flex w-full items-center gap-[10px] rounded-[10px] border-[1.5px] border-[var(--detail-divider)] px-[12px] py-[10px] text-left transition hover:border-[var(--detail-focus)]"
                                    @click="openMcpInfo(item)"
                                >
                                    <span class="min-w-0 flex-1 truncate text-[15px] font-semibold text-[var(--text-primary)]">{{ item.mcpName || '--' }}</span>
                                    <span class="shrink-0 rounded-full border-[1.5px] border-[var(--detail-divider)] px-[8px] py-[2px] text-[11px] font-semibold uppercase text-[var(--text-secondary)]">
                                        {{ (item.mcpType || '--').toUpperCase() }}
                                    </span>
                                </button>
                            </div>
                            <div v-else class="text-[13px] text-[var(--text-secondary)]">暂无 MCP 配置</div>
                        </div>
                    </section>

                    <section class="detail-section-panel space-y-[10px]">
                        <h2 class="detail-section-title text-[18px] font-semibold text-[var(--text-primary)]">角色流程</h2>
                        <div class="grid grid-cols-[40px_minmax(0,1fr)_40px_minmax(0,1fr)_40px_minmax(0,1fr)_40px_minmax(0,1fr)] items-stretch gap-[8px]">
                            <template v-for="row in orderedRoleRows" :key="row.roleKey">
                                <button
                                    class="inline-flex min-w-0 items-center justify-center rounded-[10px] border-[1.5px] border-[var(--detail-divider)] text-[var(--text-secondary)] transition hover:border-[var(--detail-focus)] hover:text-[var(--text-primary)]"
                                    :title="`编辑第 ${row.flowIndex} 步 User Prompt`"
                                    @click="openPromptEdit(row, 'user')"
                                >
                                    <span class="text-[24px] font-bold leading-none">{{ row.flowIndex }}</span>
                                </button>
                                <button
                                    class="flex min-w-0 flex-col rounded-[14px] border-[1.5px] border-[var(--detail-divider)] px-[12px] py-[12px] text-left transition hover:border-[var(--detail-focus)] min-h-[160px]"
                                    @click="openPromptEdit(row, 'system')"
                                >
                                    <div class="text-center text-[28px] font-bold leading-none text-[var(--text-primary)] max-[1280px]:text-[24px]">{{ row.roleName }}</div>
                                    <div class="mt-[10px] text-[14px] leading-[1.65] text-[var(--text-secondary)] role-desc-clamp-long">{{ row.roleDesc || '暂无配置' }}</div>
                                </button>
                            </template>
                        </div>
                    </section>
                </div>
            </div>
        </div>

        <Footer />

        <div v-if="baseModal.open" class="detail-modal-wrap" @click.self="closeBaseEdit">
            <div class="detail-modal-box h-[420px] w-[760px]">
                <div class="detail-modal-header">
                    <h3 class="detail-section-title text-[16px] font-semibold text-[var(--text-primary)]">编辑智能体信息</h3>
                    <button class="text-[20px] text-[var(--text-secondary)]" @click="closeBaseEdit">×</button>
                </div>
                <div class="detail-modal-body space-y-[12px]">
                    <label class="flex flex-col gap-[6px]">
                        <span class="text-[13px] text-[var(--text-secondary)]">智能体名称</span>
                        <input v-model="baseModal.agentName" class="detail-input" maxlength="64" />
                    </label>
                    <label class="flex min-h-0 flex-1 flex-col gap-[6px]">
                        <span class="text-[13px] text-[var(--text-secondary)]">智能体概述</span>
                        <textarea v-model="baseModal.agentDesc" class="detail-textarea min-h-0 flex-1" maxlength="500" />
                    </label>
                </div>
                <div class="detail-modal-actions">
                    <button class="detail-btn muted" :disabled="baseModal.saving" @click="closeBaseEdit">取消</button>
                    <button class="detail-btn primary" :disabled="baseModal.saving" @click="saveBaseEdit">{{ baseModal.saving ? '保存中...' : '确定' }}</button>
                </div>
            </div>
        </div>

        <div v-if="modelModal.open" class="detail-modal-wrap" @click.self="closeModelEdit">
            <div class="detail-modal-box h-[480px] w-[860px]">
                <div class="detail-modal-header">
                    <h3 class="detail-section-title text-[16px] font-semibold text-[var(--text-primary)]">选择模型</h3>
                    <button class="text-[20px] text-[var(--text-secondary)]" @click="closeModelEdit">×</button>
                </div>
                <div class="detail-modal-body overflow-y-auto">
                    <div class="space-y-[8px]">
                        <button
                            v-for="option in modelModal.options"
                            :key="option.apiId"
                            class="w-full rounded-[10px] border px-[12px] py-[10px] text-left transition"
                            :class="
                                option.disabled
                                    ? 'cursor-not-allowed border-[var(--detail-divider)] text-[var(--text-muted-2)] opacity-70'
                                    : modelModal.selectedApiId === option.apiId
                                    ? 'border-[var(--detail-focus)] bg-[rgba(148,163,184,0.08)]'
                                    : 'border-[var(--detail-divider)] hover:border-[var(--detail-focus)]'
                            "
                            :disabled="option.disabled"
                            @click="modelModal.selectedApiId = option.apiId"
                        >
                            <div class="flex items-center gap-[8px]">
                                <div class="text-[15px] font-semibold text-[var(--text-primary)]">{{ option.modelName }}</div>
                                <span class="rounded-full border border-[var(--detail-divider)] px-[8px] py-[2px] text-[11px] text-[var(--text-secondary)]">{{ option.modelType }}</span>
                            </div>
                            <div class="mt-[4px] text-[13px] text-[var(--text-secondary)]">{{ option.apiAddress }}</div>
                            <div v-if="option.disabled" class="mt-[3px] text-[12px] text-[#f97316]">未找到可用 modelId</div>
                        </button>
                    </div>
                </div>
                <div class="detail-modal-actions">
                    <button class="detail-btn muted" :disabled="modelModal.saving" @click="closeModelEdit">取消</button>
                    <button class="detail-btn primary" :disabled="modelModal.saving" @click="saveModelEdit">{{ modelModal.saving ? '保存中...' : '确定' }}</button>
                </div>
            </div>
        </div>

        <div v-if="mcpEditModal.open" class="detail-modal-wrap" @click.self="closeMcpEdit">
            <div class="detail-modal-box h-[500px] w-[860px]">
                <div class="detail-modal-header">
                    <h3 class="detail-section-title text-[16px] font-semibold text-[var(--text-primary)]">选择 MCP</h3>
                    <button class="text-[20px] text-[var(--text-secondary)]" @click="closeMcpEdit">×</button>
                </div>
                <div class="detail-modal-body overflow-y-auto">
                    <div class="space-y-[8px]">
                        <button
                            v-for="item in userMcpOptions"
                            :key="item.mcpId"
                            class="w-full rounded-[10px] border border-[var(--detail-divider)] px-[12px] py-[10px] text-left transition hover:border-[var(--detail-focus)]"
                            @click="toggleMcpId(item.mcpId)"
                        >
                            <div class="flex items-center gap-[8px]">
                                <span class="inline-flex h-[16px] w-[16px] items-center justify-center rounded-[4px] border text-[11px]"
                                      :class="mcpEditModal.selectedIds.includes(item.mcpId) ? 'border-[var(--detail-focus)] bg-[var(--detail-focus)] text-white' : 'border-[var(--detail-divider)] text-transparent'">✓</span>
                                <span class="text-[15px] font-semibold text-[var(--text-primary)]">{{ item.mcpName }}</span>
                                <span class="rounded-full border border-[var(--detail-divider)] px-[8px] py-[2px] text-[11px] uppercase text-[var(--text-secondary)]">{{ item.mcpType || '--' }}</span>
                            </div>
                            <div class="mt-[4px] text-[13px] text-[var(--text-secondary)]">{{ item.mcpDesc || '暂无描述' }}</div>
                        </button>
                    </div>
                </div>
                <div class="detail-modal-actions">
                    <button class="detail-btn muted" :disabled="mcpEditModal.saving" @click="closeMcpEdit">取消</button>
                    <button class="detail-btn primary" :disabled="mcpEditModal.saving" @click="saveMcpEdit">{{ mcpEditModal.saving ? '保存中...' : '确定' }}</button>
                </div>
            </div>
        </div>

        <div v-if="promptModal.open" class="detail-modal-wrap" @click.self="closePromptEdit">
            <div class="detail-modal-box h-[520px] w-[860px]">
                <div class="detail-modal-header">
                    <h3 class="detail-section-title text-[16px] font-semibold text-[var(--text-primary)]">{{ promptModal.title }}</h3>
                    <button class="text-[20px] text-[var(--text-secondary)]" @click="closePromptEdit">×</button>
                </div>
                <div class="detail-modal-body">
                    <textarea v-model="promptModal.value" class="detail-textarea h-full" />
                </div>
                <div class="detail-modal-actions">
                    <button class="detail-btn muted" :disabled="promptModal.saving" @click="closePromptEdit">取消</button>
                    <button class="detail-btn primary" :disabled="promptModal.saving" @click="savePromptEdit">{{ promptModal.saving ? '保存中...' : '确定' }}</button>
                </div>
            </div>
        </div>

        <div v-if="mcpInfoModal.open" class="detail-modal-wrap" @click.self="closeMcpInfo">
            <div class="detail-modal-box h-[440px] w-[760px]">
                <div class="detail-modal-header">
                    <h3 class="detail-section-title text-[16px] font-semibold text-[var(--text-primary)]">{{ mcpInfoModal.title }}</h3>
                    <button class="text-[20px] text-[var(--text-secondary)]" @click="closeMcpInfo">×</button>
                </div>
                <div class="detail-modal-body overflow-y-auto">
                    <div class="space-y-[10px]">
                        <div
                            v-for="(row, idx) in mcpInfoModal.rows"
                            :key="`${row.label}-${idx}`"
                            class="grid grid-cols-[124px_minmax(0,1fr)] items-start gap-x-[14px] gap-y-[4px]"
                        >
                            <div class="pt-[2px] text-left text-[15px] font-semibold text-[var(--text-secondary)]">{{ row.label }}</div>
                            <div class="min-w-0 text-left text-[17px] text-[var(--text-primary)] whitespace-pre-wrap break-words" :class="{ 'leading-[1.7]': row.multiline }">
                                {{ row.value || '--' }}
                            </div>
                        </div>

                        <div class="pt-[4px]">
                            <div class="mb-[6px] text-[15px] font-semibold text-[var(--text-primary)]">密钥信息</div>
                            <div class="max-h-[160px] overflow-y-auto rounded-[10px] border border-[var(--detail-divider)]">
                                <table class="w-full table-fixed border-collapse text-left text-[14px] text-[var(--text-primary)]">
                                    <thead>
                                        <tr class="border-b border-[var(--detail-divider)]">
                                            <th class="w-[36%] px-[10px] py-[8px] font-semibold">Key</th>
                                            <th class="px-[10px] py-[8px] font-semibold">Value</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <tr v-for="(row, idx) in mcpInfoModal.secretRows" :key="`${row.key}-${idx}`" class="border-b last:border-b-0 border-[var(--detail-divider)]">
                                            <td class="px-[10px] py-[7px] font-medium">{{ row.key }}</td>
                                            <td class="px-[10px] py-[7px] break-all">{{ row.value }}</td>
                                        </tr>
                                        <tr v-if="mcpInfoModal.secretRows.length === 0">
                                            <td class="px-[10px] py-[10px] text-[var(--text-secondary)]" colspan="2">无</td>
                                        </tr>
                                    </tbody>
                                </table>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </section>
</template>

<style scoped>
.detail-section-panel {
    border: 2px solid var(--detail-section-border);
    background: var(--detail-card-bg);
    border-radius: 14px;
    padding: 12px;
}

.detail-section-title {
    position: relative;
    padding-left: 10px;
}

.detail-section-title::before {
    content: '';
    position: absolute;
    left: 0;
    top: 50%;
    width: 3px;
    height: 16px;
    transform: translateY(-50%);
    border-radius: 999px;
    background: var(--detail-focus);
}

.detail-model-row {
    display: flex;
    align-items: center;
    min-height: 46px;
    border: 2px solid var(--detail-divider);
    border-radius: 10px;
    padding: 0 12px;
    font-size: 15px;
    color: var(--text-primary);
    overflow: hidden;
    white-space: nowrap;
    text-overflow: ellipsis;
}

.role-desc-clamp-long {
    display: -webkit-box;
    overflow: hidden;
    -webkit-box-orient: vertical;
    -webkit-line-clamp: 5;
}

.detail-modal-wrap {
    position: absolute;
    inset: 0;
    z-index: 40;
    display: grid;
    place-items: center;
    background: rgba(0, 0, 0, 0.3);
    padding: 20px;
}

.detail-modal-box {
    display: flex;
    max-width: calc(100% - 48px);
    max-height: calc(100% - 48px);
    flex-direction: column;
    border-radius: 14px;
    border: 2px solid var(--detail-section-border);
    background: var(--surface-1);
    padding: 16px;
    box-shadow: 0 20px 50px rgba(15, 23, 42, 0.24);
}

.detail-modal-header {
    margin-bottom: 10px;
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 12px;
    border-bottom: 2px solid var(--detail-divider);
    padding-bottom: 10px;
}

.detail-modal-body {
    min-height: 0;
    flex: 1;
    padding: 4px;
}

.detail-modal-actions {
    margin-top: 10px;
    display: flex;
    justify-content: flex-end;
    gap: 10px;
}

.detail-btn {
    height: 36px;
    min-width: 82px;
    border-radius: 10px;
    border: 2px solid var(--detail-divider);
    padding: 0 14px;
    font-size: 14px;
    font-weight: 600;
    transition: all 0.2s;
}

.detail-btn.muted {
    background: var(--surface-1);
    color: var(--text-secondary);
}

.detail-btn.primary {
    border-color: var(--detail-focus);
    background: var(--detail-focus);
    color: #ffffff;
}

.detail-input,
.detail-textarea {
    border: 2px solid var(--detail-divider);
    border-radius: 10px;
    background: var(--surface-1);
    color: var(--text-primary);
    padding: 10px 12px;
    font-size: 14px;
    line-height: 1.6;
    outline: none;
    transition: border-color 0.2s;
}

.detail-input:focus,
.detail-textarea:focus {
    border-color: var(--detail-focus);
}

.detail-textarea {
    resize: none;
}
</style>
