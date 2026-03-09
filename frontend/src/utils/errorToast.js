import { reactive } from 'vue';

const MAX_VISIBLE = 3;
const DEFAULT_DURATION = 1000;

let seed = 0;

export const appErrorToasts = reactive([]);
const pendingQueue = reactive([]);
const timerMap = new Map();

const clearTimer = (id) => {
    const timer = timerMap.get(id);
    if (timer) {
        clearTimeout(timer);
        timerMap.delete(id);
    }
};

const scheduleDismiss = (id, duration) => {
    clearTimer(id);
    const timer = setTimeout(() => {
        dismissErrorToast(id);
    }, duration);
    timerMap.set(id, timer);
};

const showNextToast = () => {
    while (appErrorToasts.length < MAX_VISIBLE && pendingQueue.length > 0) {
        const next = pendingQueue.shift();
        if (!next) break;
        appErrorToasts.push(next);
        scheduleDismiss(next.id, next.duration);
    }
};

const removeFromList = (list, id) => {
    const index = list.findIndex((item) => item.id === id);
    if (index >= 0) {
        list.splice(index, 1);
        return true;
    }
    return false;
};

export const dismissErrorToast = (id) => {
    if (!id) return;
    clearTimer(id);
    const removedFromActive = removeFromList(appErrorToasts, id);
    if (!removedFromActive) {
        removeFromList(pendingQueue, id);
        return;
    }
    showNextToast();
};

export const pushErrorToast = (payload, options = {}) => {
    const source = typeof payload === 'string' ? { message: payload } : payload || {};
    const text = String(source.message || '').trim();
    if (!text) return '';

    const duration = Number(source.duration) > 0
        ? Number(source.duration)
        : Number(options.duration) > 0
            ? Number(options.duration)
            : DEFAULT_DURATION;

    const id = `app_error_${Date.now()}_${seed++}`;
    const toast = {
        id,
        message: text,
        duration
    };

    if (appErrorToasts.length < MAX_VISIBLE) {
        appErrorToasts.push(toast);
        scheduleDismiss(id, duration);
    } else {
        pendingQueue.push(toast);
    }
    return id;
};
