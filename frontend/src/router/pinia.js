import { defineStore } from 'pinia';

const SETTINGS_KEY = 'chat_settings';
const AGENT_SETTINGS_KEY = 'agent_settings';
const AUTH_KEY = 'auth_info';
const CHAT_SESSION_CLIENT_KEY = 'chat_session_client_map';
const WORK_SESSION_AGENT_KEY = 'work_session_agent_map';
const CHAT_SESSION_LOCK_KEY = 'chat_session_lock_map';
const WORK_SESSION_LOCK_KEY = 'work_session_lock_map';
const APP_BASE = (import.meta.env.BASE_URL || '/').replace(/\/$/, '');

const loadSessionBindingMap = (key) => {
    try {
        const raw = localStorage.getItem(key);
        if (!raw) return {};
        const parsed = JSON.parse(raw);
        return parsed && typeof parsed === 'object' ? parsed : {};
    } catch (error) {
        console.warn('无法解析会话绑定信息，使用默认值', error);
        return {};
    }
};

const saveSessionBindingMap = (key, map) => {
    try {
        localStorage.setItem(key, JSON.stringify(map && typeof map === 'object' ? map : {}));
    } catch (error) {
        console.warn('保存会话绑定信息失败', error);
    }
};

const getSessionBindingKey = (session) => {
    if (!session || typeof session !== 'object') return '';
    if (session.sessionId) return String(session.sessionId);
    return '';
};

const getPersistedSessionBinding = (storageKey, session) => {
    const key = getSessionBindingKey(session);
    if (!key) return '';
    const map = loadSessionBindingMap(storageKey);
    return map[key] || '';
};

const setPersistedSessionBinding = (storageKey, session, value) => {
    const key = getSessionBindingKey(session);
    if (!key) return;
    const map = loadSessionBindingMap(storageKey);
    if (!value) {
        delete map[key];
    } else {
        map[key] = value;
    }
    saveSessionBindingMap(storageKey, map);
};

const removePersistedSessionBinding = (storageKey, session) => {
    const key = getSessionBindingKey(session);
    if (!key) return;
    const map = loadSessionBindingMap(storageKey);
    if (Object.prototype.hasOwnProperty.call(map, key)) {
        delete map[key];
        saveSessionBindingMap(storageKey, map);
    }
};

const getPersistedSessionLock = (storageKey, session) => {
    const key = getSessionBindingKey(session);
    if (!key) return false;
    const map = loadSessionBindingMap(storageKey);
    return Boolean(map[key]);
};

const setPersistedSessionLock = (storageKey, session, locked) => {
    const key = getSessionBindingKey(session);
    if (!key) return;
    const map = loadSessionBindingMap(storageKey);
    if (!locked) {
        delete map[key];
    } else {
        map[key] = true;
    }
    saveSessionBindingMap(storageKey, map);
};

const removePersistedSessionLock = (storageKey, session) => {
    const key = getSessionBindingKey(session);
    if (!key) return;
    const map = loadSessionBindingMap(storageKey);
    if (Object.prototype.hasOwnProperty.call(map, key)) {
        delete map[key];
        saveSessionBindingMap(storageKey, map);
    }
};

const withBasePath = (path) => {
    if (!path) return APP_BASE || '/';
    if (!path.startsWith('/')) {
        return `${APP_BASE}/${path}`;
    }
    return `${APP_BASE}${path}`;
};

const defaultSettings = () => ({
    type: 'complete',
    temperature: null,
    presencePenalty: null,
    maxCompletionTokens: null,
    model: '',
    ragTag: '',
    token: '',
    theme: 'light'
});

const loadSettings = () => {
    try {
        const raw = localStorage.getItem(SETTINGS_KEY);
        if (!raw) {
            return defaultSettings();
        }
        return { ...defaultSettings(), ...JSON.parse(raw) };
    } catch (error) {
        console.warn('无法解析本地设置，使用默认值', error);
        return defaultSettings();
    }
};

const normalizeStoredUser = (user) => {
    if (!user || typeof user !== 'object') {
        return null;
    }
    const username = typeof (user.username || user.userName) === 'string' ? String(user.username || user.userName).trim() : '';
    const role = typeof (user.role || user.userRole) === 'string' ? String(user.role || user.userRole).trim() : '';
    if (!username || !role) {
        return null;
    }
    return {
        userId: Number.isFinite(Number(user.userId)) ? Number(user.userId) : null,
        username,
        role,
        userName: username,
        userRole: role,
        userAvatar: user.userAvatar || user.avatarUrl || '',
        avatarUrl: user.avatarUrl || user.userAvatar || '',
        userStatus: user.userStatus
    };
};

