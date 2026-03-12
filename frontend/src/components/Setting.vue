<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue';
import Cropper from 'cropperjs';
import 'cropperjs/dist/cropper.css';
import {
    fetchProfile,
    queryAgentList,
    updatePassword,
    userApiInsert,
    userApiList,
    userApiUpdate,
    userMcpInsert,
    userMcpList,
    userMcpUpdate,
    userTaskDelete,
    userTaskInsert,
    userTaskList,
    userTaskToggle,
    userTaskUpdate
} from '../request/api';
import { parseAuthPayload } from '../request/auth';
import { normalizeError, notifyAppError } from '../request/request';
import { useAuthStore, useSettingsStore } from '../router/pinia';
import Footer from './Footer.vue';

const authStore = useAuthStore();
const settingsStore = useSettingsStore();
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
const MCP_TYPE_OPTIONS = [
    { value: 'sse', label: 'SSE' },
    { value: 'stdio', label: 'STDIO' }
];
const MCP_TYPE_SET = new Set(MCP_TYPE_OPTIONS.map((item) => item.value));
const mcpForm = reactive({
    mcpName: '',
    mcpType: '',
    mcpDesc: '',
    mcpParam: '{}',
    mcpSecret: '{}'
});
const mcpCreateDialogOpen = ref(false);
const mcpParamFormatError = ref('');
const mcpSecretTableError = ref('');

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
const mcpDialogParamFormatError = ref('');
const mcpDialogSecretTableError = ref('');
let mcpSecretRowIdSeed = 0;
const mcpSecretRows = ref([]);
const mcpDialogSecretRows = ref([]);

const apiLoading = ref(false);
const apiSaving = ref(false);
const apiError = ref('');
const apiKeyword = ref('');
const apiList = ref([]);
const apiForm = reactive({
    modelType: '',
    modelName: '',
    apiBaseUrl: '',
    apiCompletionPath: '/v1/chat/completions',
    apiKey: ''
});
const apiCreateDialogOpen = ref(false);

const apiDialogOpen = ref(false);
const apiDialogSaving = ref(false);
const apiDialogError = ref('');
const apiDialogForm = reactive({
    apiId: '',
    modelType: '',
    modelName: '',
    apiBaseUrl: '',
    apiCompletionPath: '/v1/chat/completions',
    apiKey: ''
});

const taskLoading = ref(false);
const taskSaving = ref(false);
const taskError = ref('');
const taskKeyword = ref('');
const taskList = ref([]);
const taskAgents = ref([]);
const taskEditing = ref(false);
const taskFormDialogOpen = ref(false);
const taskForm = reactive({
    taskId: '',
    agentId: '',
    taskCron: '',
    taskDesc: '',
    taskParam: '{"maxRetry":2,"maxRound":2,"userMessage":""}',
    taskStatus: 1
});
const taskDeleteTarget = ref(null);
const taskDeleteConfirmOpen = ref(false);
const taskAgentDropdownOpen = ref(false);
const taskStatusDropdownOpen = ref(false);
const taskAgentDropdownRef = ref(null);
const taskStatusDropdownRef = ref(null);
const taskStatusOptions = [
    { value: 1, label: '启用' },
    { value: 0, label: '禁用' }
];

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

const optionalJsonText = (value, fieldLabel) => {
    const normalized = (value || '').trim();
    if (!normalized) {
        return null;
    }
    try {
        const parsed = JSON.parse(normalized);
        return JSON.stringify(parsed);
    } catch (_) {
        throw new Error(`${fieldLabel} 必须是合法 JSON`);
    }
};

const normalizeMcpTypeValue = (value) => {
    const normalized = String(value || '')
        .trim()
        .toLowerCase();
    return MCP_TYPE_SET.has(normalized) ? normalized : '';
};

const createMcpSecretRow = (key = '', value = '') => {
    const rowId = `mcp-secret-${Date.now()}-${mcpSecretRowIdSeed}`;
    mcpSecretRowIdSeed += 1;
    return {
        rowId,
        key: String(key ?? ''),
        value: String(value ?? '')
    };
};

const resetMcpSecretRows = (scope = 'form') => {
    const targetRows = scope === 'dialog' ? mcpDialogSecretRows : mcpSecretRows;
    targetRows.value = [];
};

const prettifyJsonForEditor = (value, fallback = '') => {
    const normalized = String(value || '').trim();
    if (!normalized) {
        return fallback;
    }
    try {
        return JSON.stringify(JSON.parse(normalized), null, 2);
    } catch (_) {
        return normalized;
    }
};

const prettifyMcpParam = (scope = 'form') => {
    const targetForm = scope === 'dialog' ? mcpDialogForm : mcpForm;
    const targetError = scope === 'dialog' ? mcpDialogParamFormatError : mcpParamFormatError;
    targetError.value = '';
    const normalized = String(targetForm.mcpParam || '').trim();
    if (!normalized) {
        return;
    }
    try {
        targetForm.mcpParam = JSON.stringify(JSON.parse(normalized), null, 2);
    } catch (_) {
        targetError.value = 'MCP 配置必须是合法 JSON';
    }
};

const parseMcpSecretRows = (value) => {
    const normalized = String(value || '').trim();
    if (!normalized) {
        return [];
    }
    try {
        const parsed = JSON.parse(normalized);
        if (parsed && typeof parsed === 'object' && !Array.isArray(parsed)) {
            const entries = Object.entries(parsed);
            if (entries.length === 0) {
                return [];
            }
            return entries.map(([key, rowValue]) =>
                createMcpSecretRow(
                    key,
                    rowValue === null || rowValue === undefined
                        ? ''
                        : typeof rowValue === 'string'
                          ? rowValue
                          : JSON.stringify(rowValue)
                )
            );
        }
        return [
            createMcpSecretRow(
                'value',
                typeof parsed === 'string' ? parsed : JSON.stringify(parsed)
            )
        ];
    } catch (_) {
        return [createMcpSecretRow('value', normalized)];
    }
};

const buildMcpSecretJson = (rows, fieldLabel = 'MCP 密钥配置') => {
    const result = {};
    const keySet = new Set();
    rows.forEach((row) => {
        const rowKey = String(row?.key || '').trim();
        const rowValue = String(row?.value || '');
        const hasKey = rowKey.length > 0;
        const hasValue = rowValue.trim().length > 0;
        if (!hasKey && !hasValue) {
            return;
        }
        if (!hasKey && hasValue) {
            throw new Error(`${fieldLabel} 的 Key 不能为空`);
        }
        if (keySet.has(rowKey)) {
            throw new Error(`${fieldLabel} 的 Key 不能重复：${rowKey}`);
        }
        keySet.add(rowKey);
        result[rowKey] = rowValue;
    });
    if (keySet.size === 0) {
        return null;
    }
    return JSON.stringify(result);
};

const addMcpSecretRow = (scope = 'form') => {
    const targetRows = scope === 'dialog' ? mcpDialogSecretRows : mcpSecretRows;
    targetRows.value.push(createMcpSecretRow());
};

const removeMcpSecretRow = (scope, rowId) => {
    const targetRows = scope === 'dialog' ? mcpDialogSecretRows : mcpSecretRows;
    targetRows.value = targetRows.value.filter((row) => row.rowId !== rowId);
};

