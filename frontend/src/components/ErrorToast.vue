<script setup>
import { appErrorToasts, dismissErrorToast } from '../utils/errorToast';
</script>

<template>
    <div class="pointer-events-none absolute left-1/2 top-[16px] z-[1100] flex w-[min(320px,calc(100%-32px))] -translate-x-1/2 flex-col gap-[7px]">
        <TransitionGroup name="app-error-toast">
            <div
                v-for="toast in appErrorToasts"
                :key="toast.id"
                class="pointer-events-auto relative overflow-hidden rounded-[10px] border border-[rgba(248,113,113,0.3)] bg-[var(--surface-1)] shadow-[0_8px_18px_rgba(15,23,42,0.13)]"
            >
                <button
                    class="absolute right-[8px] top-[8px] grid h-[20px] w-[20px] place-items-center rounded-[6px] text-[16px] leading-none text-[var(--text-secondary)] transition hover:bg-[rgba(148,163,184,0.16)] hover:text-[var(--text-primary)]"
                    type="button"
                    aria-label="关闭错误提示"
                    @click="dismissErrorToast(toast.id)"
                >
                    ×
                </button>
                <div class="flex items-start gap-[9px] px-[11px] py-[9px] pr-[32px]">
                    <div class="mt-[1px] grid h-[26px] w-[26px] shrink-0 place-items-center rounded-full bg-[#ef4444] text-[14px] font-bold leading-none text-white">
                        !
                    </div>
                    <div class="min-w-0 flex-1 break-words text-[12px] leading-[1.42] text-[var(--text-primary)]">
                        {{ toast.message }}
                    </div>
                </div>
                <div
                    class="h-[3px] bg-[#ef4444] animate-app-error-progress"
                    :style="{ animationDuration: `${toast.duration}ms` }"
                    @animationend="dismissErrorToast(toast.id)"
                ></div>
            </div>
        </TransitionGroup>
    </div>
</template>

<style scoped>
@keyframes app-error-progress {
    from {
        width: 100%;
    }
    to {
        width: 0%;
    }
}

.animate-app-error-progress {
    animation-name: app-error-progress;
    animation-timing-function: linear;
    animation-fill-mode: forwards;
}

.app-error-toast-enter-active,
.app-error-toast-leave-active {
    transition: all 0.16s ease-out;
}

.app-error-toast-enter-from,
.app-error-toast-leave-to {
    opacity: 0;
    transform: translateY(-4px);
}
</style>
