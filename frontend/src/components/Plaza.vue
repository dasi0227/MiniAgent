<script setup>
import { onMounted, reactive, ref } from 'vue';
import { plazaComment, plazaDetail, plazaFavor, plazaLike, plazaList, plazaPublish, repoFork, studioListMine } from '../request/api';
import { normalizeError } from '../request/request';
import AppFooter from './AppFooter.vue';

const loading = ref(false);
const message = ref('');
const agentList = ref([]);
const plazaItems = ref([]);
const detailData = ref(null);
const detailOpen = ref(false);

const publishForm = reactive({
    agentId: '',
    plazaTitle: '',
    plazaDesc: ''
});

const commentForm = reactive({
    plazaId: '',
    commentContent: ''
});

const coverPalette = [
    ['#fee2e2', '#fef3c7', '#fecaca'],
    ['#dbeafe', '#e0f2fe', '#bfdbfe'],
    ['#dcfce7', '#f0fdf4', '#bbf7d0'],
    ['#ede9fe', '#f5f3ff', '#ddd6fe'],
    ['#ffedd5', '#fff7ed', '#fed7aa']
];

const pickData = (resp) => {
    if (resp && typeof resp === 'object' && Object.prototype.hasOwnProperty.call(resp, 'code')) {
        if (resp.code !== 200) {
            throw new Error(resp.info || '操作失败');
        }
        return resp.data;
    }
    return resp?.data ?? resp?.result ?? resp;
};

const coverStyle = (index) => {
    const palette = coverPalette[index % coverPalette.length];
    return {
        background: `linear-gradient(140deg, ${palette[0]} 0%, ${palette[1]} 56%, ${palette[2]} 100%)`
    };
};

const displayAuthor = (item) => item?.publishUsername || item?.username || 'MiniAgent 创作者';

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

const loadMineAgent = async () => {
    try {
        const resp = await studioListMine();
        agentList.value = pickData(resp) || [];
        if (!publishForm.agentId && agentList.value.length > 0) {
            publishForm.agentId = agentList.value[0].agentId;
        }
    } catch (error) {
        message.value = normalizeError(error).message || '获取 MiniAgent 失败';
    }
};

const doPublish = async () => {
    if (!publishForm.agentId || !publishForm.plazaTitle.trim()) {
        message.value = '请选择 MiniAgent 并填写标题';
        return;
    }
    try {
        await plazaPublish(publishForm);
        publishForm.plazaTitle = '';
        publishForm.plazaDesc = '';
        message.value = '发布成功';
        await loadPlaza();
    } catch (error) {
        message.value = normalizeError(error).message || '发布失败';
    }
};

const doLike = async (plazaId) => {
    try {
        await plazaLike({ plazaId });
        await loadPlaza();
    } catch (error) {
        message.value = normalizeError(error).message || '点赞失败';
    }
};

const doFavor = async (plazaId) => {
    try {
        await plazaFavor({ plazaId });
        await loadPlaza();
    } catch (error) {
        message.value = normalizeError(error).message || '收藏失败';
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
    await Promise.all([loadMineAgent(), loadPlaza()]);
});
</script>