const loadAuth = () => {
    try {
        const raw = localStorage.getItem(AUTH_KEY);
        if (!raw) {
            return { token: '', user: null };
        }
        const parsed = JSON.parse(raw);
        const token = typeof parsed.token === 'string' ? parsed.token.trim() : '';
        const user = normalizeStoredUser(parsed.user);
        if (!token || !user) {
            return { token: '', user: null };
        }
        return {
            token,
            user
        };
    } catch (error) {
        console.warn('无法解析登录信息，已清空', error);
        return { token: '', user: null };
    }
};

export const getStoredAuth = () => loadAuth();

export const useAuthStore = defineStore('auth', {
    state: () => ({
        token: loadAuth().token,
        user: loadAuth().user
    }),
    getters: {
        isLogin(state) {
            return Boolean(state.token);
        },
        isAdmin(state) {
            return state.user?.role === 'admin';
        }
    },
    actions: {
        setToken(token) {
            this.token = token || '';
            this.persist();
        },
        setUser(user) {
            this.user = normalizeStoredUser(user);
            this.persist();
        },
        logout(redirectPath = '/login') {
            this.clear();
            window.location.href = withBasePath(redirectPath);
        },
        setAuth({ token, user }) {
            const normalizedToken = typeof token === 'string' ? token.trim() : '';
            const normalizedUser = normalizeStoredUser(user);
            if (!normalizedToken || !normalizedUser) {
                this.token = '';
                this.user = null;
            } else {
                this.token = normalizedToken;
                this.user = normalizedUser;
            }
            this.persist();
        },
        clear() {
            this.token = '';
            this.user = null;
            localStorage.removeItem(AUTH_KEY);
        },
        persist() {
            localStorage.setItem(
                AUTH_KEY,
                JSON.stringify({
                    token: this.token,
                    user: this.user
                })
            );
        }
    }
});

export const useSettingsStore = defineStore('settings', {
    state: () => ({
        ...loadSettings()
    }),
    actions: {
        updateSettings(partial) {
            Object.assign(this, partial);
            this.persist();
        },
        persist() {
            const payload = {
                type: this.type,
                temperature: this.temperature,
                presencePenalty: this.presencePenalty,
                maxCompletionTokens: this.maxCompletionTokens,
                model: this.model,
                ragTag: this.ragTag,
                token: this.token,
                theme: this.theme
            };
            localStorage.setItem(SETTINGS_KEY, JSON.stringify(payload));
        },
        reset() {
            Object.assign(this, defaultSettings());
            this.persist();
        }
    }
});

const defaultAgentSettings = () => ({
    maxRetry: 2,
    maxRound: 2
});

const loadAgentSettings = () => {
    try {
        const raw = localStorage.getItem(AGENT_SETTINGS_KEY);
        if (!raw) {
            return defaultAgentSettings();
        }
        return { ...defaultAgentSettings(), ...JSON.parse(raw) };
    } catch (error) {
        console.warn('无法解析 MiniAgent 设置，使用默认值', error);
        return defaultAgentSettings();
    }
};

export const useAgentSettingsStore = defineStore('agentSettings', {
    state: () => ({
        ...loadAgentSettings()
    }),
    actions: {
        updateSettings(partial) {
            Object.assign(this, partial);
            this.persist();
        },
        persist() {
            const payload = {
                maxRetry: this.maxRetry,
                maxRound: this.maxRound
            };
            localStorage.setItem(AGENT_SETTINGS_KEY, JSON.stringify(payload));
        },
        reset() {
            Object.assign(this, defaultAgentSettings());
            this.persist();
        }
    }
});

