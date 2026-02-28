<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue';
import { useRouter } from 'vue-router';
import Cropper from 'cropperjs';
import 'cropperjs/dist/cropper.css';
import {
    fetchProfile,
    updatePassword,
    userApiInsert,
    userApiList,
    userApiUpdate,
    userMcpInsert,
    userMcpList,
    userMcpUpdate
} from '../request/api';
import { parseAuthPayload } from '../request/auth';
import { normalizeError } from '../request/request';
import { useAuthStore, useSettingsStore } from '../router/pinia';
import Footer from './Footer.vue';

const authStore = useAuthStore();
const settingsStore = useSettingsStore();
const router = useRouter();
const isDarkTheme = computed(() => settingsStore.theme === 'dark');
const activeTab = ref('profile');

const pickData = (resp, message = '操作失败') => {
    if (resp && typeof resp === 'object' && Object.prototype.hasOwnProperty.call(resp, 'code')) {
        if (resp.code !== 200) {
            throw new Error(resp.info || message);
        }
        return resp.data;
    }
    return resp?.data ?? resp?.result ?? resp;
};

const currentUser = computed(() => authStore.user || { userId: '', username: '访客', role: 'guest' });
const avatarChar = computed(() => (currentUser.value.username || '访客').slice(0, 1).toUpperCase());

const DEFAULT_AVATAR_URL_PATTERN = /^https?:\/\/avatars\.githubusercontent\.com\/u\/1(?:[/?]|$)/i;
const currentUserAvatarUrl = computed(() => {
    const raw = currentUser.value?.avatarUrl || currentUser.value?.userAvatar || '';
    const normalized = typeof raw === 'string' ? raw.trim() : '';
    if (!normalized) return '';
    if (DEFAULT_AVATAR_URL_PATTERN.test(normalized)) return '';
    return normalized;
});

const profileLoading = ref(false);
const profileSaving = ref(false);
const profileError = ref('');
const profileForm = reactive({
    username: '',
    oldPassword: '',
    newPassword: ''
});

const profileAvatarError = ref('');
const profileAvatarFileRef = ref(null);
const profileAvatarFile = ref(null);
const profileAvatarPreviewUrl = ref('');
const profileAvatarPreviewFallback = ref(false);

const showAvatarCropper = ref(false);
const avatarCropImageRef = ref(null);
const avatarCropper = ref(null);
const avatarCropSourceUrl = ref('');
const avatarCropPreviewUrl = ref('');

const profileAvatarDisplayUrl = computed(() => profileAvatarPreviewUrl.value || currentUserAvatarUrl.value);
const canShowProfileAvatarImage = computed(() => Boolean(profileAvatarDisplayUrl.value) && !profileAvatarPreviewFallback.value);
const profileAvatarFallbackClass = computed(() =>
    isDarkTheme.value
        ? 'border-[rgba(148,163,184,0.42)] bg-[linear-gradient(135deg,#1f3f77,#2a5f9f)] text-[#e8f1ff] shadow-[inset_0_0_0_1px_rgba(255,255,255,0.08)]'
        : 'border-[rgba(255,255,255,0.42)] bg-[linear-gradient(135deg,#dcecff,#c8deff)] text-[#1f3d77] shadow-[inset_0_0_0_1px_rgba(255,255,255,0.28)]'
);

const mcpLoading = ref(false);
const mcpSaving = ref(false);
const mcpError = ref('');
const mcpKeyword = ref('');
const mcpList = ref([]);
const mcpForm = reactive({
    mcpName: '',
    mcpType: '',
    mcpDesc: '',
    mcpParam: '{}',
    mcpSecret: '{}'
});

const mcpDialogOpen = ref(false);
const mcpDialogSaving = ref(false);
const mcpDialogError = ref('');
const mcpDialogForm = reactive({
    mcpId: '',
    mcpName: '',
    mcpType: '',
    mcpDesc: '',
    mcpParam: '{}',
    mcpSecret: '{}'
});

const apiLoading = ref(false);
const apiSaving = ref(false);
const apiError = ref('');
const apiKeyword = ref('');
const apiList = ref([]);
const apiForm = reactive({
    modelName: '',
    apiBaseUrl: '',
    apiCompletionPath: '/v1/chat/completions',
    apiKey: ''
});

const apiDialogOpen = ref(false);
const apiDialogSaving = ref(false);
const apiDialogError = ref('');
const apiDialogForm = reactive({
    apiId: '',
    modelName: '',
    apiBaseUrl: '',
    apiCompletionPath: '/v1/chat/completions',
    apiKey: ''
});

const revokeObjectUrl = (url) => {
    if (url && typeof url === 'string' && url.startsWith('blob:')) {
        URL.revokeObjectURL(url);
    }
};

const resetAvatarCropper = () => {
    if (avatarCropper.value) {
        avatarCropper.value.destroy();
        avatarCropper.value = null;
    }
    revokeObjectUrl(avatarCropSourceUrl.value);
    avatarCropSourceUrl.value = '';
    revokeObjectUrl(avatarCropPreviewUrl.value);
    avatarCropPreviewUrl.value = '';
    showAvatarCropper.value = false;
};

const resetProfileAvatarDraft = () => {
    if (profileAvatarFileRef.value) {
        profileAvatarFileRef.value.value = '';
    }
    profileAvatarFile.value = null;
    revokeObjectUrl(profileAvatarPreviewUrl.value);
    profileAvatarPreviewUrl.value = '';
    profileAvatarPreviewFallback.value = false;
    profileAvatarError.value = '';
    resetAvatarCropper();
};

