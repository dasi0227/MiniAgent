<script setup>
import { computed, onBeforeUnmount, onMounted, reactive } from 'vue';
import { useRouter } from 'vue-router';
import { useSettingsStore } from '../router/pinia';
import { createTypewriter, DEFAULT_TYPEWRITER_SEGMENTS } from '../utils/TypeWriter';
import Footer from './Footer.vue';

const router = useRouter();
const settingsStore = useSettingsStore();
const isDarkTheme = computed(() => settingsStore.theme === 'dark');

const typewriterState = reactive({
    lines: [],
    lineIndex: 0,
    playing: false
});

const featureCards = [
    {
        key: 'chat',
        title: '新建 Chat Session',
        description: '快速开始对话会话入口，后续将接入会话配置。',
        cta: '立即尝试',
        route: ''
    },
    {
        key: 'work',
        title: '新建 Work Session',
        description: '快速开始执行会话入口，后续将接入执行配置。',
        cta: '立即尝试',
        route: ''
    },
    {
        key: 'repository',
        title: '浏览 MiniAgent Repository',
        description: '查看你创建、添加与收藏的 MiniAgent。',
        cta: '立即前往',
        route: '/repository'
    },
    {
        key: 'plaza',
        title: '浏览 MiniAgent Plaza',
        description: '探索广场内容并一键 Fork 到你的仓库。',
        cta: '立即前往',
        route: '/plaza'
    },
    {
        key: 'studio',
        title: '进入 MiniAgent Studio',
        description: '配置策略与工具组合，创建专属 MiniAgent。',
        cta: '立即前往',
        route: '/studio'
    },
    {
        key: 'setting',
        title: '进入 MiniAgent Setting',
        description: '管理个人资料、MCP、API 与 Task 配置。',
        cta: '立即前往',
        route: '/setting'
    }
];

const typewriterController = createTypewriter({
    segments: DEFAULT_TYPEWRITER_SEGMENTS,
    charDelay: 45,
    segmentPause: 3000,
    loop: true,
    onUpdate: ({ lines, lineIndex, playing }) => {
        typewriterState.lines = [...lines];
        typewriterState.lineIndex = lineIndex;
        typewriterState.playing = playing;
    }
});

const handleCardClick = (route) => {
    if (!route) return;
    router.push(route);
};

const cardBackgroundStyle = computed(() =>
    isDarkTheme.value
        ? 'linear-gradient(135deg,rgba(30,64,175,0.34),rgba(30,41,59,0.78))'
        : 'linear-gradient(135deg,rgba(219,234,254,0.64),rgba(224,242,254,0.46))'
);

const cardGlowStyle = computed(() =>
    isDarkTheme.value
        ? 'radial-gradient(circle_at_100%_0%,rgba(96,165,250,0.2),transparent_62%)'
        : 'radial-gradient(circle_at_100%_0%,rgba(59,130,246,0.18),transparent_58%)'
);

onMounted(() => {
    typewriterController.start();
});

onBeforeUnmount(() => {
    typewriterController.stop();
});
</script>

<template>
    <section class="grid h-screen grid-rows-[1fr_var(--footer-height)] bg-[var(--bg-page)]">
        <div class="h-full overflow-y-auto bg-[var(--bg-page)]">
            <div
                class="mx-auto flex min-h-full w-full max-w-[1060px] flex-col px-[24px] pr-[calc(24px+var(--scrollbar-w))] py-[16px] max-[720px]:px-[12px] max-[720px]:pr-[calc(12px+var(--scrollbar-w))] max-[720px]:py-[10px]"
            >
                <div class="my-auto flex flex-col gap-[10px]">
                    <div
                        class="flex h-[156px] flex-col items-start gap-[6px] overflow-hidden text-[var(--text-primary)] max-[720px]:h-[136px]"
                    >
                        <div
                            v-for="(line, idx) in typewriterState.lines"
                            :key="idx"
                            class="bg-clip-text text-[28px] font-extrabold leading-[1.34] tracking-[0.2px] text-transparent opacity-[0.94] max-[720px]:text-[24px]"
                            :style="{ backgroundImage: 'var(--typewriter-gradient)' }"
                        >
                            {{ line }}
                            <span
                                v-if="typewriterState.playing && idx === typewriterState.lineIndex"
                                class="animate-caret text-[var(--accent-color)]"
                                >▍</span
                            >
                        </div>
                    </div>

                    <div class="grid items-stretch gap-[14px] md:grid-cols-2">
                        <article
                            v-for="card in featureCards"
                            :key="card.key"
                            class="group relative flex flex-col overflow-hidden rounded-[18px] border border-[var(--border-color)] bg-[var(--surface-1)] px-[18px] py-[14px] shadow-[var(--shadow-soft)] transition-all duration-300 hover:-translate-y-[2px] hover:border-[var(--accent-color)]"
                            :style="{ backgroundImage: cardBackgroundStyle }"
                        >
                            <div class="pointer-events-none absolute inset-0" :style="{ backgroundImage: cardGlowStyle }"></div>
                            <div class="relative flex flex-col">
                                <h3 class="mt-[4px] text-[22px] font-bold leading-[1.2] text-[var(--text-primary)] max-[720px]:text-[18px]">
                                    {{ card.title }}
                                </h3>
                                <p class="mt-[8px] overflow-hidden text-ellipsis whitespace-nowrap text-[14px] leading-[1.45] text-[var(--text-secondary)]">
                                    {{ card.description }}
                                </p>
                                <button
                                    v-if="card.route"
                                    type="button"
                                    class="mt-[10px] inline-flex w-fit items-center gap-[6px] text-[13px] font-semibold text-[var(--accent-color)]"
                                    @click="handleCardClick(card.route)"
                                >
                                    {{ card.cta }}
                                    <span aria-hidden="true">→</span>
                                </button>
                                <span
                                    v-else
                                    class="mt-[10px] inline-flex w-fit cursor-not-allowed items-center gap-[6px] text-[13px] font-semibold text-[var(--accent-color)] opacity-80"
                                >
                                    {{ card.cta }}
                                    <span aria-hidden="true">→</span>
                                </span>
                            </div>
                        </article>
                    </div>
                </div>
            </div>
        </div>

        <Footer />
    </section>
</template>
