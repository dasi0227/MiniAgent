import axios from 'axios';
import router from '../router/router';
import { useAuthStore } from '../router/pinia';
import {
    pushAdminErrorToast,
    pushAdminSuccessToast,
    pushAppErrorToast,
    pushAppSuccessToast
} from '../utils/toast';

// Dev: VITE_API_BASE=http://localhost:8066/miniagent
// Prod behind nginx under /miniagent: VITE_API_BASE=/miniagent
const BASE_URL = import.meta.env.VITE_API_BASE || 'http://localhost:8066/miniagent';
const REQUEST_TIMEOUT = 600000;
const AUTH_KEY = 'auth_info';
const APP_BASE = (import.meta.env.BASE_URL || '/').replace(/\/$/, '');
const LOGIN_PATH = `${APP_BASE}/login`;

const WRITE_ACTION_TEXT = {
    insert: '新增成功',
    update: '更新成功',
    delete: '删除成功',
    toggle: '状态更新成功',
    save: '保存成功',
    create: '创建成功',
    edit: '编辑成功',
    publish: '发布成功',
    fork: '复制成功',
    like: '点赞成功',
    dislike: '取消点赞成功',
    favor: '收藏成功',
    disfavor: '取消收藏成功',
    comment: '评论成功',
    discomment: '删除评论成功'
};

const WRITE_ACTION_PATTERN =
    /\/(insert|update|delete|toggle|save|create|edit|publish|fork|like|dislike|favor|disfavor|comment|discomment)(?:\/|$)/i;

const APP_TOAST_EXCLUDED_ERROR_PATH_PREFIXES = [
    '/api/v1/ai/chat/stream',
    '/api/v1/ai/chat/complete',
    '/api/v1/ai/work/execute',
    '/api/v1/session/message/'
];

const http = axios.create({
    baseURL: BASE_URL,
    timeout: REQUEST_TIMEOUT
});

export const normalizeError = (error) => {
    if (error?.name === 'AbortError') {
        return {
            message: '请求已取消',
            status: null,
            isNetworkError: false,
            raw: error
        };
    }
    if (axios.isCancel(error)) {
        return {
            message: '请求已取消',
            status: null,
            isNetworkError: false,
            raw: error
        };
    }
    const isNetworkError = !error.response;
    const status = error.response?.status ?? null;
    const data = error.response?.data ?? {};
    const message = data.message || data.error || data.info || error.message || '请求失败，请稍后再试';
    return {
        message,
        status,
        isNetworkError,
        raw: error
    };
};

const normalizeUrlPath = (url = '') => {
    const text = String(url || '').trim();
    if (!text) return '';

    let pathText = text;
    if (/^https?:\/\//i.test(pathText)) {
        try {
            pathText = new URL(pathText).pathname || '';
        } catch {
            pathText = text;
        }
    }

    if (BASE_URL && pathText.startsWith(BASE_URL)) {
        pathText = pathText.slice(BASE_URL.length);
    }
    pathText = pathText.split('?')[0] || '';
    if (!pathText.startsWith('/')) {
        pathText = `/${pathText}`;
    }
    return pathText.replace(/\/{2,}/g, '/');
};

