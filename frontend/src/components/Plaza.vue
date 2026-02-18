<script setup>
import { computed, onMounted, reactive, ref } from 'vue';
import { plazaComment, plazaCommentCount, plazaDetail, plazaFavor, plazaLike, plazaList, repoFork } from '../request/api';
import { normalizeError } from '../request/request';
import AppFooter from './AppFooter.vue';

const loading = ref(false);
const message = ref('');
const plazaItems = ref([]);
const detailData = ref(null);
const detailOpen = ref(false);
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

const doLike = async (item) => {
    if (!item || item.liked) {
        return;
    }
    try {
        await plazaLike({ plazaId: item.plazaId });
        item.likeCount = (item.likeCount || 0) + 1;
        item.liked = true;
    } catch (error) {
        message.value = normalizeError(error).message || '点赞失败';
    }
};

const doFavor = async (item) => {
    if (!item || item.favored) {
        return;
    }
    try {
        await plazaFavor({ plazaId: item.plazaId });
        item.favorCount = (item.favorCount || 0) + 1;
        item.favored = true;
    } catch (error) {
        message.value = normalizeError(error).message || '收藏失败';
    }
};

const doCommentCount = async (item) => {
    if (!item) {
        return;
    }
    try {
        await plazaCommentCount({ plazaId: item.plazaId });
        item.commentCount = (item.commentCount || 0) + 1;
    } catch (error) {
        message.value = normalizeError(error).message || '评论计数失败';
    }
};

const doFork = async (plazaId) => {
    try {
        await repoFork({ plazaId });
        message.value = '已加入仓库';
    } catch (error) {
        message.value = normalizeError(error).message || 'Fork 失败';
    }
};

const openDetail = async (plazaId) => {
    try {
        const resp = await plazaDetail(plazaId);
        detailData.value = pickData(resp);
        detailOpen.value = true;
        commentForm.plazaId = plazaId;
        commentForm.commentContent = '';
    } catch (error) {
        message.value = normalizeError(error).message || '加载详情失败';
    }
};

const doComment = async () => {
    if (!commentForm.plazaId || !commentForm.commentContent.trim()) {
        return;
    }
    try {
        await plazaComment(commentForm);
        await openDetail(commentForm.plazaId);
        await loadPlaza();
    } catch (error) {
        message.value = normalizeError(error).message || '评论失败';
    }
};

onMounted(async () => {
    await loadPlaza();
});
</script>