const resetMcpForm = () => {
    mcpForm.mcpName = '';
    mcpForm.mcpType = '';
    mcpForm.mcpDesc = '';
    mcpForm.mcpParam = '{}';
    mcpForm.mcpSecret = '{}';
    mcpParamFormatError.value = '';
    mcpSecretTableError.value = '';
    resetMcpSecretRows('form');
};

const openMcpCreateDialog = () => {
    mcpError.value = '';
    resetMcpForm();
    mcpCreateDialogOpen.value = true;
};

const resetApiForm = () => {
    apiForm.modelType = '';
    apiForm.modelName = '';
    apiForm.apiBaseUrl = '';
    apiForm.apiCompletionPath = '/v1/chat/completions';
    apiForm.apiKey = '';
};

const openApiCreateDialog = () => {
    apiError.value = '';
    resetApiForm();
    apiCreateDialogOpen.value = true;
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
        notifyAppError(error, '获取用户资料失败');
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
        notifyAppError(error, '保存失败');
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
        mcpList.value = [];
        notifyAppError(error, '获取 MCP 失败');
    } finally {
        mcpLoading.value = false;
    }
};

const submitMcpInsert = async () => {
    mcpError.value = '';
    mcpParamFormatError.value = '';
    mcpSecretTableError.value = '';
    let payload = null;
    try {
        const mcpType = normalizeMcpTypeValue(mcpForm.mcpType);
        payload = {
            mcpName: (mcpForm.mcpName || '').trim(),
            mcpType,
            mcpDesc: (mcpForm.mcpDesc || '').trim(),
            mcpParam: optionalJsonText(mcpForm.mcpParam, 'MCP 配置'),
            mcpSecret: buildMcpSecretJson(mcpSecretRows.value, 'MCP 密钥配置')
        };
        if (!payload.mcpType) {
            throw new Error('请选择 MCP 类型（SSE / STDIO）');
        }
        if (!payload.mcpName || !payload.mcpDesc) {
            throw new Error('请完整填写 MCP 必填项');
        }
    } catch (error) {
        const errorMessage = normalizeError(error).message || '新增 MCP 失败';
        if (errorMessage.includes('MCP 配置')) {
            mcpParamFormatError.value = errorMessage;
        } else if (errorMessage.includes('MCP 密钥配置')) {
            mcpSecretTableError.value = errorMessage;
        } else {
            mcpError.value = errorMessage;
        }
        return;
    }
    mcpSaving.value = true;
    try {
        await userMcpInsert(payload);
        resetMcpForm();
        mcpCreateDialogOpen.value = false;
        await loadMcpList(mcpKeyword.value);
    } catch (error) {
        notifyAppError(error, '新增 MCP 失败');
    } finally {
        mcpSaving.value = false;
    }
};

const openMcpDialog = (item) => {
    if (!item) return;
    mcpDialogError.value = '';
    mcpDialogParamFormatError.value = '';
    mcpDialogSecretTableError.value = '';
    mcpDialogForm.mcpId = (item.mcpId || '').trim();
    mcpDialogForm.mcpName = item.mcpName || '';
    const normalizedType = normalizeMcpTypeValue(item.mcpType);
    mcpDialogForm.mcpType = normalizedType;
    mcpDialogForm.mcpDesc = item.mcpDesc || '';
    mcpDialogForm.mcpParam = prettifyJsonForEditor(item.mcpParam, '{}');
    mcpDialogForm.mcpSecret = item.mcpSecret || '{}';
    mcpDialogSecretRows.value = parseMcpSecretRows(item.mcpSecret);
    if (!normalizedType && String(item.mcpType || '').trim()) {
        mcpDialogError.value = '当前 MCP 类型无效，请重新选择 SSE / STDIO';
    }
    mcpDialogOpen.value = true;
};

const saveMcpDialog = async () => {
    mcpDialogError.value = '';
    mcpDialogParamFormatError.value = '';
    mcpDialogSecretTableError.value = '';
    let payload = null;
    try {
        if (!mcpDialogForm.mcpId.trim()) {
            throw new Error('MCP 配置标识缺失，请刷新后重试');
        }
        const mcpType = normalizeMcpTypeValue(mcpDialogForm.mcpType);
        payload = {
            mcpId: mcpDialogForm.mcpId.trim(),
            mcpName: (mcpDialogForm.mcpName || '').trim(),
            mcpType,
            mcpDesc: (mcpDialogForm.mcpDesc || '').trim(),
            mcpParam: optionalJsonText(mcpDialogForm.mcpParam, 'MCP 配置'),
            mcpSecret: buildMcpSecretJson(mcpDialogSecretRows.value, 'MCP 密钥配置')
        };
        if (!payload.mcpType) {
            throw new Error('请选择 MCP 类型（SSE / STDIO）');
        }
        if (!payload.mcpName || !payload.mcpDesc) {
            throw new Error('请完整填写 MCP 必填项');
        }
    } catch (error) {
        const errorMessage = normalizeError(error).message || '更新 MCP 失败';
        if (errorMessage.includes('MCP 配置')) {
            mcpDialogParamFormatError.value = errorMessage;
        } else if (errorMessage.includes('MCP 密钥配置')) {
            mcpDialogSecretTableError.value = errorMessage;
        } else {
            mcpDialogError.value = errorMessage;
        }
        return;
    }
    mcpDialogSaving.value = true;
    try {
        await userMcpUpdate(payload);
        mcpDialogOpen.value = false;
        await loadMcpList(mcpKeyword.value);
    } catch (error) {
        notifyAppError(error, '更新 MCP 失败');
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
        apiList.value = [];
        notifyAppError(error, '获取 API 失败');
    } finally {
        apiLoading.value = false;
    }
};

