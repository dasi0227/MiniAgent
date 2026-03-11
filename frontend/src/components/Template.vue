<script setup>
import { computed, onBeforeUnmount, onMounted, reactive, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { plazaDetail, queryRoleMap as queryRoleMapApi, repoFork } from '../request/api';
import { notifyAppError } from '../request/request';
import { useSettingsStore } from '../router/pinia';
import { pushErrorToast } from '../utils/errorToast';
import { getStrategyTone, normalizeStrategyType } from '../utils/StrategyTone';
import Footer from './Footer.vue';

const route = useRoute();
const router = useRouter();
const settingsStore = useSettingsStore();
const isDarkTheme = computed(() => settingsStore.theme === 'dark');

const detail = ref(null);
const roleMap = ref({});
const forkState = ref('idle');
let forkDoneTimer = null;

const modal = reactive({
    open: false,
    kind: 'text',
    title: '',
    content: '',
    asJson: false,
    mcpRows: []
});

const strategyRoleOrder = {
    step: ['inspector', 'planner', 'runner', 'replier'],
    loop: ['analyzer', 'performer', 'supervisor', 'summarizer'],
    react: ['observer', 'reasoner', 'actor', 'evaluator']
};

const templateId = computed(() => (route.params.templateId || '').toString().trim());
const routeForked = computed(() => {
    const raw = Array.isArray(route.query?.forked) ? route.query.forked[0] : route.query?.forked;
    if (raw === undefined || raw === null || raw === '') return null;
    const normalized = raw.toString().trim().toLowerCase();
    if (['1', 'true', 'yes'].includes(normalized)) return true;
    if (['0', 'false', 'no'].includes(normalized)) return false;
    return null;
});

const pickData = (resp) => {
    if (resp && typeof resp === 'object' && Object.prototype.hasOwnProperty.call(resp, 'code')) {
        if (resp.code !== 200) {
            throw new Error(resp.info || '加载失败');
        }
        return resp.data;
    }
    return resp?.data ?? resp?.result ?? resp;
};

const strategy = computed(() => normalizeStrategyType(detail.value?.agentType || 'react'));
const detailTone = computed(() => getStrategyTone(strategy.value, isDarkTheme.value));

const detailThemeVars = computed(() => ({
    '--detail-page-bg': detailTone.value.sectionTintBg || 'var(--bg-page)',
    '--detail-card-bg': 'var(--surface-1)',
    '--detail-section-border': detailTone.value.detailSectionBorder || detailTone.value.sectionBorder || 'var(--border-color)',
    '--detail-divider': detailTone.value.detailDivider || detailTone.value.divider || 'var(--border-color)',
    '--detail-focus': detailTone.value.detailFocus || detailTone.value.focus || 'var(--accent-color)'
}));

const resolvedAgentType = computed(() => (detail.value?.agentType || '--').toString().toUpperCase());
const resolvedAgentName = computed(() => detail.value?.agentName || 'MiniAgent');
const resolvedAuthor = computed(() => detail.value?.userName || '--');

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
    const base = (detail.value?.apiUrl || '').trim();
    const completion = (detail.value?.apiCompletionUrl || '').trim();
    if (!base && !completion) return '--';
    if (!completion) return base;
    if (!base) return completion.startsWith('/') ? completion : `/${completion}`;
    if (/^https?:\/\//i.test(completion)) return completion;
    return `${base.replace(/\/+$/, '')}/${completion.replace(/^\/+/, '')}`;
});

const mcpInfoList = computed(() => (Array.isArray(detail.value?.mcpInfoList) ? detail.value.mcpInfoList : []));
const clientInfoList = computed(() => (Array.isArray(detail.value?.clientInfoList) ? detail.value.clientInfoList : []));

const isAlreadyForked = computed(() => {
    if (routeForked.value !== null) {
        return routeForked.value;
    }
    return false;
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
            flowIndex: index + 1
        };
    });
});

const normalizeConfig = (raw) => {
    if (!raw) return {};
    if (typeof raw === 'object') return raw;
    if (typeof raw === 'string') {
        try {
            const parsed = JSON.parse(raw);
            return parsed && typeof parsed === 'object' ? parsed : {};
        } catch {
            return {};
        }
    }
    return {};
};