export const useChatStore = defineStore('chat', {
    state: () => ({
        chats: [],
        currentChatId: null,
        sending: false,
        abortController: null
    }),
    getters: {
        currentChat(state) {
            const chat = state.chats.find((item) => item.sessionId === state.currentChatId);
            return chat || state.chats[0] || null;
        },
        currentMessages(state) {
            return this.currentChat?.messages || [];
        }
    },
    actions: {
        setChats(chats) {
            const existingMap = new Map(this.chats.map((item) => [item.sessionId, item]));
            const normalized = (Array.isArray(chats) ? chats : []).map((chat) => {
                const existing = existingMap.get(chat?.sessionId);
                const merged = {
                    ...(existing || {}),
                    ...(chat || {})
                };
                merged.messages = Array.isArray(merged.messages) ? merged.messages : [];
                const persistedClientId = getPersistedSessionBinding(CHAT_SESSION_CLIENT_KEY, merged);
                const persistedClientLocked = getPersistedSessionLock(CHAT_SESSION_LOCK_KEY, merged);
                merged.clientId = merged.clientId || existing?.clientId || persistedClientId || '';
                merged.clientLocked = Boolean(existing?.clientLocked || merged.clientLocked || persistedClientLocked);
                if (merged.clientId) {
                    setPersistedSessionBinding(CHAT_SESSION_CLIENT_KEY, merged, merged.clientId);
                }
                if (merged.clientLocked) {
                    setPersistedSessionLock(CHAT_SESSION_LOCK_KEY, merged, true);
                }
                return merged;
            });
            this.chats = normalized;
        },
        setCurrentChatId(chatId) {
            this.currentChatId = chatId || null;
        },
        upsertChat(chat) {
            if (!chat) return;
            const persistedClientId = getPersistedSessionBinding(CHAT_SESSION_CLIENT_KEY, chat);
            const persistedClientLocked = getPersistedSessionLock(CHAT_SESSION_LOCK_KEY, chat);
            const idx = this.chats.findIndex((item) => item.sessionId === chat.sessionId);
            if (idx === -1) {
                const normalized = {
                    ...chat,
                    messages: Array.isArray(chat.messages) ? chat.messages : [],
                    clientId: chat.clientId || persistedClientId || '',
                    clientLocked: Boolean(chat.clientLocked || persistedClientLocked)
                };
                this.chats.unshift(normalized);
                if (normalized.clientId) {
                    setPersistedSessionBinding(CHAT_SESSION_CLIENT_KEY, normalized, normalized.clientId);
                }
                if (normalized.clientLocked) {
                    setPersistedSessionLock(CHAT_SESSION_LOCK_KEY, normalized, true);
                }
            } else {
                const merged = { ...this.chats[idx], ...chat };
                merged.messages = Array.isArray(merged.messages) ? merged.messages : [];
                merged.clientId = merged.clientId || persistedClientId || '';
                merged.clientLocked = Boolean(merged.clientLocked || persistedClientLocked);
                this.chats[idx] = merged;
                if (merged.clientId) {
                    setPersistedSessionBinding(CHAT_SESSION_CLIENT_KEY, merged, merged.clientId);
                }
                if (merged.clientLocked) {
                    setPersistedSessionLock(CHAT_SESSION_LOCK_KEY, merged, true);
                }
            }
        },
        updateChatTitle(chatId, newTitle) {
            const chat = this.chats.find((item) => item.sessionId === chatId);
            if (chat) {
                chat.title = newTitle || '未命名会话';
            }
        },
        removeChat(chatId) {
            const idx = this.chats.findIndex((item) => item.sessionId === chatId);
            if (idx === -1) return;
            const target = this.chats[idx];
            this.chats.splice(idx, 1);
            removePersistedSessionBinding(CHAT_SESSION_CLIENT_KEY, target);
            removePersistedSessionLock(CHAT_SESSION_LOCK_KEY, target);
            if (this.currentChatId === chatId) {
                this.currentChatId = this.chats[0]?.sessionId || null;
            }
        },
        setChatMessages(chatId, messages) {
            const chat = this.chats.find((item) => item.sessionId === chatId);
            if (chat) {
                chat.messages = Array.isArray(messages) ? messages : [];
            }
        },
        setChatClient(chatId, clientId) {
            const chat = this.chats.find((item) => item.sessionId === chatId);
            if (chat) {
                chat.clientId = clientId || '';
                setPersistedSessionBinding(CHAT_SESSION_CLIENT_KEY, chat, chat.clientId);
            }
        },
        setChatSelectionLocked(chatId, locked = true) {
            const chat = this.chats.find((item) => item.sessionId === chatId);
            if (!chat) return;
            chat.clientLocked = Boolean(locked);
            setPersistedSessionLock(CHAT_SESSION_LOCK_KEY, chat, chat.clientLocked);
        },
        isChatSelectionLocked(chatId) {
            const chat = this.chats.find((item) => item.sessionId === chatId);
            if (chat) {
                return Boolean(chat.clientLocked);
            }
            if (!chatId) return false;
            return Boolean(loadSessionBindingMap(CHAT_SESSION_LOCK_KEY)[chatId]);
        },
        addUserMessage(content) {
            const chat = this.currentChat;
            if (!chat) return null;
            const message = {
                id: `msg_${Date.now()}_${Math.random().toString(16).slice(2, 8)}`,
                role: 'user',
                content,
                think: '',
                pending: false,
                error: null,
                createdAt: Date.now()
            };
            chat.messages.push(message);
            return message;
        },
        addAssistantMessage(payload) {
            const chat = this.currentChat;
            if (!chat) return null;
            const message = {
                id: payload.id || `msg_${Date.now()}_${Math.random().toString(16).slice(2, 8)}`,
                role: 'assistant',
                content: payload.content || '',
                think: payload.think || '',
                pending: Boolean(payload.pending),
                error: payload.error || null,
                createdAt: Date.now()
            };
            chat.messages.push(message);
            return message;
        },
        updateAssistantMessage(messageId, partial) {
            const chat = this.currentChat;
            if (!chat) return null;
            const target = chat.messages.find((item) => item.id === messageId);
            if (target) {
                Object.assign(target, partial);
            }
            return target;
        },
        setSending(flag) {
            this.sending = flag;
        },
        setAbortController(controller) {
            this.abortController = controller;
        },
        stopCurrentRequest() {
            if (this.abortController) {
                this.abortController.abort();
                this.abortController = null;
            }
            this.sending = false;
        }
    }
});