const makeCircleAvatarBlob = (sourceCanvas) => {
    if (!sourceCanvas) {
        return Promise.reject(new Error('头像裁剪失败'));
    }
    const size = Math.min(sourceCanvas.width, sourceCanvas.height);
    const output = document.createElement('canvas');
    output.width = size;
    output.height = size;
    const ctx = output.getContext('2d');
    if (!ctx) {
        return Promise.reject(new Error('头像裁剪失败'));
    }
    ctx.clearRect(0, 0, size, size);
    ctx.save();
    ctx.beginPath();
    ctx.arc(size / 2, size / 2, size / 2, 0, Math.PI * 2);
    ctx.closePath();
    ctx.clip();
    ctx.drawImage(sourceCanvas, 0, 0, size, size);
    ctx.restore();
    return new Promise((resolve, reject) => {
        output.toBlob(
            (blob) => {
                if (!blob) {
                    reject(new Error('头像裁剪失败'));
                    return;
                }
                resolve(blob);
            },
            'image/png',
            0.92
        );
    });
};

const refreshAvatarCropPreview = async () => {
    if (!avatarCropper.value) return;
    const canvas = avatarCropper.value.getCroppedCanvas({
        width: 320,
        height: 320,
        fillColor: '#ffffff',
        imageSmoothingQuality: 'high'
    });
    const blob = await makeCircleAvatarBlob(canvas);
    revokeObjectUrl(avatarCropPreviewUrl.value);
    avatarCropPreviewUrl.value = URL.createObjectURL(blob);
};

const initAvatarCropper = async () => {
    if (!avatarCropImageRef.value || !avatarCropSourceUrl.value) {
        return;
    }
    if (avatarCropper.value) {
        avatarCropper.value.destroy();
        avatarCropper.value = null;
    }
    avatarCropper.value = new Cropper(avatarCropImageRef.value, {
        viewMode: 1,
        dragMode: 'move',
        aspectRatio: 1,
        autoCropArea: 1,
        responsive: true,
        guides: false,
        background: false,
        center: false,
        movable: true,
        cropBoxMovable: false,
        cropBoxResizable: false,
        zoomOnWheel: true,
        toggleDragModeOnDblclick: false,
        ready: async () => {
            try {
                await refreshAvatarCropPreview();
            } catch (_) {
                avatarCropPreviewUrl.value = '';
            }
        },
        crop: async () => {
            try {
                await refreshAvatarCropPreview();
            } catch (_) {
                avatarCropPreviewUrl.value = '';
            }
        }
    });
};

const triggerProfileAvatarUpload = () => {
    profileAvatarError.value = '';
    profileAvatarFileRef.value?.click();
};

const handleProfileAvatarUpload = async (event) => {
    const file = event?.target?.files?.[0];
    if (!file) return;
    const type = (file.type || '').toLowerCase();
    const byMime = type === 'image/jpeg' || type === 'image/png';
    const byName = /\.(jpe?g|png)$/i.test(file.name || '');
    if (!byMime && !byName) {
        profileAvatarError.value = '仅支持 JPG / PNG 格式';
        event.target.value = '';
        return;
    }
    if (file.size > 1 * 1024 * 1024) {
        profileAvatarError.value = '图片大小不能超过 1MB';
        event.target.value = '';
        return;
    }
    profileAvatarError.value = '';
    resetAvatarCropper();
    avatarCropSourceUrl.value = URL.createObjectURL(file);
    showAvatarCropper.value = true;
    await nextTick();
    await initAvatarCropper();
};

const applyAvatarCrop = async () => {
    if (!avatarCropper.value) return;
    profileAvatarError.value = '';
    try {
        const canvas = avatarCropper.value.getCroppedCanvas({
            width: 320,
            height: 320,
            fillColor: '#ffffff',
            imageSmoothingQuality: 'high'
        });
        const blob = await makeCircleAvatarBlob(canvas);
        const avatarFile = new File([blob], `avatar-${Date.now()}.png`, { type: 'image/png' });
        profileAvatarFile.value = avatarFile;
        revokeObjectUrl(profileAvatarPreviewUrl.value);
        profileAvatarPreviewUrl.value = URL.createObjectURL(blob);
        profileAvatarPreviewFallback.value = false;
        resetAvatarCropper();
    } catch (error) {
        profileAvatarError.value = normalizeError(error).message || '头像裁剪失败';
    }
};

const cancelAvatarCrop = () => {
    resetAvatarCropper();
    if (profileAvatarFileRef.value) {
        profileAvatarFileRef.value.value = '';
    }
};

const clearProfileAvatarSelection = () => {
    if (profileAvatarFileRef.value) {
        profileAvatarFileRef.value.value = '';
    }
    profileAvatarFile.value = null;
    revokeObjectUrl(profileAvatarPreviewUrl.value);
    profileAvatarPreviewUrl.value = '';
    profileAvatarPreviewFallback.value = false;
    profileAvatarError.value = '';
};

const ensureJsonText = (value, fieldLabel) => {
    const normalized = (value || '').trim();
    if (!normalized) {
        throw new Error(`${fieldLabel} 不能为空`);
    }
    try {
        const parsed = JSON.parse(normalized);
        return JSON.stringify(parsed);
    } catch (_) {
        throw new Error(`${fieldLabel} 必须是合法 JSON`);
    }
};

const resetMcpForm = () => {
    mcpForm.mcpName = '';
    mcpForm.mcpType = '';
    mcpForm.mcpDesc = '';
    mcpForm.mcpParam = '{}';
    mcpForm.mcpSecret = '{}';
};

const resetApiForm = () => {
    apiForm.modelName = '';
    apiForm.apiBaseUrl = '';
    apiForm.apiCompletionPath = '/v1/chat/completions';
    apiForm.apiKey = '';
};

