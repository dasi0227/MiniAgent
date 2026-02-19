<script setup>
import { computed } from 'vue';
import beianIcon from '../assets/beian.png';

const props = defineProps({
    layout: {
        type: String,
        default: 'app'
    },
    wrapperClass: {
        type: String,
        default: ''
    },
    innerClass: {
        type: String,
        default: ''
    }
});

const currentYear = new Date().getFullYear();

const wrapperByLayout = computed(() => {
    if (props.layout === 'admin') {
        return 'bg-white border-[#e2e8f0] backdrop-blur-0';
    }
    if (props.layout === 'auth') {
        return 'bg-transparent border-[rgba(15,23,42,0.06)] backdrop-blur-0';
    }
    return 'bg-[var(--surface-3)] border-[rgba(15,23,42,0.06)] backdrop-blur-[6px]';
});

const innerByLayout = computed(() => {
    if (props.layout === 'admin') {
        return 'px-6 text-[#64748b]';
    }
    if (props.layout === 'auth') {
        return 'px-[16px]';
    }
    return 'max-w-[var(--footer-content-max)] pl-[var(--footer-pad-x)] pr-[calc(var(--footer-pad-x)+var(--scrollbar-w))] max-[720px]:pl-[var(--footer-pad-x-mobile)] max-[720px]:pr-[calc(var(--footer-pad-x-mobile)+var(--scrollbar-w))]';
});
</script>

<template>
    <footer
        :class="[
            'h-[var(--footer-height)] w-full border-t',
            wrapperByLayout,
            wrapperClass
        ]"
    >
        <div
            :class="[
                'mx-auto grid h-full w-full grid-cols-3 items-center gap-[12px] text-[12px] text-[var(--text-secondary)]',
                innerByLayout,
                innerClass
            ]"
        >
            <a
                class="inline-flex min-w-0 items-center gap-[6px] truncate text-[inherit] no-underline transition hover:text-[var(--accent-color)]"
                href="https://beian.mps.gov.cn/#/query/webSearch?code=44010602015173"
                rel="noreferrer"
                target="_blank"
                title="粤公网安备44010602015173号"
            >
                <img :src="beianIcon" alt="备案图标" class="h-[14px] w-[14px] shrink-0" />
                <span class="truncate">粤公网安备44010602015173号</span>
            </a>
            <span class="min-w-0 truncate text-center" :title="`Copyright © ${currentYear} Dasi. All Rights Reserved.`">
                Copyright © {{ currentYear }} Dasi. All Rights Reserved.
            </span>
            <span class="min-w-0 truncate text-right" title="本网站内容由 AI 生成，仅供参考，请注意甄别。">
                本网站内容由 AI 生成，仅供参考，请注意甄别。
            </span>
        </div>
    </footer>
</template>
