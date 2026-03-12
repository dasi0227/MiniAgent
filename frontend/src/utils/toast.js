import { computed, reactive } from 'vue';

const DEFAULT_DURATION = 2000;
const MAX_VISIBLE = 3;
let seed = 0;

export const toasts = reactive([]);
const timerMap = new Map();

const clearTimer = (id) => {
    const timer = timerMap.get(id);
    if (timer) {
        clearTimeout(timer);
        timerMap.delete(id);
    }
};

const removeToastById = (id) => {
    const index = toasts.findIndex((item) => item.id === id);
    if (index >= 0) {
        toasts.splice(index, 1);
        return true;
    }
    return false;
};

const scheduleDismiss = (id, duration) => {
    clearTimer(id);
    const timer = setTimeout(() => {
        dismissToast(id);
    }, duration);
    timerMap.set(id, timer);
};

const normalizeScope = (scope) => (scope === 'admin' ? 'admin' : 'app');
const normalizeType = (type) => (type === 'success' ? 'success' : 'error');

const normalizeDuration = (sourceDuration, optionDuration) => {
    const payloadDuration = Number(sourceDuration);
    if (Number.isFinite(payloadDuration) && payloadDuration > 0) {
        return payloadDuration;
    }
    const optionsValue = Number(optionDuration);
    if (Number.isFinite(optionsValue) && optionsValue > 0) {
        return optionsValue;
    }
    return DEFAULT_DURATION;
};

const evictOldestIfNeeded = () => {
    if (toasts.length < MAX_VISIBLE) {
        return;
    }
    const oldest = toasts[0];
    if (oldest?.id) {
        dismissToast(oldest.id);
    }
};

export const dismissToast = (id) => {
    if (!id) return;
    clearTimer(id);
    removeToastById(id);
};

export const pushToast = (payload, options = {}) => {
    const source = typeof payload === 'string' ? { message: payload } : payload || {};
    const message = String(source.message || '').trim();
    if (!message) return '';

    evictOldestIfNeeded();

    const toast = {
        id: `toast_${Date.now()}_${seed++}`,
        scope: normalizeScope(source.scope || options.scope),
        type: normalizeType(source.type || options.type),
        message,
        operation: String(source.operation || '').trim(),
        requestPath: String(source.requestPath || '').trim(),
        duration: normalizeDuration(source.duration, options.duration)
    };

    toasts.push(toast);
    scheduleDismiss(toast.id, toast.duration);
    return toast.id;
};

const pushScopedToast = (scope, type, payload, options = {}) => {
    const source = typeof payload === 'string' ? { message: payload } : payload || {};
    return pushToast(
        {
            ...source,
            scope,
            type
        },
        options
    );
};

export const pushAdminToast = (payload, options = {}) => {
    const source = typeof payload === 'string' ? { message: payload } : payload || {};
    const type = normalizeType(source.type || options.type);
    return pushScopedToast('admin', type, source, options);
};

export const pushAdminErrorToast = (payload, options = {}) =>
    pushScopedToast('admin', 'error', payload, options);

export const pushAdminSuccessToast = (payload, options = {}) =>
    pushScopedToast('admin', 'success', payload, options);

export const pushAppErrorToast = (payload, options = {}) =>
    pushScopedToast('app', 'error', payload, options);

export const pushAppSuccessToast = (payload, options = {}) =>
    pushScopedToast('app', 'success', payload, options);

export const pushErrorToast = (payload, options = {}) => {
    const source = typeof payload === 'string' ? { message: payload } : payload || {};
    const type = normalizeType(source.type || options.type);
    return pushScopedToast('app', type, source, options);
};

export const dismissAdminToast = dismissToast;
export const dismissAdminErrorToast = dismissToast;
export const dismissErrorToast = dismissToast;

export const adminToasts = computed(() => toasts.filter((item) => item.scope === 'admin'));
export const appErrorToasts = computed(() => toasts.filter((item) => item.scope === 'app'));
