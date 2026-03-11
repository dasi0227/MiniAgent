<script setup>
import { computed, onMounted, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { repoDeleteMineAgent, repoList } from '../request/api';
import { notifyAppError } from '../request/request';
import { useSettingsStore } from '../router/pinia';
import { COLLAPSE_INNER_CLASS, getCollapseClasses } from '../utils/CollapseUtil';
import { getStrategyTone } from '../utils/StrategyTone';
import Footer from './Footer.vue';

const settingsStore = useSettingsStore();
const router = useRouter();

const isDarkTheme = computed(() => settingsStore.theme === 'dark');
const loading = ref(false);
const message = ref('');
const mineAgents = ref([]);
const addedAgents = ref([]);
const favorAgents = ref([]);

const sectionOpen = reactive({
    mine: false,
    added: false,
    favor: false
});

const resolveAgentName = (item) => item?.name || item?.agentName || item?.templateName || '未命名 MiniAgent';
const resolveAgentDesc = (item) => item?.desc || item?.agentDesc || item?.templateDesc || '暂无描述';
const resolveAgentType = (item) => (item?.agentType || 'react').toUpperCase();

const resolveSectionLabel = (key) => {
    if (key === 'mine') return '已创建';
    if (key === 'added') return '已添加';
    return '已收藏';
};

const resolveCardTone = (item) => getStrategyTone(item?.agentType, isDarkTheme.value);

const dangerButtonClass = computed(() =>
    isDarkTheme.value
        ? 'border-[rgba(248,113,113,0.24)] bg-[rgba(127,29,29,0.16)] text-[#fda4af] hover:bg-[rgba(127,29,29,0.24)]'
        : 'border-[rgba(239,68,68,0.22)] bg-[rgba(254,242,242,0.92)] text-[#dc2626] hover:bg-[rgba(254,226,226,0.96)]'
);

const sections = computed(() => [
    {
        key: 'mine',
        title: '我创建的',
        items: mineAgents.value,
        emptyText: '暂无已创建的 MiniAgent'
    },
    {
        key: 'added',
        title: '我添加的',
        items: addedAgents.value,
        emptyText: '暂无已添加的 MiniAgent'
    },
    {
        key: 'favor',
        title: '我收藏的',
        items: favorAgents.value,
        emptyText: '暂无已收藏的 MiniAgent'
    }
]);

const toggleSection = (key) => {
    if (!key) return;
    sectionOpen[key] = !sectionOpen[key];
};

const doRemove = () => {
    message.value = '当前后端未提供仓库移除接口';
};

const viewDetail = (item) => {
    const templateId = (item?.templateId || '').toString().trim();
    if (!templateId) {
        message.value = '该条数据缺少 templateId，暂时无法查看详情';
        return;
    }
    router.push(`/detail/${encodeURIComponent(templateId)}`);
};

const deleteMineAgent = async (item) => {
    if (!item?.agentId) {
        message.value = '该条目缺少可删除的 MiniAgent 标识';
        return;
    }
    const confirmed = window.confirm(`确认删除「${resolveAgentName(item)}」吗？`);
    if (!confirmed) return;
    loading.value = true;
    message.value = '';
    try {
        await repoDeleteMineAgent({ agentId: item.agentId });
        await loadRepository();
    } catch (error) {
        notifyAppError(error, '删除 MiniAgent 失败');
        loading.value = false;
    }
};

const loadRepository = async () => {
    loading.value = true;
    message.value = '';
    try {
        const resp = await repoList();
        const payload = resp?.data ?? resp?.result ?? resp;
        mineAgents.value = Array.isArray(payload?.self) ? payload.self : [];
        addedAgents.value = Array.isArray(payload?.fork) ? payload.fork : [];
        favorAgents.value = Array.isArray(payload?.favor) ? payload.favor : [];
    } catch (error) {
        mineAgents.value = [];
        addedAgents.value = [];
        favorAgents.value = [];
        notifyAppError(error, '获取仓库失败');
    } finally {
        loading.value = false;
    }
};

onMounted(loadRepository);
</script>

<template>
    <section class="grid h-screen grid-rows-[1fr_var(--footer-height)] bg-white">
        <div class="overflow-y-auto py-[24px] pl-[24px] pr-[calc(24px+var(--scrollbar-w))]">
            <div class="mx-auto max-w-[1100px] space-y-[16px]">
                <header class="flex items-center justify-between gap-[12px]">
                    <h1 class="text-[24px] font-bold text-[var(--text-primary)]">MiniAgent Repository</h1>
                </header>

                <div
                    v-if="message"
                    class="mt-[18px] rounded-[14px] border border-[rgba(248,113,113,0.2)] bg-[rgba(254,242,242,0.9)] px-[14px] py-[10px] text-[13px] text-[#b91c1c]"
                >
                    {{ message }}
                </div>

                <div class="space-y-[2px]">
                    <section
                        v-for="section in sections"
                        :key="section.key"
                        class="rounded-[22px] px-[4px] py-0"
                    >
                        <button
                            class="flex w-full items-center gap-[10px] rounded-[18px] px-[10px] py-[6px] text-left transition hover:bg-[rgba(148,163,184,0.06)]"
                            type="button"
                            @click="toggleSection(section.key)"
                        >
                            <svg viewBox="0 0 20 20" class="h-[14px] w-[14px] shrink-0 text-[var(--text-secondary)]" fill="currentColor" aria-hidden="true">
                                <path :d="sectionOpen[section.key] ? 'M5.5 7.5 10 12l4.5-4.5H5.5z' : 'M7 4.5 13 10 7 15.5V4.5z'" />
                            </svg>

                            <div class="flex min-w-0 items-center gap-[10px]">
                                <h2 class="text-[18px] font-semibold text-[var(--text-primary)]">{{ section.title }}</h2>
                                <span class="rounded-full border border-[rgba(148,163,184,0.18)] px-[9px] py-[2px] text-[11px] font-semibold text-[var(--text-secondary)]">
                                    {{ section.items.length }}
                                </span>
                            </div>
                        </button>

                        <div :class="getCollapseClasses(sectionOpen[section.key], { disablePointerWhenClosed: true })">
                            <div :class="[COLLAPSE_INNER_CLASS, 'pt-[6px] pb-[8px]']">
                                <div
                                    v-if="loading"
                                    class="rounded-[20px] border border-dashed border-[var(--border-color)] px-[16px] py-[22px] text-center text-[13px] text-[var(--text-secondary)]"
                                >
                                    加载中...
                                </div>

                                <div
                                    v-else-if="section.items.length === 0"
                                    class="px-[6px] py-[8px] text-center text-[13px] text-[var(--text-secondary)]"
                                >
                                    {{ section.emptyText }}
                                </div>

                                <div v-else class="grid gap-[14px] sm:grid-cols-2 xl:grid-cols-3">
                                    <article
                                        v-for="item in section.items"
                                        :key="item.repoId || item.agentId || item.templateId || resolveAgentName(item)"
                                        class="group relative flex min-h-[208px] flex-col overflow-hidden rounded-[30px] border p-[18px] shadow-[0_14px_30px_rgba(15,23,42,0.07),inset_0_1px_0_rgba(255,255,255,0.32)] transition duration-200 hover:-translate-y-[2px] hover:shadow-[0_22px_42px_rgba(15,23,42,0.14)]"
                                        :style="{
                                            borderColor: resolveCardTone(item).cardBorder,
                                            backgroundColor: resolveCardTone(item).cardBg
                                        }"
                                    >
                                        <div class="pointer-events-none absolute inset-0 opacity-[0.72]" :style="{ backgroundImage: resolveCardTone(item).overlay }" />

                                        <div class="relative flex items-start justify-between gap-[12px]">
                                            <span
                                                class="inline-flex items-center rounded-full border px-[10px] py-[4px] text-[11px] font-semibold tracking-[0.08em]"
                                                :style="{
                                                    borderColor: resolveCardTone(item).badgeBorder,
                                                    backgroundColor: resolveCardTone(item).badgeBg,
                                                    color: resolveCardTone(item).badgeText
                                                }"
                                            >
                                                {{ resolveAgentType(item) }}
                                            </span>
                                        </div>

                                        <div class="relative mt-[16px] space-y-[10px]">
                                            <h3 class="text-[20px] font-semibold leading-[1.25] text-[var(--text-primary)] repo-title-clamp">
                                                {{ resolveAgentName(item) }}
                                            </h3>
                                            <p class="text-[13px] leading-[1.7] text-[var(--text-secondary)] repo-desc-clamp">
                                                {{ resolveAgentDesc(item) }}
                                            </p>
                                        </div>

                                        <div class="relative mt-auto flex items-center justify-start gap-[8px] pt-[18px]">
                                            <button
                                                class="rounded-[12px] border px-[12px] py-[8px] text-[12px] font-semibold transition"
                                                :style="{
                                                    '--repo-primary-border': resolveCardTone(item).primaryBtnBorder,
                                                    '--repo-primary-bg': resolveCardTone(item).primaryBtnBg,
                                                    '--repo-primary-text': resolveCardTone(item).primaryBtnText,
                                                    '--repo-primary-hover-bg': resolveCardTone(item).primaryBtnHoverBg
                                                }"
                                                :class="'border-[var(--repo-primary-border)] bg-[var(--repo-primary-bg)] text-[var(--repo-primary-text)] hover:bg-[var(--repo-primary-hover-bg)]'"
                                                type="button"
                                                @click="viewDetail(item)"
                                            >
                                                查看
                                            </button>
                                            <button
                                                v-if="section.key === 'mine'"
                                                class="rounded-[12px] border px-[12px] py-[8px] text-[12px] font-semibold transition"
                                                :class="dangerButtonClass"
                                                type="button"
                                                @click="deleteMineAgent(item)"
                                            >
                                                删除
                                            </button>
                                            <button
                                                v-else
                                                class="rounded-[12px] border px-[12px] py-[8px] text-[12px] font-semibold transition"
                                                :class="dangerButtonClass"
                                                type="button"
                                                @click="doRemove(item)"
                                            >
                                                移除
                                            </button>
                                        </div>
                                    </article>
                                </div>
                            </div>
                        </div>
                    </section>
                </div>
            </div>
        </div>

        <Footer />
    </section>
</template>

<style scoped>
.repo-title-clamp {
    display: -webkit-box;
    overflow: hidden;
    -webkit-box-orient: vertical;
    -webkit-line-clamp: 2;
}

.repo-desc-clamp {
    display: -webkit-box;
    overflow: hidden;
    -webkit-box-orient: vertical;
    -webkit-line-clamp: 2;
}
</style>