export const useAgentStore = defineStore('agent', {
    state: () => ({
        sessions: [],
        currentSessionId: null,
        sending: false,
        abortController: null
    }),
    getters: {
        currentSession(state) {
            const session = state.sessions.find((item) => item.sessionId === state.currentSessionId);
            return session || state.sessions[0] || null;
        },
        currentMessages() {
            return this.currentSession?.messages || [];
        },
        currentCards() {
            return this.currentSession?.cards || [];
        }
    },
    actions: {
        setSessions(sessions) {
            const existingMap = new Map(this.sessions.map((item) => [item.sessionId, item]));
            const normalized = (Array.isArray(sessions) ? sessions : []).map((session) => {
                const existing = existingMap.get(session?.sessionId);
                const merged = {
                    ...(existing || {}),
                    ...(session || {})
                };
                merged.messages = Array.isArray(merged.messages) ? merged.messages : [];
                merged.cards = Array.isArray(merged.cards) ? merged.cards : [];
                const persistedAgentId = getPersistedSessionBinding(WORK_SESSION_AGENT_KEY, merged);
                const persistedAgentLocked = getPersistedSessionLock(WORK_SESSION_LOCK_KEY, merged);
                merged.agentId = merged.agentId || existing?.agentId || persistedAgentId || '';
                merged.agentLocked = Boolean(existing?.agentLocked || merged.agentLocked || persistedAgentLocked);
                if (merged.agentId) {
                    setPersistedSessionBinding(WORK_SESSION_AGENT_KEY, merged, merged.agentId);
                }
                if (merged.agentLocked) {
                    setPersistedSessionLock(WORK_SESSION_LOCK_KEY, merged, true);
                }
                return merged;
            });
            this.sessions = normalized;
        },
        setCurrentSessionId(sessionId) {
            this.currentSessionId = sessionId || null;
        },
        upsertSession(session) {
            if (!session) return;
            const persistedAgentId = getPersistedSessionBinding(WORK_SESSION_AGENT_KEY, session);
            const persistedAgentLocked = getPersistedSessionLock(WORK_SESSION_LOCK_KEY, session);
            const idx = this.sessions.findIndex((item) => item.sessionId === session.sessionId);
            if (idx === -1) {
                const normalized = {
                    ...session,
                    messages: Array.isArray(session.messages) ? session.messages : [],
                    cards: Array.isArray(session.cards) ? session.cards : [],
                    agentId: session.agentId || persistedAgentId || '',
                    agentLocked: Boolean(session.agentLocked || persistedAgentLocked)
                };
                this.sessions.unshift(normalized);
                if (normalized.agentId) {
                    setPersistedSessionBinding(WORK_SESSION_AGENT_KEY, normalized, normalized.agentId);
                }
                if (normalized.agentLocked) {
                    setPersistedSessionLock(WORK_SESSION_LOCK_KEY, normalized, true);
                }
            } else {
                const merged = { ...this.sessions[idx], ...session };
                merged.messages = Array.isArray(merged.messages) ? merged.messages : [];
                merged.cards = Array.isArray(merged.cards) ? merged.cards : [];
                merged.agentId = merged.agentId || persistedAgentId || '';
                merged.agentLocked = Boolean(merged.agentLocked || persistedAgentLocked);
                this.sessions[idx] = merged;
                if (merged.agentId) {
                    setPersistedSessionBinding(WORK_SESSION_AGENT_KEY, merged, merged.agentId);
                }
                if (merged.agentLocked) {
                    setPersistedSessionLock(WORK_SESSION_LOCK_KEY, merged, true);
                }
            }
        },
        updateSessionTitle(sessionId, newTitle) {
            const session = this.sessions.find((item) => item.sessionId === sessionId);
            if (session) {
                session.title = newTitle || '未命名会话';
            }
        },
        removeSession(sessionId) {
            const idx = this.sessions.findIndex((item) => item.sessionId === sessionId);
            if (idx === -1) return;
            const target = this.sessions[idx];
            this.sessions.splice(idx, 1);
            removePersistedSessionBinding(WORK_SESSION_AGENT_KEY, target);
            removePersistedSessionLock(WORK_SESSION_LOCK_KEY, target);
            if (this.currentSessionId === sessionId) {
                this.currentSessionId = this.sessions[0]?.sessionId || null;
            }
        },
        setSessionMessages(sessionId, messages) {
            const session = this.sessions.find((item) => item.sessionId === sessionId);
            if (session) {
                session.messages = Array.isArray(messages) ? messages : [];
            }
        },
        setSessionCards(sessionId, cards) {
            const session = this.sessions.find((item) => item.sessionId === sessionId);
            if (session) {
                session.cards = Array.isArray(cards) ? cards : [];
            }
        },
        setSessionAgent(sessionId, agentId) {
            const session = this.sessions.find((item) => item.sessionId === sessionId);
            if (session) {
                session.agentId = agentId || '';
                setPersistedSessionBinding(WORK_SESSION_AGENT_KEY, session, session.agentId);
            }
        },
        setSessionSelectionLocked(sessionId, locked = true) {
            const session = this.sessions.find((item) => item.sessionId === sessionId);
            if (!session) return;
            session.agentLocked = Boolean(locked);
            setPersistedSessionLock(WORK_SESSION_LOCK_KEY, session, session.agentLocked);
        },
        isSessionSelectionLocked(sessionId) {
            const session = this.sessions.find((item) => item.sessionId === sessionId);
            if (session) {
                return Boolean(session.agentLocked);
            }
            if (!sessionId) return false;
            return Boolean(loadSessionBindingMap(WORK_SESSION_LOCK_KEY)[sessionId]);
        },
        addUserMessage(content) {
            const session = this.currentSession;
            if (!session) return null;
            const message = {
                id: `msg_${Date.now()}_${Math.random().toString(16).slice(2, 8)}`,
                role: 'user',
                content,
                pending: false,
                error: null,
                createdAt: Date.now()
            };
            session.messages.push(message);
            return message;
        },
        addAssistantMessage(payload) {
            const session = this.currentSession;
            if (!session) return null;
            const message = {
                id: payload.id || `msg_${Date.now()}_${Math.random().toString(16).slice(2, 8)}`,
                role: 'assistant',
                content: payload.content || '',
                pending: Boolean(payload.pending),
                error: payload.error || null,
                createdAt: Date.now()
            };
            session.messages.push(message);
            return message;
        },
        updateAssistantMessage(messageId, partial) {
            const session = this.currentSession;
            if (!session) return null;
            const target = session.messages.find((item) => item.id === messageId);
            if (target) {
                Object.assign(target, partial);
            }
            return target;
        },
        addCard(payload) {
            const session = this.currentSession;
            if (!session) return null;
            const card = {
                id: payload.id || `card_${Date.now()}_${Math.random().toString(16).slice(2, 8)}`,
                clientType: payload.clientType || '',
                sectionType: payload.sectionType || '',
                sectionContent: payload.sectionContent || '',
                round: payload.round ?? null,
                step: payload.step ?? null,
                timestamp: payload.timestamp ?? null
            };
            session.cards.push(card);
            return card;
        },
        setSending(flag) {
            this.sending = flag;
        },
        setAbortController(controller) {
            this.abortController = controller;
        },
        stopCurrentRequest() {
            if (this.abortController) {
                this.abortController.abort();
                this.abortController = null;
            }
            this.sending = false;
        }
    }
});

export const useWelcomeLaunchStore = defineStore('welcomeLaunch', {
    state: () => ({
        task: null
    }),
    actions: {
        setTask(task) {
            this.task = task || null;
        },
        takeTask(type = '') {
            if (!this.task) return null;
            if (type && this.task.type !== type) return null;
            const task = this.task;
            this.task = null;
            return task;
        },
        clearTask() {
            this.task = null;
        }
    }
});