<template>
    <section class="grid h-screen grid-rows-[1fr_var(--footer-height)] bg-[var(--bg-page)]">
        <div class="overflow-y-auto py-[24px] pl-[24px] pr-[calc(24px+var(--scrollbar-w))]">
            <div class="mx-auto max-w-[1100px] space-y-[16px]">
                <h1 class="text-[24px] font-bold">MiniAgent Plaza</h1>

                <div class="rounded-[14px] border border-[var(--border-color)] bg-white p-[14px] space-y-[10px]">
                    <div class="text-[16px] font-semibold">发布我的 MiniAgent</div>
                    <div class="grid gap-[10px] md:grid-cols-[220px_1fr_1fr_auto]">
                        <select v-model="publishForm.agentId" class="rounded-[10px] border border-[var(--border-color)] px-[10px] py-[10px] text-[14px]">
                            <option v-for="item in agentList" :key="item.agentId" :value="item.agentId">{{ item.agentName }}</option>
                        </select>
                        <input v-model="publishForm.plazaTitle" class="rounded-[10px] border border-[var(--border-color)] px-[10px] py-[10px] text-[14px]" placeholder="展示标题" />
                        <input v-model="publishForm.plazaDesc" class="rounded-[10px] border border-[var(--border-color)] px-[10px] py-[10px] text-[14px]" placeholder="展示描述" />
                        <button class="rounded-[10px] border border-[var(--accent-color)] bg-[var(--accent-color)] px-[14px] py-[10px] text-white font-semibold" @click="doPublish">发布</button>
                    </div>
                </div>

                <div v-if="message" class="text-[13px] text-[var(--text-secondary)]">{{ message }}</div>

                <div class="grid gap-[14px] sm:grid-cols-2 xl:grid-cols-3">
                    <article
                        v-for="(item, index) in plazaItems"
                        :key="item.plazaId"
                        class="group overflow-hidden rounded-[16px] border border-[var(--border-color)] bg-white shadow-[0_10px_24px_rgba(15,23,42,0.06)] transition-all duration-200 hover:-translate-y-[2px] hover:shadow-[0_16px_36px_rgba(15,23,42,0.1)]"
                    >
                        <button class="block w-full text-left" @click="openDetail(item.plazaId)">
                            <div class="relative h-[168px] px-[14px] py-[12px]" :style="coverStyle(index)">
                                <div class="inline-flex rounded-[999px] bg-[rgba(255,255,255,0.72)] px-[8px] py-[3px] text-[11px] font-semibold text-[#0f172a]">
                                    MiniAgent 帖子
                                </div>
                                <div class="mt-[10px] max-w-[78%] text-[18px] font-bold text-[#0f172a] leading-[1.35]">
                                    {{ item.plazaTitle }}
                                </div>
                                <div class="absolute bottom-[12px] right-[12px] rounded-[999px] bg-[rgba(15,23,42,0.7)] px-[8px] py-[3px] text-[11px] text-white">
                                    {{ item.agentId }}
                                </div>
                            </div>
                            <div class="space-y-[8px] px-[14px] py-[12px]">
                                <div class="min-h-[38px] text-[13px] text-[var(--text-secondary)] leading-[1.45]">
                                    {{ item.plazaDesc || '这个 MiniAgent 还没有补充描述，点进详情查看能力与评论。' }}
                                </div>
                                <div class="flex items-center justify-between text-[12px] text-[var(--text-secondary)]">
                                    <div class="inline-flex items-center gap-[7px]">
                                        <span class="grid h-[20px] w-[20px] place-items-center rounded-full bg-[#e2e8f0] text-[11px] font-semibold text-[#0f172a]">
                                            {{ displayAuthor(item).slice(0, 1) }}
                                        </span>
                                        <span>{{ displayAuthor(item) }}</span>
                                    </div>
                                    <div class="inline-flex items-center gap-[10px]">
                                        <span>赞 {{ item.likeCount || 0 }}</span>
                                        <span>藏 {{ item.favorCount || 0 }}</span>
                                        <span>评 {{ item.commentCount || 0 }}</span>
                                    </div>
                                </div>
                            </div>
                        </button>
                        <div class="flex items-center justify-between border-t border-[var(--border-color)] bg-[#fcfdff] px-[10px] py-[8px]">
                            <button class="rounded-[8px] px-[8px] py-[4px] text-[12px] text-[var(--text-secondary)] hover:bg-[#f1f5f9]" @click="doLike(item.plazaId)">点赞</button>
                            <button class="rounded-[8px] px-[8px] py-[4px] text-[12px] text-[var(--text-secondary)] hover:bg-[#f1f5f9]" @click="doFavor(item.plazaId)">收藏</button>
                            <button class="rounded-[8px] px-[8px] py-[4px] text-[12px] text-[var(--text-secondary)] hover:bg-[#f1f5f9]" @click="doFork(item.plazaId)">Fork</button>
                            <button class="rounded-[8px] px-[8px] py-[4px] text-[12px] text-[var(--text-secondary)] hover:bg-[#f1f5f9]" @click="openDetail(item.plazaId)">详情</button>
                        </div>
                    </article>
                </div>

                <div v-if="loading" class="text-[13px] text-[var(--text-secondary)]">加载中...</div>
            </div>
        </div>

        <AppFooter />

        <div v-if="detailOpen" class="fixed inset-0 z-[20] grid place-items-center bg-[rgba(0,0,0,0.35)] p-[20px]" @click.self="detailOpen=false">
            <div class="w-full max-w-[760px] rounded-[14px] border border-[var(--border-color)] bg-white p-[14px] space-y-[10px]">
                <div class="flex items-center justify-between">
                    <div class="text-[16px] font-semibold">{{ detailData?.plazaItem?.plazaTitle || '详情' }}</div>
                    <button class="text-[20px]" @click="detailOpen=false">×</button>
                </div>
                <div class="text-[13px] text-[var(--text-secondary)]">{{ detailData?.plazaItem?.plazaDesc }}</div>
                <div class="space-y-[8px] max-h-[260px] overflow-y-auto">
                    <div v-for="item in detailData?.commentList || []" :key="item.commentId" class="rounded-[10px] border border-[var(--border-color)] px-[10px] py-[8px]">
                        <div class="text-[12px] font-semibold">{{ item.username }}</div>
                        <div class="text-[13px]">{{ item.commentContent }}</div>
                    </div>
                </div>
                <div class="flex gap-[8px]">
                    <input v-model="commentForm.commentContent" class="flex-1 rounded-[10px] border border-[var(--border-color)] px-[10px] py-[8px] text-[13px]" placeholder="写评论..." />
                    <button class="rounded-[10px] border border-[var(--accent-color)] bg-[var(--accent-color)] px-[12px] py-[8px] text-white text-[13px]" @click="doComment">发送</button>
                </div>
            </div>
        </div>
    </section>
</template>