const loadUserProfile = async () => {
    profileLoading.value = true;
    profileError.value = '';
    try {
        const resp = await fetchProfile();
        const { token, user } = parseAuthPayload(resp);
        authStore.setAuth({
            token: token || authStore.token,
            user: user || authStore.user
        });
        profileForm.username = (user?.username || user?.userName || currentUser.value.username || '').trim();
    } catch (error) {
        profileError.value = normalizeError(error).message || '获取用户资料失败';
        profileForm.username = (currentUser.value.username || '').trim();
    } finally {
        profileLoading.value = false;
    }
};

const saveProfile = async () => {
    profileError.value = '';
    profileSaving.value = true;
    if (profileForm.newPassword && !profileForm.oldPassword) {
        profileError.value = '请先输入旧密码';
        profileSaving.value = false;
        return;
    }
    try {
        const resp = await updatePassword({
            username: profileForm.username,
            oldPassword: profileForm.oldPassword,
            newPassword: profileForm.newPassword,
            avatar: profileAvatarFile.value
        });
        const { token, user } = parseAuthPayload(resp);
        authStore.setAuth({
            token: token || authStore.token,
            user: user || authStore.user
        });
        profileForm.oldPassword = '';
        profileForm.newPassword = '';
        resetProfileAvatarDraft();
    } catch (error) {
        profileError.value = normalizeError(error).message || '保存失败';
    } finally {
        profileSaving.value = false;
    }
};

const loadMcpList = async (keyword = '') => {
    mcpLoading.value = true;
    mcpError.value = '';
    try {
        const resp = await userMcpList(keyword);
        const list = pickData(resp, '获取 MCP 失败') || [];
        mcpList.value = Array.isArray(list) ? list : [];
    } catch (error) {
        mcpError.value = normalizeError(error).message || '获取 MCP 失败';
    } finally {
        mcpLoading.value = false;
    }
};

const submitMcpInsert = async () => {
    mcpError.value = '';
    mcpSaving.value = true;
    try {
        const payload = {
            mcpName: (mcpForm.mcpName || '').trim(),
            mcpType: (mcpForm.mcpType || '').trim(),
            mcpDesc: (mcpForm.mcpDesc || '').trim(),
            mcpParam: ensureJsonText(mcpForm.mcpParam, 'MCP 配置'),
            mcpSecret: ensureJsonText(mcpForm.mcpSecret, 'MCP 密钥配置')
        };
        if (!payload.mcpName || !payload.mcpType || !payload.mcpDesc) {
            throw new Error('请完整填写 MCP 必填项');
        }
        await userMcpInsert(payload);
        resetMcpForm();
        await loadMcpList(mcpKeyword.value);
    } catch (error) {
        mcpError.value = normalizeError(error).message || '新增 MCP 失败';
    } finally {
        mcpSaving.value = false;
    }
};

const openMcpDialog = (item) => {
    if (!item) return;
    mcpDialogError.value = '';
    mcpDialogForm.mcpId = (item.mcpId || '').trim();
    mcpDialogForm.mcpName = item.mcpName || '';
    mcpDialogForm.mcpType = item.mcpType || '';
    mcpDialogForm.mcpDesc = item.mcpDesc || '';
    mcpDialogForm.mcpParam = item.mcpParam || '{}';
    mcpDialogForm.mcpSecret = item.mcpSecret || '{}';
    mcpDialogOpen.value = true;
};

const saveMcpDialog = async () => {
    mcpDialogError.value = '';
    mcpDialogSaving.value = true;
    try {
        if (!mcpDialogForm.mcpId.trim()) {
            throw new Error('MCP 配置标识缺失，请刷新后重试');
        }
        const payload = {
            mcpId: mcpDialogForm.mcpId.trim(),
            mcpName: (mcpDialogForm.mcpName || '').trim(),
            mcpType: (mcpDialogForm.mcpType || '').trim(),
            mcpDesc: (mcpDialogForm.mcpDesc || '').trim(),
            mcpParam: ensureJsonText(mcpDialogForm.mcpParam, 'MCP 配置'),
            mcpSecret: ensureJsonText(mcpDialogForm.mcpSecret, 'MCP 密钥配置')
        };
        if (!payload.mcpName || !payload.mcpType || !payload.mcpDesc) {
            throw new Error('请完整填写 MCP 必填项');
        }
        await userMcpUpdate(payload);
        mcpDialogOpen.value = false;
        await loadMcpList(mcpKeyword.value);
    } catch (error) {
        mcpDialogError.value = normalizeError(error).message || '更新 MCP 失败';
    } finally {
        mcpDialogSaving.value = false;
    }
};

const loadApiList = async (keyword = '') => {
    apiLoading.value = true;
    apiError.value = '';
    try {
        const resp = await userApiList(keyword);
        const list = pickData(resp, '获取 API 失败') || [];
        apiList.value = Array.isArray(list) ? list : [];
    } catch (error) {
        apiError.value = normalizeError(error).message || '获取 API 失败';
    } finally {
        apiLoading.value = false;
    }
};

const buildApiPayload = (form, apiId = '') => {
    const modelName = (form.modelName || '').trim();
    const apiBaseUrl = (form.apiBaseUrl || '').trim();
    const apiCompletionPath = (form.apiCompletionPath || '').trim();
    const apiKey = (form.apiKey || '').trim();
    if (!modelName || !apiBaseUrl || !apiCompletionPath || !apiKey) {
        throw new Error('请完整填写 API 必填项');
    }
    const payload = {
        modelName,
        modelType: 'chat',
        apiBaseUrl,
        apiCompletionPath,
        apiKey
    };
    if (apiId) {
        payload.apiId = apiId;
    }
    return payload;
};

