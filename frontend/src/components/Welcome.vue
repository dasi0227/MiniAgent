<script setup>
import { onBeforeUnmount, onMounted, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { insertSession } from '../request/api';
import { useAgentStore, useChatStore } from '../router/pinia';
import { notifyAppError } from '../request/request';
import { createTypewriter, DEFAULT_TYPEWRITER_SEGMENTS } from '../utils/TypeWriter';
import Footer from './Footer.vue';

const router = useRouter();
const chatStore = useChatStore();
const agentStore = useAgentStore();
const creatingSessionType = ref('');

const typewriterState = reactive({
    lines: [],
    lineIndex: 0,
    playing: false
});

const featureCards = [
    {
        key: 'chat',
        title: '新建 Chat',
        description: '立即创建一个 Chat 会话，开始你的对话。',
        cta: '立即新建',
        action: 'create-chat'
    },
    {
        key: 'work',
        title: '新建 Work',
        description: '立即创建一个 Work 会话，开始你的任务执行。',
        cta: '立即新建',
        action: 'create-work'
    },
    {
        key: 'repository',
        title: '浏览 Repository',
        description: '查看你创建、添加与收藏的 MiniAgent。',
        cta: '立即前往',
        route: '/repository'
    },
    {
        key: 'plaza',
        title: '浏览 Plaza',
        description: '探索广场内容并一键 Fork 到你的仓库。',
        cta: '立即前往',
        route: '/plaza'
    },
    {
        key: 'studio',
        title: '进入 Studio',
        description: '配置策略与工具组合，创建专属 MiniAgent。',
        cta: '立即前往',
        route: '/studio'
    },
    {
        key: 'setting',
        title: '进入 Setting',
        description: '管理个人资料、MCP、API 与 Task 配置。',
        cta: '立即前往',
        route: '/setting'
    }
];

const pickData = (resp, fallbackMessage = '操作失败') => {
    if (resp && typeof resp === 'object' && Object.prototype.hasOwnProperty.call(resp, 'code')) {
        if (resp.code !== 200) {
            throw new Error(resp.info || fallbackMessage);
        }
        return resp.data;
    }
    return resp?.data ?? resp?.result ?? resp;
};

const normalizeSessionType = (value) => (value ? value.toString().toLowerCase() : '');

const mapSession = (session) => {
    if (!session) return null;
    return {
        sessionId: session.sessionId,
        sessionUser: session.userName || session.sessionUser || '',
        title: session.sessionTitle || '新会话',
        sessionType: normalizeSessionType(session.sessionType || session.type),
        createdAt: session.createTime ? Date.parse(session.createTime) : Date.now(),
        messages: [],
        cards: []
    };
};

const createSessionAndNavigate = async (sessionType) => {
    if (creatingSessionType.value) return;
    creatingSessionType.value = sessionType;
    try {
        const resp = await insertSession({ sessionTitle: '新会话', sessionType });
        const created = mapSession(pickData(resp, '创建会话失败'));
        if (!created?.sessionId) {
            throw new Error('创建会话失败');
        }
        if (sessionType === 'work') {
            agentStore.upsertSession(created);
            agentStore.setCurrentSessionId(created.sessionId);
            await router.push('/work');
            return;
        }
        chatStore.upsertChat(created);
        chatStore.setCurrentChatId(created.sessionId);
        await router.push('/chat');
    } catch (error) {
        notifyAppError(error, '创建会话失败');
    } finally {
        creatingSessionType.value = '';
    }
};

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

const handleCardClick = async (card) => {
    if (!card) return;
    if (card.action === 'create-chat') {
        await createSessionAndNavigate('chat');
        return;
    }
    if (card.action === 'create-work') {
        await createSessionAndNavigate('work');
        return;
    }
    if (!card.route) return;
    router.push(card.route);
};

const resolveCardCta = (card) => {
    if (card?.action === 'create-chat' && creatingSessionType.value === 'chat') {
        return '创建中...';
    }
    if (card?.action === 'create-work' && creatingSessionType.value === 'work') {
        return '创建中...';
    }
    return card?.cta || '';
};

onMounted(() => {
    typewriterController.start();
});

onBeforeUnmount(() => {
    typewriterController.stop();
});
</script>

<template>
    <section class="welcome-page grid h-screen grid-rows-[1fr_var(--footer-height)] bg-[var(--bg-page)]">
        <div class="h-full overflow-y-auto bg-[var(--bg-page)]">
            <div
                class="mx-auto flex min-h-full w-full flex-col px-[24px] pr-[calc(24px+var(--scrollbar-w))] py-[10px] max-[720px]:px-[12px] max-[720px]:pr-[calc(12px+var(--scrollbar-w))] max-[720px]:py-[10px]"
            >
                <div class="welcome-hero-wrap">
                    <div class="mx-auto w-full max-w-[1320px] pl-[50px] max-[720px]:px-0">
                        <div class="welcome-hero">
                            <div class="welcome-hero-inner">
                                <div class="welcome-mark-block">
                                    <div class="welcome-mark-line" data-text="Mini">Mini</div>
                                    <div class="welcome-mark-line" data-text="Agent">Agent</div>
                                </div>
                                <div
                                    class="welcome-typewriter flex h-[156px] flex-1 flex-col items-start justify-center gap-[6px] overflow-hidden text-[var(--text-primary)] max-[900px]:h-[136px]"
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
                            </div>
                        </div>
                    </div>
                </div>

                <div class="welcome-section-gap"></div>

                <div class="welcome-links-wrap mx-auto w-full max-w-[1060px]">
                    <div class="grid items-stretch gap-[14px] md:grid-cols-2">
                        <article
                            v-for="card in featureCards"
                            :key="card.key"
                            class="welcome-feature-card group relative flex flex-col overflow-hidden rounded-[18px] border border-[var(--border-color)] bg-[var(--surface-1)] px-[18px] py-[14px] shadow-[var(--shadow-soft)] transition-all duration-300 hover:-translate-y-[2px] hover:border-[var(--accent-color)]"
                        >
                            <div class="welcome-feature-glow pointer-events-none absolute inset-0"></div>
                            <div class="relative flex flex-col">
                                <h3 class="mt-[4px] text-[22px] font-bold leading-[1.2] text-[var(--text-primary)] max-[720px]:text-[18px]">
                                    {{ card.title }}
                                </h3>
                                <p class="mt-[8px] overflow-hidden text-ellipsis whitespace-nowrap text-[14px] leading-[1.45] text-[var(--text-secondary)]">
                                    {{ card.description }}
                                </p>
                                <button
                                    v-if="card.route || card.action"
                                    type="button"
                                    class="mt-[10px] inline-flex w-fit items-center gap-[6px] text-[13px] font-semibold text-[var(--accent-color)]"
                                    :class="creatingSessionType ? 'opacity-70' : ''"
                                    :disabled="Boolean(creatingSessionType)"
                                    @click="handleCardClick(card)"
                                >
                                    {{ resolveCardCta(card) }}
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

<style scoped>
.welcome-hero-wrap {
    margin-top: 80px;
}

.welcome-section-gap {
    height: 80px;
}

.welcome-links-wrap {
    width: 100%;
}

.welcome-mark-block {
    min-width: 218px;
    padding-bottom: 12px;
    text-align: left;
}

.welcome-mark-line {
    position: relative;
    display: block;
    font-family: 'Arial Black', 'PingFang SC', 'Microsoft YaHei', sans-serif;
    margin: 0;
    font-size: clamp(58px, 6.4vw, 92px);
    font-weight: 900;
    line-height: 0.9;
    letter-spacing: 0.02em;
    color: #3b82f6;
    background:
        radial-gradient(circle at 30% 25%, #ffffff 0%, #d6e8ff 12%, rgba(255, 255, 255, 0.15) 18%, transparent 19%),
        radial-gradient(circle at 68% 30%, rgba(255, 255, 255, 0.95) 0%, rgba(255, 255, 255, 0.2) 10%, transparent 18%),
        radial-gradient(circle at 50% 120%, #2f7cf6 0%, #3b82f6 25%, #5592ff 55%, #7aaeff 78%, #a9ceff 100%);
    -webkit-background-clip: text;
    background-clip: text;
    -webkit-text-fill-color: transparent;
    -webkit-text-stroke: 4px #2f66db;
    text-shadow:
        0 2px 0 rgba(255, 255, 255, 0.35),
        0 5px 0 #3f85fb,
        0 8px 0 #3a7bed,
        0 12px 0 #336fdb,
        0 18px 18px rgba(37, 99, 235, 0.24),
        0 30px 36px rgba(30, 64, 175, 0.2);
    filter: drop-shadow(0 12px 18px rgba(30, 64, 175, 0.18));
    transform: skewX(-6deg);
}

.welcome-mark-line + .welcome-mark-line {
    margin-top: -8px;
}

.welcome-mark-line::before {
    content: attr(data-text);
    position: absolute;
    inset: 0;
    z-index: -1;
    color: #3374ea;
    -webkit-text-stroke: 8px #3374ea;
    filter: blur(6px);
    opacity: 0.5;
    transform: translate(4px, 7px) scale(1.01);
}

.welcome-mark-line::after {
    content: attr(data-text);
    position: absolute;
    inset: 0;
    background:
        linear-gradient(
            to bottom,
            rgba(255, 255, 255, 0.95) 0%,
            rgba(255, 255, 255, 0.65) 18%,
            rgba(255, 255, 255, 0.18) 32%,
            rgba(255, 255, 255, 0) 48%
        );
    -webkit-background-clip: text;
    background-clip: text;
    -webkit-text-fill-color: transparent;
    pointer-events: none;
    transform: translate(-4px, -9px);
    opacity: 0.95;
}

.welcome-hero {
    display: flex;
    justify-content: center;
    margin-bottom: 6px;
    overflow: visible;
}

.welcome-hero-inner {
    display: flex;
    width: 100%;
    align-items: center;
    justify-content: center;
    gap: 26px;
    overflow: visible;
}

.welcome-typewriter {
    margin-left: 18px;
    max-width: none;
    min-width: 0;
}

.welcome-feature-card {
    background-image: linear-gradient(135deg, rgba(219, 234, 254, 0.64), rgba(224, 242, 254, 0.46));
}

.welcome-feature-glow {
    background-image: radial-gradient(circle at 100% 0%, rgba(59, 130, 246, 0.18), transparent 58%);
}

@media (max-width: 900px) {
    .welcome-hero-inner {
        gap: 14px;
    }

    .welcome-mark-block {
        min-width: 154px;
        padding-bottom: 8px;
    }

    .welcome-mark-line {
        font-size: clamp(48px, 10.8vw, 72px);
        -webkit-text-stroke: 3px #2f66db;
    }

    .welcome-typewriter {
        margin-left: 8px;
        max-width: none;
    }
}

@media (max-width: 720px) {
    .welcome-section-gap {
        height: 18px;
    }

    .welcome-hero {
        margin-bottom: 2px;
    }

    .welcome-hero-inner {
        flex-direction: column;
        align-items: flex-start;
        justify-content: flex-start;
        gap: 10px;
    }

    .welcome-mark-block {
        min-width: 0;
        padding-bottom: 2px;
    }

    .welcome-typewriter {
        margin-left: 0;
        width: 100%;
    }
}
</style>