const buildApiPayload = (form, apiId = '') => {
    const modelType = (form.modelType || '').trim();
    const modelName = (form.modelName || '').trim();
    const apiBaseUrl = (form.apiBaseUrl || '').trim();
    const apiCompletionPath = (form.apiCompletionPath || '').trim();
    const apiKey = (form.apiKey || '').trim();
    if (!modelType || !modelName || !apiBaseUrl || !apiCompletionPath || !apiKey) {
        throw new Error('请完整填写 API 必填项');
    }
    const payload = {
        modelName,
        modelType,
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
    let payload = null;
    try {
        payload = buildApiPayload(apiForm);
    } catch (error) {
        apiError.value = normalizeError(error).message || '新增 API 失败';
        return;
    }
    apiSaving.value = true;
    try {
        await userApiInsert(payload);
        resetApiForm();
        apiCreateDialogOpen.value = false;
        await loadApiList(apiKeyword.value);
    } catch (error) {
        notifyAppError(error, '新增 API 失败');
    } finally {
        apiSaving.value = false;
    }
};

const openApiDialog = (item) => {
    if (!item) return;
    apiDialogError.value = '';
    apiDialogForm.apiId = (item.apiId || '').trim();
    apiDialogForm.modelType = item.modelType || '';
    apiDialogForm.modelName = item.modelName || '';
    apiDialogForm.apiBaseUrl = item.apiBaseUrl || '';
    apiDialogForm.apiCompletionPath = item.apiCompletionPath || '/v1/chat/completions';
    apiDialogForm.apiKey = item.apiKey || '';
    apiDialogOpen.value = true;
};

const saveApiDialog = async () => {
    apiDialogError.value = '';
    let payload = null;
    try {
        if (!apiDialogForm.apiId.trim()) {
            throw new Error('API 配置标识缺失，请刷新后重试');
        }
        payload = buildApiPayload(apiDialogForm, apiDialogForm.apiId.trim());
    } catch (error) {
        apiDialogError.value = normalizeError(error).message || '更新 API 失败';
        return;
    }
    apiDialogSaving.value = true;
    try {
        await userApiUpdate(payload);
        apiDialogOpen.value = false;
        await loadApiList(apiKeyword.value);
    } catch (error) {
        notifyAppError(error, '更新 API 失败');
    } finally {
        apiDialogSaving.value = false;
    }
};

const resetTaskForm = () => {
    taskEditing.value = false;
    taskForm.taskId = '';
    taskForm.agentId = '';
    taskForm.taskCron = '';
    taskForm.taskDesc = '';
    taskForm.taskParam = '{"maxRetry":2,"maxRound":2,"userMessage":""}';
    taskForm.taskStatus = 1;
    taskAgentDropdownOpen.value = false;
    taskStatusDropdownOpen.value = false;
};

const loadTaskAgents = async () => {
    try {
        const resp = await queryAgentList();
        const list = pickData(resp, '获取 MiniAgent 列表失败') || [];
        taskAgents.value = (Array.isArray(list) ? list : [])
            .map((item) => {
                const agentId = item?.agentId || '';
                if (!agentId) return null;
                return {
                    agentId,
                    agentName: item?.agentName || item?.name || agentId,
                    agentDesc: item?.agentDesc || item?.desc || ''
                };
            })
            .filter(Boolean);
    } catch (error) {
        notifyAppError(error, '获取 MiniAgent 列表失败');
        taskAgents.value = [];
    }
};

const loadTaskList = async () => {
    taskLoading.value = true;
    taskError.value = '';
    try {
        const resp = await userTaskList();
        const list = pickData(resp, '获取 Task 失败') || [];
        taskList.value = Array.isArray(list) ? list : [];
    } catch (error) {
        taskList.value = [];
        notifyAppError(error, '获取 Task 失败');
    } finally {
        taskLoading.value = false;
    }
};

const taskAgentLabelMap = computed(() => {
    const map = new Map();
    taskAgents.value.forEach((item) => map.set(item.agentId, item.agentName || item.agentId));
    return map;
});

const selectedTaskAgentLabel = computed(() => taskAgentLabelMap.value.get(taskForm.agentId) || '请选择 MiniAgent');
const selectedTaskStatusLabel = computed(() => taskStatusOptions.find((item) => item.value === Number(taskForm.taskStatus))?.label || '启用');

const formatApiDisplayUrl = (item) => {
    const baseUrl = String(item?.apiBaseUrl || '').trim();
    const completionPath = String(item?.apiCompletionPath || '').trim();
    if (baseUrl && completionPath) return `${baseUrl}${completionPath}`;
    return baseUrl || completionPath || '-';
};

const formatMcpTypeLabel = (value) => {
    const text = String(value || '').trim();
    return text ? text.toUpperCase() : 'UNKNOWN';
};

const formatModelTypeLabel = (value) => {
    const text = String(value || '').trim();
    return text || 'chat';
};

const closeTaskDropdowns = () => {
    taskAgentDropdownOpen.value = false;
    taskStatusDropdownOpen.value = false;
};

const toggleTaskDropdown = (key) => {
    if (key === 'agent') {
        taskAgentDropdownOpen.value = !taskAgentDropdownOpen.value;
        taskStatusDropdownOpen.value = false;
        return;
    }
    taskStatusDropdownOpen.value = !taskStatusDropdownOpen.value;
    taskAgentDropdownOpen.value = false;
};

const selectTaskAgent = (agentId) => {
    taskForm.agentId = agentId;
    taskAgentDropdownOpen.value = false;
};

const selectTaskStatus = (status) => {
    taskForm.taskStatus = Number(status) === 0 ? 0 : 1;
    taskStatusDropdownOpen.value = false;
};

const filteredTaskList = computed(() => {
    const keyword = taskKeyword.value.trim().toLowerCase();
    if (!keyword) return taskList.value;
    return taskList.value.filter((item) => {
        const fields = [
            item?.taskId,
            item?.taskDesc,
            item?.taskCron,
            item?.agentId,
            taskAgentLabelMap.value.get(item?.agentId || '')
        ];
        return fields.some((field) => String(field || '').toLowerCase().includes(keyword));
    });
});

const formatTaskTime = (value) => {
    if (!value) return '暂无时间';
    const date = new Date(value);
    if (Number.isNaN(date.getTime())) return String(value);
    return date.toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
    });
};

const getTaskStatusLabel = (status) => (Number(status) === 1 ? '启用中' : '已禁用');

const buildTaskPayload = (form) => {
    const agentId = (form.agentId || '').trim();
    const taskCron = (form.taskCron || '').trim();
    const taskDesc = (form.taskDesc || '').trim();
    const taskParam = ensureJsonText(form.taskParam, 'Task 参数');
    if (!agentId || !taskCron || !taskDesc) {
        throw new Error('请完整填写 Task 必填项（MiniAgent、执行周期、任务描述、任务参数）');
    }
    const payload = {
        agentId,
        taskCron,
        taskDesc,
        taskParam,
        taskStatus: Number(form.taskStatus) === 0 ? 0 : 1
    };
    if (taskEditing.value && form.taskId.trim()) {
        payload.taskId = form.taskId.trim();
    }
    return payload;
};

const editTask = (item) => {
    if (!item) return;
    taskEditing.value = true;
    taskError.value = '';
    taskForm.taskId = (item.taskId || '').trim();
    taskForm.agentId = (item.agentId || '').trim();
    taskForm.taskCron = item.taskCron || '';
    taskForm.taskDesc = item.taskDesc || '';
    taskForm.taskParam = item.taskParam || '{"maxRetry":2,"maxRound":2,"userMessage":""}';
    taskForm.taskStatus = Number(item.taskStatus) === 0 ? 0 : 1;
    closeTaskDropdowns();
    taskFormDialogOpen.value = true;
};

const openTaskCreateDialog = () => {
    taskError.value = '';
    resetTaskForm();
    taskFormDialogOpen.value = true;
};

const submitTask = async () => {
    taskError.value = '';
    let payload = null;
    try {
        payload = buildTaskPayload(taskForm);
        if (taskEditing.value && !payload.taskId) {
            throw new Error('Task 标识缺失，请刷新后重试');
        }
    } catch (error) {
        taskError.value = normalizeError(error).message || (taskEditing.value ? '更新 Task 失败' : '新增 Task 失败');
        return;
    }
    taskSaving.value = true;
    try {
        if (taskEditing.value) {
            await userTaskUpdate(payload);
        } else {
            await userTaskInsert(payload);
        }
        resetTaskForm();
        taskFormDialogOpen.value = false;
        await loadTaskList();
    } catch (error) {
        notifyAppError(error, taskEditing.value ? '更新 Task 失败' : '新增 Task 失败');
    } finally {
        taskSaving.value = false;
    }
};

