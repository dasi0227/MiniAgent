<script setup>
import { computed, onBeforeUnmount, watch } from 'vue';
import { RouterView, useRoute } from 'vue-router';
import Sidebar from './components/Sidebar.vue';
import Toast from './components/Toast.vue';
import { useSettingsStore } from './router/pinia';

const route = useRoute();
const hideSidebar = computed(() => route.meta?.hideSidebar);

const settingsStore = useSettingsStore();
const resolvedTheme = computed(() => (settingsStore.theme === 'dark' ? 'dark' : 'light'));
let switchRaf = 0;

watch(
    resolvedTheme,
    (theme) => {
        const root = document.documentElement;
        root.classList.add('theme-switching');
        root.setAttribute('data-theme', theme);
        root.style.colorScheme = theme;
        if (switchRaf) cancelAnimationFrame(switchRaf);
        switchRaf = requestAnimationFrame(() => {
            root.classList.remove('theme-switching');
            switchRaf = 0;
        });
    },
    { immediate: true }
);

onBeforeUnmount(() => {
    if (switchRaf) {
        cancelAnimationFrame(switchRaf);
    }
    document.documentElement.classList.remove('theme-switching');
});
</script>

<template>
    <div
        :class="[
            'grid h-screen bg-[var(--bg-page)] text-[var(--text-primary)]',
            hideSidebar
                ? 'grid-cols-[1fr]'
                : 'grid-cols-[280px_1fr] max-[960px]:grid-cols-[240px_1fr] max-[720px]:grid-cols-[1fr]'
        ]"
    >
        <Sidebar v-if="!hideSidebar" />
        <div class="relative min-w-0">
            <RouterView />
            <Toast />
        </div>
    </div>
</template>