const openTextModal = (title, content, asJson = false) => {
    modal.kind = 'text';
    modal.title = title;
    modal.content = content || '暂无配置';
    modal.asJson = asJson;
    modal.mcpRows = [];
    modal.open = true;
};

const openSystemPrompt = (row) => {
    openTextModal(`${row?.roleName || '--'} · SYSTEM PROMPT`, row?.systemPrompt || '');
};

const openUserPrompt = (row) => {
    openTextModal(`${row?.roleName || '--'} · USER PROMPT`, row?.flowPrompt || '');
};

const openMcpInfo = (item) => {
    const config = normalizeConfig(item?.mcpParam);
    const mcpType = (item?.mcpType || '').toString().toLowerCase();
    const baseUri = config.baseUri || config.baseUrl || '--';
    const sseEndpoint = config.sseEndpoint || config.sseEndPoint || '--';
    const stdioCommand = config.command || config.cmd || '--';
    const stdioArgs = Array.isArray(config.args) ? config.args.join(' ') : '--';
    const requiredSecrets = Array.isArray(item?.requiredSecrets) ? item.requiredSecrets : [];

    modal.kind = 'mcp';
    modal.title = `MCP · ${item?.mcpName || '--'}`;
    modal.content = '';
    modal.asJson = false;
    modal.mcpRows = [
        { label: '使用描述', value: item?.mcpDesc || '暂无描述', multiline: true },
        ...(mcpType === 'stdio'
            ? [
                { label: '命令', value: stdioCommand || '--' },
                { label: '参数', value: stdioArgs || '--' }
            ]
            : [
                { label: '基础路径', value: baseUri || '--' },
                { label: 'SSE 端点', value: sseEndpoint || '--' }
            ]),
        { label: '密钥信息', value: requiredSecrets, isTags: true }
    ];
    modal.open = true;
};

const closeModal = () => {
    modal.open = false;
};

const loadDetail = async () => {
    if (!templateId.value) {
        notifyAppError(new Error('缺少 templateId'), '缺少 templateId');
        detail.value = null;
        roleMap.value = {};
        return;
    }
    try {
        const [detailResp, roleResp] = await Promise.all([
            plazaDetail({ templateId: templateId.value }),
            queryRoleMapApi()
        ]);
        detail.value = pickData(detailResp) || {};
        const rolePayload = pickData(roleResp);
        roleMap.value = rolePayload && typeof rolePayload === 'object' ? rolePayload : {};
    } catch (error) {
        detail.value = null;
        roleMap.value = {};
        notifyAppError(error, '加载模板失败');
    }
};

const goBack = () => {
    if (window.history.length > 1) {
        router.back();
        return;
    }
    router.push('/plaza');
};

const doFork = async () => {
    if (!templateId.value) return;
    if (isAlreadyForked.value) {
        router.push('/repository');
        return;
    }
    if (forkState.value === 'loading' || forkState.value === 'done') return;
    forkState.value = 'loading';
    try {
        await repoFork({ templateId: templateId.value });
        forkState.value = 'done';
        pushErrorToast({ message: 'Fork 成功', type: 'success' });
        if (forkDoneTimer) clearTimeout(forkDoneTimer);
        forkDoneTimer = setTimeout(() => {
            forkState.value = 'idle';
            forkDoneTimer = null;
        }, 2000);
    } catch (error) {
        forkState.value = 'idle';
        notifyAppError(error, 'Fork 失败');
    }
};

onMounted(loadDetail);

onBeforeUnmount(() => {
    if (forkDoneTimer) {
        clearTimeout(forkDoneTimer);
        forkDoneTimer = null;
    }
});
</script>

