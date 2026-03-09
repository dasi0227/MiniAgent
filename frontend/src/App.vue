<script setup>
import { computed, watchEffect } from 'vue';
import { RouterView, useRoute } from 'vue-router';
import Sidebar from './components/Sidebar.vue';
import AdminErrorToast from './components/AdminErrorToast.vue';
import ErrorToast from './components/ErrorToast.vue';
import { useSettingsStore } from './router/pinia';

const route = useRoute();
const hideSidebar = computed(() => route.meta?.hideSidebar);
const isAdminRoute = computed(() => route.path.startsWith('/admin'));
const showAppErrorToast = computed(
    () => !isAdminRoute.value && route.path !== '/login' && route.path !== '/register'
);

const settingsStore = useSettingsStore();

watchEffect(() => {
    const theme = settingsStore.theme === 'dark' ? 'dark' : 'light';
    document.documentElement.setAttribute('data-theme', theme);
    document.documentElement.style.colorScheme = theme;
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
            <ErrorToast v-if="showAppErrorToast" />
        </div>
        <AdminErrorToast v-if="isAdminRoute" />
    </div>
</template>