const confirmDeleteTask = (item) => {
    taskDeleteTarget.value = item || null;
    taskDeleteConfirmOpen.value = Boolean(item?.taskId);
};

const doDeleteTask = async () => {
    if (!taskDeleteTarget.value?.taskId) return;
    taskError.value = '';
    taskSaving.value = true;
    try {
        await userTaskDelete(taskDeleteTarget.value.taskId);
        if (taskForm.taskId && taskForm.taskId === taskDeleteTarget.value.taskId) {
            resetTaskForm();
        }
        taskDeleteConfirmOpen.value = false;
        taskDeleteTarget.value = null;
        await loadTaskList();
    } catch (error) {
        notifyAppError(error, '删除 Task 失败');
    } finally {
        taskSaving.value = false;
    }
};

const toggleTaskStatus = async (item) => {
    if (!item?.taskId) return;
    taskError.value = '';
    try {
        const nextStatus = Number(item.taskStatus) === 1 ? 0 : 1;
        await userTaskToggle(item.taskId, nextStatus);
        if (taskForm.taskId && taskForm.taskId === item.taskId) {
            taskForm.taskStatus = nextStatus;
        }
        await loadTaskList();
    } catch (error) {
        notifyAppError(error, '切换 Task 状态失败');
    }
};

const handleTaskDropdownOutside = (event) => {
    const target = event?.target;
    if (
        taskAgentDropdownRef.value &&
        !taskAgentDropdownRef.value.contains(target) &&
        taskStatusDropdownRef.value &&
        !taskStatusDropdownRef.value.contains(target)
    ) {
        closeTaskDropdowns();
        return;
    }
    if (taskAgentDropdownRef.value && !taskAgentDropdownRef.value.contains(target)) {
        taskAgentDropdownOpen.value = false;
    }
    if (taskStatusDropdownRef.value && !taskStatusDropdownRef.value.contains(target)) {
        taskStatusDropdownOpen.value = false;
    }
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
    resetTaskForm();
    document.addEventListener('pointerdown', handleTaskDropdownOutside);
    await Promise.all([loadUserProfile(), loadMcpList(), loadApiList(), loadTaskList(), loadTaskAgents()]);
});