<template>
    <section class="relative grid h-screen grid-rows-[1fr_var(--footer-height)] overflow-x-hidden" :style="detailThemeVars">
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
                                    borderColor: detailTone.badgeBorder,
                                    backgroundColor: detailTone.badgeBg,
                                    color: detailTone.badgeText
                                }"
                            >
                                {{ resolvedAgentType }}
                            </span>
                            <h1 class="justify-self-center text-center text-[42px] font-bold leading-[1.08] text-[var(--text-primary)] max-[980px]:text-[34px]">
                                {{ resolvedAgentName }}
                            </h1>
                            <div class="flex flex-wrap items-end justify-self-start gap-x-[14px] text-[14px] text-[var(--text-secondary)] max-[980px]:justify-self-center">
                                <span>作者：{{ resolvedAuthor }}</span>
                                <span>创建时间：{{ resolvedCreateTime }}</span>
                            </div>
                        </div>
                    </header>

                    <div class="h-px w-full bg-[var(--detail-divider)]" />

                    <section class="detail-section-panel space-y-[10px]">
                        <h2 class="detail-section-title text-[18px] font-semibold text-[var(--text-primary)]">智能体概述</h2>
                        <p class="whitespace-pre-wrap text-[15px] leading-[1.7] text-[var(--text-secondary)]">{{ detail.agentDesc || '暂无描述' }}</p>
                    </section>

                    <section class="grid items-stretch gap-[18px] min-[980px]:grid-cols-2">
                        <div class="detail-section-panel flex h-full flex-col space-y-[8px]">
                            <h2 class="detail-section-title text-[18px] font-semibold text-[var(--text-primary)]">模型信息</h2>
                            <div class="flex h-full flex-col gap-[6px]">
                                <div class="detail-model-row"><span class="text-[var(--text-secondary)]">模型类别：</span>{{ detail.modelType || '--' }}</div>
                                <div class="detail-model-row"><span class="text-[var(--text-secondary)]">模型名称：</span>{{ detail.modelName || '--' }}</div>
                                <div class="detail-model-row"><span class="text-[var(--text-secondary)]">接口地址：</span>{{ resolvedApiAddress }}</div>
                            </div>
                        </div>

                        <div class="detail-section-panel flex h-full flex-col space-y-[8px]">
                            <h2 class="detail-section-title text-[18px] font-semibold text-[var(--text-primary)]">MCP 信息</h2>
                            <div v-if="mcpInfoList.length > 0" class="h-full max-h-[160px] w-full space-y-[6px] overflow-y-auto pr-[2px]">
                                <button
                                    v-for="(item, index) in mcpInfoList"
                                    :key="`${item.mcpName || 'mcp'}-${index}`"
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
                                    :title="`查看第 ${row.flowIndex} 步 User Prompt`"
                                    @click="openUserPrompt(row)"
                                >
                                    <span class="text-[24px] font-bold leading-none">{{ row.flowIndex }}</span>
                                </button>
                                <button
                                    class="flex min-w-0 flex-col rounded-[14px] border-[1.5px] border-[var(--detail-divider)] px-[12px] py-[12px] text-left transition hover:border-[var(--detail-focus)] min-h-[160px]"
                                    @click="openSystemPrompt(row)"
                                >
                                    <div class="text-center text-[28px] font-bold leading-none text-[var(--text-primary)] max-[1280px]:text-[24px]">{{ row.roleName }}</div>
                                    <div class="mt-[10px] text-[14px] leading-[1.65] text-[var(--text-secondary)] role-desc-clamp-long">{{ row.roleDesc || '暂无配置' }}</div>
                                </button>
                            </template>
                        </div>
                    </section>

                    <div class="flex justify-center pt-[2px]">
                        <button
                            class="inline-flex h-[42px] items-center justify-center gap-[8px] rounded-[12px] border px-[22px] text-[15px] font-semibold transition disabled:cursor-not-allowed disabled:opacity-70"
                            :class="
                                isAlreadyForked || forkState === 'done'
                                    ? 'border-[var(--detail-fork-done-border)] bg-[var(--detail-fork-done-bg)] text-[var(--detail-fork-done-text)] hover:border-[var(--detail-fork-hover-bg)] hover:bg-[var(--detail-fork-hover-bg)] hover:text-white'
                                    : 'border-[var(--detail-fork-border)] bg-[var(--detail-fork-bg)] text-[var(--detail-fork-text)] hover:border-[var(--detail-fork-hover-bg)] hover:bg-[var(--detail-fork-hover-bg)] hover:text-white'
                            "
                            :style="{
                                '--detail-fork-border': detailTone.forkBorder,
                                '--detail-fork-bg': detailTone.forkBg,
                                '--detail-fork-text': detailTone.forkText,
                                '--detail-fork-hover-bg': detailTone.forkHoverBg,
                                '--detail-fork-done-border': detailTone.forkBorder,
                                '--detail-fork-done-bg': detailTone.forkBg,
                                '--detail-fork-done-text': detailTone.forkText
                            }"
                            :disabled="!isAlreadyForked && forkState !== 'idle'"
                            @click="doFork"
                        >
                            <svg
                                viewBox="0 0 24 24"
                                class="h-[16px] w-[16px] shrink-0"
                                fill="none"
                                stroke="currentColor"
                                stroke-width="2"
                                stroke-linecap="round"
                                stroke-linejoin="round"
                                aria-hidden="true"
                            >
                                <circle cx="6" cy="6" r="2.5" />
                                <circle cx="18" cy="6" r="2.5" />
                                <circle cx="12" cy="18" r="2.5" />
                                <path d="M8.2 7.8L10.3 15.1" />
                                <path d="M15.8 7.8L13.7 15.1" />
                            </svg>
                            {{ isAlreadyForked ? '已 Fork，请前往仓库查看' : forkState === 'loading' ? 'FORK 中...' : forkState === 'done' ? '已 FORK' : 'FORK' }}
                        </button>
                    </div>
                </div>
            </div>
        </div>

        <Footer />

        <div v-if="modal.open" class="absolute inset-0 z-[40] grid place-items-center bg-[rgba(0,0,0,0.28)] p-[20px]" @click.self="closeModal">
            <div
                class="flex max-h-[calc(100%-48px)] max-w-[calc(100%-48px)] flex-col rounded-[14px] border border-[var(--detail-section-border)] bg-[var(--surface-1)] p-[16px] shadow-[0_20px_50px_rgba(15,23,42,0.24)]"
                :class="modal.kind === 'mcp' ? 'h-[440px] w-[760px]' : 'h-[520px] w-[860px]'"
            >
                <div class="mb-[10px] flex items-center justify-between gap-[12px] border-b border-[var(--detail-divider)] pb-[10px]">
                    <h3 class="detail-section-title text-[16px] font-semibold text-[var(--text-primary)]">{{ modal.title }}</h3>
                    <button class="text-[20px] text-[var(--text-secondary)]" @click="closeModal">×</button>
                </div>
                <div class="min-h-0 flex-1 overflow-y-auto px-[4px] py-[4px]">
                    <div v-if="modal.kind === 'mcp'" class="space-y-[10px]">
                        <div
                            v-for="(row, idx) in modal.mcpRows"
                            :key="`${row.label}-${idx}`"
                            class="grid grid-cols-[124px_minmax(0,1fr)] items-start gap-x-[14px] gap-y-[4px]"
                        >
                            <div class="pt-[2px] text-left text-[15px] font-semibold text-[var(--text-secondary)]">{{ row.label }}</div>
                            <div class="min-w-0 text-left text-[16px] text-[var(--text-primary)]">
                                <div v-if="row.isTags" class="flex flex-wrap gap-[6px]">
                                    <span
                                        v-for="secret in row.value"
                                        :key="secret"
                                        class="inline-flex items-center rounded-full border border-[var(--detail-divider)] px-[9px] py-[3px] text-[13px] text-[var(--text-primary)]"
                                    >
                                        {{ secret }}
                                    </span>
                                    <span v-if="!row.value || row.value.length === 0" class="text-[14px] text-[var(--text-secondary)]">无</span>
                                </div>
                                <div v-else class="whitespace-pre-wrap break-words" :class="{ 'leading-[1.7]': row.multiline }">{{ row.value || '--' }}</div>
                            </div>
                        </div>
                    </div>
                    <pre
                        v-else
                        class="whitespace-pre-wrap break-words text-[14px] leading-[1.7] text-[var(--text-primary)]"
                        :class="{ 'font-mono text-[13px]': modal.asJson }"
                    >{{ modal.content }}</pre>
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
</style>
