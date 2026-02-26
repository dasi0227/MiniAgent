<script setup>
import { computed, reactive, ref, onMounted, onBeforeUnmount, nextTick, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import Cropper from 'cropperjs';
import 'cropperjs/dist/cropper.css';
import logoImg from '../assets/logo.jpg';
import chatIconDark from '../assets/chat-white.svg';
import chatIconLight from '../assets/chat-black.svg';
import workIconDark from '../assets/work-white.svg';
import workIconLight from '../assets/work-black.svg';
import { useAgentStore, useAuthStore, useChatStore, useSettingsStore } from '../router/pinia';
import {
    deleteSession,
    fetchProfile,
    insertSession,
    listSessions,
    updatePassword,
    userApiDelete,
    userApiInsert,
    userApiList,
    userApiUpdate,
    updateSession,
    userMcpDelete,
    userMcpInsert,
    userMcpList,
    userMcpUpdate
} from '../request/api';
import { parseAuthPayload } from '../request/auth';
import { normalizeError } from '../request/request';
import { COLLAPSE_INNER_CLASS, getCollapseClasses } from '../utils/CollapseUtil';

const router = useRouter();
const route = useRoute();

const chatStore = useChatStore();
const agentStore = useAgentStore();
const authStore = useAuthStore();
const settingsStore = useSettingsStore();

const isLogin = computed(() => authStore.isLogin);
const currentUser = computed(() => authStore.user || { username: '访客', role: 'guest' });
const currentUserAvatarUrl = computed(() => {
    const raw = currentUser.value?.avatarUrl || currentUser.value?.userAvatar || '';
    return typeof raw === 'string' ? raw.trim() : '';
});
const avatarChar = computed(() => (currentUser.value.username || '访客').slice(0, 1).toUpperCase());
const showSidebarAvatarImage = ref(true);
const isDarkTheme = computed(() => settingsStore.theme === 'dark');
const sidebarShellClass = computed(() =>
    isDarkTheme.value
        ? 'bg-[radial-gradient(120%_120%_at_0%_0%,#122544_0%,#0f172a_60%,#0b1220_100%)] text-[#e7ecf4] border-[rgba(255,255,255,0.06)] shadow-[10px_0_30px_rgba(0,0,0,0.08)]'
        : 'bg-[radial-gradient(140%_130%_at_0%_0%,#2c476e_0%,#1f385a_50%,#162b46_100%)] text-[#edf3fb] border-[rgba(255,255,255,0.18)] shadow-[10px_0_30px_rgba(15,23,42,0.12)]'
);
const sidebarGhostButtonClass = computed(() =>
    isDarkTheme.value
        ? 'border-[rgba(255,255,255,0.2)] bg-[rgba(255,255,255,0.08)] hover:bg-[rgba(255,255,255,0.16)]'
        : 'border-[rgba(255,255,255,0.24)] bg-[rgba(255,255,255,0.14)] hover:bg-[rgba(255,255,255,0.22)]'
);
const sidebarIconButtonClass = computed(() =>
    isDarkTheme.value
        ? 'border-[rgba(255,255,255,0.18)] bg-[rgba(255,255,255,0.08)] text-[rgba(231,236,244,0.9)] hover:bg-[rgba(255,255,255,0.14)] hover:text-white'
        : 'border-[rgba(255,255,255,0.3)] bg-[rgba(255,255,255,0.2)] text-[rgba(237,243,251,0.95)] hover:bg-[rgba(255,255,255,0.3)] hover:text-white'
);
const sidebarProfileCardClass = computed(() =>
    isDarkTheme.value
        ? 'border-[rgba(255,255,255,0.08)] bg-[rgba(255,255,255,0.05)] hover:border-[rgba(123,200,255,0.35)] hover:bg-[rgba(123,200,255,0.08)]'
        : 'border-[rgba(255,255,255,0.2)] bg-[rgba(255,255,255,0.1)] hover:border-[rgba(144,203,255,0.5)] hover:bg-[rgba(144,203,255,0.14)]'
);
const chatIcon = computed(() => (isDarkTheme.value ? chatIconDark : chatIconLight));
const workIcon = computed(() => (isDarkTheme.value ? workIconDark : workIconLight));

const chats = computed(() => chatStore.chats);
const currentChatId = computed(() => chatStore.currentChatId);
const agentSessions = computed(() => agentStore.sessions);
const currentAgentSessionId = computed(() => agentStore.currentSessionId);
const isAgentRoute = computed(() => route.path.startsWith('/work'));
const isChatRoute = computed(() => route.path.startsWith('/chat'));
const isWelcomeRoute = computed(() => route.path.startsWith('/welcome'));
const isStudioRoute = computed(() => route.path.startsWith('/studio'));
const isPlazaRoute = computed(() => route.path.startsWith('/plaza'));

const showChatList = ref(true);
const showAgentList = ref(true);

const editingChatId = ref(null);
const editChatTitle = ref('');
const editingAgentId = ref(null);
const editAgentTitle = ref('');
const chatTitleInputRefs = ref({});
const agentTitleInputRefs = ref({});

const showDeleteConfirm = ref(false);
const deleteTarget = ref({ type: 'chat', id: '' });
const showNewSessionPicker = ref(false);
const showProfile = ref(false);
const showLogoutConfirm = ref(false);
const profileSaving = ref(false);
const profileLoading = ref(false);
const profileError = ref('');
const profileTab = ref('profile');
const mcpLoading = ref(false);
const mcpError = ref('');
const mcpList = ref([]);
const mcpEditing = ref(false);
const apiError = ref('');
const apiList = ref([]);
const apiEditing = ref(false);
const profileForm = reactive({
    username: currentUser.value.username || '',
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
const mcpForm = reactive({
    id: null,
    mcpName: '',
    mcpTransport: 'mcp',
    mcpConfig: '',
    mcpDesc: '',
    secretMapText: ''
});
const apiForm = reactive({
    id: null,
    modelName: '',
    modelType: '',
    apiBaseUrl: '',
    apiKey: '',
    apiCompletionPath: '/v1/chat/completions'
});
const sessionLoading = ref(false);
const sessionError = ref('');
const sessionLimitError = ref('');

const pickData = (resp, message = '操作失败') => {
    if (resp && typeof resp === 'object' && Object.prototype.hasOwnProperty.call(resp, 'code')) {
        if (resp.code !== 200) {
            const err = new Error(resp.info || message);
            err.status = 500;
            throw err;
        }
        return resp.data;
    }
    return resp?.data ?? resp?.result ?? resp;
};

const mapBackendMcpTypeToTransport = (mcpType) => {
    const normalized = (mcpType || '').toString().toLowerCase();
    return normalized === 'stdio' ? 'stdio' : 'mcp';
};

const mapTransportToBackendMcpType = (transport) => (transport === 'stdio' ? 'stdio' : 'sse');

const getMcpTransportLabel = (mcpType) => mapBackendMcpTypeToTransport(mcpType);

const mcpConfigPlaceholder = computed(() =>
    mcpForm.mcpTransport === 'stdio'
        ? '{"command":"npx","args":["-y","@modelcontextprotocol/server-filesystem","/tmp"]}'
        : '{"baseUri":"http://127.0.0.1:9002","sseEndPoint":"/sse"}'
);
const profileAvatarDisplayUrl = computed(() => profileAvatarPreviewUrl.value || currentUserAvatarUrl.value);
const canShowProfileAvatarImage = computed(() => Boolean(profileAvatarDisplayUrl.value) && !profileAvatarPreviewFallback.value);

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

const normalizeSessionType = (value) => (value ? value.toString().toLowerCase() : '');

const mapSession = (session) => {
    if (!session) return null;
    return {
        id: session.id,
        sessionId: session.sessionId,
        sessionUser: session.sessionUser,
        title: session.sessionTitle || '新会话',
        sessionType: normalizeSessionType(session.sessionType || session.type),
        createdAt: session.createTime ? Date.parse(session.createTime) : Date.now(),
        messages: [],
        cards: []
    };
};

const handleNewSession = () => {
    showNewSessionPicker.value = true;
    sessionLimitError.value = '';
};

const redirectWelcomeIfNoSession = () => {
    const isMainRoute =
        route.path.startsWith('/chat') || route.path.startsWith('/work') || route.path.startsWith('/welcome');
    if (!isMainRoute) return;
    if (chatStore.chats.length > 0 || agentStore.sessions.length > 0) return;
    if (route.path.startsWith('/welcome')) return;
    router.replace('/welcome');
};

const goWelcome = () => {
    if (!isWelcomeRoute.value) {
        router.push('/welcome');
    }
};

const goRoute = (path) => {
    if (route.path !== path) {
        router.push(path);
    }
};

const loadSessions = async () => {
    sessionLoading.value = true;
    sessionError.value = '';
    try {
        const resp = await listSessions();
        const list = pickData(resp, '获取会话失败') || [];
        const normalized = (Array.isArray(list) ? list : [])
            .map(mapSession)
            .filter(Boolean);
        const chatsList = normalized.filter((item) => item.sessionType === 'chat');
        const agentList = normalized.filter((item) => item.sessionType === 'work');
        chatStore.setChats(chatsList);
        agentStore.setSessions(agentList);

        const nextChatId =
            chatStore.currentChatId && chatsList.some((item) => item.id === chatStore.currentChatId)
                ? chatStore.currentChatId
                : chatsList[0]?.id || null;
        const nextAgentId =
            agentStore.currentSessionId && agentList.some((item) => item.id === agentStore.currentSessionId)
                ? agentStore.currentSessionId
                : agentList[0]?.id || null;
        chatStore.setCurrentChatId(nextChatId);
        agentStore.setCurrentSessionId(nextAgentId);
        redirectWelcomeIfNoSession();
    } catch (error) {
        sessionError.value = normalizeError(error).message || '获取会话失败';
        chatStore.setChats([]);
        agentStore.setSessions([]);
    } finally {
        sessionLoading.value = false;
    }
};

onMounted(() => {
    if (isLogin.value) {
        loadSessions();
    }
});

onBeforeUnmount(() => {
    resetProfileAvatarDraft();
});

watch(
    isLogin,
    (loggedIn) => {
        if (loggedIn) {
            loadSessions();
            return;
        }
        chatStore.setChats([]);
        agentStore.setSessions([]);
    },
    { immediate: false }
);

watch(
    currentUserAvatarUrl,
    () => {
        showSidebarAvatarImage.value = true;
    },
    { immediate: true }
);

watch(
    profileAvatarDisplayUrl,
    () => {
        profileAvatarPreviewFallback.value = false;
    },
    { immediate: true }
);

const handleSelectChat = (chatId) => {
    if (route.path !== '/chat') {
        router.push('/chat');
    }
    if (chatId !== currentChatId.value) {
        chatStore.setCurrentChatId(chatId);
    }
};

const handleSelectAgent = (sessionId) => {
    if (route.path !== '/work') {
        router.push('/work');
    }
    if (sessionId !== currentAgentSessionId.value) {
        agentStore.setCurrentSessionId(sessionId);
    }
};

const formatDate = (timestamp) => {
    if (!timestamp) {
        return '';
    }
    return new Date(timestamp).toLocaleString('zh-CN', {
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
    });
};

const formatTitle = (title) => {
    const raw = title || '未命名会话';
    if (raw.length > 7) {
        return `${raw.slice(0, 7)}..`;
    }
    return raw;
};

const startRenameChat = (chat) => {
    editingChatId.value = chat.id;
    editChatTitle.value = chat.title || '';
    nextTick(() => {
        chatTitleInputRefs.value?.[chat.id]?.focus?.();
    });
};

const saveRenameChat = async (chat) => {
    if (!chat || editingChatId.value !== chat.id) {
        return;
    }
    const title = editChatTitle.value.trim() || '未命名会话';
    try {
        await updateSession({ id: chat.id, sessionTitle: title });
        chatStore.updateChatTitle(chat.id, title);
        sessionError.value = '';
    } catch (error) {
        sessionError.value = normalizeError(error).message || '更新会话失败';
    }
    editingChatId.value = null;
    editChatTitle.value = '';
};

const cancelRenameChat = () => {
    editingChatId.value = null;
    editChatTitle.value = '';
};

const startRenameAgent = (session) => {
    editingAgentId.value = session.id;
    editAgentTitle.value = session.title || '';
    nextTick(() => {
        agentTitleInputRefs.value?.[session.id]?.focus?.();
    });
};

const setChatTitleInputRef = (id, el) => {
    if (!id) return;
    if (el) {
        chatTitleInputRefs.value[id] = el;
    } else {
        delete chatTitleInputRefs.value[id];
    }
};

const setAgentTitleInputRef = (id, el) => {
    if (!id) return;
    if (el) {
        agentTitleInputRefs.value[id] = el;
    } else {
        delete agentTitleInputRefs.value[id];
    }
};

const saveRenameAgent = async (session) => {
    if (!session || editingAgentId.value !== session.id) {
        return;
    }
    const title = editAgentTitle.value.trim() || '未命名会话';
    try {
        await updateSession({ id: session.id, sessionTitle: title });
        agentStore.updateSessionTitle(session.id, title);
        sessionError.value = '';
    } catch (error) {
        sessionError.value = normalizeError(error).message || '更新会话失败';
    }
    editingAgentId.value = null;
    editAgentTitle.value = '';
};

const cancelRenameAgent = () => {
    editingAgentId.value = null;
    editAgentTitle.value = '';
};

const openDeleteConfirm = (type, id) => {
    deleteTarget.value = { type, id };
    showDeleteConfirm.value = true;
};

const handleDelete = async () => {
    if (!deleteTarget.value?.id) {
        showDeleteConfirm.value = false;
        return;
    }
    try {
        const target =
            deleteTarget.value.type === 'agent'
                ? agentStore.sessions.find((item) => item.id === deleteTarget.value.id)
                : chatStore.chats.find((item) => item.id === deleteTarget.value.id);
        if (!target) {
            throw new Error('会话不存在');
        }
        await deleteSession({ id: target.id, sessionId: target.sessionId });
        if (deleteTarget.value.type === 'agent') {
            agentStore.removeSession(deleteTarget.value.id);
        } else {
            chatStore.removeChat(deleteTarget.value.id);
        }
        redirectWelcomeIfNoSession();
        sessionError.value = '';
    } catch (error) {
        sessionError.value = normalizeError(error).message || '删除会话失败';
    }
    showDeleteConfirm.value = false;
    deleteTarget.value = { type: 'chat', id: '' };
    cancelRenameChat();
    cancelRenameAgent();
};

const closeNewSessionPicker = () => {
    showNewSessionPicker.value = false;
    sessionLimitError.value = '';
};

const confirmNewSession = async (type) => {
    sessionLimitError.value = '';
    const chatCount = chats.value.length;
    const workCount = agentSessions.value.length;
    if (type === 'chat' && chatCount >= 3) {
        sessionLimitError.value = 'Chat 会话已达到 3 个上限';
        return;
    }
    if (type === 'work' && workCount >= 3) {
        sessionLimitError.value = 'Work 会话已达到 3 个上限';
        return;
    }
    try {
        const resp = await insertSession({ sessionTitle: '新会话', sessionType: type });
        const created = mapSession(pickData(resp, '创建会话失败'));
        if (created) {
            if (type === 'work') {
                agentStore.upsertSession(created);
                agentStore.setCurrentSessionId(created.id);
                router.push('/work');
            } else {
                chatStore.upsertChat(created);
                chatStore.setCurrentChatId(created.id);
                router.push('/chat');
            }
            closeNewSessionPicker();
            sessionError.value = '';
            return;
        }
        await loadSessions();
        const session = type === 'work' ? agentStore.sessions[0] : chatStore.chats[0];
        if (!session) {
            throw new Error('创建会话失败');
        }
        if (type === 'work') {
            agentStore.setCurrentSessionId(session.id);
            router.push('/work');
        } else {
            chatStore.setCurrentChatId(session.id);
            router.push('/chat');
        }
        closeNewSessionPicker();
        sessionError.value = '';
    } catch (error) {
        sessionError.value = normalizeError(error).message || '创建会话失败';
    }
};

const openProfile = () => {
    if (!authStore.isLogin) {
        router.push('/login');
        return;
    }
    router.push('/setting');
};

const closeProfile = () => {
    showProfile.value = false;
    profileSaving.value = false;
    profileLoading.value = false;
    profileError.value = '';
    profileAvatarError.value = '';
    mcpError.value = '';
    mcpEditing.value = false;
    apiError.value = '';
    apiEditing.value = false;
    resetProfileAvatarDraft();
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
            id: currentUser.value.userId,
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
        resetProfileAvatarDraft();
        showProfile.value = false;
    } catch (error) {
        profileError.value = normalizeError(error).message || '更新失败，请稍后重试';
    } finally {
        profileSaving.value = false;
    }
};

const handleLogout = () => {
    authStore.clear();
    settingsStore.updateSettings({ token: '' });
    closeProfile();
    router.push('/login');
};

const openLogoutConfirm = () => {
    if (!isLogin.value) {
        router.push('/login');
        return;
    }
    showLogoutConfirm.value = true;
};

const confirmLogout = () => {
    showLogoutConfirm.value = false;
    handleLogout();
};

const toggleTheme = () => {
    settingsStore.updateSettings({ theme: isDarkTheme.value ? 'light' : 'dark' });
};

const resetMcpForm = () => {
    mcpForm.id = null;
    mcpForm.mcpName = '';
    mcpForm.mcpTransport = 'mcp';
    mcpForm.mcpConfig = '';
    mcpForm.mcpDesc = '';
    mcpForm.secretMapText = '';
    mcpEditing.value = false;
};

const resetApiForm = () => {
    apiForm.id = null;
    apiForm.modelName = '';
    apiForm.modelType = '';
    apiForm.apiBaseUrl = '';
    apiForm.apiKey = '';
    apiForm.apiCompletionPath = '/v1/chat/completions';
    apiEditing.value = false;
};

const loadUserProfile = async () => {
    profileLoading.value = true;
    try {
        const resp = await fetchProfile();
        const { token, user } = parseAuthPayload(resp);
        if (user) {
            authStore.setAuth({
                token: token || authStore.token,
                user
            });
            profileForm.username = user.username || user.userName || currentUser.value.username || '';
        }
    } catch (error) {
        profileError.value = normalizeError(error).message || '获取用户资料失败';
    } finally {
        profileLoading.value = false;
    }
};

const loadUserApi = async () => {
    apiError.value = '';
    apiEditing.value = false;
    try {
        const resp = await userApiList('');
        const list = pickData(resp, '获取 API 失败') || [];
        apiList.value = Array.isArray(list) ? list : [];
    } catch (error) {
        apiList.value = [];
        apiError.value = normalizeError(error).message || '获取 API 失败';
    }
};

const loadUserMcp = async () => {
    mcpLoading.value = true;
    mcpError.value = '';
    try {
        const resp = await userMcpList('');
        const list = pickData(resp, '获取 MCP 失败') || [];
        mcpList.value = Array.isArray(list) ? list : [];
    } catch (error) {
        mcpError.value = normalizeError(error).message || '获取 MCP 失败';
    } finally {
        mcpLoading.value = false;
    }
};

const editMcp = (item) => {
    if (!item) return;
    mcpForm.id = Number.isFinite(Number(item.id)) ? Number(item.id) : null;
    mcpForm.mcpName = item.mcpName || '';
    mcpForm.mcpTransport = mapBackendMcpTypeToTransport(item.mcpType);
    mcpForm.mcpConfig = item.mcpParam || item.mcpConfig || '';
    mcpForm.mcpDesc = item.mcpDesc || '';
    mcpForm.secretMapText = item.mcpSecret || '';
    mcpEditing.value = true;
};

const saveMcp = async () => {
    if (!mcpForm.mcpName.trim() || !mcpForm.mcpTransport.trim() || !mcpForm.mcpConfig.trim()) {
        mcpError.value = '请完整填写 MCP 必填字段';
        return;
    }
    mcpError.value = '';
    const payload = {
        mcpName: mcpForm.mcpName,
        mcpType: mapTransportToBackendMcpType(mcpForm.mcpTransport),
        mcpParam: mcpForm.mcpConfig,
        mcpDesc: mcpForm.mcpDesc,
        mcpSecret: mcpForm.secretMapText || '{}'
    };
    if (mcpEditing.value && Number.isFinite(Number(mcpForm.id))) {
        payload.id = Number(mcpForm.id);
    }
    try {
        if (mcpEditing.value) {
            await userMcpUpdate(payload);
        } else {
            await userMcpInsert(payload);
        }
        resetMcpForm();
        await loadUserMcp();
    } catch (error) {
        mcpError.value = normalizeError(error).message || '保存 MCP 失败';
    }
};

const deleteMcp = async (item) => {
    if (!item?.id) return;
    try {
        await userMcpDelete(item.id);
        if (mcpForm.id === item.id) {
            resetMcpForm();
        }
        await loadUserMcp();
    } catch (error) {
        mcpError.value = normalizeError(error).message || '删除 MCP 失败';
    }
};

const editApi = (item) => {
    if (!item) return;
    apiForm.id = Number.isFinite(Number(item.id)) ? Number(item.id) : null;
    apiForm.modelName = item.modelName || '';
    apiForm.modelType = item.modelType || '';
    apiForm.apiBaseUrl = item.apiBaseUrl || '';
    apiForm.apiKey = item.apiKey || '';
    apiForm.apiCompletionPath = item.apiCompletionPath || '/v1/chat/completions';
    apiEditing.value = true;
};

const saveApi = async () => {
    if (
        !apiForm.modelName.trim() ||
        !apiForm.modelType.trim() ||
        !apiForm.apiBaseUrl.trim() ||
        !apiForm.apiKey.trim()
    ) {
        apiError.value = '请完整填写 API 必填字段';
        return;
    }
    apiError.value = '';
    const payload = {
        modelName: apiForm.modelName.trim(),
        modelType: apiForm.modelType.trim(),
        apiBaseUrl: apiForm.apiBaseUrl.trim(),
        apiKey: apiForm.apiKey.trim(),
        apiCompletionPath: apiForm.apiCompletionPath.trim() || '/v1/chat/completions'
    };
    if (apiEditing.value && Number.isFinite(Number(apiForm.id))) {
        payload.id = Number(apiForm.id);
    }
    try {
        if (apiEditing.value) {
            await userApiUpdate(payload);
        } else {
            await userApiInsert(payload);
        }
        resetApiForm();
        await loadUserApi();
    } catch (error) {
        apiError.value = normalizeError(error).message || '保存 API 失败';
    }
};

const deleteApi = async (item) => {
    if (!item?.id) return;
    try {
        await userApiDelete(item.id);
        if (apiForm.id === item.id) {
            resetApiForm();
        }
        await loadUserApi();
    } catch (error) {
        apiError.value = normalizeError(error).message || '删除 API 失败';
    }
};

const exportApi = async (item) => {
    try {
        await navigator.clipboard.writeText(JSON.stringify(item || {}, null, 2));
        apiError.value = '已复制导出 JSON 到剪贴板';
    } catch (error) {
        apiError.value = '导出 API 失败';
    }
};

const loadProfileResources = async () => {
    await Promise.all([loadUserProfile(), loadUserMcp(), loadUserApi()]);
};
</script>

<template>
    <aside
        class="flex h-screen flex-col border-r p-[20px] max-[720px]:hidden"
        :class="sidebarShellClass"
    >
        <div class="mb-[12px] pr-[4px]">
            <button
                class="group flex w-full min-w-0 items-center gap-[12px] rounded-[14px] border border-transparent px-[10px] py-[8px] text-left transition-all duration-200 hover:border-[rgba(123,200,255,0.35)] hover:bg-[rgba(123,200,255,0.08)]"
                type="button"
                @click="goWelcome"
            >
                <div
                    class="h-[52px] w-[52px] shrink-0 overflow-hidden rounded-[15px] border border-[rgba(255,255,255,0.2)] bg-[radial-gradient(120%_120%_at_0%_0%,rgba(111,125,255,0.2),rgba(83,197,255,0.1))] transition-transform duration-200 group-hover:scale-[1.04]"
                >
                    <img :src="logoImg" alt="Logo" class="h-full w-full object-cover block" />
                </div>
                <div class="flex min-w-0 flex-col gap-[4px]">
                    <div class="text-[26px] font-bold leading-[1.1] transition-colors duration-200 group-hover:text-[#9ed7ff]">
                        MiniAgent
                    </div>
                    <div class="whitespace-nowrap text-[14px] tracking-[0.02em] text-[rgba(231,236,244,0.7)]">RAG · MCP · OPENAI</div>
                </div>
            </button>
        </div>

        <div
            class="mb-[12px] mt-[8px] flex flex-1 flex-col gap-[12px] overflow-y-auto pr-[4px] [scrollbar-gutter:stable_both-edges] [scrollbar-width:none] [-ms-overflow-style:none] [&::-webkit-scrollbar]:hidden"
        >
            <div class="flex flex-col gap-[8px]">
                <button
                    class="w-full rounded-[12px] border px-[14px] py-[10px] text-[14px] font-semibold transition-all duration-200"
                    :class="
                        isStudioRoute
                            ? 'border-[#7bc8ff] bg-[linear-gradient(135deg,rgba(111,125,255,0.32),rgba(83,197,255,0.2))] text-white shadow-[0_8px_20px_rgba(0,0,0,0.18)]'
                            : sidebarGhostButtonClass
                    "
                    type="button"
                    @click="goRoute('/studio')"
                >
                    MiniAgent Studio
                </button>
                <button
                    class="w-full rounded-[12px] border px-[14px] py-[10px] text-[14px] font-semibold transition-all duration-200"
                    :class="
                        isPlazaRoute
                            ? 'border-[#7bc8ff] bg-[linear-gradient(135deg,rgba(111,125,255,0.32),rgba(83,197,255,0.2))] text-white shadow-[0_8px_20px_rgba(0,0,0,0.18)]'
                            : sidebarGhostButtonClass
                    "
                    type="button"
                    @click="goRoute('/plaza')"
                >
                    MiniAgent Plaza
                </button>
                <button
                    class="w-full rounded-[12px] border px-[14px] py-[10px] text-[14px] font-semibold transition-all duration-200"
                    :class="
                        isWelcomeRoute
                            ? 'border-[#7bc8ff] bg-[linear-gradient(135deg,rgba(111,125,255,0.32),rgba(83,197,255,0.2))] text-white shadow-[0_8px_20px_rgba(0,0,0,0.18)]'
                            : sidebarGhostButtonClass
                    "
                    type="button"
                    @click="handleNewSession"
                >
                    ＋ 新建会话
                </button>
            </div>
            <div v-if="sessionLoading" class="text-[12px] text-[rgba(231,236,244,0.7)]">会话加载中...</div>
            <div v-else-if="sessionError" class="text-[12px] text-[#fca5a5]">{{ sessionError }}</div>

            <div class="flex flex-col gap-[8px]">
                <button
                    class="flex w-full items-center justify-between px-[4px] py-[6px] text-[20px] font-bold text-[#f8fafc] transition-all duration-200 hover:text-[#7bc8ff]"
                    :class="isChatRoute ? 'text-[#7bc8ff]' : ''"
                    type="button"
                    @click="showChatList = !showChatList"
                >
                    <span class="inline-flex w-[140px] items-center">Chat 会话</span>
                    <svg
                        viewBox="0 0 20 20"
                        class="h-[14px] w-[14px] shrink-0 transition-transform duration-200"
                        :class="showChatList ? 'rotate-90' : ''"
                        fill="currentColor"
                        aria-hidden="true"
                    >
                        <path d="M7 4.5L13 10L7 15.5V4.5z" />
                    </svg>
                </button>
                <div
                    :class="getCollapseClasses(showChatList)"
                    :aria-hidden="!showChatList"
                >
                    <div :class="[COLLAPSE_INNER_CLASS, 'flex flex-col gap-[8px]']">
                            <div
                                v-for="chat in chats"
                                :key="chat.id"
                                :class="[
                                    'w-full rounded-[12px] border border-[rgba(255,255,255,0.08)] bg-[rgba(255,255,255,0.04)] px-[12px] py-[10px] transition-all duration-200 hover:border-[rgba(111,125,255,0.8)] hover:bg-[rgba(255,255,255,0.07)]',
                                    chat.id === currentChatId && route.path.startsWith('/chat')
                                        ? 'border-[#7bc8ff] bg-[linear-gradient(135deg,rgba(111,125,255,0.25),rgba(83,197,255,0.1))] shadow-[0_10px_20px_rgba(0,0,0,0.12)]'
                                        : ''
                                ]"
                            >
                                <div
                                    class="flex items-center justify-between gap-[10px]"
                                    @click="editingChatId === chat.id ? null : handleSelectChat(chat.id)"
                                >
                                    <div class="min-w-0 flex flex-col">
                                        <template v-if="editingChatId === chat.id">
                                            <input
                                                v-model="editChatTitle"
                                                :ref="(el) => setChatTitleInputRef(chat.id, el)"
                                                class="w-full rounded-[8px] border border-[rgba(255,255,255,0.2)] bg-[rgba(255,255,255,0.08)] px-[8px] py-[6px] font-semibold text-[#e7ecf4]"
                                                :placeholder="chat.title || '未命名会话'"
                                                maxlength="30"
                                                @keydown.enter.prevent="saveRenameChat(chat)"
                                                @keydown.esc.prevent="cancelRenameChat"
                                                @blur="saveRenameChat(chat)"
                                                @click.stop
                                            />
                                        </template>
                                        <template v-else>
                                            <div class="mb-[4px] max-w-[140px] truncate font-semibold" :title="chat.title || '未命名会话'">
                                                {{ formatTitle(chat.title) }}
                                            </div>
                                        </template>
                                        <div class="text-[12px] text-[rgba(231,236,244,0.7)]">{{ formatDate(chat.createdAt) }}</div>
                                    </div>
                                    <div class="flex shrink-0 gap-[6px]" @click.stop>
                                        <button
                                            class="grid h-[30px] w-[30px] place-items-center rounded-full border border-[rgba(255,255,255,0.25)] text-[13px] text-[#e7ecf4] transition-all duration-200 hover:border-[#7bc8ff] hover:text-[#7bc8ff] hover:bg-[rgba(123,200,255,0.12)]"
                                            type="button"
                                            title="重命名"
                                            @click.stop="startRenameChat(chat)"
                                        >
                                            ✎
                                        </button>
                                        <button
                                            class="grid h-[30px] w-[30px] place-items-center rounded-full border border-[rgba(255,255,255,0.25)] text-[13px] text-[#e7ecf4] transition-all duration-200 hover:border-[#ef4444] hover:text-[#ef4444] hover:bg-[rgba(239,68,68,0.16)]"
                                            type="button"
                                            title="删除"
                                            @click.stop="openDeleteConfirm('chat', chat.id)"
                                        >
                                            🗑
                                        </button>
                                    </div>
                                </div>
                            </div>
                            <div v-if="chats.length === 0" class="mt-[4px] text-[13px] text-[rgba(231,236,244,0.7)]">
                                暂无会话
                            </div>
                    </div>
                </div>
            </div>

            <div class="flex flex-col gap-[8px]">
                <button
                    class="flex w-full items-center justify-between px-[4px] py-[6px] text-[20px] font-bold text-[#f8fafc] transition-all duration-200 hover:text-[#7bc8ff]"
                    :class="isAgentRoute ? 'text-[#7bc8ff]' : ''"
                    type="button"
                    @click="showAgentList = !showAgentList"
                >
                    <span class="inline-flex w-[140px] items-center">Work 会话</span>
                    <svg
                        viewBox="0 0 20 20"
                        class="h-[14px] w-[14px] shrink-0 transition-transform duration-200"
                        :class="showAgentList ? 'rotate-90' : ''"
                        fill="currentColor"
                        aria-hidden="true"
                    >
                        <path d="M7 4.5L13 10L7 15.5V4.5z" />
                    </svg>
                </button>
                <div
                    :class="getCollapseClasses(showAgentList)"
                    :aria-hidden="!showAgentList"
                >
                    <div :class="[COLLAPSE_INNER_CLASS, 'flex flex-col gap-[8px]']">
                            <div
                                v-for="session in agentSessions"
                                :key="session.id"
                                :class="[
                                    'w-full rounded-[12px] border border-[rgba(255,255,255,0.08)] bg-[rgba(255,255,255,0.04)] px-[12px] py-[10px] transition-all duration-200 hover:border-[rgba(111,125,255,0.8)] hover:bg-[rgba(255,255,255,0.07)]',
                                    session.id === currentAgentSessionId && route.path.startsWith('/work')
                                        ? 'border-[#7bc8ff] bg-[linear-gradient(135deg,rgba(111,125,255,0.25),rgba(83,197,255,0.1))] shadow-[0_10px_20px_rgba(0,0,0,0.12)]'
                                        : ''
                                ]"
                            >
                                <div
                                    class="flex items-center justify-between gap-[10px]"
                                    @click="editingAgentId === session.id ? null : handleSelectAgent(session.id)"
                                >
                                    <div class="min-w-0 flex flex-col">
                                        <template v-if="editingAgentId === session.id">
                                            <input
                                                v-model="editAgentTitle"
                                                :ref="(el) => setAgentTitleInputRef(session.id, el)"
                                                class="w-full rounded-[8px] border border-[rgba(255,255,255,0.2)] bg-[rgba(255,255,255,0.08)] px-[8px] py-[6px] font-semibold text-[#e7ecf4]"
                                                :placeholder="session.title || '未命名会话'"
                                                maxlength="30"
                                                @keydown.enter.prevent="saveRenameAgent(session)"
                                                @keydown.esc.prevent="cancelRenameAgent"
                                                @blur="saveRenameAgent(session)"
                                                @click.stop
                                            />
                                        </template>
                                        <template v-else>
                                            <div class="mb-[4px] max-w-[140px] truncate font-semibold" :title="session.title || '未命名会话'">
                                                {{ formatTitle(session.title) }}
                                            </div>
                                        </template>
                                        <div class="text-[12px] text-[rgba(231,236,244,0.7)]">{{ formatDate(session.createdAt) }}</div>
                                    </div>
                                    <div class="flex shrink-0 gap-[6px]" @click.stop>
                                        <button
                                            class="grid h-[30px] w-[30px] place-items-center rounded-full border border-[rgba(255,255,255,0.25)] text-[13px] text-[#e7ecf4] transition-all duration-200 hover:border-[#7bc8ff] hover:text-[#7bc8ff] hover:bg-[rgba(123,200,255,0.12)]"
                                            type="button"
                                            title="重命名"
                                            @click.stop="startRenameAgent(session)"
                                        >
                                            ✎
                                        </button>
                                        <button
                                            class="grid h-[30px] w-[30px] place-items-center rounded-full border border-[rgba(255,255,255,0.25)] text-[13px] text-[#e7ecf4] transition-all duration-200 hover:border-[#ef4444] hover:text-[#ef4444] hover:bg-[rgba(239,68,68,0.16)]"
                                            type="button"
                                            title="删除"
                                            @click.stop="openDeleteConfirm('agent', session.id)"
                                        >
                                            🗑
                                        </button>
                                    </div>
                                </div>
                            </div>
                            <div v-if="agentSessions.length === 0" class="mt-[4px] text-[13px] text-[rgba(231,236,244,0.7)]">
                                暂无会话
                            </div>
                    </div>
                </div>
            </div>
        </div>

        <div
            class="group flex w-full items-center justify-between gap-[12px] rounded-[14px] border p-[12px] text-left transition-all duration-200"
            :class="sidebarProfileCardClass"
            role="button"
            tabindex="0"
            title="进入 Settings"
            @click="openProfile"
            @keydown.enter.prevent="openProfile"
            @keydown.space.prevent="openProfile"
        >
            <div class="flex min-w-0 items-center gap-[10px]">
                <div
                    class="grid h-[40px] w-[40px] shrink-0 place-items-center overflow-hidden border font-bold shadow-[inset_0_0_0_1px_rgba(255,255,255,0.16)] transition-transform duration-200 group-hover:scale-[1.04]"
                    :class="
                        currentUserAvatarUrl && showSidebarAvatarImage
                            ? 'rounded-full border-[rgba(255,255,255,0.35)] bg-transparent'
                            : isDarkTheme
                                ? 'rounded-full border-[rgba(148,163,184,0.42)] bg-[linear-gradient(135deg,#1f3f77,#2a5f9f)] text-[#e8f1ff] shadow-[inset_0_0_0_1px_rgba(255,255,255,0.08)]'
                                : 'rounded-full border-[rgba(255,255,255,0.42)] bg-[linear-gradient(135deg,#dcecff,#c8deff)] text-[#1f3d77]'
                    "
                >
                    <img
                        v-if="currentUserAvatarUrl && showSidebarAvatarImage"
                        :src="currentUserAvatarUrl"
                        alt="User Avatar"
                        class="h-full w-full rounded-full object-cover"
                        @error="showSidebarAvatarImage = false"
                    />
                    <span v-else>{{ avatarChar }}</span>
                </div>
                <div class="min-w-0">
                    <div class="truncate font-bold text-white transition-colors duration-200 group-hover:text-[#9ed7ff]">{{ currentUser.username || '访客' }}</div>
                    <div class="text-[12px] text-[rgba(231,236,244,0.68)] transition-colors duration-200 group-hover:text-[rgba(158,215,255,0.92)]">
                        Settings
                    </div>
                </div>
            </div>
            <div class="flex shrink-0 items-center gap-[8px]">
                <button
                    v-if="isLogin"
                    class="grid h-[34px] w-[34px] place-items-center rounded-[10px] border transition"
                    :class="sidebarIconButtonClass"
                    type="button"
                    title="退出登录"
                    @click.stop="openLogoutConfirm"
                >
                    <svg viewBox="0 0 24 24" class="h-[17px] w-[17px]" fill="currentColor" aria-hidden="true">
                        <path
                            d="M10.5 3.75a.75.75 0 000 1.5h6.75v13.5H10.5a.75.75 0 000 1.5H18a.75.75 0 00.75-.75V4.5A.75.75 0 0018 3.75h-7.5z"
                        />
                        <path
                            d="M12.53 12.53a.75.75 0 000-1.06L9.81 8.75a.75.75 0 00-1.06 1.06l1.44 1.44H4.5a.75.75 0 000 1.5h5.69l-1.44 1.44a.75.75 0 101.06 1.06l2.72-2.72z"
                        />
                    </svg>
                </button>
                <button
                    class="grid h-[34px] w-[34px] place-items-center rounded-[10px] border transition"
                    :class="sidebarIconButtonClass"
                    type="button"
                    :title="isDarkTheme ? '切换到白天' : '切换到黑天'"
                    @click.stop="toggleTheme"
                >
                    <svg v-if="isDarkTheme" viewBox="0 0 24 24" class="h-[18px] w-[18px]" fill="currentColor" aria-hidden="true">
                        <path
                            d="M12 3.75a.75.75 0 01.75.75v1.5a.75.75 0 01-1.5 0v-1.5A.75.75 0 0112 3.75zm6.22 2.53a.75.75 0 011.06 1.06l-1.06 1.06a.75.75 0 11-1.06-1.06l1.06-1.06zM20.25 11.25a.75.75 0 010 1.5h-1.5a.75.75 0 010-1.5h1.5zm-2.47 6.72a.75.75 0 011.06-1.06l1.06 1.06a.75.75 0 11-1.06 1.06l-1.06-1.06zM12 18.75a.75.75 0 01.75.75v1.5a.75.75 0 01-1.5 0v-1.5a.75.75 0 01.75-.75zm-6.22-.78a.75.75 0 011.06 0l1.06 1.06a.75.75 0 11-1.06 1.06l-1.06-1.06a.75.75 0 010-1.06zM3.75 12a.75.75 0 01.75-.75h1.5a.75.75 0 010 1.5h-1.5A.75.75 0 013.75 12zm2.47-6.72a.75.75 0 011.06 0l1.06 1.06a.75.75 0 11-1.06 1.06L6.22 6.34a.75.75 0 010-1.06zM12 7.5a4.5 4.5 0 100 9 4.5 4.5 0 000-9z"
                        />
                    </svg>
                    <svg v-else viewBox="0 0 24 24" class="h-[18px] w-[18px]" fill="currentColor" aria-hidden="true">
                        <path
                            d="M21.752 15.002A9.718 9.718 0 0112 21.75 9.75 9.75 0 0112 2.25c.33 0 .658.016.983.048a.75.75 0 01.34 1.38 7.5 7.5 0 009.098 11.072.75.75 0 011.33.252z"
                        />
                    </svg>
                </button>
            </div>
        </div>

        <div v-if="showDeleteConfirm" class="fixed inset-0 z-[20] grid place-items-center bg-[rgba(0,0,0,0.35)] p-[20px]" @click.self="showDeleteConfirm = false">
            <div
                class="w-full max-w-[420px] rounded-[14px] border border-[rgba(255,255,255,0.1)] bg-[#0f172a] text-[#e7ecf4] shadow-[0_20px_50px_rgba(0,0,0,0.2)]"
            >
                <div class="flex items-center justify-between border-b border-[rgba(255,255,255,0.08)] px-[16px] py-[14px]">
                    <div class="text-[16px] font-bold">删除会话</div>
                    <button
                        class="text-[20px] text-[#e7ecf4]"
                        type="button"
                        @click="showDeleteConfirm = false"
                    >
                        ×
                    </button>
                </div>
                <div class="px-[16px] py-[14px]">
                    <div class="text-[14px]">确认删除当前会话吗？</div>
                </div>
                <div class="flex items-center justify-between border-b border-[rgba(255,255,255,0.08)] px-[16px] py-[14px]">
                    <button
                        class="flex items-center justify-center rounded-[12px] border border-[rgba(255,255,255,0.15)] bg-[rgba(255,255,255,0.08)] px-[14px] py-[10px] font-semibold text-[#e7ecf4] transition-all duration-200 hover:bg-[rgba(255,255,255,0.16)]"
                        type="button"
                        @click="showDeleteConfirm = false"
                    >
                        取消
                    </button>
                    <button
                        class="flex items-center justify-center rounded-[12px] border border-[rgba(255,255,255,0.15)] bg-[rgba(255,255,255,0.08)] px-[14px] py-[10px] font-semibold text-[#e7ecf4] transition-all duration-200 hover:bg-[rgba(255,255,255,0.16)]"
                        type="button"
                        @click="handleDelete"
                    >
                        确认删除
                    </button>
                </div>
            </div>
        </div>

        <div v-if="showLogoutConfirm" class="fixed inset-0 z-[21] grid place-items-center bg-[rgba(0,0,0,0.35)] p-[20px]" @click.self="showLogoutConfirm = false">
            <div class="w-full max-w-[420px] rounded-[14px] border border-[rgba(255,255,255,0.1)] bg-[#0f172a] text-[#e7ecf4] shadow-[0_20px_50px_rgba(0,0,0,0.2)]">
                <div class="flex items-center justify-between border-b border-[rgba(255,255,255,0.08)] px-[16px] py-[14px]">
                    <div class="text-[16px] font-bold">退出登录</div>
                    <button class="text-[20px] text-[#e7ecf4]" type="button" @click="showLogoutConfirm = false">×</button>
                </div>
                <div class="px-[16px] py-[14px]">
                    <div class="text-[14px]">确认退出当前账号吗？</div>
                </div>
                <div class="flex items-center justify-between border-b border-[rgba(255,255,255,0.08)] px-[16px] py-[14px]">
                    <button
                        class="flex items-center justify-center rounded-[12px] border border-[rgba(255,255,255,0.15)] bg-[rgba(255,255,255,0.08)] px-[14px] py-[10px] font-semibold text-[#e7ecf4] transition-all duration-200 hover:bg-[rgba(255,255,255,0.16)]"
                        type="button"
                        @click="showLogoutConfirm = false"
                    >
                        取消
                    </button>
                    <button
                        class="flex items-center justify-center rounded-[12px] border border-[rgba(255,255,255,0.15)] bg-[rgba(255,255,255,0.08)] px-[14px] py-[10px] font-semibold text-[#e7ecf4] transition-all duration-200 hover:bg-[rgba(255,255,255,0.16)]"
                        type="button"
                        @click="confirmLogout"
                    >
                        确认退出
                    </button>
                </div>
            </div>
        </div>

        <div
            v-if="showNewSessionPicker"
            class="fixed inset-0 z-[30] grid place-items-center bg-[rgba(0,0,0,0.35)] backdrop-blur-[6px] p-[20px]"
            @click.self="closeNewSessionPicker"
        >
            <div class="flex flex-col items-center gap-[32px] -translate-y-[18px]">
                <div class="flex items-center gap-[32px]">
                    <button
                        class="flex h-[440px] w-[440px] flex-col items-center justify-center gap-[26px] rounded-[28px] border border-[rgba(0,0,0,0.08)] bg-[#f8fafc] text-[#0f172a] shadow-[0_24px_50px_rgba(15,23,42,0.18)] transition-all duration-200 hover:border-[#94a3b8] hover:bg-[#e2e8f0] hover:shadow-[0_30px_60px_rgba(15,23,42,0.22)]"
                        type="button"
                        @click="confirmNewSession('chat')"
                    >
                        <img :src="chatIcon" alt="Chat" class="h-[200px] w-[200px]" />
                        <div class="text-[40px] font-semibold">Chat Client</div>
                    </button>
                    <button
                        class="flex h-[440px] w-[440px] flex-col items-center justify-center gap-[26px] rounded-[28px] border border-[rgba(0,0,0,0.08)] bg-[#f8fafc] text-[#0f172a] shadow-[0_24px_50px_rgba(15,23,42,0.18)] transition-all duration-200 hover:border-[#94a3b8] hover:bg-[#e2e8f0] hover:shadow-[0_30px_60px_rgba(15,23,42,0.22)]"
                        type="button"
                        @click="confirmNewSession('work')"
                    >
                        <img :src="workIcon" alt="Work" class="h-[200px] w-[200px]" />
                        <div class="text-[40px] font-semibold">Work MiniAgent</div>
                    </button>
                </div>
                <div v-if="sessionLimitError" class="rounded-[10px] border border-[rgba(15,23,42,0.1)] bg-white px-[16px] py-[10px] text-[14px] text-[#dc2626] shadow-[0_12px_30px_rgba(15,23,42,0.12)]">
                    {{ sessionLimitError }}
                </div>
            </div>
        </div>

    </aside>
</template>