onBeforeUnmount(() => {
    document.removeEventListener('pointerdown', handleTaskDropdownOutside);
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
                            <button
                                class="border-b-2 px-[2px] pb-[10px] pt-[2px] text-[14px] font-semibold transition"
                                :class="activeTab === 'task' ? 'border-[var(--accent-color)] text-[var(--accent-color)]' : 'border-transparent text-[var(--text-secondary)] hover:text-[var(--text-primary)]'"
                                @click="activeTab = 'task'"
                            >
                                Task
                            </button>
                        </nav>
                    </div>
                </div>
                <div class="h-[1px] w-full bg-[var(--border-color)]"></div>

                <div v-if="activeTab === 'profile'" class="space-y-[14px]">
                    <div>
                        <div class="mb-[10px] text-[13px] font-semibold text-[var(--text-secondary)]">头像设置</div>
                        <div class="flex flex-wrap items-center gap-[12px]">
                            <div
                                class="grid h-[84px] w-[84px] shrink-0 cursor-pointer place-items-center overflow-hidden rounded-full border text-[24px] font-bold transition hover:brightness-95"
                                :class="
                                    canShowProfileAvatarImage
                                        ? 'border-[rgba(15,23,42,0.12)] bg-transparent'
                                        : profileAvatarFallbackClass
                                "
                                title="点击上传头像"
                                @click="triggerProfileAvatarUpload"
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
                    <div class="flex items-center justify-start gap-[10px] pt-[14px]">
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
                        <div class="flex flex-wrap items-center gap-[10px]">
                            <div class="flex w-full items-center gap-[8px] md:w-[360px]">
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
                                <button
                                    class="inline-flex h-[37px] w-[37px] items-center justify-center rounded-[10px] border border-[var(--border-color)] text-[var(--text-secondary)] transition hover:bg-[#eef2f7] hover:text-[var(--text-primary)]"
                                    type="button"
                                    aria-label="新增 MCP"
                                    title="新增 MCP"
                                    @click="openMcpCreateDialog"
                                >
                                    <svg viewBox="0 0 24 24" class="h-[16px] w-[16px]" fill="none" stroke="currentColor" stroke-width="1.8" aria-hidden="true">
                                        <path d="M12 5v14M5 12h14" stroke-linecap="round" />
                                    </svg>
                                </button>
                            </div>
                        </div>
                        <div v-if="mcpLoading" class="text-[12px] text-[var(--text-secondary)]">加载中...</div>
                        <div v-else-if="mcpList.length === 0" class="text-[12px] text-[var(--text-secondary)]">暂无 MCP 配置</div>
                        <div v-else class="grid gap-[10px] lg:grid-cols-3">
                            <button
                                v-for="item in mcpList"
                                :key="item.mcpId"
                                class="w-full rounded-[12px] border border-[var(--border-color)] p-[14px] text-left transition hover:border-[var(--accent-color)] hover:bg-[#f8fafc]"
                                @click="openMcpDialog(item)"
                            >
                                <div class="flex min-w-0 flex-wrap items-center gap-x-[10px] gap-y-[6px]">
                                    <div class="text-[15px] font-semibold text-[var(--text-primary)]">
                                        {{ item.mcpName || '-' }}
                                    </div>
                                    <span class="shrink-0 rounded-full border border-[rgba(59,130,246,0.16)] bg-[rgba(59,130,246,0.08)] px-[9px] py-[3px] text-[11px] font-semibold uppercase tracking-[0.06em] text-[#4f6f95]">
                                        {{ formatMcpTypeLabel(item.mcpType) }}
                                    </span>
                                </div>
                                <div class="mt-[6px] text-[12px] leading-[1.6] text-[var(--text-secondary)]">
                                    {{ item.mcpDesc || '暂无描述' }}
                                </div>
                            </button>
                        </div>
                    </div>
                </div>

                <div v-else-if="activeTab === 'api'" class="space-y-[14px]">
                    <div class="space-y-[10px]">
                        <div class="flex flex-wrap items-center gap-[10px]">
                            <div class="flex w-full items-center gap-[8px] md:w-[360px]">
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
                                <button
                                    class="inline-flex h-[37px] w-[37px] items-center justify-center rounded-[10px] border border-[var(--border-color)] text-[var(--text-secondary)] transition hover:bg-[#eef2f7] hover:text-[var(--text-primary)]"
                                    type="button"
                                    aria-label="新增 API"
                                    title="新增 API"
                                    @click="openApiCreateDialog"
                                >
                                    <svg viewBox="0 0 24 24" class="h-[16px] w-[16px]" fill="none" stroke="currentColor" stroke-width="1.8" aria-hidden="true">
                                        <path d="M12 5v14M5 12h14" stroke-linecap="round" />
                                    </svg>
                                </button>
                            </div>
                        </div>
                        <div v-if="apiLoading" class="text-[12px] text-[var(--text-secondary)]">加载中...</div>
                        <div v-else-if="apiList.length === 0" class="text-[12px] text-[var(--text-secondary)]">暂无 API 配置</div>
                        <div v-else class="grid gap-[10px] lg:grid-cols-3">
                            <button
                                v-for="item in apiList"
                                :key="item.apiId"
                                class="w-full rounded-[12px] border border-[var(--border-color)] p-[14px] text-left transition hover:border-[var(--accent-color)] hover:bg-[#f8fafc]"
                                @click="openApiDialog(item)"
                            >
                                <div class="flex min-w-0 flex-wrap items-center gap-x-[10px] gap-y-[6px]">
                                    <div class="text-[15px] font-semibold text-[var(--text-primary)]">
                                        {{ item.modelName || '-' }}
                                    </div>
                                    <span class="shrink-0 rounded-full border border-[rgba(59,130,246,0.16)] bg-[rgba(59,130,246,0.08)] px-[9px] py-[3px] text-[11px] font-semibold text-[#4f6f95]">
                                        {{ formatModelTypeLabel(item.modelType || 'chat') }}
                                    </span>
                                </div>
                                <div class="mt-[6px] text-[12px] leading-[1.6] text-[var(--text-secondary)] break-all">
                                    {{ formatApiDisplayUrl(item) }}
                                </div>
                            </button>
                        </div>
                    </div>
                </div>

                <div v-else class="space-y-[14px]">
                    <div class="space-y-[10px]">
                        <div class="flex flex-wrap items-center gap-[10px]">
                            <div class="flex w-full items-center gap-[8px] md:w-[360px]">
                                <input
                                    v-model="taskKeyword"
                                    class="min-w-0 flex-1 rounded-[10px] border border-[var(--border-color)] px-[10px] py-[9px] text-[13px]"
                                    placeholder="输入关键字查询 Task"
                                />
                                <button
                                    class="inline-flex h-[37px] w-[37px] items-center justify-center rounded-[10px] border border-[var(--border-color)] text-[var(--text-secondary)] transition hover:bg-[#eef2f7] hover:text-[var(--text-primary)]"
                                    type="button"
                                    aria-label="刷新 Task"
                                    title="刷新 Task"
                                    @click="loadTaskList"
                                >
                                    <svg viewBox="0 0 24 24" class="h-[16px] w-[16px]" fill="none" stroke="currentColor" stroke-width="1.8" aria-hidden="true">
                                        <path d="M21 12a9 9 0 1 1-2.64-6.36" />
                                        <path d="M21 3v6h-6" />
                                    </svg>
                                </button>
                                <button
                                    class="inline-flex h-[37px] w-[37px] items-center justify-center rounded-[10px] border border-[var(--border-color)] text-[var(--text-secondary)] transition hover:bg-[#eef2f7] hover:text-[var(--text-primary)]"
                                    type="button"
                                    aria-label="新增 Task"
                                    title="新增 Task"
                                    @click="openTaskCreateDialog"
                                >
                                    <svg viewBox="0 0 24 24" class="h-[16px] w-[16px]" fill="none" stroke="currentColor" stroke-width="1.8" aria-hidden="true">
                                        <path d="M12 5v14M5 12h14" stroke-linecap="round" />
                                    </svg>
                                </button>
                            </div>
                        </div>
                        <div v-if="taskLoading" class="text-[12px] text-[var(--text-secondary)]">加载中...</div>
                        <div v-else-if="filteredTaskList.length === 0" class="text-[12px] text-[var(--text-secondary)]">暂无 Task 配置</div>
                        <div v-else class="space-y-[8px]">
                            <div
                                v-for="item in filteredTaskList"
                                :key="item.taskId"
                                class="rounded-[10px] border border-[var(--border-color)] p-[12px] transition hover:border-[var(--accent-color)] hover:bg-[#f8fafc]"
                            >
                                <div class="flex flex-wrap items-start justify-between gap-[10px]">
                                    <button class="min-w-0 flex-1 text-left" @click="editTask(item)">
                                        <div class="flex flex-wrap items-center gap-[8px]">
                                            <div class="text-[14px] font-semibold">Task 描述：{{ item.taskDesc || '暂无描述' }}</div>
                                            <span
                                                class="rounded-full px-[8px] py-[2px] text-[11px] font-semibold"
                                                :class="Number(item.taskStatus) === 1 ? 'bg-[rgba(16,185,129,0.12)] text-[#047857]' : 'bg-[rgba(148,163,184,0.18)] text-[#475569]'"
                                            >
                                                {{ getTaskStatusLabel(item.taskStatus) }}
                                            </span>
                                        </div>
                                        <div class="mt-[4px] text-[12px] text-[var(--text-secondary)]">
                                            MiniAgent：{{ taskAgentLabelMap.get(item.agentId) || item.agentId || '-' }}
                                        </div>
                                        <div class="mt-[2px] text-[12px] text-[var(--text-secondary)]">
                                            Cron：{{ item.taskCron || '-' }}
                                        </div>
                                        <div class="mt-[2px] text-[12px] text-[var(--text-secondary)]">
                                            更新时间：{{ formatTaskTime(item.updateTime) }}
                                        </div>
                                    </button>
                                    <div class="flex items-center gap-[8px]">
                                        <button
                                            class="rounded-[10px] border border-[var(--border-color)] px-[10px] py-[7px] text-[12px] font-semibold text-[var(--text-primary)] transition hover:bg-[#eef2f7]"
                                            @click="toggleTaskStatus(item)"
                                        >
                                            {{ Number(item.taskStatus) === 1 ? '禁用' : '启用' }}
                                        </button>
                                        <button
                                            class="rounded-[10px] border border-[rgba(248,113,113,0.28)] px-[10px] py-[7px] text-[12px] font-semibold text-[#dc2626] transition hover:bg-[rgba(254,242,242,0.9)]"
                                            @click="confirmDeleteTask(item)"
                                        >
                                            删除
                                        </button>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
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
            v-if="mcpCreateDialogOpen"
            class="fixed inset-0 z-[41] grid place-items-center bg-[rgba(0,0,0,0.35)] p-[20px]"
            @click.self="mcpCreateDialogOpen = false"
        >
            <div class="w-full max-w-[760px] rounded-[14px] bg-white p-[18px] shadow-[0_20px_50px_rgba(15,23,42,0.24)]">
                <div class="mb-[12px] flex items-center justify-between">
                    <div class="text-[16px] font-semibold">新增个人 MCP</div>
                    <button class="text-[20px] text-[var(--text-secondary)]" @click="mcpCreateDialogOpen = false">×</button>
                </div>
                <div class="space-y-[10px]">
                    <label class="grid items-start gap-[10px] text-[13px] md:grid-cols-[140px_1fr]">
                        <span class="pt-[10px] text-[var(--text-secondary)]">MCP 名称</span>
                        <input v-model="mcpForm.mcpName" class="rounded-[10px] border border-[var(--border-color)] px-[10px] py-[10px]" placeholder="请输入 MCP 名称" />
                    </label>
                    <label class="grid items-start gap-[10px] text-[13px] md:grid-cols-[140px_1fr]">
                        <span class="pt-[10px] text-[var(--text-secondary)]">MCP 描述</span>
                        <input v-model="mcpForm.mcpDesc" class="rounded-[10px] border border-[var(--border-color)] px-[10px] py-[10px]" placeholder="请输入 MCP 描述" />
                    </label>
                    <label class="grid items-start gap-[10px] text-[13px] md:grid-cols-[140px_1fr]">
                        <span class="pt-[10px] text-[var(--text-secondary)]">MCP 类型</span>
                        <div class="relative">
                            <select
                                v-model="mcpForm.mcpType"
                                class="w-full appearance-none rounded-[10px] border border-[var(--border-color)] bg-white px-[10px] py-[10px] pr-[34px] text-[13px] text-[var(--text-primary)] outline-none focus:border-[var(--accent-color)]"
                            >
                                <option value="" disabled>请选择 MCP 类型</option>
                                <option v-for="option in MCP_TYPE_OPTIONS" :key="option.value" :value="option.value">
                                    {{ option.label }}
                                </option>
                            </select>
                            <svg class="pointer-events-none absolute right-[10px] top-1/2 h-[14px] w-[14px] -translate-y-1/2 text-[var(--text-secondary)]" viewBox="0 0 20 20" fill="currentColor">
                                <path
                                    fill-rule="evenodd"
                                    d="M5.23 7.21a.75.75 0 0 1 1.06.02L10 11.113l3.71-3.884a.75.75 0 1 1 1.08 1.04l-4.25 4.45a.75.75 0 0 1-1.08 0l-4.25-4.45a.75.75 0 0 1 .02-1.06Z"
                                    clip-rule="evenodd"
                                />
                            </svg>
                        </div>
                    </label>
                    <label class="grid items-start gap-[10px] text-[13px] md:grid-cols-[140px_1fr]">
                        <span class="pt-[10px] text-[var(--text-secondary)]">MCP 配置</span>
                        <textarea
                            v-model="mcpForm.mcpParam"
                            class="min-h-[110px] rounded-[10px] border border-[var(--border-color)] px-[10px] py-[10px] font-mono text-[12px] leading-[1.6]"
                            placeholder='可选，JSON 格式；例如 {"baseUri":"http://127.0.0.1:9002","sseEndPoint":"/sse"}'
                            @blur="prettifyMcpParam('form')"
                        ></textarea>
                    </label>
                    <div v-if="mcpParamFormatError" class="text-[12px] text-[#ef4444]">{{ mcpParamFormatError }}</div>
                    <label class="grid items-start gap-[10px] text-[13px] md:grid-cols-[140px_1fr]">
                        <span class="pt-[10px] text-[var(--text-secondary)]">MCP 密钥配置</span>
                        <div class="space-y-[8px]">
                            <div class="overflow-hidden rounded-[10px] border border-[var(--border-color)]">
                                <div class="grid grid-cols-[1fr_1fr_68px] bg-[#f8fafc] px-[10px] py-[8px] text-[12px] font-semibold text-[var(--text-secondary)]">
                                    <span>Key</span>
                                    <span>Value</span>
                                    <span class="text-center">操作</span>
                                </div>
                                <div class="max-h-[220px] overflow-y-auto">
                                    <div
                                        v-for="row in mcpSecretRows"
                                        :key="row.rowId"
                                        class="grid grid-cols-[1fr_1fr_68px] items-center gap-[8px] border-t border-[var(--border-color)] px-[10px] py-[8px]"
                                    >
                                        <input
                                            v-model="row.key"
                                            class="min-w-0 rounded-[8px] border border-[var(--border-color)] px-[8px] py-[7px] text-[12px] outline-none focus:border-[var(--accent-color)]"
                                            placeholder="请输入 key"
                                        />
                                        <input
                                            v-model="row.value"
                                            class="min-w-0 rounded-[8px] border border-[var(--border-color)] px-[8px] py-[7px] text-[12px] outline-none focus:border-[var(--accent-color)]"
                                            placeholder="请输入 value"
                                        />
                                        <button
                                            type="button"
                                            class="rounded-[8px] border border-[var(--border-color)] px-[6px] py-[6px] text-[12px] text-[var(--text-secondary)] transition hover:border-[rgba(248,113,113,0.35)] hover:text-[#dc2626]"
                                            @click="removeMcpSecretRow('form', row.rowId)"
                                        >
                                            删除
                                        </button>
                                    </div>
                                    <div class="grid grid-cols-[1fr_1fr_68px] items-center border-t border-[var(--border-color)] px-[10px] py-[8px]">
                                        <div class="col-span-3 flex justify-center">
                                            <button
                                                type="button"
                                                class="rounded-[8px] border border-[var(--border-color)] px-[12px] py-[4px] text-[12px] font-semibold text-[var(--text-primary)] transition hover:border-[var(--accent-color)] hover:text-[var(--accent-color)]"
                                                @click="addMcpSecretRow('form')"
                                            >
                                                + 添加
                                            </button>
                                        </div>
                                    </div>
                                </div>
                            </div>
                            <div v-if="mcpSecretTableError" class="text-[12px] text-[#ef4444]">{{ mcpSecretTableError }}</div>
                        </div>
                    </label>
                </div>
                <div v-if="mcpError" class="mt-[10px] text-[12px] text-[#ef4444]">{{ mcpError }}</div>
                <div class="mt-[12px] flex justify-end gap-[8px]">
                    <button class="rounded-[10px] border border-[var(--border-color)] px-[12px] py-[8px] text-[13px]" @click="mcpCreateDialogOpen = false">取消</button>
                    <button
                        class="rounded-[10px] border border-[var(--accent-color)] bg-[var(--accent-color)] px-[12px] py-[8px] text-[13px] font-semibold text-white disabled:opacity-70"
                        :disabled="mcpSaving"
                        @click="submitMcpInsert"
                    >
                        确定
                    </button>
                </div>
            </div>
        </div>

        <div
            v-if="apiCreateDialogOpen"
            class="fixed inset-0 z-[41] grid place-items-center bg-[rgba(0,0,0,0.35)] p-[20px]"
            @click.self="apiCreateDialogOpen = false"
        >
            <div class="w-full max-w-[760px] rounded-[14px] bg-white p-[18px] shadow-[0_20px_50px_rgba(15,23,42,0.24)]">
                <div class="mb-[12px] flex items-center justify-between">
                    <div class="text-[16px] font-semibold">新增个人 API</div>
                    <button class="text-[20px] text-[var(--text-secondary)]" @click="apiCreateDialogOpen = false">×</button>
                </div>
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
                        <span class="pt-[10px] text-[var(--text-secondary)]">模型类别</span>
                        <input v-model="apiForm.modelType" class="rounded-[10px] border border-[var(--border-color)] px-[10px] py-[10px]" placeholder="请输入模型类别，例如 chat" />
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
                <div v-if="apiError" class="mt-[10px] text-[12px] text-[#ef4444]">{{ apiError }}</div>
                <div class="mt-[12px] flex justify-end gap-[8px]">
                    <button class="rounded-[10px] border border-[var(--border-color)] px-[12px] py-[8px] text-[13px]" @click="apiCreateDialogOpen = false">取消</button>
                    <button
                        class="rounded-[10px] border border-[var(--accent-color)] bg-[var(--accent-color)] px-[12px] py-[8px] text-[13px] font-semibold text-white disabled:opacity-70"
                        :disabled="apiSaving"
                        @click="submitApiInsert"
                    >
                        确定
                    </button>
                </div>
            </div>
        </div>

        <div
            v-if="taskFormDialogOpen"
            class="fixed inset-0 z-[41] grid place-items-center bg-[rgba(0,0,0,0.35)] p-[20px]"
            @click.self="taskFormDialogOpen = false"
        >
            <div class="w-full max-w-[820px] rounded-[14px] bg-white p-[18px] shadow-[0_20px_50px_rgba(15,23,42,0.24)]">
                <div class="mb-[12px] flex items-center justify-between">
                    <div class="text-[16px] font-semibold">{{ taskEditing ? '编辑 Task' : '新增 Task' }}</div>
                    <button class="text-[20px] text-[var(--text-secondary)]" @click="taskFormDialogOpen = false">×</button>
                </div>
                <div class="space-y-[10px]">
                    <label class="grid items-start gap-[10px] text-[13px] md:grid-cols-[140px_1fr]">
                        <span class="pt-[10px] text-[var(--text-secondary)]">MiniAgent</span>
                        <div ref="taskAgentDropdownRef" class="relative">
                            <button
                                class="flex w-full items-center justify-between rounded-[12px] border border-[var(--border-color)] bg-white px-[12px] py-[10px] text-left text-[13px] text-[var(--text-primary)] outline-none transition hover:border-[var(--accent-color)] hover:bg-[#f8fafc]"
                                type="button"
                                :class="taskAgentDropdownOpen ? 'border-[var(--accent-color)] shadow-[0_0_0_3px_rgba(59,130,246,0.08)]' : ''"
                                @click="toggleTaskDropdown('agent')"
                            >
                                <span class="truncate">{{ selectedTaskAgentLabel }}</span>
                                <svg
                                    viewBox="0 0 20 20"
                                    class="h-[14px] w-[14px] shrink-0 text-[var(--text-secondary)] transition-transform duration-200"
                                    :class="taskAgentDropdownOpen ? 'rotate-180' : ''"
                                    fill="currentColor"
                                    aria-hidden="true"
                                >
                                    <path d="M5.5 7.5 10 12l4.5-4.5H5.5z" />
                                </svg>
                            </button>
                            <div
                                v-if="taskAgentDropdownOpen"
                                class="absolute left-0 right-0 top-[calc(100%+8px)] z-[20] max-h-[240px] overflow-y-auto rounded-[14px] border border-[var(--border-color)] bg-white p-[6px] shadow-[0_18px_38px_rgba(15,23,42,0.14)]"
                            >
                                <button
                                    class="flex w-full items-center rounded-[10px] px-[10px] py-[9px] text-left text-[13px] text-[var(--text-primary)] transition hover:bg-[#f8fafc]"
                                    :class="!taskForm.agentId ? 'bg-[rgba(59,130,246,0.08)] text-[var(--accent-color)]' : ''"
                                    type="button"
                                    @click="selectTaskAgent('')"
                                >
                                    请选择 MiniAgent
                                </button>
                                <button
                                    v-for="item in taskAgents"
                                    :key="item.agentId"
                                    class="flex w-full items-center rounded-[10px] px-[10px] py-[9px] text-left text-[13px] text-[var(--text-primary)] transition hover:bg-[#f8fafc]"
                                    :class="taskForm.agentId === item.agentId ? 'bg-[rgba(59,130,246,0.08)] text-[var(--accent-color)]' : ''"
                                    type="button"
                                    @click="selectTaskAgent(item.agentId)"
                                >
                                    <span class="truncate">{{ item.agentName }}</span>
                                </button>
                            </div>
                        </div>
                    </label>
                    <label class="grid items-start gap-[10px] text-[13px] md:grid-cols-[140px_1fr]">
                        <span class="pt-[10px] text-[var(--text-secondary)]">执行周期</span>
                        <input
                            v-model="taskForm.taskCron"
                            class="rounded-[10px] border border-[var(--border-color)] px-[10px] py-[10px]"
                            placeholder="请输入 Cron 表达式，例如 0 0/30 * * * ?"
                        />
                    </label>
                    <label class="grid items-start gap-[10px] text-[13px] md:grid-cols-[140px_1fr]">
                        <span class="pt-[10px] text-[var(--text-secondary)]">任务描述</span>
                        <input
                            v-model="taskForm.taskDesc"
                            class="rounded-[10px] border border-[var(--border-color)] px-[10px] py-[10px]"
                            placeholder="请输入任务描述，便于后续识别"
                        />
                    </label>
                    <label class="grid items-start gap-[10px] text-[13px] md:grid-cols-[140px_1fr]">
                        <span class="pt-[10px] text-[var(--text-secondary)]">任务参数</span>
                        <textarea
                            v-model="taskForm.taskParam"
                            class="min-h-[108px] rounded-[10px] border border-[var(--border-color)] px-[10px] py-[10px]"
                            placeholder='请输入 JSON，例如 {"maxRetry":2,"maxRound":2,"userMessage":"请汇总今天的关键数据"}'
                        ></textarea>
                    </label>
                    <label class="grid items-start gap-[10px] text-[13px] md:grid-cols-[140px_1fr]">
                        <span class="pt-[10px] text-[var(--text-secondary)]">状态</span>
                        <div ref="taskStatusDropdownRef" class="relative">
                            <button
                                class="flex w-full items-center justify-between rounded-[12px] border border-[var(--border-color)] bg-white px-[12px] py-[10px] text-left text-[13px] text-[var(--text-primary)] outline-none transition hover:border-[var(--accent-color)] hover:bg-[#f8fafc]"
                                type="button"
                                :class="taskStatusDropdownOpen ? 'border-[var(--accent-color)] shadow-[0_0_0_3px_rgba(59,130,246,0.08)]' : ''"
                                @click="toggleTaskDropdown('status')"
                            >
                                <span>{{ selectedTaskStatusLabel }}</span>
                                <svg
                                    viewBox="0 0 20 20"
                                    class="h-[14px] w-[14px] shrink-0 text-[var(--text-secondary)] transition-transform duration-200"
                                    :class="taskStatusDropdownOpen ? 'rotate-180' : ''"
                                    fill="currentColor"
                                    aria-hidden="true"
                                >
                                    <path d="M5.5 7.5 10 12l4.5-4.5H5.5z" />
                                </svg>
                            </button>
                            <div
                                v-if="taskStatusDropdownOpen"
                                class="absolute left-0 right-0 top-[calc(100%+8px)] z-[20] overflow-hidden rounded-[14px] border border-[var(--border-color)] bg-white p-[6px] shadow-[0_18px_38px_rgba(15,23,42,0.14)]"
                            >
                                <button
                                    v-for="item in taskStatusOptions"
                                    :key="item.value"
                                    class="flex w-full items-center rounded-[10px] px-[10px] py-[9px] text-left text-[13px] text-[var(--text-primary)] transition hover:bg-[#f8fafc]"
                                    :class="Number(taskForm.taskStatus) === item.value ? 'bg-[rgba(59,130,246,0.08)] text-[var(--accent-color)]' : ''"
                                    type="button"
                                    @click="selectTaskStatus(item.value)"
                                >
                                    {{ item.label }}
                                </button>
                            </div>
                        </div>
                    </label>
                </div>
                <div v-if="taskError" class="mt-[10px] text-[12px] text-[#ef4444]">{{ taskError }}</div>
                <div class="mt-[12px] flex justify-end gap-[8px]">
                    <button
                        class="rounded-[10px] border border-[var(--border-color)] px-[12px] py-[8px] text-[13px]"
                        @click="
                            taskFormDialogOpen = false;
                            if (taskEditing) resetTaskForm();
                        "
                    >
                        取消
                    </button>
                    <button
                        class="rounded-[10px] border border-[var(--accent-color)] bg-[var(--accent-color)] px-[12px] py-[8px] text-[13px] font-semibold text-white disabled:opacity-70"
                        :disabled="taskSaving"
                        @click="submitTask"
                    >
                        确定
                    </button>
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
                        <span class="pt-[10px] text-[var(--text-secondary)]">MCP 描述</span>
                        <input v-model="mcpDialogForm.mcpDesc" class="rounded-[10px] border border-[var(--border-color)] px-[10px] py-[10px]" />
                    </label>
                    <label class="grid items-start gap-[10px] text-[13px] md:grid-cols-[140px_1fr]">
                        <span class="pt-[10px] text-[var(--text-secondary)]">MCP 类型</span>
                        <div class="relative">
                            <select
                                v-model="mcpDialogForm.mcpType"
                                class="w-full appearance-none rounded-[10px] border border-[var(--border-color)] bg-white px-[10px] py-[10px] pr-[34px] text-[13px] text-[var(--text-primary)] outline-none focus:border-[var(--accent-color)]"
                            >
                                <option value="" disabled>请选择 MCP 类型</option>
                                <option v-for="option in MCP_TYPE_OPTIONS" :key="option.value" :value="option.value">
                                    {{ option.label }}
                                </option>
                            </select>
                            <svg class="pointer-events-none absolute right-[10px] top-1/2 h-[14px] w-[14px] -translate-y-1/2 text-[var(--text-secondary)]" viewBox="0 0 20 20" fill="currentColor">
                                <path
                                    fill-rule="evenodd"
                                    d="M5.23 7.21a.75.75 0 0 1 1.06.02L10 11.113l3.71-3.884a.75.75 0 1 1 1.08 1.04l-4.25 4.45a.75.75 0 0 1-1.08 0l-4.25-4.45a.75.75 0 0 1 .02-1.06Z"
                                    clip-rule="evenodd"
                                />
                            </svg>
                        </div>
                    </label>
                    <label class="grid items-start gap-[10px] text-[13px] md:grid-cols-[140px_1fr]">
                        <span class="pt-[10px] text-[var(--text-secondary)]">MCP 配置</span>
                        <textarea
                            v-model="mcpDialogForm.mcpParam"
                            class="min-h-[110px] rounded-[10px] border border-[var(--border-color)] px-[10px] py-[10px] font-mono text-[12px] leading-[1.6]"
                            @blur="prettifyMcpParam('dialog')"
                        ></textarea>
                    </label>
                    <div v-if="mcpDialogParamFormatError" class="text-[12px] text-[#ef4444]">{{ mcpDialogParamFormatError }}</div>
                    <label class="grid items-start gap-[10px] text-[13px] md:grid-cols-[140px_1fr]">
                        <span class="pt-[10px] text-[var(--text-secondary)]">MCP 密钥配置</span>
                        <div class="space-y-[8px]">
                            <div class="overflow-hidden rounded-[10px] border border-[var(--border-color)]">
                                <div class="grid grid-cols-[1fr_1fr_68px] bg-[#f8fafc] px-[10px] py-[8px] text-[12px] font-semibold text-[var(--text-secondary)]">
                                    <span>Key</span>
                                    <span>Value</span>
                                    <span class="text-center">操作</span>
                                </div>
                                <div class="max-h-[220px] overflow-y-auto">
                                    <div
                                        v-for="row in mcpDialogSecretRows"
                                        :key="row.rowId"
                                        class="grid grid-cols-[1fr_1fr_68px] items-center gap-[8px] border-t border-[var(--border-color)] px-[10px] py-[8px]"
                                    >
                                        <input
                                            v-model="row.key"
                                            class="min-w-0 rounded-[8px] border border-[var(--border-color)] px-[8px] py-[7px] text-[12px] outline-none focus:border-[var(--accent-color)]"
                                            placeholder="请输入 key"
                                        />
                                        <input
                                            v-model="row.value"
                                            class="min-w-0 rounded-[8px] border border-[var(--border-color)] px-[8px] py-[7px] text-[12px] outline-none focus:border-[var(--accent-color)]"
                                            placeholder="请输入 value"
                                        />
                                        <button
                                            type="button"
                                            class="rounded-[8px] border border-[var(--border-color)] px-[6px] py-[6px] text-[12px] text-[var(--text-secondary)] transition hover:border-[rgba(248,113,113,0.35)] hover:text-[#dc2626]"
                                            @click="removeMcpSecretRow('dialog', row.rowId)"
                                        >
                                            删除
                                        </button>
                                    </div>
                                    <div class="grid grid-cols-[1fr_1fr_68px] items-center border-t border-[var(--border-color)] px-[10px] py-[8px]">
                                        <div class="col-span-3 flex justify-center">
                                            <button
                                                type="button"
                                                class="rounded-[8px] border border-[var(--border-color)] px-[12px] py-[4px] text-[12px] font-semibold text-[var(--text-primary)] transition hover:border-[var(--accent-color)] hover:text-[var(--accent-color)]"
                                                @click="addMcpSecretRow('dialog')"
                                            >
                                                + 添加
                                            </button>
                                        </div>
                                    </div>
                                </div>
                            </div>
                            <div v-if="mcpDialogSecretTableError" class="text-[12px] text-[#ef4444]">{{ mcpDialogSecretTableError }}</div>
                        </div>
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
                        <span class="pt-[10px] text-[var(--text-secondary)]">模型类别</span>
                        <input v-model="apiDialogForm.modelType" class="rounded-[10px] border border-[var(--border-color)] px-[10px] py-[10px]" />
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

        <div
            v-if="taskDeleteConfirmOpen"
            class="fixed inset-0 z-[41] grid place-items-center bg-[rgba(0,0,0,0.35)] p-[20px]"
            @click.self="taskDeleteConfirmOpen = false"
        >
            <div class="w-full max-w-[420px] rounded-[14px] bg-white p-[18px] shadow-[0_20px_50px_rgba(15,23,42,0.24)]">
                <div class="text-[16px] font-semibold text-[var(--text-primary)]">删除 Task</div>
                <div class="mt-[10px] text-[13px] leading-[1.7] text-[var(--text-secondary)]">
                    确认删除该 Task 吗？删除后将无法恢复。
                </div>
                <div class="mt-[14px] flex justify-end gap-[8px]">
                    <button class="rounded-[10px] border border-[var(--border-color)] px-[12px] py-[8px] text-[13px]" @click="taskDeleteConfirmOpen = false">取消</button>
                    <button
                        class="rounded-[10px] border border-[rgba(248,113,113,0.28)] bg-[rgba(254,242,242,0.92)] px-[12px] py-[8px] text-[13px] font-semibold text-[#dc2626] disabled:opacity-70"
                        :disabled="taskSaving"
                        @click="doDeleteTask"
                    >
                        {{ taskSaving ? '删除中...' : '确认删除' }}
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
