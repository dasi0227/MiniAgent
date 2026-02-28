<script setup>
import { computed, onMounted, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { repoList } from '../request/api';
import { normalizeError } from '../request/request';
import { useSettingsStore, useWelcomeLaunchStore } from '../router/pinia';
import { COLLAPSE_INNER_CLASS, getCollapseClasses } from '../utils/CollapseUtil';
import Footer from './Footer.vue';

const router = useRouter();
const settingsStore = useSettingsStore();
const welcomeLaunchStore = useWelcomeLaunchStore();

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

const resolveCardTone = (item) => {
    const type = resolveAgentType(item);
    if (isDarkTheme.value) {
        if (type === 'STEP') {
            return {
                card: 'border-[rgba(245,158,11,0.22)] bg-[rgba(15,23,42,0.72)] shadow-[0_18px_40px_rgba(15,23,42,0.26)]',
                glow: 'linear-gradient(160deg, rgba(251,191,36,0.18), rgba(15,23,42,0) 52%), radial-gradient(circle at 100% 0, rgba(251,191,36,0.26), rgba(15,23,42,0) 58%)',
                badge: 'border-[rgba(251,191,36,0.34)] bg-[rgba(251,191,36,0.12)] text-[#fcd34d]',
                primaryButton: 'border-[rgba(251,191,36,0.34)] bg-[rgba(251,191,36,0.12)] text-[#fcd34d] hover:bg-[rgba(251,191,36,0.18)]'
            };
        }
        if (type === 'LOOP') {
            return {
                card: 'border-[rgba(16,185,129,0.22)] bg-[rgba(15,23,42,0.72)] shadow-[0_18px_40px_rgba(15,23,42,0.26)]',
                glow: 'linear-gradient(160deg, rgba(16,185,129,0.16), rgba(15,23,42,0) 52%), radial-gradient(circle at 100% 0, rgba(45,212,191,0.22), rgba(15,23,42,0) 58%)',
                badge: 'border-[rgba(52,211,153,0.34)] bg-[rgba(16,185,129,0.12)] text-[#86efac]',
                primaryButton: 'border-[rgba(52,211,153,0.34)] bg-[rgba(16,185,129,0.12)] text-[#86efac] hover:bg-[rgba(16,185,129,0.18)]'
            };
        }
        return {
            card: 'border-[rgba(96,165,250,0.22)] bg-[rgba(15,23,42,0.72)] shadow-[0_18px_40px_rgba(15,23,42,0.26)]',
            glow: 'linear-gradient(160deg, rgba(96,165,250,0.17), rgba(15,23,42,0) 52%), radial-gradient(circle at 100% 0, rgba(96,165,250,0.24), rgba(15,23,42,0) 58%)',
            badge: 'border-[rgba(96,165,250,0.34)] bg-[rgba(96,165,250,0.12)] text-[#bfdbfe]',
            primaryButton: 'border-[rgba(96,165,250,0.34)] bg-[rgba(96,165,250,0.12)] text-[#bfdbfe] hover:bg-[rgba(96,165,250,0.18)]'
        };
    }

    if (type === 'STEP') {
        return {
            card: 'border-[rgba(245,158,11,0.16)] bg-[rgba(255,255,255,0.96)] shadow-[0_14px_34px_rgba(15,23,42,0.06)]',
            glow: 'linear-gradient(160deg, rgba(251,191,36,0.1), rgba(255,255,255,0) 52%), radial-gradient(circle at 100% 0, rgba(251,191,36,0.18), rgba(255,255,255,0) 58%)',
            badge: 'border-[rgba(245,158,11,0.24)] bg-[rgba(251,191,36,0.12)] text-[#92400e]',
            primaryButton: 'border-[rgba(245,158,11,0.26)] bg-[rgba(255,251,235,0.92)] text-[#b45309] hover:bg-[rgba(255,247,237,0.98)]'
        };
    }
    if (type === 'LOOP') {
        return {
            card: 'border-[rgba(16,185,129,0.16)] bg-[rgba(255,255,255,0.96)] shadow-[0_14px_34px_rgba(15,23,42,0.06)]',
            glow: 'linear-gradient(160deg, rgba(16,185,129,0.08), rgba(255,255,255,0) 52%), radial-gradient(circle at 100% 0, rgba(45,212,191,0.16), rgba(255,255,255,0) 58%)',
            badge: 'border-[rgba(16,185,129,0.22)] bg-[rgba(16,185,129,0.1)] text-[#047857]',
            primaryButton: 'border-[rgba(16,185,129,0.24)] bg-[rgba(236,253,245,0.92)] text-[#047857] hover:bg-[rgba(220,252,231,0.98)]'
        };
    }
    return {
        card: 'border-[rgba(59,130,246,0.14)] bg-[rgba(255,255,255,0.96)] shadow-[0_14px_34px_rgba(15,23,42,0.06)]',
        glow: 'linear-gradient(160deg, rgba(59,130,246,0.08), rgba(255,255,255,0) 52%), radial-gradient(circle at 100% 0, rgba(96,165,250,0.16), rgba(255,255,255,0) 58%)',
        badge: 'border-[rgba(59,130,246,0.22)] bg-[rgba(59,130,246,0.09)] text-[#1d4ed8]',
        primaryButton: 'border-[rgba(59,130,246,0.22)] bg-[rgba(239,246,255,0.92)] text-[#2563eb] hover:bg-[rgba(219,234,254,0.98)]'
    };
};

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

const useAgent = (item) => {
    if (!item?.agentId) {
        message.value = '该条目没有可直接使用的 MiniAgent';
        return;
    }
    welcomeLaunchStore.setTask({
        type: 'work',
        prompt: '请根据当前任务执行。',
        sessionTitle: resolveAgentName(item),
        agentId: item.agentId
    });
    router.push('/work');
};

const doRemove = () => {
    message.value = '当前后端未提供仓库移除接口';
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
        message.value = normalizeError(error).message || '获取仓库失败';
    } finally {
        loading.value = false;
    }
};