const parseRequestMeta = (config = {}) => {
    const method = String(config?.method || '').toUpperCase();
    const path = normalizeUrlPath(config?.url || '');
    const segments = path.split('/').filter(Boolean);

    const adminMatch = path.match(/\/api\/v1\/admin\/([^/]+)\/([^/?#]+)/i);
    let moduleKey = adminMatch ? String(adminMatch[1] || '').toLowerCase() : String(segments[segments.length - 2] || '').toLowerCase();
    let action = adminMatch ? String(adminMatch[2] || '').toLowerCase() : String(segments[segments.length - 1] || '').toLowerCase();

    let actionToken = Object.prototype.hasOwnProperty.call(WRITE_ACTION_TEXT, action) ? action : '';
    if (!actionToken) {
        const matched = path.match(WRITE_ACTION_PATTERN);
        actionToken = matched ? String(matched[1] || '').toLowerCase() : '';
    }

    if (actionToken) {
        const actionIndex = segments.findIndex((item) => item.toLowerCase() === actionToken);
        if (actionIndex > 0) {
            moduleKey = segments[actionIndex - 1].toLowerCase();
        }
    }

    return {
        method,
        path,
        moduleKey,
        action,
        actionToken,
        isAdmin: path.startsWith('/api/v1/admin/'),
        requestPath: [method, path].filter(Boolean).join(' ').trim()
    };
};

const resolveRequestMeta = (errorLike) => {
    const config = errorLike?.response?.config || errorLike?.config || errorLike?.raw?.config || null;
    return parseRequestMeta(config || {});
};

const isWriteRequest = (meta = {}) => {
    if (!meta.actionToken) return false;
    return ['POST', 'PUT', 'PATCH', 'DELETE'].includes(meta.method);
};

const isToastSuppressed = (config = {}) => {
    if (!config || typeof config !== 'object') return false;
    if (config.toast === false) return true;
    if (config.meta && typeof config.meta === 'object' && config.meta.toast === false) return true;
    return false;
};

const isAppToastExcludedPath = (path = '') => {
    const normalizedPath = normalizeUrlPath(path || '');
    if (!normalizedPath) return false;
    return APP_TOAST_EXCLUDED_ERROR_PATH_PREFIXES.some((prefix) => normalizedPath.startsWith(prefix));
};

const resolveSuccessMessage = ({ moduleKey, actionToken }) => {
    const actionText = WRITE_ACTION_TEXT[actionToken];
    if (!actionText) return '';
    const moduleText = String(moduleKey || '').trim().toUpperCase();
    return moduleText ? `${moduleText} ${actionText}` : actionText;
};

const isAdminPage = () => {
    const path = router.currentRoute.value.path || '';
    return path.startsWith('/admin') && path !== '/admin/login';
};

const isAppPage = () => {
    const path = router.currentRoute.value.path || '';
    return !path.startsWith('/admin') && path !== '/login' && path !== '/register';
};

const isNotifiedError = (error, flag) => {
    if (!error || typeof error !== 'object') return false;
    return Boolean(error?.[flag] || error.raw?.[flag]);
};

const markErrorNotified = (error, flag) => {
    if (!error || typeof error !== 'object') return;
    error[flag] = true;
    if (error.raw && typeof error.raw === 'object') {
        error.raw[flag] = true;
    }
};

const shouldShowOperation = (operation) => {
    const text = String(operation || '').trim();
    if (!text) return false;
    const hidden = new Set(['操作失败', '请求失败', '失败']);
    return !hidden.has(text);
};

const shouldShowSuccessOperation = (operation) => {
    const text = String(operation || '').trim();
    if (!text) return false;
    const hidden = new Set(['操作成功', '请求成功', '成功']);
    return !hidden.has(text);
};

export const notifyAdminError = (error, fallbackMessage = '操作失败') => {
    const normalized =
        error && typeof error === 'object' && Object.prototype.hasOwnProperty.call(error, 'message')
            ? error
            : normalizeError(error);
    const message = normalized?.message || fallbackMessage;
    const requestMeta = resolveRequestMeta(normalized);
    if (!isAdminPage() || !message || message === '请求已取消' || isNotifiedError(normalized, '__adminToastShown')) {
        return message;
    }
    pushAdminErrorToast({
        message,
        requestPath: normalized?.requestPath || requestMeta.requestPath,
        operation: fallbackMessage && fallbackMessage !== message && shouldShowOperation(fallbackMessage) ? fallbackMessage : ''
    });
    markErrorNotified(normalized, '__adminToastShown');
    return message;
};

export const notifyAdminSuccess = (message, meta = {}) => {
    const text = String(message || '').trim();
    if (!isAdminPage() || !text) {
        return text;
    }
    pushAdminSuccessToast({
        message: text,
        operation: shouldShowSuccessOperation(meta.operation) ? meta.operation : '',
        requestPath: String(meta.requestPath || '').trim()
    });
    return text;
};

export const notifyAppError = (error, fallbackMessage = '操作失败') => {
    const normalized =
        error && typeof error === 'object' && Object.prototype.hasOwnProperty.call(error, 'message')
            ? error
            : normalizeError(error);
    const message = normalized?.message || fallbackMessage;
    const requestMeta = resolveRequestMeta(normalized);

    if (
        !isAppPage() ||
        !message ||
        message === '请求已取消' ||
        isNotifiedError(normalized, '__appToastShown') ||
        isAppToastExcludedPath(requestMeta.path)
    ) {
        return message;
    }

    pushAppErrorToast({ message });
    markErrorNotified(normalized, '__appToastShown');
    return message;
};

export const notifyAppSuccess = (message) => {
    const text = String(message || '').trim();
    if (!isAppPage() || !text) {
        return text;
    }
    pushAppSuccessToast({ message: text });
    return text;
};

http.interceptors.request.use(
    (config) => {
        let authStore;
        try {
            authStore = useAuthStore();
        } catch (e) {
            authStore = null;
        }
        const storedAuth = localStorage.getItem(AUTH_KEY);
        const auth = storedAuth ? JSON.parse(storedAuth) : {};
        const isFormData = config.data instanceof FormData;
        config.headers = {
            ...(isFormData ? {} : { 'Content-Type': 'application/json' }),
            Accept: 'application/json',
            ...(config.headers || {})
        };
        const token = authStore?.token || auth.token;
        if (token) {
            config.headers.Authorization = `Bearer ${token}`;
        }
        config.metadata = { startTime: Date.now() };
        return config;
    },
    (error) => Promise.reject(normalizeError(error))
);

http.interceptors.response.use(
    (response) => {
        const body = response?.data;
        const requestMeta = parseRequestMeta(response?.config || {});
        const isResultEnvelope =
            body && typeof body === 'object' && Object.prototype.hasOwnProperty.call(body, 'code');
        const success = !isResultEnvelope || body.code === 200;

        if (success && isWriteRequest(requestMeta) && !isToastSuppressed(response?.config)) {
            const successMessage = resolveSuccessMessage(requestMeta);
            if (requestMeta.isAdmin) {
                notifyAdminSuccess(successMessage, {
                    operation: requestMeta.actionToken,
                    requestPath: requestMeta.requestPath
                });
            } else if (!isAppToastExcludedPath(requestMeta.path)) {
                notifyAppSuccess(successMessage);
            }
        }

        return response.data;
    },
    (error) => {
        const status = error?.response?.status;
        const normalized = normalizeError(error);
        let authStore;
        try {
            authStore = useAuthStore();
        } catch (e) {
            authStore = null;
        }

        if (status === 401) {
            if (isAdminPage()) {
                if (!isToastSuppressed(error?.response?.config || error?.config)) {
                    notifyAdminError(normalized, '未登录或登录已过期，请重新登录');
                }
            } else {
                if (!isToastSuppressed(error?.response?.config || error?.config)) {
                    notifyAppError(normalized, '未登录或登录已过期，请重新登录');
                }
            }
            authStore?.clear();
            if (router.currentRoute.value.path !== '/login' && router.currentRoute.value.path !== '/admin/login') {
                router.replace('/login');
            }
            return Promise.reject(normalized);
        }

        if (!isToastSuppressed(error?.response?.config || error?.config)) {
            if (isAdminPage()) {
                notifyAdminError(normalized);
            } else {
                notifyAppError(normalized, status === 403 ? '无权限访问该资源' : '操作失败');
            }
        }

        return Promise.reject(normalized);
    }
);

export async function streamFetch(url, body, onData, onError, onDone, signal) {
    const controller = new AbortController();
    const activeSignal = signal || controller.signal;
    const headers = { Accept: 'text/event-stream', 'Content-Type': 'application/json' };
    try {
        const storedAuth = localStorage.getItem(AUTH_KEY);
        const auth = storedAuth ? JSON.parse(storedAuth) : {};
        const token = auth.token;
        if (token) {
            headers.Authorization = `Bearer ${token}`;
        }
    } catch (_) {
        // ignore
    }
    try {
        const buildUrl = (rawUrl) => {
            if (!rawUrl) return rawUrl;
            if (/^https?:\/\//i.test(rawUrl)) return rawUrl;
            const base = (BASE_URL || '').replace(/\/$/, '');
            const path = rawUrl.startsWith('/') ? rawUrl : `/${rawUrl}`;
            return `${base}${path}`;
        };
        const requestUrl = buildUrl(url);
        console.log(`[stream] POST ${requestUrl}`);
        const response = await fetch(requestUrl, {
            method: 'POST',
            headers,
            body: JSON.stringify(body || {}),
            signal: activeSignal
        });
        if (response.status === 401) {
            localStorage.removeItem(AUTH_KEY);
            if (window.location.pathname !== LOGIN_PATH) {
                window.location.href = LOGIN_PATH;
            }
            throw new Error('未登录或登录已过期');
        }
        if (!response.ok || !response.body) {
            throw new Error(`流式请求失败: ${response.status}`);
        }

        const reader = response.body.getReader();
        const decoder = new TextDecoder('utf-8');
        let buffer = '';
        while (true) {
            const { done, value } = await reader.read();
            if (done) {
                break;
            }
            buffer += decoder.decode(value, { stream: true });
            let boundary = buffer.indexOf('\n\n');
            while (boundary !== -1) {
                const chunk = buffer.slice(0, boundary);
                buffer = buffer.slice(boundary + 2);
                if (chunk) {
                    processEventChunk(chunk, onData, onError);
                }
                boundary = buffer.indexOf('\n\n');
            }
        }
        if (buffer) {
            processEventChunk(buffer, onData, onError);
        }
        onDone && onDone();
    } catch (error) {
        if (error.name === 'AbortError') {
            onError && onError(normalizeError(error));
            return;
        }
        onError && onError(normalizeError(error));
        throw error;
    }
}

const processEventChunk = (chunk, onData, onError) => {
    const lines = chunk.split('\n').map((line) => line.replace(/\r$/, ''));
    const dataLines = lines.filter((line) => line.startsWith('data:'));
    if (!dataLines.length) {
        return;
    }
    const payload = dataLines.map((line) => line.replace(/^data:\s*/, '')).join('\n');
    if (!payload) {
        return;
    }
    try {
        const json = JSON.parse(payload);
        onData && onData(json);
        return;
    } catch (error) {
        if (onData) {
            onData(payload);
            return;
        }
        onError && onError(normalizeError(error));
    }
};

export default http;
