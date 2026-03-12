<script setup>
import { dismissToast, toasts } from '../utils/toast';

const showSuccessMeta = (value) => {
    const text = String(value || '').trim();
    if (!text) return false;
    const hidden = new Set(['操作成功', '请求成功', '成功']);
    return !hidden.has(text);
};
</script>

<template>
    <div class="pointer-events-none absolute left-1/2 top-[16px] z-[1200] flex w-[min(420px,calc(100%-32px))] -translate-x-1/2 flex-col gap-[8px]">
        <TransitionGroup name="toast-stack">
            <div
                v-for="toast in toasts"
                :key="toast.id"
                class="pointer-events-auto relative overflow-hidden rounded-[12px] border bg-[var(--surface-1)] shadow-[0_10px_24px_rgba(15,23,42,0.18)]"
                :class="
                    toast.type === 'success'
                        ? 'border-[rgba(34,197,94,0.3)]'
                        : 'border-[rgba(239,68,68,0.3)]'
                "
            >
                <button
                    class="absolute right-[8px] top-[8px] grid h-[20px] w-[20px] place-items-center rounded-[6px] text-[16px] leading-none text-[var(--text-secondary)] transition hover:bg-[rgba(148,163,184,0.16)] hover:text-[var(--text-primary)]"
                    type="button"
                    aria-label="关闭提示"
                    @click="dismissToast(toast.id)"
                >
                    ×
                </button>
                <div class="flex items-start gap-[10px] px-[12px] py-[10px] pr-[34px]">
                    <div
                        class="mt-[1px] grid h-[26px] w-[26px] shrink-0 place-items-center rounded-full text-[14px] font-bold leading-none text-white"
                        :class="toast.type === 'success' ? 'bg-[#22c55e]' : 'bg-[#ef4444]'"
                    >
                        {{ toast.type === 'success' ? '✓' : '!' }}
                    </div>
                    <div class="min-w-0 flex-1">
                        <div class="break-words text-[13px] font-medium leading-[1.45] text-[var(--text-primary)]">
                            {{ toast.type === 'success' ? '成功：' : '错误：' }}{{ toast.message }}
                        </div>
                        <div
                            v-if="toast.scope === 'admin' && (showSuccessMeta(toast.operation) || toast.requestPath)"
                            class="mt-[6px] flex flex-wrap gap-[6px] text-[11px] leading-[1.4] text-[var(--text-secondary)]"
                        >
                            <span
                                v-if="showSuccessMeta(toast.operation)"
                                class="rounded-[999px] border border-[var(--border-color)] bg-[var(--surface-2)] px-[7px] py-[2px]"
                            >
                                操作：{{ toast.operation }}
                            </span>
                            <span
                                v-if="toast.requestPath"
                                class="max-w-full rounded-[6px] border border-[var(--border-color)] bg-[var(--surface-2)] px-[7px] py-[2px] font-mono break-all [overflow-wrap:anywhere]"
                            >
                                {{ toast.requestPath }}
                            </span>
                        </div>
                    </div>
                </div>
                <div
                    class="h-[3px] animate-toast-progress"
                    :class="toast.type === 'success' ? 'bg-[#22c55e]' : 'bg-[#ef4444]'"
                    :style="{ animationDuration: `${toast.duration}ms` }"
                ></div>
            </div>
        </TransitionGroup>
    </div>
</template>

<style scoped>
@keyframes toast-progress {
    from {
        width: 100%;
    }
    to {
        width: 0%;
    }
}

.animate-toast-progress {
    animation-name: toast-progress;
    animation-timing-function: linear;
    animation-fill-mode: forwards;
}

.toast-stack-enter-active,
.toast-stack-leave-active {
    transition: all 0.24s ease;
}

.toast-stack-enter-from {
    opacity: 0;
    transform: translateY(8px);
}

.toast-stack-leave-to {
    opacity: 0;
    transform: translateY(-14px);
}

.toast-stack-move {
    transition: transform 0.24s ease;
}
</style>