<template>
    <section class="grid h-screen grid-rows-[1fr_var(--footer-height)] bg-white">
        <div class="overflow-y-auto py-[24px] pl-[24px] pr-[calc(24px+var(--scrollbar-w))]">
            <div class="mx-auto max-w-[1100px] space-y-[16px]">
                <h1 class="text-[24px] font-bold">MiniAgent Plaza</h1>

                <input
                    v-model="searchKeyword"
                    class="w-full rounded-[10px] border border-[var(--border-color)] bg-white px-[12px] py-[10px] text-[14px]"
                    placeholder="搜索 MiniAgent（标题 / 描述 / 作者 / 类型）"
                />

                <div v-if="message" class="text-[13px] text-[var(--text-secondary)]">{{ message }}</div>

                <div class="grid gap-[14px] sm:grid-cols-2 xl:grid-cols-3">
                    <article
                        v-for="item in filteredItems"
                        :key="item.plazaId"
                        class="group overflow-hidden rounded-[16px] border border-[var(--border-color)] bg-white shadow-[0_10px_24px_rgba(15,23,42,0.06)] transition-all duration-200 hover:-translate-y-[2px] hover:shadow-[0_16px_36px_rgba(15,23,42,0.1)]"
                    >
                        <div class="space-y-[10px] px-[14px] py-[12px]">
                            <div class="inline-flex rounded-[999px] border border-[var(--border-color)] bg-[#f8fafc] px-[8px] py-[3px] text-[11px] font-semibold text-[#334155]">
                                {{ displayType(item) }}
                            </div>
                            <button class="block w-full text-left text-[18px] font-bold leading-[1.35] text-[#0f172a]" @click="openDetail(item.plazaId)">
                                {{ item.plazaTitle }}
                            </button>
                            <button class="min-h-[38px] w-full text-left text-[13px] leading-[1.45] text-[var(--text-secondary)]" @click="openDetail(item.plazaId)">
                                {{ item.plazaDesc || '这个 MiniAgent 还没有补充描述，点进详情查看能力与评论。' }}
                            </button>
                            <div class="text-[12px] text-[var(--text-secondary)]">
                                Author: {{ displayAuthor(item) }}
                            </div>
                            <div class="flex items-center justify-between">
                                <button
                                    class="inline-flex items-center gap-[6px] rounded-[8px] px-[6px] py-[4px] text-[13px] font-semibold text-[#ef4444] hover:bg-[#fef2f2] disabled:cursor-not-allowed disabled:opacity-60"
                                    :disabled="item.liked"
                                    @click="doLike(item)"
                                >
                                    <svg viewBox="0 0 24 24" class="h-[16px] w-[16px]" fill="currentColor" aria-hidden="true">
                                        <path d="M12 21.35 10.55 20.03C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09A5.96 5.96 0 0 1 16.5 3C19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54z"/>
                                    </svg>
                                    <span>{{ item.likeCount || 0 }}</span>
                                </button>
                                <button
                                    class="inline-flex items-center gap-[6px] rounded-[8px] px-[6px] py-[4px] text-[13px] font-semibold text-[#f59e0b] hover:bg-[#fffbeb] disabled:cursor-not-allowed disabled:opacity-60"
                                    :disabled="item.favored"
                                    @click="doFavor(item)"
                                >
                                    <svg viewBox="0 0 24 24" class="h-[16px] w-[16px]" fill="currentColor" aria-hidden="true">
                                        <path d="M17 3H7a2 2 0 0 0-2 2v16l7-3 7 3V5a2 2 0 0 0-2-2z"/>
                                    </svg>
                                    <span>{{ item.favorCount || 0 }}</span>
                                </button>
                                <button
                                    class="inline-flex items-center gap-[6px] rounded-[8px] px-[6px] py-[4px] text-[13px] font-semibold text-[#3b82f6] hover:bg-[#eff6ff]"
                                    @click="doCommentCount(item)"
                                >
                                    <svg viewBox="0 0 24 24" class="h-[16px] w-[16px]" fill="currentColor" aria-hidden="true">
                                        <path d="M20 2H4a2 2 0 0 0-2 2v18l4-4h14a2 2 0 0 0 2-2V4a2 2 0 0 0-2-2z"/>
                                    </svg>
                                    <span>{{ item.commentCount || 0 }}</span>
                                </button>
                            </div>
                        </div>
                        <div class="flex items-center justify-end border-t border-[var(--border-color)] bg-white px-[10px] py-[8px]">
                            <button class="rounded-[8px] border border-[var(--accent-color)] bg-[var(--accent-color)] px-[12px] py-[6px] text-[12px] font-semibold text-white" @click="doFork(item.plazaId)">Fork</button>
                        </div>
                    </article>
                </div>

                <div v-if="!loading && filteredItems.length === 0" class="text-[13px] text-[var(--text-secondary)]">暂无数据</div>

                <div v-if="loading" class="text-[13px] text-[var(--text-secondary)]">加载中...</div>
            </div>
        </div>

        <AppFooter />

        <div v-if="detailOpen" class="fixed inset-0 z-[20] grid place-items-center bg-[rgba(0,0,0,0.35)] p-[20px]" @click.self="detailOpen=false">
            <div class="w-full max-w-[760px] space-y-[10px] rounded-[14px] border border-[var(--border-color)] bg-white p-[14px]">
                <div class="flex items-center justify-between">
                    <div class="text-[16px] font-semibold">{{ detailData?.plazaItem?.plazaTitle || '详情' }}</div>
                    <button class="text-[20px]" @click="detailOpen=false">×</button>
                </div>
                <div class="text-[13px] text-[var(--text-secondary)]">{{ detailData?.plazaItem?.plazaDesc }}</div>
                <div class="max-h-[260px] space-y-[8px] overflow-y-auto">
                    <div v-for="item in detailData?.commentList || []" :key="item.commentId" class="rounded-[10px] border border-[var(--border-color)] px-[10px] py-[8px]">
                        <div class="text-[12px] font-semibold">{{ item.username }}</div>
                        <div class="text-[13px]">{{ item.commentContent }}</div>
                    </div>
                </div>
                <div class="flex gap-[8px]">
                    <input v-model="commentForm.commentContent" class="flex-1 rounded-[10px] border border-[var(--border-color)] px-[10px] py-[8px] text-[13px]" placeholder="写评论..." />
                    <button class="rounded-[10px] border border-[var(--accent-color)] bg-[var(--accent-color)] px-[12px] py-[8px] text-[13px] text-white" @click="doComment">发送</button>
                </div>
            </div>
        </div>
    </section>
</template>