const submitApiInsert = async () => {
    apiError.value = '';
    apiSaving.value = true;
    try {
        const payload = buildApiPayload(apiForm);
        await userApiInsert(payload);
        resetApiForm();
        await loadApiList(apiKeyword.value);
    } catch (error) {
        apiError.value = normalizeError(error).message || '新增 API 失败';
    } finally {
        apiSaving.value = false;
    }
};

const openApiDialog = (item) => {
    if (!item) return;
    apiDialogError.value = '';
    apiDialogForm.apiId = (item.apiId || '').trim();
    apiDialogForm.modelName = item.modelName || '';
    apiDialogForm.apiBaseUrl = item.apiBaseUrl || '';
    apiDialogForm.apiCompletionPath = item.apiCompletionPath || '/v1/chat/completions';
    apiDialogForm.apiKey = item.apiKey || '';
    apiDialogOpen.value = true;
};

const saveApiDialog = async () => {
    apiDialogError.value = '';
    apiDialogSaving.value = true;
    try {
        if (!apiDialogForm.apiId.trim()) {
            throw new Error('API 配置标识缺失，请刷新后重试');
        }
        const payload = buildApiPayload(apiDialogForm, apiDialogForm.apiId.trim());
        await userApiUpdate(payload);
        apiDialogOpen.value = false;
        await loadApiList(apiKeyword.value);
    } catch (error) {
        apiDialogError.value = normalizeError(error).message || '更新 API 失败';
    } finally {
        apiDialogSaving.value = false;
    }
};

const goRepository = () => {
    router.push('/repository');
};

watch(
    profileAvatarDisplayUrl,
    () => {
        profileAvatarPreviewFallback.value = false;
    },
    { immediate: true }
);

onMounted(async () => {
    resetMcpForm();
    resetApiForm();
    await Promise.all([loadUserProfile(), loadMcpList(), loadApiList()]);
});

onBeforeUnmount(() => {
    resetProfileAvatarDraft();
});
</script>

