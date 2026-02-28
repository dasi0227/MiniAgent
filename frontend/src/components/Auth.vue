<script setup>
import { reactive, ref } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { login, register } from '../request/api';
import { parseAuthPayload } from '../request/auth';
import { useAuthStore, useSettingsStore } from '../router/pinia';
import coverImg from '../assets/cover.png';
import Footer from './Footer.vue';

const router = useRouter();
const route = useRoute();
const authStore = useAuthStore();
const settingsStore = useSettingsStore();

const mode = ref(route.path.includes('register') ? 'register' : 'login');
const loading = ref(false);
const error = ref('');
const target = ref('chat');

const form = reactive({
    username: '',
    password: ''
});

const syncModeFromRoute = () => {
    mode.value = route.path.includes('register') ? 'register' : 'login';
    target.value = 'chat';
};

const switchMode = (next) => {
    if (mode.value === next) return;
    mode.value = next;
    error.value = '';
    const path = next === 'login' ? '/login' : '/register';
    router.replace({ path, query: route.query });
};

syncModeFromRoute();

const submit = async () => {
    if (!form.username || !form.password) {
        error.value = '请填写用户名和密码';
        return;
    }
    loading.value = true;
    error.value = '';
    try {
        const api = mode.value === 'login' ? login : register;
        const resp = await api({ username: form.username.trim(), password: form.password });
        const { token, user } = parseAuthPayload(resp);
        if (!token || !user) {
            throw new Error('登录信息异常');
        }
        if (user.userStatus === 0) {
            throw new Error('账号已被禁用');
        }
        authStore.setAuth({ token, user });
        settingsStore.updateSettings({ token });
        router.push('/chat');
    } catch (e) {
        error.value = e?.message || '操作失败，请稍后重试';
    } finally {
        loading.value = false;
    }
};
</script>

<template>
    <div class="grid h-screen grid-rows-[1fr_var(--footer-height)] bg-[radial-gradient(circle_at_top_left,#eef4ff_0%,#e6eefc_38%,#f4f7fc_100%)]">
        <div class="flex items-center justify-center p-[18px] md:p-[28px]">
            <div class="grid h-[min(760px,calc(100vh-var(--footer-height)-72px))] w-full max-w-[1000px] overflow-hidden rounded-[28px] bg-[rgba(255,255,255,0.66)] shadow-[0_26px_70px_rgba(15,23,42,0.14)] backdrop-blur-[18px] md:grid-cols-[1.14fr_0.86fr]">
                <div class="relative hidden min-h-0 overflow-hidden md:block">
                    <img :src="coverImg" alt="MiniAgent Cover" class="h-full w-full object-cover" />
                    <div class="absolute inset-0 bg-[linear-gradient(180deg,rgba(255,255,255,0.04),rgba(15,23,42,0.12))]" />
                </div>

                <div class="flex min-h-0 items-center justify-center px-[20px] py-[24px] md:px-[36px]">
                    <div class="w-full max-w-[340px]">
                        <div class="mb-[20px] flex items-center justify-between gap-[14px]">
                            <div class="text-[28px] font-bold tracking-[-0.03em] text-[#0f172a]">
                                {{ mode === 'login' ? '欢迎登录' : '创建账号' }}
                            </div>
                            <div class="flex overflow-hidden rounded-[14px] border border-[rgba(15,23,42,0.08)] bg-[#f8fafc] p-[4px]">
                                <button
                                    class="rounded-[10px] px-[14px] py-[8px] text-[13px] font-semibold transition"
                                    :class="mode === 'login' ? 'bg-[#1d4ed8] text-white shadow-[0_8px_20px_rgba(37,99,235,0.24)]' : 'text-[#475569]'"
                                    type="button"
                                    @click="switchMode('login')"
                                >
                                    登录
                                </button>
                                <button
                                    class="rounded-[10px] px-[14px] py-[8px] text-[13px] font-semibold transition"
                                    :class="mode === 'register' ? 'bg-[#1d4ed8] text-white shadow-[0_8px_20px_rgba(37,99,235,0.24)]' : 'text-[#475569]'"
                                    type="button"
                                    @click="switchMode('register')"
                                >
                                    注册
                                </button>
                            </div>
                        </div>

                        <div class="space-y-[14px]">
                            <div>
                                <div class="mb-[8px] text-[13px] font-semibold text-[#475569]">用户名</div>
                                <input
                                    v-model="form.username"
                                    class="w-full rounded-[15px] border border-[#dbe5f3] bg-[#f8fbff] px-[14px] py-[12px] text-[14px] text-[#0f172a] outline-none transition focus:border-[#7fa9ff] focus:bg-white focus:ring-2 focus:ring-[#dbeafe]"
                                    placeholder="请输入用户名"
                                    @keydown.enter.prevent="submit"
                                />
                            </div>
                            <div>
                                <div class="mb-[8px] text-[13px] font-semibold text-[#475569]">密码</div>
                                <input
                                    v-model="form.password"
                                    type="password"
                                    class="w-full rounded-[15px] border border-[#dbe5f3] bg-[#f8fbff] px-[14px] py-[12px] text-[14px] text-[#0f172a] outline-none transition focus:border-[#7fa9ff] focus:bg-white focus:ring-2 focus:ring-[#dbeafe]"
                                    placeholder="请输入密码"
                                    @keydown.enter.prevent="submit"
                                />
                            </div>
                            <div v-if="error" class="rounded-[14px] border border-[rgba(248,113,113,0.22)] bg-[rgba(254,242,242,0.9)] px-[12px] py-[10px] text-[13px] text-[#dc2626]">
                                {{ error }}
                            </div>
                            <button
                                class="flex w-full items-center justify-center rounded-[15px] bg-[#2f67ff] px-[14px] py-[12px] text-[15px] font-semibold text-white shadow-[0_16px_32px_rgba(47,103,255,0.26)] transition hover:bg-[#2557e8] disabled:cursor-not-allowed disabled:bg-[#94a3b8]"
                                type="button"
                                :disabled="loading"
                                @click="submit"
                            >
                                {{ loading ? '提交中...' : mode === 'login' ? '立即登录' : '立即注册' }}
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <Footer layout="auth" />
    </div>
</template>
