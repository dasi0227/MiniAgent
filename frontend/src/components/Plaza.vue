<script setup>
import { computed, onBeforeUnmount, onMounted, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { useSettingsStore } from '../router/pinia';
import {
    plazaComment,
    plazaCommentArea,
    plazaDiscomment,
    plazaDisfavor,
    plazaDislike,
    plazaFavor,
    plazaLike,
    plazaList
} from '../request/api';
import { notifyAppError } from '../request/request';
import { getStrategyTone } from '../utils/StrategyTone';
import Footer from './Footer.vue';

const router = useRouter();
const settingsStore = useSettingsStore();
const isDarkTheme = computed(() => settingsStore.theme === 'dark');
const loading = ref(false);
const message = ref('');
const plazaItems = ref([]);
const COMMENT_PAGE_SIZE = 10;
const COMMENT_CONTENT_LIMIT = 128;
const commentData = ref({ list: [], total: 0, pageNum: 1, pageSize: COMMENT_PAGE_SIZE });
const currentCommentItem = ref(null);
const commentOpen = ref(false);
const searchText = ref('');
const searchKeyword = ref('');
const sortField = ref('like');
const sortOrder = ref('desc');
const filterPopoverOpen = ref(false);
let filterPopoverCloseTimer = null;

const sortFieldOptions = [
    { value: 'like', label: '点赞' },
    { value: 'favor', label: '收藏' },
    { value: 'comment', label: '评论' }
];

const commentForm = reactive({
    plazaId: '',
    commentContent: ''
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

const displayAuthor = (item) => item?.userName || '未知用户';
const displayType = (item) => (item?.agentType || 'react').toUpperCase();
const isCommented = (item) => Boolean(item?.commented);

const resolveIconColor = (active, color) => (active ? color : '#94a3b8');

const resolveCardTone = (item) => getStrategyTone(item?.agentType, isDarkTheme.value);

const resolveSortValue = (item, field) => {
    if (field === 'favor') return Number(item?.favorCount) || 0;
    if (field === 'comment') return Number(item?.commentCount) || 0;
    return Number(item?.likeCount) || 0;
};

const filteredItems = computed(() => {
    const list = Array.isArray(plazaItems.value) ? [...plazaItems.value] : [];
    if (!list.length) {
        return [];
    }
    const hasStableOrder =
        searchKeyword.value.trim() !== '' || ['like', 'favor', 'comment'].includes(sortField.value);
    if (hasStableOrder) {
        return list;
    }
    return list.sort((a, b) => {
        const aValue = resolveSortValue(a, sortField.value);
        const bValue = resolveSortValue(b, sortField.value);
        if (aValue === bValue) {
            const aId = Number(a?.plazaId) || 0;
            const bId = Number(b?.plazaId) || 0;
            return bId - aId;
        }
        return sortOrder.value === 'asc' ? aValue - bValue : bValue - aValue;
    });
});

const parseTimeValue = (value) => {
    if (!value) return null;
    const date = new Date(value);
    if (Number.isNaN(date.getTime())) {
        return null;
    }
    return date;
};

const orderedCommentList = computed(() => {
    const list = [...(commentData.value?.list || [])];
    return list.sort((a, b) => {
        const aTime = parseTimeValue(a?.createTime)?.getTime() || 0;
        const bTime = parseTimeValue(b?.createTime)?.getTime() || 0;
        return bTime - aTime;
    });
});

const commentCurrentPage = computed(() => Math.max(1, Number(commentData.value?.pageNum) || 1));
const commentPageSize = computed(() => Math.max(1, Number(commentData.value?.pageSize) || COMMENT_PAGE_SIZE));
const commentPageCount = computed(() => {
    const total = Math.max(0, Number(commentData.value?.total) || 0);
    return Math.max(1, Math.ceil(total / commentPageSize.value));
});

const formatCommentTime = (value) => {
    const date = parseTimeValue(value);
    if (!date) return '时间未知';
    return date.toLocaleString('zh-CN', {
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
    });
};

const loadPlaza = async () => {
    loading.value = true;
    message.value = '';
    try {
        const resp = await plazaList({
            keyword: searchKeyword.value,
            sortBy: sortField.value,
            sortOrder: sortOrder.value,
            pageNum: 1,
            pageSize: 10
        });
        const data = pickData(resp) || {};
        plazaItems.value = data.list || [];
    } catch (error) {
        notifyAppError(error, '获取广场失败');
    } finally {
        loading.value = false;
    }
};

const loadCommentDetail = async (plazaId, pageNum = 1) => {
    const safePageNum = Math.max(1, Number(pageNum) || 1);
    const resp = await plazaCommentArea({ plazaId, pageNum: safePageNum, pageSize: COMMENT_PAGE_SIZE });
    const data = pickData(resp) || {};
    commentData.value = {
        ...data,
        list: Array.isArray(data.list) ? data.list : [],
        total: Math.max(0, Number(data.total) || 0),
        pageNum: Math.max(1, Number(data.pageNum) || safePageNum),
        pageSize: Math.max(1, Number(data.pageSize) || COMMENT_PAGE_SIZE)
    };
    const target = plazaItems.value.find((item) => item.plazaId === plazaId);
    if (target) {
        target.commentCount = Number(commentData.value.total || 0);
        target.commented = Number(commentData.value.total || 0) > 0;
    }
};

const doLike = async (item) => {
    if (!item) {
        return;
    }
    const currentLiked = Boolean(item.liked);
    try {
        if (currentLiked) {
            await plazaDislike({ plazaId: item.plazaId });
        } else {
            await plazaLike({ plazaId: item.plazaId });
        }
        item.likeCount = Math.max(0, (item.likeCount || 0) + (currentLiked ? -1 : 1));
        item.liked = !currentLiked;
    } catch (error) {
        notifyAppError(error, '点赞失败');
    }
};

const doFavor = async (item) => {
    if (!item) {
        return;
    }
    const currentFavored = Boolean(item.favored);
    try {
        if (currentFavored) {
            await plazaDisfavor({ plazaId: item.plazaId });
        } else {
            await plazaFavor({ plazaId: item.plazaId });
        }
        item.favorCount = Math.max(0, (item.favorCount || 0) + (currentFavored ? -1 : 1));
        item.favored = !currentFavored;
    } catch (error) {
        notifyAppError(error, '收藏失败');
    }
};

const openComment = async (item) => {
    if (!item) {
        return;
    }
    commentOpen.value = true;
    commentForm.plazaId = item.plazaId;
    commentForm.commentContent = '';
    currentCommentItem.value = item;
    commentData.value = { list: [], total: 0, pageNum: 1, pageSize: COMMENT_PAGE_SIZE };
    try {
        await loadCommentDetail(item.plazaId, 1);
    } catch (error) {
        notifyAppError(error, '加载评论失败');
    }
};

const doFork = (item) => {
    openDetail(item);
};

const openDetail = (item) => {
    const templateId = (item?.templateId || '').toString().trim();
    if (!templateId) {
        message.value = '该条数据缺少 templateId，暂时无法查看详情';
        return;
    }
    router.push(`/detail/${encodeURIComponent(templateId)}`);
};

const doComment = async () => {
    const content = commentForm.commentContent.trim();
    if (!commentForm.plazaId || !content) {
        return;
    }
    if (content.length > COMMENT_CONTENT_LIMIT) {
        message.value = `评论最多 ${COMMENT_CONTENT_LIMIT} 字`;
        return;
    }
    try {
        await plazaComment({ ...commentForm, commentContent: content });
        await loadCommentDetail(commentForm.plazaId, 1);
        const target = plazaItems.value.find((item) => item.plazaId === commentForm.plazaId);
        if (target) {
            target.commented = true;
        }
        commentForm.commentContent = '';
    } catch (error) {
        notifyAppError(error, '评论失败');
    }
};

const doDeleteComment = async (comment) => {
    if (!comment?.mine || !comment?.commentId || !commentForm.plazaId) {
        return;
    }
    try {
        await plazaDiscomment({ plazaId: commentForm.plazaId, commentId: comment.commentId });
        const nextTotal = Math.max(0, Number(commentData.value.total || 0) - 1);
        const maxPage = Math.max(1, Math.ceil(nextTotal / commentPageSize.value));
        await loadCommentDetail(commentForm.plazaId, Math.min(commentCurrentPage.value, maxPage));
    } catch (error) {
        notifyAppError(error, '删除评论失败');
    }
};

const onCommentInput = () => {
    if (commentForm.commentContent.length > COMMENT_CONTENT_LIMIT) {
        commentForm.commentContent = commentForm.commentContent.slice(0, COMMENT_CONTENT_LIMIT);
    }
};

const changeCommentPage = async (step) => {
    if (!commentForm.plazaId) {
        return;
    }
    const nextPage = commentCurrentPage.value + step;
    if (nextPage < 1 || nextPage > commentPageCount.value) {
        return;
    }
    try {
        await loadCommentDetail(commentForm.plazaId, nextPage);
    } catch (error) {
        notifyAppError(error, '加载评论失败');
    }
};

const applySearch = () => {
    searchKeyword.value = (searchText.value || '').trim();
    void loadPlaza();
};

const toggleSortOrder = () => {
    sortOrder.value = sortOrder.value === 'desc' ? 'asc' : 'desc';
    void loadPlaza();
};

const selectSortField = (field) => {
    sortField.value = field;
    filterPopoverOpen.value = false;
    void loadPlaza();
};

const clearFilterPopoverCloseTimer = () => {
    if (filterPopoverCloseTimer) {
        clearTimeout(filterPopoverCloseTimer);
        filterPopoverCloseTimer = null;
    }
};

const openFilterPopover = () => {
    clearFilterPopoverCloseTimer();
    filterPopoverOpen.value = true;
};

const scheduleCloseFilterPopover = () => {
    clearFilterPopoverCloseTimer();
    filterPopoverCloseTimer = setTimeout(() => {
        filterPopoverOpen.value = false;
        filterPopoverCloseTimer = null;
    }, 180);
};

onMounted(async () => {
    await loadPlaza();
});

onBeforeUnmount(() => {
    clearFilterPopoverCloseTimer();
});

</script>

<template>
    <section class="grid h-screen grid-rows-[1fr_var(--footer-height)] bg-white">
        <div class="overflow-y-auto py-[24px] pl-[24px] pr-[calc(24px+var(--scrollbar-w))]">
            <div class="mx-auto max-w-[1100px] space-y-[16px]">
                <div class="flex items-center justify-between gap-[12px]">
                    <h1 class="text-[24px] font-bold">MiniAgent Plaza</h1>
                </div>

                <div class="flex w-full max-w-[560px] items-center gap-[8px]">
                    <input
                        v-model="searchText"
                        class="min-w-0 flex-1 rounded-[999px] border border-[var(--border-color)] bg-white px-[14px] py-[9px] text-[14px] text-[#0f172a] outline-none placeholder:text-[#94a3b8]"
                        placeholder="搜索 MiniAgent（标题 / 描述 / 作者 / 类型）"
                        @keydown.enter.prevent="applySearch"
                    />
                    <button
                        class="inline-flex h-[36px] w-[36px] items-center justify-center rounded-[10px] border border-[var(--border-color)] text-[#64748b] transition hover:bg-[#eef2f7] hover:text-[#334155]"
                        title="搜索"
                        aria-label="搜索"
                        @click="applySearch"
                    >
                        <svg viewBox="0 0 24 24" class="h-[16px] w-[16px]" fill="none" stroke="currentColor" stroke-width="1.8" aria-hidden="true">
                            <circle cx="11" cy="11" r="7" />
                            <path d="m20 20-3.6-3.6" />
                        </svg>
                    </button>
                    <button
                        class="inline-flex h-[36px] w-[36px] items-center justify-center rounded-[10px] border transition"
                        :class="sortOrder === 'desc' ? 'border-[#94a3b8] bg-[#edf2f7] text-[#334155]' : 'border-[#cbd5e1] bg-white text-[#64748b]'"
                        :title="sortOrder === 'desc' ? '当前：降序' : '当前：升序'"
                        :aria-label="sortOrder === 'desc' ? '当前降序，点击切换升序' : '当前升序，点击切换降序'"
                        @click="toggleSortOrder"
                    >
                        <svg v-if="sortOrder === 'desc'" viewBox="0 0 24 24" class="h-[16px] w-[16px]" fill="none" stroke="currentColor" stroke-width="1.8" aria-hidden="true">
                            <path d="M8 7v10" />
                            <path d="m5 14 3 3 3-3" />
                            <path d="M13 8h6" />
                            <path d="M13 12h4" />
                            <path d="M13 16h2" />
                        </svg>
                        <svg v-else viewBox="0 0 24 24" class="h-[16px] w-[16px]" fill="none" stroke="currentColor" stroke-width="1.8" aria-hidden="true">
                            <path d="M8 17V7" />
                            <path d="m5 10 3-3 3 3" />
                            <path d="M13 8h2" />
                            <path d="M13 12h4" />
                            <path d="M13 16h6" />
                        </svg>
                    </button>
                    <div
                        class="relative"
                        @mouseenter="openFilterPopover"
                        @mouseleave="scheduleCloseFilterPopover"
                    >
                        <button
                            class="inline-flex h-[36px] w-[36px] items-center justify-center rounded-[10px] border border-[var(--border-color)] text-[#64748b] transition hover:bg-[#eef2f7] hover:text-[#334155]"
                            title="排序字段"
                            aria-label="排序字段"
                        >
                            <svg viewBox="0 0 24 24" class="h-[16px] w-[16px]" fill="none" stroke="currentColor" stroke-width="1.8" aria-hidden="true">
                                <path d="M4 6h16" />
                                <path d="M7 12h10" />
                                <path d="M10 18h4" />
                            </svg>
                        </button>
                        <div
                            v-if="filterPopoverOpen"
                            class="absolute left-full top-1/2 z-[12] ml-[8px] -translate-y-1/2 rounded-[10px] border border-[var(--border-color)] bg-white p-[6px] shadow-[0_10px_24px_rgba(15,23,42,0.12)]"
                            @mouseenter="openFilterPopover"
                            @mouseleave="scheduleCloseFilterPopover"
                        >
                            <div class="flex items-center gap-[6px]">
                                <button
                                    v-for="option in sortFieldOptions"
                                    :key="option.value"
                                    class="inline-flex whitespace-nowrap rounded-[8px] px-[10px] py-[6px] text-[13px] transition"
                                    :class="sortField === option.value ? 'bg-[#e9f1ff] text-[#2b5fb8] font-semibold' : 'text-[#475569] hover:bg-[#f1f5f9]'"
                                    @click="selectSortField(option.value)"
                                >
                                    {{ option.label }}
                                </button>
                            </div>
                        </div>
                    </div>
                </div>

                <div v-if="message" class="text-[13px] text-[var(--text-secondary)]">{{ message }}</div>

                <div class="grid gap-[14px] sm:grid-cols-2 xl:grid-cols-3">
                    <article
                        v-for="item in filteredItems"
                        :key="item.plazaId"
                        class="group relative overflow-hidden rounded-[18px] border border-[var(--border-color)] bg-[var(--surface-1)] px-[14px] py-[12px] shadow-[0_14px_30px_rgba(15,23,42,0.07),inset_0_1px_0_rgba(255,255,255,0.32)] transition-all duration-300 hover:-translate-y-[3px] hover:shadow-[0_22px_42px_rgba(15,23,42,0.14)]"
                    >
                        <div class="pointer-events-none absolute inset-0" :style="{ backgroundImage: resolveCardTone(item).overlay }"></div>
                        <div
                            class="pointer-events-none absolute right-[14px] top-[12px] inline-flex rounded-[999px] border px-[9px] py-[4px] text-[11px] font-semibold tracking-[0.03em] shadow-[inset_0_1px_0_rgba(255,255,255,0.45)]"
                            :style="{
                                borderColor: resolveCardTone(item).badgeBorder,
                                backgroundColor: resolveCardTone(item).badgeBg,
                                color: resolveCardTone(item).badgeText
                            }"
                        >
                            {{ displayType(item) }}
                        </div>
                        <div class="relative space-y-[10px] pr-[78px]">
                            <button class="block w-full pt-[2px] text-left text-[18px] font-bold leading-[1.35] text-[var(--text-primary)]" @click="openDetail(item)">
                                {{ item.plazaTitle }}
                            </button>
                            <button
                                class="min-h-[38px] w-full overflow-hidden text-left text-[13px] leading-[1.45] text-[var(--text-secondary)] [display:-webkit-box] [-webkit-box-orient:vertical] [-webkit-line-clamp:2]"
                                @click="openDetail(item)"
                            >
                                {{ item.plazaDesc || '这个 MiniAgent 还没有补充描述，点进详情查看能力与评论。' }}
                            </button>
                            <div class="text-[12px] text-[var(--text-secondary)]">
                                作者：{{ displayAuthor(item) }}
                            </div>
                            <div class="-mr-[78px] flex items-center justify-between pt-[2px]">
                                <div class="flex items-center gap-[10px]">
                                    <button
                                        class="inline-flex items-center gap-[5px] rounded-[8px] px-[6px] py-[4px] text-[13px] font-semibold hover:bg-[#f8fafc]"
                                        :style="{ color: resolveIconColor(item.liked, '#ef4444') }"
                                        @click="doLike(item)"
                                    >
                                        <svg viewBox="0 0 24 24" class="h-[16px] w-[16px]" fill="currentColor" aria-hidden="true">
                                            <path d="M12 21.35 10.55 20.03C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09A5.96 5.96 0 0 1 16.5 3C19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54z"/>
                                        </svg>
                                        <span class="inline-block min-w-[2ch]" style="font-variant-numeric: tabular-nums;">{{ item.likeCount || 0 }}</span>
                                    </button>
                                    <button
                                        class="inline-flex items-center gap-[5px] rounded-[8px] px-[6px] py-[4px] text-[13px] font-semibold hover:bg-[#f8fafc]"
                                        :style="{ color: resolveIconColor(item.favored, '#f59e0b') }"
                                        @click="doFavor(item)"
                                    >
                                        <svg viewBox="0 0 24 24" class="h-[16px] w-[16px]" fill="currentColor" aria-hidden="true">
                                            <path d="m12 2.7 2.86 5.79 6.4.93-4.63 4.51 1.09 6.37L12 17.28l-5.72 3 1.09-6.37-4.63-4.51 6.4-.93L12 2.7z"/>
                                        </svg>
                                        <span class="inline-block min-w-[2ch]" style="font-variant-numeric: tabular-nums;">{{ item.favorCount || 0 }}</span>
                                    </button>
                                    <button
                                        class="inline-flex items-center gap-[5px] rounded-[8px] px-[6px] py-[4px] text-[13px] font-semibold hover:bg-[#f8fafc]"
                                        :style="{ color: resolveIconColor(isCommented(item), '#3b82f6') }"
                                        @click="openComment(item)"
                                    >
                                        <svg viewBox="0 0 24 24" class="h-[16px] w-[16px]" fill="currentColor" aria-hidden="true">
                                            <path d="M20 2H4a2 2 0 0 0-2 2v18l4-4h14a2 2 0 0 0 2-2V4a2 2 0 0 0-2-2z"/>
                                        </svg>
                                        <span class="inline-block min-w-[2ch]" style="font-variant-numeric: tabular-nums;">{{ item.commentCount || 0 }}</span>
                                    </button>
                                </div>
                                <div class="ml-[16px] inline-flex items-center gap-[12px]">
                                    <button
                                        class="inline-flex h-[34px] items-center gap-[6px] rounded-[10px] border border-[var(--fork-border)] bg-[var(--fork-bg)] px-[12px] text-[13px] font-semibold text-[var(--fork-text)] transition hover:border-[var(--fork-text)] hover:bg-[var(--fork-text)] hover:text-white"
                                        :style="{
                                            '--fork-border': resolveCardTone(item).forkBorder,
                                            '--fork-bg': resolveCardTone(item).forkBg,
                                            '--fork-text': resolveCardTone(item).forkText
                                        }"
                                        @click="doFork(item)"
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
                                        <span>Fork</span>
                                    </button>
                                </div>
                            </div>
                        </div>
                    </article>
                </div>

                <div v-if="!loading && filteredItems.length === 0" class="text-[13px] text-[var(--text-secondary)]">暂无数据</div>

                <div v-if="loading" class="text-[13px] text-[var(--text-secondary)]">加载中...</div>
            </div>
        </div>

        <Footer />

        <div v-if="commentOpen" class="fixed inset-0 z-[30] grid place-items-center bg-[rgba(0,0,0,0.35)] p-[20px]" @click.self="commentOpen=false">
            <div class="flex h-[min(82vh,680px)] w-full max-w-[980px] flex-col gap-[12px] rounded-[14px] border border-[var(--border-color)] bg-white p-[16px]">
                <div class="flex items-center justify-between">
                    <div class="text-[16px] font-semibold">{{ currentCommentItem?.plazaTitle || '评论区' }}</div>
                    <button class="text-[20px]" @click="commentOpen=false">×</button>
                </div>
                <div class="text-[13px] text-[var(--text-secondary)]">{{ currentCommentItem?.plazaDesc }}</div>
                <div class="flex gap-[8px]">
                    <input
                        v-model="commentForm.commentContent"
                        maxlength="128"
                        class="flex-1 rounded-[10px] border border-[var(--border-color)] px-[10px] py-[8px] text-[13px]"
                        placeholder="写评论..."
                        @input="onCommentInput"
                    />
                    <button class="rounded-[10px] border border-[var(--accent-color)] bg-[var(--accent-color)] px-[12px] py-[8px] text-[13px] text-white" @click="doComment">发送</button>
                </div>
                <div class="flex items-center justify-between gap-[10px] text-[12px] text-[var(--text-secondary)]">
                    <div>评论共 {{ commentData?.total || 0 }} 条</div>
                    <div class="flex items-center gap-[8px]">
                        <span>第 {{ commentCurrentPage }} / {{ commentPageCount }} 页</span>
                        <button
                            class="rounded-[8px] border border-[var(--border-color)] px-[8px] py-[4px] text-[12px] text-[#334155] transition hover:bg-[#f1f5f9] disabled:cursor-not-allowed disabled:opacity-50 disabled:hover:bg-transparent"
                            :disabled="commentCurrentPage <= 1"
                            @click="changeCommentPage(-1)"
                        >
                            上一页
                        </button>
                        <button
                            class="rounded-[8px] border border-[var(--border-color)] px-[8px] py-[4px] text-[12px] text-[#334155] transition hover:bg-[#f1f5f9] disabled:cursor-not-allowed disabled:opacity-50 disabled:hover:bg-transparent"
                            :disabled="commentCurrentPage >= commentPageCount"
                            @click="changeCommentPage(1)"
                        >
                            下一页
                        </button>
                    </div>
                </div>
                <div class="min-h-0 flex-1 overflow-y-auto rounded-[12px] border border-[var(--border-color)] bg-[#f8fafc]">
                    <div v-for="item in orderedCommentList" :key="item.commentId" class="flex gap-[10px] border-b border-[var(--border-color)] px-[12px] py-[10px] last:border-b-0">
                        <div class="grid h-[30px] w-[30px] shrink-0 place-items-center rounded-full bg-white text-[12px] font-semibold text-[#334155]">
                            {{ (item.userName || '?').slice(0, 1).toUpperCase() }}
                        </div>
                        <div class="min-w-0 flex-1">
                            <div class="flex items-center justify-between gap-[8px]">
                                <div class="truncate text-[13px] font-semibold text-[#0f172a]">{{ item.userName }}</div>
                                <div class="inline-flex shrink-0 items-center gap-[4px]">
                                    <span class="inline-flex h-[22px] w-[22px] items-center justify-center">
                                        <button
                                            v-if="item.mine"
                                            class="inline-flex h-[20px] w-[20px] items-center justify-center rounded-[6px] text-[#94a3b8] transition hover:bg-[#fee2e2] hover:text-[#dc2626]"
                                            title="删除"
                                            @click="doDeleteComment(item)"
                                        >
                                            <svg viewBox="0 0 24 24" class="h-[14px] w-[14px]" fill="currentColor" aria-hidden="true">
                                                <path d="M9 3h6l1 2h4v2H4V5h4l1-2zm1 7h2v8h-2v-8zm4 0h2v8h-2v-8zM7 10h2v8H7v-8zm-1 11h12a2 2 0 0 0 2-2V8H4v11a2 2 0 0 0 2 2z"/>
                                            </svg>
                                        </button>
                                    </span>
                                    <div class="text-[12px] text-[var(--text-secondary)]">{{ formatCommentTime(item.createTime) }}</div>
                                </div>
                            </div>
                            <div class="mt-[4px] break-words text-[15px] leading-[1.45] text-[#1e293b]">{{ item.commentContent }}</div>
                        </div>
                    </div>
                    <div v-if="orderedCommentList.length === 0" class="px-[12px] py-[16px] text-[13px] text-[var(--text-secondary)]">暂无评论</div>
                </div>
            </div>
        </div>
    </section>
</template>
