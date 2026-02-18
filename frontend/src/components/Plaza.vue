<script setup>
import { computed, onMounted, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { plazaComment, plazaDetail, plazaFavor, plazaLike, plazaList, repoFork } from '../request/api';
import { normalizeError } from '../request/request';
import AppFooter from './AppFooter.vue';

const router = useRouter();
const loading = ref(false);
const message = ref('');
const plazaItems = ref([]);
const detailData = ref({});
const commentData = ref(null);
const detailOpen = ref(false);
const commentOpen = ref(false);
const searchKeyword = ref('');

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

const displayAuthor = (item) => item?.username || '未知用户';
const displayType = (item) => (item?.agentType || 'react').toUpperCase();
const isCommented = (item) => Boolean(item?.commented);

const resolveIconColor = (active, color) => (active ? color : '#94a3b8');

const filteredItems = computed(() => {
    const keyword = searchKeyword.value.trim().toLowerCase();
    if (!keyword) return plazaItems.value;
    return plazaItems.value.filter((item) => {
        const haystack = [
            item?.plazaTitle || '',
            item?.plazaDesc || '',
            item?.agentType || '',
            item?.username || ''
        ]
            .join(' ')
            .toLowerCase();
        return haystack.includes(keyword);
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
    const list = [...(commentData.value?.commentList || [])];
    return list.sort((a, b) => {
        const aTime = parseTimeValue(a?.createTime)?.getTime() || 0;
        const bTime = parseTimeValue(b?.createTime)?.getTime() || 0;
        return aTime - bTime;
    });
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
    try {
        const resp = await plazaList({ pageNum: 1, pageSize: 30 });
        const data = pickData(resp) || {};
        plazaItems.value = data.list || [];
    } catch (error) {
        message.value = normalizeError(error).message || '获取广场失败';
    } finally {
        loading.value = false;
    }
};

const syncCardStateByDetail = (plazaItem) => {
    const plazaId = plazaItem?.plazaId;
    if (!plazaId) return;

    const target = plazaItems.value.find((item) => item.plazaId === plazaId);
    if (target) {
        if (typeof plazaItem.commentCount === 'number') {
            target.commentCount = plazaItem.commentCount;
        }
        if (typeof plazaItem.commented === 'boolean') {
            target.commented = plazaItem.commented;
        }
        if (typeof plazaItem.liked === 'boolean') {
            target.liked = plazaItem.liked;
        }
        if (typeof plazaItem.favored === 'boolean') {
            target.favored = plazaItem.favored;
        }
    }
};

const loadCommentDetail = async (plazaId) => {
    const resp = await plazaDetail(plazaId);
    const data = pickData(resp) || { plazaItem: {}, commentList: [] };
    commentData.value = data;
    syncCardStateByDetail(data?.plazaItem);
};

const doLike = async (item) => {
    if (!item) {
        return;
    }
    const currentLiked = Boolean(item.liked);
    try {
        await plazaLike({ plazaId: item.plazaId });
        item.likeCount = Math.max(0, (item.likeCount || 0) + (currentLiked ? -1 : 1));
        item.liked = !currentLiked;
    } catch (error) {
        message.value = normalizeError(error).message || '点赞失败';
    }
};

const doFavor = async (item) => {
    if (!item) {
        return;
    }
    const currentFavored = Boolean(item.favored);
    try {
        await plazaFavor({ plazaId: item.plazaId });
        item.favorCount = Math.max(0, (item.favorCount || 0) + (currentFavored ? -1 : 1));
        item.favored = !currentFavored;
    } catch (error) {
        message.value = normalizeError(error).message || '收藏失败';
    }
};

const openComment = async (item) => {
    if (!item) {
        return;
    }
    commentOpen.value = true;
    commentForm.plazaId = item.plazaId;
    commentForm.commentContent = '';
    commentData.value = {
        plazaItem: item,
        commentList: []
    };
    try {
        await loadCommentDetail(item.plazaId);
    } catch (error) {
        message.value = normalizeError(error).message || '加载评论失败';
    }
};

const doFork = async (plazaId) => {
    try {
        await repoFork({ plazaId });
    } catch (error) {
        message.value = normalizeError(error).message || 'Fork 失败';
    }
};

const openDetail = (item) => {
    detailData.value = item || {};
    detailOpen.value = true;
};

const doComment = async () => {
    if (!commentForm.plazaId || !commentForm.commentContent.trim()) {
        return;
    }
    try {
        await plazaComment(commentForm);
        await loadCommentDetail(commentForm.plazaId);
        const target = plazaItems.value.find((item) => item.plazaId === commentForm.plazaId);
        if (target) {
            target.commented = true;
        }
        commentForm.commentContent = '';
    } catch (error) {
        message.value = normalizeError(error).message || '评论失败';
    }
};

onMounted(async () => {
    await loadPlaza();
});

const goRepository = () => {
    router.push('/repository');
};
</script>

<template>
    <section class="grid h-screen grid-rows-[1fr_var(--footer-height)] bg-white">
        <div class="overflow-y-auto py-[24px] pl-[24px] pr-[calc(24px+var(--scrollbar-w))]">
            <div class="mx-auto max-w-[1100px] space-y-[16px]">
                <div class="flex items-center justify-between gap-[12px]">
                    <h1 class="text-[24px] font-bold">MiniAgent Plaza</h1>
                    <button
                        class="rounded-[10px] border border-[var(--border-color)] bg-white px-[12px] py-[8px] text-[13px] font-semibold text-[#334155] transition hover:border-[var(--accent-color)] hover:text-[var(--accent-color)]"
                        @click="goRepository"
                    >
                        我的仓库
                    </button>
                </div>

                <input
                    v-model="searchKeyword"
                    class="w-full rounded-[999px] border border-[var(--border-color)] bg-white px-[16px] py-[10px] text-[14px] text-[#0f172a] outline-none placeholder:text-[#94a3b8]"
                    placeholder="搜索 MiniAgent（标题 / 描述 / 作者 / 类型）"
                />

                <div v-if="message" class="text-[13px] text-[var(--text-secondary)]">{{ message }}</div>

                <div class="grid gap-[14px] sm:grid-cols-2 xl:grid-cols-3">
                    <article
                        v-for="item in filteredItems"
                        :key="item.plazaId"
                        class="group relative overflow-hidden rounded-[16px] border border-[var(--border-color)] bg-white px-[14px] py-[12px] shadow-[0_10px_24px_rgba(15,23,42,0.06)] transition-all duration-200 hover:-translate-y-[2px] hover:shadow-[0_16px_36px_rgba(15,23,42,0.1)]"
                    >
                        <div class="pointer-events-none absolute right-[14px] top-[12px] inline-flex rounded-[999px] border border-[var(--border-color)] bg-[#f8fafc] px-[8px] py-[3px] text-[11px] font-semibold text-[#334155]">
                            {{ displayType(item) }}
                        </div>
                        <div class="space-y-[10px] pr-[78px]">
                            <button class="block w-full pt-[2px] text-left text-[18px] font-bold leading-[1.35] text-[#0f172a]" @click="openDetail(item)">
                                {{ item.plazaTitle }}
                            </button>
                            <button class="min-h-[38px] w-full text-left text-[13px] leading-[1.45] text-[var(--text-secondary)]" @click="openDetail(item)">
                                {{ item.plazaDesc || '这个 MiniAgent 还没有补充描述，点进详情查看能力与评论。' }}
                            </button>
                            <div class="text-[12px] text-[var(--text-secondary)]">
                                Author: {{ displayAuthor(item) }}
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
                                            <path d="M17 3H7a2 2 0 0 0-2 2v16l7-3 7 3V5a2 2 0 0 0-2-2z"/>
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
                                        class="inline-flex h-[34px] items-center gap-[6px] rounded-[10px] border border-[rgba(59,130,246,0.5)] bg-[rgba(59,130,246,0.12)] px-[12px] text-[13px] font-semibold text-[var(--accent-color)] transition hover:border-[var(--accent-color)] hover:bg-[var(--accent-color)] hover:text-white"
                                        @click="doFork(item.plazaId)"
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

        <AppFooter />

        <div v-if="detailOpen" class="fixed inset-0 z-[20] grid place-items-center bg-[rgba(0,0,0,0.35)] p-[20px]" @click.self="detailOpen=false">
            <div class="w-full max-w-[760px] space-y-[12px] rounded-[14px] border border-[var(--border-color)] bg-white p-[16px]">
                <div class="flex items-center justify-between">
                    <div class="text-[16px] font-semibold">{{ detailData?.plazaTitle || '详情' }}</div>
                    <button class="text-[20px]" @click="detailOpen=false">×</button>
                </div>
                <div class="rounded-[10px] border border-[var(--border-color)] bg-[#f8fafc] px-[12px] py-[18px] text-[14px] text-[var(--text-secondary)]">
                    详情页暂未开放，后续补充。
                </div>
            </div>
        </div>

        <div v-if="commentOpen" class="fixed inset-0 z-[30] grid place-items-center bg-[rgba(0,0,0,0.35)] p-[20px]" @click.self="commentOpen=false">
            <div class="w-full max-w-[980px] space-y-[12px] rounded-[14px] border border-[var(--border-color)] bg-white p-[16px]">
                <div class="flex items-center justify-between">
                    <div class="text-[16px] font-semibold">{{ commentData?.plazaItem?.plazaTitle || '评论区' }}</div>
                    <button class="text-[20px]" @click="commentOpen=false">×</button>
                </div>
                <div class="text-[13px] text-[var(--text-secondary)]">{{ commentData?.plazaItem?.plazaDesc }}</div>
                <div class="flex gap-[8px]">
                    <input v-model="commentForm.commentContent" class="flex-1 rounded-[10px] border border-[var(--border-color)] px-[10px] py-[8px] text-[13px]" placeholder="写评论..." />
                    <button class="rounded-[10px] border border-[var(--accent-color)] bg-[var(--accent-color)] px-[12px] py-[8px] text-[13px] text-white" @click="doComment">发送</button>
                </div>
                <div class="text-[12px] text-[var(--text-secondary)]">评论 {{ orderedCommentList.length }} 条</div>
                <div class="max-h-[340px] overflow-y-auto rounded-[12px] border border-[var(--border-color)] bg-[#f8fafc]">
                    <div v-for="item in orderedCommentList" :key="item.commentId" class="flex gap-[10px] border-b border-[var(--border-color)] px-[12px] py-[10px] last:border-b-0">
                        <div class="grid h-[30px] w-[30px] shrink-0 place-items-center rounded-full bg-white text-[12px] font-semibold text-[#334155]">
                            {{ (item.username || '?').slice(0, 1).toUpperCase() }}
                        </div>
                        <div class="min-w-0 flex-1">
                            <div class="flex items-center justify-between gap-[8px]">
                                <div class="truncate text-[13px] font-semibold text-[#0f172a]">{{ item.username }}</div>
                                <div class="shrink-0 text-[12px] text-[var(--text-secondary)]">{{ formatCommentTime(item.createTime) }}</div>
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