onMounted(loadRepository);
</script>

<template>
    <section class="grid h-screen grid-rows-[1fr_var(--footer-height)] bg-[var(--page-bg)]">
        <div class="overflow-y-auto py-[24px] pl-[24px] pr-[calc(24px+var(--scrollbar-w))]">
            <div class="mx-auto max-w-[1120px]">
                <header class="flex flex-wrap items-end justify-between gap-[14px]">
                    <div>
                        <h1 class="text-[24px] font-bold text-[var(--text-primary)]">MiniAgent Repository</h1>
                    </div>
                </header>

                <div
                    v-if="message"
                    class="mt-[18px] rounded-[14px] border border-[rgba(248,113,113,0.2)] bg-[rgba(254,242,242,0.9)] px-[14px] py-[10px] text-[13px] text-[#b91c1c]"
                >
                    {{ message }}
                </div>

                <div class="mt-[20px] space-y-[2px]">
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
                            <div class="flex min-w-0 items-center gap-[10px]">
                                <h2 class="text-[18px] font-semibold text-[var(--text-primary)]">{{ section.title }}</h2>
                                <span class="rounded-full border border-[rgba(148,163,184,0.18)] px-[9px] py-[2px] text-[11px] font-semibold text-[var(--text-secondary)]">
                                    {{ section.items.length }}
                                </span>
                            </div>

                            <div
                                class="ml-auto flex h-[34px] w-[34px] shrink-0 items-center justify-center rounded-full border border-[rgba(148,163,184,0.18)] text-[var(--text-secondary)] transition"
                                :class="sectionOpen[section.key] ? 'rotate-180 bg-[rgba(148,163,184,0.08)]' : 'bg-transparent'"
                            >
                                <svg viewBox="0 0 24 24" class="h-[16px] w-[16px]" fill="none" stroke="currentColor" stroke-width="1.8" aria-hidden="true">
                                    <path d="m6 9 6 6 6-6" stroke-linecap="round" stroke-linejoin="round" />
                                </svg>
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
                                        class="group relative flex min-h-[208px] flex-col overflow-hidden rounded-[30px] border p-[18px] transition duration-200 hover:-translate-y-[2px]"
                                        :class="resolveCardTone(item).card"
                                    >
                                        <div class="pointer-events-none absolute inset-0 opacity-100" :style="{ background: resolveCardTone(item).glow }" />

                                        <div class="relative flex items-start justify-between gap-[12px]">
                                            <span
                                                class="inline-flex items-center rounded-full border px-[10px] py-[4px] text-[11px] font-semibold tracking-[0.08em]"
                                                :class="resolveCardTone(item).badge"
                                            >
                                                {{ resolveAgentType(item) }}
                                            </span>
                                            <span class="text-[11px] text-[var(--text-secondary)]">
                                                {{ resolveSectionLabel(section.key) }}
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

                                        <div class="relative mt-auto flex items-center justify-end gap-[8px] pt-[18px]">
                                            <button
                                                class="rounded-[12px] border px-[12px] py-[8px] text-[12px] font-semibold transition"
                                                :class="resolveCardTone(item).primaryButton"
                                                type="button"
                                                @click="useAgent(item)"
                                            >
                                                使用
                                            </button>
                                            <button
                                                v-if="section.key !== 'mine'"
                                                class="rounded-[12px] border border-[rgba(148,163,184,0.18)] bg-[rgba(255,255,255,0.76)] px-[12px] py-[8px] text-[12px] font-semibold text-[var(--text-secondary)] transition hover:border-[rgba(100,116,139,0.24)] hover:text-[var(--text-primary)] dark:bg-[rgba(15,23,42,0.24)]"
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