<template>
    <section class="grid h-screen grid-rows-[1fr_var(--footer-height)] bg-white">
        <div class="overflow-y-scroll [scrollbar-gutter:stable] py-[24px] pl-[24px] pr-[calc(24px+var(--scrollbar-w))]">
            <div class="mx-auto max-w-[1100px] space-y-[16px]">
                <div class="flex flex-wrap items-center justify-between gap-[12px]">
                    <div class="flex flex-wrap items-center gap-[28px]">
                        <h1 class="text-[24px] font-bold text-[var(--text-primary)]">个人中心设置</h1>
                        <nav class="flex items-center gap-[20px]">
                            <button
                                class="border-b-2 px-[2px] pb-[10px] pt-[2px] text-[14px] font-semibold transition"
                                :class="activeTab === 'profile' ? 'border-[var(--accent-color)] text-[var(--accent-color)]' : 'border-transparent text-[var(--text-secondary)] hover:text-[var(--text-primary)]'"
                                @click="activeTab = 'profile'"
                            >
                                Profile
                            </button>
                            <button
                                class="border-b-2 px-[2px] pb-[10px] pt-[2px] text-[14px] font-semibold transition"
                                :class="activeTab === 'mcp' ? 'border-[var(--accent-color)] text-[var(--accent-color)]' : 'border-transparent text-[var(--text-secondary)] hover:text-[var(--text-primary)]'"
                                @click="activeTab = 'mcp'"
                            >
                                MCP
                            </button>
                            <button
                                class="border-b-2 px-[2px] pb-[10px] pt-[2px] text-[14px] font-semibold transition"
                                :class="activeTab === 'api' ? 'border-[var(--accent-color)] text-[var(--accent-color)]' : 'border-transparent text-[var(--text-secondary)] hover:text-[var(--text-primary)]'"
                                @click="activeTab = 'api'"
                            >
                                API
                            </button>
                        </nav>
                    </div>
                    <button
                        class="rounded-[10px] border border-[#9ab6d2] bg-[#f2f7ff] px-[12px] py-[8px] text-[14px] font-semibold text-[#6888ad] transition hover:border-[#88a8c7] hover:bg-[#e9f2ff] hover:text-[#57789f]"
                        @click="goRepository"
                    >
                        我的仓库
                    </button>
                </div>
                <div class="h-[1px] w-full bg-[var(--border-color)]"></div>

                <div v-if="activeTab === 'profile'" class="space-y-[14px]">
                    <div>
                        <div class="mb-[10px] text-[13px] font-semibold text-[var(--text-secondary)]">头像设置</div>
                        <div class="flex flex-wrap items-center gap-[12px]">
                            <div
                                class="grid h-[84px] w-[84px] shrink-0 place-items-center overflow-hidden rounded-full border text-[24px] font-bold"
                                :class="
                                    canShowProfileAvatarImage
                                        ? 'border-[rgba(15,23,42,0.12)] bg-transparent'
                                        : profileAvatarFallbackClass
                                "
                            >
                                <img
                                    v-if="canShowProfileAvatarImage"
                                    :src="profileAvatarDisplayUrl"
                                    alt="Profile Avatar"
                                    class="h-full w-full object-cover"
                                    @error="profileAvatarPreviewFallback = true"
                                />
                                <span v-else>{{ avatarChar }}</span>
                            </div>
                            <div class="flex min-w-0 flex-1 flex-col gap-[8px]">
                                <div class="flex flex-wrap gap-[8px]">
                                    <button
                                        class="rounded-[10px] border border-[var(--accent-color)] bg-[var(--accent-color)] px-[12px] py-[8px] text-[12px] text-white transition hover:brightness-95"
                                        type="button"
                                        @click="triggerProfileAvatarUpload"
                                    >
                                        上传并裁剪
                                    </button>
                                    <button
                                        v-if="profileAvatarFile"
                                        class="rounded-[10px] border border-[var(--border-color)] bg-white px-[12px] py-[8px] text-[12px] text-[var(--text-primary)] transition hover:bg-[#eef2f7]"
                                        type="button"
                                        @click="clearProfileAvatarSelection"
                                    >
                                        取消新头像
                                    </button>
                                </div>
                                <div class="text-[12px] text-[var(--text-secondary)]">支持 JPG / PNG，最大 1MB。</div>
                            </div>
                        </div>
                        <input
                            ref="profileAvatarFileRef"
                            type="file"
                            accept=".jpg,.jpeg,.png,image/jpeg,image/png"
                            class="hidden"
                            @change="handleProfileAvatarUpload"
                        />
                    </div>

                    <div>
                        <div class="mb-[6px] text-[13px] font-semibold text-[var(--text-secondary)]">用户名</div>
                        <input
                            v-model="profileForm.username"
                            class="w-full rounded-[10px] border border-[var(--border-color)] bg-white px-[10px] py-[10px] text-[14px] text-[var(--text-primary)] outline-none focus:border-[var(--accent-color)]"
                            placeholder="请输入用户名"
                        />
                    </div>
                    <div class="grid grid-cols-2 gap-[12px] max-[720px]:grid-cols-1">
                        <div>
                            <div class="mb-[6px] text-[13px] font-semibold text-[var(--text-secondary)]">旧密码</div>
                            <input
                                v-model="profileForm.oldPassword"
                                type="password"
                                class="w-full rounded-[10px] border border-[var(--border-color)] bg-white px-[10px] py-[10px] text-[14px] text-[var(--text-primary)] outline-none focus:border-[var(--accent-color)]"
                                placeholder="修改密码时必填"
                            />
                        </div>
                        <div>
                            <div class="mb-[6px] text-[13px] font-semibold text-[var(--text-secondary)]">新密码</div>
                            <input
                                v-model="profileForm.newPassword"
                                type="password"
                                class="w-full rounded-[10px] border border-[var(--border-color)] bg-white px-[10px] py-[10px] text-[14px] text-[var(--text-primary)] outline-none focus:border-[var(--accent-color)]"
                                placeholder="输入新密码"
                            />
                        </div>
                    </div>
                    <div v-if="profileLoading" class="text-[12px] text-[var(--text-secondary)]">用户资料加载中...</div>
                    <div v-if="profileAvatarError" class="text-[12px] text-[#ef4444]">{{ profileAvatarError }}</div>
                    <div v-if="profileError" class="text-[12px] text-[#ef4444]">{{ profileError }}</div>
                    <div class="flex items-center justify-end gap-[10px] pt-[14px]">
                        <button
                            class="rounded-[10px] border border-[var(--accent-color)] bg-[var(--accent-color)] px-[14px] py-[10px] text-[14px] font-semibold text-white transition hover:brightness-95 disabled:cursor-not-allowed disabled:opacity-70"
                            type="button"
                            :disabled="profileSaving || profileLoading"
                            @click="saveProfile"
                        >
                            {{ profileSaving ? '保存中...' : '保存' }}
                        </button>
                    </div>
                </div>

                <div v-else-if="activeTab === 'mcp'" class="space-y-[14px]">
                    <div class="space-y-[10px]">
                        <div class="flex flex-wrap items-center justify-between gap-[10px]">
                            <div class="text-[16px] font-semibold">我配置过的 MCP 列表</div>
                            <div class="ml-auto flex w-full items-center justify-end gap-[8px] md:w-[360px]">
                                <input
                                    v-model="mcpKeyword"
                                    class="min-w-0 flex-1 rounded-[10px] border border-[var(--border-color)] px-[10px] py-[9px] text-[13px]"
                                    placeholder="输入关键字查询 MCP"
                                    @keydown.enter.prevent="loadMcpList(mcpKeyword)"
                                />
                                <button
                                    class="inline-flex h-[37px] w-[37px] items-center justify-center rounded-[10px] border border-[var(--border-color)] text-[var(--text-secondary)] transition hover:bg-[#eef2f7] hover:text-[var(--text-primary)]"
                                    type="button"
                                    aria-label="查询 MCP"
                                    title="查询 MCP"
                                    @click="loadMcpList(mcpKeyword)"
                                >
                                    <svg viewBox="0 0 24 24" class="h-[16px] w-[16px]" fill="none" stroke="currentColor" stroke-width="1.8" aria-hidden="true">
                                        <circle cx="11" cy="11" r="7" />
                                        <path d="m20 20-3.6-3.6" />
                                    </svg>
                                </button>
                            </div>
                        </div>
                        <div v-if="mcpLoading" class="text-[12px] text-[var(--text-secondary)]">加载中...</div>
                        <div v-else-if="mcpList.length === 0" class="text-[12px] text-[var(--text-secondary)]">暂无 MCP 配置</div>
                        <button
                            v-for="item in mcpList"
                            :key="item.mcpId"
                            class="w-full rounded-[10px] border border-[var(--border-color)] p-[12px] text-left transition hover:border-[var(--accent-color)] hover:bg-[#f8fafc]"
                            @click="openMcpDialog(item)"
                        >
                            <div class="text-[14px] font-semibold">MCP 名称：{{ item.mcpName || '-' }}</div>
                            <div class="mt-[4px] text-[12px] text-[var(--text-secondary)]">MCP 描述：{{ item.mcpDesc || '暂无描述' }}</div>
                        </button>
                    </div>
                    <div class="pt-[6px] text-[16px] font-semibold">新增个人 MCP</div>
                    <div class="space-y-[10px]">
                        <label class="grid items-start gap-[10px] text-[13px] md:grid-cols-[140px_1fr]">
                            <span class="pt-[10px] text-[var(--text-secondary)]">MCP 名称</span>
                            <input v-model="mcpForm.mcpName" class="rounded-[10px] border border-[var(--border-color)] px-[10px] py-[10px]" placeholder="请输入 MCP 名称" />
                        </label>
                        <label class="grid items-start gap-[10px] text-[13px] md:grid-cols-[140px_1fr]">
                            <span class="pt-[10px] text-[var(--text-secondary)]">MCP 类型</span>
                            <input v-model="mcpForm.mcpType" class="rounded-[10px] border border-[var(--border-color)] px-[10px] py-[10px]" placeholder="请输入 MCP 类型（例如 sse / stdio）" />
                        </label>
                        <label class="grid items-start gap-[10px] text-[13px] md:grid-cols-[140px_1fr]">
                            <span class="pt-[10px] text-[var(--text-secondary)]">MCP 描述</span>
                            <input v-model="mcpForm.mcpDesc" class="rounded-[10px] border border-[var(--border-color)] px-[10px] py-[10px]" placeholder="请输入 MCP 描述" />
                        </label>
                        <label class="grid items-start gap-[10px] text-[13px] md:grid-cols-[140px_1fr]">
                            <span class="pt-[10px] text-[var(--text-secondary)]">MCP 配置</span>
                            <textarea v-model="mcpForm.mcpParam" class="min-h-[88px] rounded-[10px] border border-[var(--border-color)] px-[10px] py-[10px]" placeholder='请输入 JSON，例如 {"baseUri":"http://127.0.0.1:9002","sseEndPoint":"/sse"}'></textarea>
                        </label>
                        <label class="grid items-start gap-[10px] text-[13px] md:grid-cols-[140px_1fr]">
                            <span class="pt-[10px] text-[var(--text-secondary)]">MCP 密钥配置</span>
                            <textarea v-model="mcpForm.mcpSecret" class="min-h-[88px] rounded-[10px] border border-[var(--border-color)] px-[10px] py-[10px]" placeholder='请输入 JSON，例如 {"token":"xxx"}'></textarea>
                        </label>
                    </div>
                    <div class="flex items-center gap-[10px]">
                        <button
                            class="rounded-[10px] border border-[var(--accent-color)] bg-[var(--accent-color)] px-[14px] py-[8px] text-[13px] font-semibold text-white transition hover:brightness-95 disabled:opacity-70"
                            :disabled="mcpSaving"
                            @click="submitMcpInsert"
                        >
                            {{ mcpSaving ? '保存中...' : '新增 MCP' }}
                        </button>
                        <button class="rounded-[10px] border border-[var(--border-color)] px-[14px] py-[8px] text-[13px]" @click="resetMcpForm">重置</button>
                    </div>
                    <div v-if="mcpError" class="text-[12px] text-[#ef4444]">{{ mcpError }}</div>
                </div>

                <div v-else class="space-y-[14px]">
                    <div class="space-y-[10px]">
                        <div class="flex flex-wrap items-center justify-between gap-[10px]">
                            <div class="text-[16px] font-semibold">我配置过的个人 API</div>
                            <div class="ml-auto flex w-full items-center justify-end gap-[8px] md:w-[360px]">
                                <input
                                    v-model="apiKeyword"
                                    class="min-w-0 flex-1 rounded-[10px] border border-[var(--border-color)] px-[10px] py-[9px] text-[13px]"
                                    placeholder="输入关键字查询 API"
                                    @keydown.enter.prevent="loadApiList(apiKeyword)"
                                />
                                <button
                                    class="inline-flex h-[37px] w-[37px] items-center justify-center rounded-[10px] border border-[var(--border-color)] text-[var(--text-secondary)] transition hover:bg-[#eef2f7] hover:text-[var(--text-primary)]"
                                    type="button"
                                    aria-label="查询 API"
                                    title="查询 API"
                                    @click="loadApiList(apiKeyword)"
                                >
                                    <svg viewBox="0 0 24 24" class="h-[16px] w-[16px]" fill="none" stroke="currentColor" stroke-width="1.8" aria-hidden="true">
                                        <circle cx="11" cy="11" r="7" />
                                        <path d="m20 20-3.6-3.6" />
                                    </svg>
                                </button>
                            </div>
                        </div>
                        <div v-if="apiLoading" class="text-[12px] text-[var(--text-secondary)]">加载中...</div>
                        <div v-else-if="apiList.length === 0" class="text-[12px] text-[var(--text-secondary)]">暂无 API 配置</div>
                        <button
                            v-for="item in apiList"
                            :key="item.apiId"
                            class="w-full rounded-[10px] border border-[var(--border-color)] p-[12px] text-left transition hover:border-[var(--accent-color)] hover:bg-[#f8fafc]"
                            @click="openApiDialog(item)"
                        >
                            <div class="text-[14px] font-semibold">模型名称：{{ item.modelName || '-' }}</div>
                            <div class="mt-[4px] text-[12px] text-[var(--text-secondary)]">API 地址：{{ item.apiBaseUrl || '-' }}</div>
                        </button>
                    </div>
                    <div class="pt-[6px] text-[16px] font-semibold">新增个人 API</div>
                    <div class="space-y-[10px]">
                        <label class="grid items-start gap-[10px] text-[13px] md:grid-cols-[140px_1fr]">
                            <span class="pt-[10px] text-[var(--text-secondary)]">API 地址</span>
                            <input v-model="apiForm.apiBaseUrl" class="rounded-[10px] border border-[var(--border-color)] px-[10px] py-[10px]" placeholder="请输入 API 地址，例如 https://api.openai.com" />
                        </label>
                        <label class="grid items-start gap-[10px] text-[13px] md:grid-cols-[140px_1fr]">
                            <span class="pt-[10px] text-[var(--text-secondary)]">补全路径</span>
                            <input v-model="apiForm.apiCompletionPath" class="rounded-[10px] border border-[var(--border-color)] px-[10px] py-[10px]" placeholder="请输入补全路径，例如 /v1/chat/completions" />
                        </label>
                        <label class="grid items-start gap-[10px] text-[13px] md:grid-cols-[140px_1fr]">
                            <span class="pt-[10px] text-[var(--text-secondary)]">模型名称</span>
                            <input v-model="apiForm.modelName" class="rounded-[10px] border border-[var(--border-color)] px-[10px] py-[10px]" placeholder="请输入模型名称，例如 qwen-plus" />
                        </label>
                        <label class="grid items-start gap-[10px] text-[13px] md:grid-cols-[140px_1fr]">
                            <span class="pt-[10px] text-[var(--text-secondary)]">API 密钥</span>
                            <input v-model="apiForm.apiKey" class="rounded-[10px] border border-[var(--border-color)] px-[10px] py-[10px]" placeholder="请输入 API 密钥" />
                        </label>
                    </div>
                    <div class="flex items-center gap-[10px]">
                        <button
                            class="rounded-[10px] border border-[var(--accent-color)] bg-[var(--accent-color)] px-[14px] py-[8px] text-[13px] font-semibold text-white transition hover:brightness-95 disabled:opacity-70"
                            :disabled="apiSaving"
                            @click="submitApiInsert"
                        >
                            {{ apiSaving ? '保存中...' : '新增 API' }}
                        </button>
                        <button class="rounded-[10px] border border-[var(--border-color)] px-[14px] py-[8px] text-[13px]" @click="resetApiForm">重置</button>
                    </div>
                    <div v-if="apiError" class="text-[12px] text-[#ef4444]">{{ apiError }}</div>
                </div>
            </div>
        </div>

        <Footer />

        <div
            v-if="showAvatarCropper"
            class="fixed inset-0 z-[40] grid place-items-center bg-[rgba(0,0,0,0.45)] backdrop-blur-[4px] p-[20px]"
            @click.self="cancelAvatarCrop"
        >
            <div class="w-full max-w-[760px] rounded-[14px] bg-white p-[16px] text-[var(--text-primary)] shadow-[0_20px_50px_rgba(15,23,42,0.24)]">
                <div class="mb-[12px] flex items-center justify-between">
                    <div class="text-[16px] font-bold">裁剪头像</div>
                    <button class="text-[20px] text-[var(--text-secondary)]" type="button" @click="cancelAvatarCrop">×</button>
                </div>
                <div class="grid gap-[16px] lg:grid-cols-[1fr_220px]">
                    <div class="profile-avatar-cropper overflow-hidden rounded-[12px] p-[8px]">
                        <img ref="avatarCropImageRef" :src="avatarCropSourceUrl" alt="Avatar Crop Source" class="block max-h-[420px] w-full object-contain" />
                    </div>
                    <div class="space-y-[12px]">
                        <div class="text-[13px] text-[var(--text-secondary)]">预览</div>
                        <div class="grid h-[168px] w-[168px] place-items-center overflow-hidden rounded-full border border-[rgba(15,23,42,0.9)]">
                            <img v-if="avatarCropPreviewUrl" :src="avatarCropPreviewUrl" alt="Avatar Preview" class="h-full w-full object-cover" />
                            <span v-else class="text-[12px] text-[var(--text-secondary)]">预览</span>
                        </div>
                        <div class="flex justify-center gap-[8px] pt-[8px]">
                            <button
                                class="rounded-[10px] border border-[var(--border-color)] bg-white px-[12px] py-[8px] text-[12px] font-semibold text-[var(--text-primary)] transition hover:bg-[#f7f9fc]"
                                type="button"
                                @click="cancelAvatarCrop"
                            >
                                取消
                            </button>
                            <button
                                class="rounded-[10px] border border-[var(--accent-color)] bg-[var(--accent-color)] px-[12px] py-[8px] text-[12px] font-semibold text-white transition hover:brightness-95"
                                type="button"
                                @click="applyAvatarCrop"
                            >
                                使用头像
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <div
            v-if="mcpDialogOpen"
            class="fixed inset-0 z-[41] grid place-items-center bg-[rgba(0,0,0,0.35)] p-[20px]"
            @click.self="mcpDialogOpen = false"
        >
            <div class="w-full max-w-[760px] rounded-[14px] bg-white p-[18px] shadow-[0_20px_50px_rgba(15,23,42,0.24)]">
                <div class="mb-[12px] flex items-center justify-between">
                    <div class="text-[16px] font-semibold">MCP 配置详情</div>
                    <button class="text-[20px] text-[var(--text-secondary)]" @click="mcpDialogOpen = false">×</button>
                </div>
                <div class="space-y-[10px]">
                    <label class="grid items-start gap-[10px] text-[13px] md:grid-cols-[140px_1fr]">
                        <span class="pt-[10px] text-[var(--text-secondary)]">MCP 名称</span>
                        <input v-model="mcpDialogForm.mcpName" class="rounded-[10px] border border-[var(--border-color)] px-[10px] py-[10px]" />
                    </label>
                    <label class="grid items-start gap-[10px] text-[13px] md:grid-cols-[140px_1fr]">
                        <span class="pt-[10px] text-[var(--text-secondary)]">MCP 类型</span>
                        <input v-model="mcpDialogForm.mcpType" class="rounded-[10px] border border-[var(--border-color)] px-[10px] py-[10px]" />
                    </label>
                    <label class="grid items-start gap-[10px] text-[13px] md:grid-cols-[140px_1fr]">
                        <span class="pt-[10px] text-[var(--text-secondary)]">MCP 描述</span>
                        <input v-model="mcpDialogForm.mcpDesc" class="rounded-[10px] border border-[var(--border-color)] px-[10px] py-[10px]" />
                    </label>
                    <label class="grid items-start gap-[10px] text-[13px] md:grid-cols-[140px_1fr]">
                        <span class="pt-[10px] text-[var(--text-secondary)]">MCP 配置</span>
                        <textarea v-model="mcpDialogForm.mcpParam" class="min-h-[88px] rounded-[10px] border border-[var(--border-color)] px-[10px] py-[10px]"></textarea>
                    </label>
                    <label class="grid items-start gap-[10px] text-[13px] md:grid-cols-[140px_1fr]">
                        <span class="pt-[10px] text-[var(--text-secondary)]">MCP 密钥配置</span>
                        <textarea v-model="mcpDialogForm.mcpSecret" class="min-h-[88px] rounded-[10px] border border-[var(--border-color)] px-[10px] py-[10px]"></textarea>
                    </label>
                </div>
                <div v-if="mcpDialogError" class="mt-[10px] text-[12px] text-[#ef4444]">{{ mcpDialogError }}</div>
                <div class="mt-[12px] flex justify-end gap-[8px]">
                    <button class="rounded-[10px] border border-[var(--border-color)] px-[12px] py-[8px] text-[13px]" @click="mcpDialogOpen = false">取消</button>
                    <button
                        class="rounded-[10px] border border-[var(--accent-color)] bg-[var(--accent-color)] px-[12px] py-[8px] text-[13px] font-semibold text-white disabled:opacity-70"
                        :disabled="mcpDialogSaving"
                        @click="saveMcpDialog"
                    >
                        {{ mcpDialogSaving ? '保存中...' : '保存' }}
                    </button>
                </div>
            </div>
        </div>

        <div
            v-if="apiDialogOpen"
            class="fixed inset-0 z-[41] grid place-items-center bg-[rgba(0,0,0,0.35)] p-[20px]"
            @click.self="apiDialogOpen = false"
        >
            <div class="w-full max-w-[760px] rounded-[14px] bg-white p-[18px] shadow-[0_20px_50px_rgba(15,23,42,0.24)]">
                <div class="mb-[12px] flex items-center justify-between">
                    <div class="text-[16px] font-semibold">API 配置详情</div>
                    <button class="text-[20px] text-[var(--text-secondary)]" @click="apiDialogOpen = false">×</button>
                </div>
                <div class="space-y-[10px]">
                    <label class="grid items-start gap-[10px] text-[13px] md:grid-cols-[140px_1fr]">
                        <span class="pt-[10px] text-[var(--text-secondary)]">API 地址</span>
                        <input v-model="apiDialogForm.apiBaseUrl" class="rounded-[10px] border border-[var(--border-color)] px-[10px] py-[10px]" />
                    </label>
                    <label class="grid items-start gap-[10px] text-[13px] md:grid-cols-[140px_1fr]">
                        <span class="pt-[10px] text-[var(--text-secondary)]">补全路径</span>
                        <input v-model="apiDialogForm.apiCompletionPath" class="rounded-[10px] border border-[var(--border-color)] px-[10px] py-[10px]" />
                    </label>
                    <label class="grid items-start gap-[10px] text-[13px] md:grid-cols-[140px_1fr]">
                        <span class="pt-[10px] text-[var(--text-secondary)]">模型名称</span>
                        <input v-model="apiDialogForm.modelName" class="rounded-[10px] border border-[var(--border-color)] px-[10px] py-[10px]" />
                    </label>
                    <label class="grid items-start gap-[10px] text-[13px] md:grid-cols-[140px_1fr]">
                        <span class="pt-[10px] text-[var(--text-secondary)]">API 密钥</span>
                        <input v-model="apiDialogForm.apiKey" class="rounded-[10px] border border-[var(--border-color)] px-[10px] py-[10px]" />
                    </label>
                </div>
                <div v-if="apiDialogError" class="mt-[10px] text-[12px] text-[#ef4444]">{{ apiDialogError }}</div>
                <div class="mt-[12px] flex justify-end gap-[8px]">
                    <button class="rounded-[10px] border border-[var(--border-color)] px-[12px] py-[8px] text-[13px]" @click="apiDialogOpen = false">取消</button>
                    <button
                        class="rounded-[10px] border border-[var(--accent-color)] bg-[var(--accent-color)] px-[12px] py-[8px] text-[13px] font-semibold text-white disabled:opacity-70"
                        :disabled="apiDialogSaving"
                        @click="saveApiDialog"
                    >
                        {{ apiDialogSaving ? '保存中...' : '保存' }}
                    </button>
                </div>
            </div>
        </div>
    </section>
</template>

<style scoped>
.profile-avatar-cropper :deep(.cropper-container),
.profile-avatar-cropper :deep(.cropper-wrap-box),
.profile-avatar-cropper :deep(.cropper-canvas) {
    background: transparent !important;
}

.profile-avatar-cropper :deep(.cropper-bg) {
    background-image: none !important;
    background-color: transparent !important;
}

.profile-avatar-cropper :deep(.cropper-modal) {
    background-color: transparent !important;
    opacity: 0 !important;
}

.profile-avatar-cropper :deep(.cropper-dashed),
.profile-avatar-cropper :deep(.cropper-center) {
    display: none !important;
}

.profile-avatar-cropper :deep(.cropper-view-box) {
    outline: 1px solid rgba(47, 124, 246, 0.45) !important;
    box-shadow: 0 0 0 1px rgba(47, 124, 246, 0.2) inset !important;
}

.profile-avatar-cropper :deep(.cropper-line),
.profile-avatar-cropper :deep(.cropper-point) {
    background-color: rgba(47, 124, 246, 0.75) !important;
}
</style>
