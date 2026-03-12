<script setup>
import { computed, onMounted, reactive, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import AdminSidebar from './AdminSidebar.vue';
import Footer from './Footer.vue';
import arrowBlackIcon from '../assets/arrow-black.svg';
import arrowWhiteIcon from '../assets/arrow-white.svg';
import { adminMenuGroups } from '../utils/CommonDataUtil';
import { adminAgentList, flowAgent, flowClients } from '../request/api';
import { normalizeError, notifyAdminError } from '../request/request';

const router = useRouter();
const route = useRoute();
const currentKey = ref('flow');
const menuGroups = adminMenuGroups;

const ROLE_MAP = {
    step: ['INSPECTOR', 'PLANNER', 'RUNNER', 'REPLIER'],
    loop: ['ANALYZER', 'PERFORMER', 'SUPERVISOR', 'SUMMARIZER']
};

const loading = reactive({
    agents: false,
    clients: false,
    flows: false
});

const agents = ref([]);
const allClients = ref([]);
const selectedAgent = ref(null);
const agentFlows = ref([]);

const clientDetailMap = computed(() => {
    const map = new Map();
    (allClients.value || []).forEach((c) => map.set(c.clientId, c));
    return map;
});

const promptDialog = reactive({ visible: false, title: '', content: '' });

const pickData = (resp, msg = '操作失败') => {
    if (resp && typeof resp === 'object' && Object.prototype.hasOwnProperty.call(resp, 'code')) {
        if (resp.code !== 200) {
            const err = new Error(resp.info || msg);
            err.status = 500;
            throw err;
        }
        return resp.data;
    }
    return resp?.data ?? resp?.result ?? resp;
};

const agentStatusDot = (status) => (status === 1 ? 'bg-emerald-500' : 'bg-rose-500');

const handleSelectModule = (key) => {
    const target = adminMenuGroups.flatMap((g) => g.items).find((i) => i.key === key);
    if (target?.path) router.push(target.path);
};

const showError = (msg) => {
    notifyAdminError(new Error(msg), msg || '操作失败');
};

const openPromptDialog = (slot) => {
    const prompt = (slot?.flow?.flowPrompt || '').trim();
    if (!prompt) return;
    promptDialog.visible = true;
    promptDialog.title = `${slot.roleLabel} · Flow Prompt`;
    promptDialog.content = prompt;
};
const closePromptDialog = () => {
    promptDialog.visible = false;
    promptDialog.title = '';
    promptDialog.content = '';
};

const loadClients = async () => {
    loading.clients = true;
    try {
        const res = await flowClients();
        allClients.value = pickData(res, '获取 Client 失败') || [];
    } catch (err) {
        const msg = normalizeError(err).message || '获取 Client 失败';
        notifyAdminError(err, msg);
        allClients.value = [];
    } finally {
        loading.clients = false;
    }
};

const loadAgents = async () => {
    loading.agents = true;
    try {
        const res = await adminAgentList({});
        agents.value = pickData(res, '获取 Agent 失败') || [];
    } catch (err) {
        const msg = normalizeError(err).message || '获取 Agent 失败';
        notifyAdminError(err, msg);
        agents.value = [];
    } finally {
        loading.agents = false;
    }
};

const loadFlows = async (agentId) => {
    loading.flows = true;
    try {
        const res = await flowAgent(agentId);
        agentFlows.value = pickData(res, '获取 Flow 失败') || [];
    } catch (err) {
        const msg = normalizeError(err).message || '获取 Flow 失败';
        notifyAdminError(err, msg);
        agentFlows.value = [];
    } finally {
        loading.flows = false;
    }
};

const isRefreshing = computed(() => loading.agents || loading.clients || loading.flows);

const handleRefresh = async () => {
    const activeAgentId = selectedAgent.value?.agentId || String(route.query.agentId || '').trim();
    await Promise.all([loadClients(), loadAgents()]);
    if (!activeAgentId) {
        return;
    }
    const matched = agents.value.find((item) => item.agentId === activeAgentId);
    if (matched) {
        selectedAgent.value = matched;
    }
    await loadFlows(activeAgentId);
};

const roleOrder = computed(() => ROLE_MAP[selectedAgent.value?.agentType || 'step'] || ROLE_MAP.step);

const slotList = computed(() =>
    roleOrder.value.map((roleLabel, idx) => {
        const seq = idx + 1;
        const matched =
            agentFlows.value.find((f) => f.flowSeq === seq) ||
            agentFlows.value.find((f) => (f.clientRole || '').toUpperCase() === roleLabel);
        return { seq, roleLabel, flow: matched || null };
    })
);

const enterDetail = async (agent) => {
    selectedAgent.value = agent;
    await loadFlows(agent.agentId);
    router.replace({ path: '/admin/flow', query: { agentId: agent.agentId } });
};

const openAgentFromRoute = async () => {
    const targetId = String(route.query.agentId || '').trim();
    if (!targetId) return;
    if (!selectedAgent.value) {
        selectedAgent.value = {
            agentId: targetId,
            agentName: targetId,
            agentType: 'step',
            agentStatus: 1
        };
    }
    const agent = agents.value.find((item) => item.agentId === targetId);
    if (agent) {
        await enterDetail(agent);
        return;
    }
    await loadFlows(targetId);
};

const backToGrid = () => {
    selectedAgent.value = null;
    agentFlows.value = [];
    router.replace({ path: '/admin/flow' });
};

const getClientDetail = (clientId) => clientDetailMap.value.get(clientId);
const getIdList = (list, key) => (list || []).map((item) => item?.[key]).filter(Boolean);

onMounted(async () => {
    await openAgentFromRoute();
    await Promise.all([loadClients(), loadAgents()]);
    await openAgentFromRoute();
});
</script>

<template>
    <div class="admin-flow-page admin-font flex h-screen bg-[#f8fafc]">
        <AdminSidebar :groups="menuGroups" :current="currentKey" @select="handleSelectModule" />
        <div class="flex min-w-0 flex-1 flex-col">
            <header class="flow-header flex items-center justify-between border-b border-[#e2e8f0] bg-white px-6 py-4 shadow-sm">
                <div class="text-[18px] font-semibold text-[#0f172a]">
                    FLOW 管理
                    <span v-if="selectedAgent" class="ml-2 text-[14px] font-normal text-[#64748b]">/ {{ selectedAgent.agentId }}</span>
                </div>
                <button
                    class="admin-icon-btn h-[34px] w-[34px] rounded-[10px] disabled:cursor-not-allowed disabled:opacity-70"
                    type="button"
                    title="刷新"
                    aria-label="刷新"
                    :disabled="isRefreshing"
                    @click="handleRefresh"
                >
                    <svg viewBox="0 0 24 24" class="h-[16px] w-[16px]" fill="none" stroke="currentColor" stroke-width="1.8" aria-hidden="true">
                        <path d="M20 12a8 8 0 1 1-2.34-5.66" />
                        <path d="M20 4v6h-6" />
                    </svg>
                </button>
            </header>

            <div class="flex-1 overflow-auto p-6">
                <!-- Agent 网格 -->
                <div v-if="!selectedAgent" class="h-full overflow-auto">
                    <div class="mb-4 text-left text-[14px] text-[#94a3b8]">共 {{ agents.length }} 个 Agent</div>
                    <div class="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
                        <div
                            v-for="agent in agents"
                            :key="agent.agentId"
                            class="agent-card group relative h-[180px] cursor-pointer overflow-hidden rounded-[16px] bg-white shadow-sm transition duration-200 hover:-translate-y-1 hover:shadow-lg"
                            @click="enterDetail(agent)"
                        >
                            <div class="absolute right-3 top-3 h-3 w-3 rounded-full" :class="agentStatusDot(agent.agentStatus)" />
                            <div class="agent-card-inner">
                                <div class="agent-card-face agent-card-front flex h-full flex-col justify-between p-4">
                                    <div
                                        class="inline-flex w-fit items-center rounded-full px-2.5 py-1 text-[14px] font-semibold uppercase tracking-[0.08em]"
                                        :class="agent.agentType === 'loop' ? 'bg-[#ede9fe] text-[#7c3aed]' : 'bg-[#dbeafe] text-[#2563eb]'"
                                    >
                                        {{ agent.agentType }}
                                    </div>
                                    <div class="text-center">
                                        <div class="text-[28px] font-semibold text-[#0f172a]">{{ agent.agentId }}</div>
                                        <div class="mt-3 text-[16px] text-[#475569]">{{ agent.agentName || '-' }}</div>
                                    </div>
                                    <div class="text-[12px] text-[#94a3b8]">&nbsp;</div>
                                </div>
                                <div class="agent-card-face agent-card-back flex h-full flex-col items-center justify-center p-4 text-center">
                                    <div class="text-[16px] leading-5 text-[#475569]">
                                        {{ agent.agentDesc || '暂无描述' }}
                                    </div>
                                </div>
                            </div>
                        </div>
                        <div v-if="!loading.agents && agents.length === 0" class="col-span-full rounded-[12px] border border-dashed border-[#cbd5e1] bg-white px-4 py-6 text-center text-[#94a3b8]">
                            暂无数据
                        </div>
                    </div>
                    <div v-if="loading.agents" class="mt-6 text-center text-[13px] text-[#94a3b8]">加载中...</div>
                </div>

                <!-- 详情视图 -->
                <div v-else class="flex h-full flex-col gap-4 overflow-visible">
                    <div class="flex flex-wrap items-center justify-between gap-3">
                        <div class="flex flex-wrap items-center gap-3">
                            <div class="text-[16px] font-semibold text-[#0f172a]">{{ selectedAgent.agentName || selectedAgent.agentId }}</div>
                            <span class="inline-flex items-center rounded-full bg-[#dbeafe] px-3 py-1 text-[12px] font-medium text-[#2563eb]">
                                类型：{{ selectedAgent.agentType }}
                            </span>
                            <span
                                class="inline-flex items-center rounded-full px-3 py-1 text-[12px] font-medium"
                                :class="selectedAgent.agentStatus === 1 ? 'bg-[#ecfdf3] text-[#16a34a]' : 'bg-[#fef2f2] text-[#dc2626]'"
                            >
                                状态：{{ selectedAgent.agentStatus === 1 ? '启用' : '禁用' }}
                            </span>
                        </div>
                        <div class="flex items-center gap-2">
                            <button
                                class="flow-top-action rounded-[10px] border border-[#e2e8f0] px-3 py-2 text-[13px] font-semibold text-[#0f172a] transition hover:bg-[#f1f5f9]"
                                type="button"
                                @click="selectedAgent && router.push(`/admin/canvas?agentId=${selectedAgent.agentId}`)"
                            >
                                查看配置图
                            </button>
                            <button
                                class="flow-top-action rounded-[10px] border border-[#e2e8f0] px-3 py-2 text-[13px] font-semibold text-[#0f172a] transition hover:bg-[#f1f5f9]"
                                type="button"
                                @click="backToGrid"
                            >
                                返回列表
                            </button>
                        </div>
                    </div>

                    <div class="grid grid-cols-1 gap-3 overflow-visible lg:grid-cols-4 lg:gap-x-6">
                        <div
                            v-for="(slot, idx) in slotList"
                            :key="slot.roleLabel"
                            class="flow-role-card relative overflow-visible rounded-[14px] border border-[#e2e8f0] bg-white p-3 shadow-sm transition"
                        >
                            <div class="mb-3 text-center">
                                <div class="text-[24px] font-semibold text-[#0f172a]">{{ slot.roleLabel }}</div>
                            </div>
                            <div v-if="slot.flow" class="flow-detail-card flex min-h-[240px] flex-col gap-2 rounded-[12px] border border-[#e2e8f0] bg-[#f8fafc] p-3 text-left">
                                <div class="mt-1 flex flex-col gap-1 text-[12px] text-[#475569]">
                                    <div class="flex items-start gap-0.5">
                                        <span class="flow-meta-label">Client：</span>
                                        <span class="flow-chip">
                                            {{ slot.flow.clientId }}
                                        </span>
                                    </div>
                                    <div class="flex items-start gap-0.5">
                                        <span class="flow-meta-label">API：</span>
                                        <span
                                            v-if="getClientDetail(slot.flow.clientId)?.api?.apiId"
                                            class="flow-chip"
                                        >
                                            {{ getClientDetail(slot.flow.clientId)?.api?.apiId }}
                                        </span>
                                        <span v-else class="text-[#475569]">-</span>
                                    </div>
                                    <div class="flex items-start gap-0.5">
                                        <span class="flow-meta-label">Model：</span>
                                        <span
                                            v-if="getClientDetail(slot.flow.clientId)?.model?.modelId"
                                            class="flow-chip"
                                        >
                                            {{ getClientDetail(slot.flow.clientId)?.model?.modelId }}
                                        </span>
                                        <span v-else class="text-[#475569]">-</span>
                                    </div>
                                    <div class="flex items-start gap-0.5">
                                        <span class="flow-meta-label">MCP：</span>
                                        <div
                                            v-if="getIdList(getClientDetail(slot.flow.clientId)?.mcpList, 'mcpId').length"
                                            class="flex flex-wrap gap-1"
                                        >
                                            <span
                                                v-for="item in getIdList(getClientDetail(slot.flow.clientId)?.mcpList, 'mcpId')"
                                                :key="item"
                                                class="flow-chip"
                                            >
                                                {{ item }}
                                            </span>
                                        </div>
                                        <span v-else class="text-[#475569]">-</span>
                                    </div>
                                    <div class="flex items-start gap-0.5">
                                        <span class="flow-meta-label">Advisor：</span>
                                        <div
                                            v-if="getIdList(getClientDetail(slot.flow.clientId)?.advisorList, 'advisorId').length"
                                            class="flex flex-wrap gap-1"
                                        >
                                            <span
                                                v-for="item in getIdList(getClientDetail(slot.flow.clientId)?.advisorList, 'advisorId')"
                                                :key="item"
                                                class="flow-chip"
                                            >
                                                {{ item }}
                                            </span>
                                        </div>
                                        <span v-else class="text-[#475569]">-</span>
                                    </div>
                                    <div class="flex items-start gap-0.5">
                                        <span class="flow-meta-label">Prompt：</span>
                                        <div
                                            v-if="getIdList(getClientDetail(slot.flow.clientId)?.promptList, 'promptId').length"
                                            class="flex flex-wrap gap-1"
                                        >
                                            <span
                                                v-for="item in getIdList(getClientDetail(slot.flow.clientId)?.promptList, 'promptId')"
                                                :key="item"
                                                class="flow-chip"
                                            >
                                                {{ item }}
                                            </span>
                                        </div>
                                        <span v-else class="text-[#475569]">-</span>
                                    </div>
                                </div>
                                <div class="mt-auto flex justify-center pt-2">
                                    <button
                                        class="flow-setting-btn rounded-full border border-[#e2e8f0] bg-white px-4 py-1 text-[12px] font-semibold text-[#475569] shadow-sm transition hover:border-[#0ea5e9] hover:text-[#0ea5e9] disabled:cursor-not-allowed disabled:text-[#cbd5e1]"
                                        type="button"
                                        :disabled="!(slot.flow.flowPrompt && slot.flow.flowPrompt.trim())"
                                        @click.stop="openPromptDialog(slot)"
                                    >
                                        查看设定
                                    </button>
                                </div>
                            </div>
                            <div v-else class="flex h-[240px] items-center justify-center rounded-[10px] border border-dashed border-[#cbd5e1] text-[28px] text-[#cbd5e1]">
                                ➕
                            </div>
                            <div
                                v-if="idx < slotList.length - 1"
                                class="pointer-events-none absolute -right-7 top-1/2 z-50 hidden -translate-y-1/2 items-center justify-center lg:flex"
                            >
                                <img :src="arrowBlackIcon" alt="arrow" class="flow-arrow-light h-7 w-7 drop-shadow-sm" />
                                <img :src="arrowWhiteIcon" alt="arrow" class="flow-arrow-dark h-7 w-7 drop-shadow-sm" />
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            <Footer layout="admin" />
        </div>

        <!-- Prompt 详情弹窗 -->
        <div
            v-if="promptDialog.visible"
            class="fixed inset-0 z-50 flex items-center justify-center bg-[#0f172a]/30 backdrop-blur-[2px]"
            @click="closePromptDialog"
        >
            <div class="flow-prompt-modal w-[520px] max-w-[90vw] rounded-[14px] bg-white p-5 shadow-lg" @click.stop>
                <div class="text-[16px] font-semibold text-[#0f172a]">{{ promptDialog.title || 'Prompt 详情' }}</div>
                <div class="flow-prompt-content mt-3 max-h-[360px] overflow-auto rounded-[10px] border border-[#e2e8f0] bg-[#f8fafc] p-3 text-[13px] text-[#475569] whitespace-pre-wrap break-all [overflow-wrap:anywhere]">
                    {{ promptDialog.content }}
                </div>
                <div class="mt-4 flex justify-end">
                    <button
                        class="flow-modal-close rounded-[10px] bg-[#0ea5e9] px-4 py-2 text-[13px] font-semibold text-white hover:bg-[#0284c7]"
                        type="button"
                        @click="closePromptDialog"
                    >
                        关闭
                    </button>
                </div>
            </div>
        </div>
    </div>
</template>

<style scoped>
.admin-font {
    font-size: 15px;
}
.admin-font .text-\[12px\] {
    font-size: 13px !important;
}
.admin-font .text-\[13px\] {
    font-size: 14px !important;
}

.agent-card {
    perspective: 1000px;
}

.agent-card-inner {
    position: relative;
    width: 100%;
    height: 100%;
    transform-style: preserve-3d;
    transition: transform 0.6s;
}

.agent-card:hover .agent-card-inner {
    transform: rotateY(180deg);
}

.agent-card-face {
    position: absolute;
    inset: 0;
    backface-visibility: hidden;
}

.agent-card-back {
    transform: rotateY(180deg);
}

.flow-meta-label {
    width: 62px;
    flex-shrink: 0;
    color: #94a3b8;
    font-size: 13px;
    line-height: 1.5;
}

.flow-chip {
    display: inline-flex;
    align-items: center;
    border-radius: 999px;
    border: 1px solid #d4dbe5;
    background: #ffffff;
    padding: 2px 10px;
    font-size: 12px;
    line-height: 1.45;
    color: #475569;
    max-width: 100%;
}

.flow-role-card:hover {
    border-color: #0ea5e9;
    box-shadow: 0 10px 22px rgba(15, 23, 42, 0.12);
}

.flow-detail-card {
    transition: border-color 0.2s ease;
}

.flow-detail-card:hover {
    border-color: #93c5fd;
}

.flow-arrow-dark {
    display: none;
}

[data-theme='dark'] .flow-arrow-light {
    display: none;
}

[data-theme='dark'] .flow-arrow-dark {
    display: block;
}

</style>
