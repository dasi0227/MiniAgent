<script setup>
import { computed, reactive, ref, watch, onMounted, onBeforeUnmount } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import AdminSidebar from './AdminSidebar.vue';
import AdminSelect from './AdminSelect.vue';
import Footer from './Footer.vue';
import { adminMenuGroups } from '../utils/CommonDataUtil';
import {
    adminPage,
    adminInsert,
    adminUpdate,
    adminDelete,
    adminToggle,
    listClientType,
    listClientRole,
    listAgentType,
    listUserRole,
    listApiId,
    listModelId
} from '../request/api';
import { formatDateTime } from '../utils/DatetimeUtil';
import { prettifyJsonString } from '../utils/StringUtil';
import { normalizeError, notifyAdminError } from '../request/request';

const route = useRoute();
const router = useRouter();

const sortByOptions = [
    { label: '点赞', value: 'like' },
    { label: '收藏', value: 'favor' },
    { label: '评论', value: 'comment' }
];

const sortOrderOptions = [
    { label: '降序', value: 'desc' },
    { label: '升序', value: 'asc' }
];

const moduleDefs = [
    {
        key: 'agent',
        label: 'AGENT',
        group: 'model',
        title: 'AGENT 管理',
        statusField: 'agentStatus',
        search: ['keyword', 'agentType'],
        query: { keyword: '', agentType: '', pageNum: 1, pageSize: 10 },
        formDefaults: () => ({
            agentId: '',
            agentName: '',
            agentType: 'loop',
            agentDesc: '',
            modelId: '',
            templateId: '',
            agentStatus: 1
        }),
        fields: [
            { prop: 'agentId', label: 'Agent ID', required: true },
            { prop: 'agentName', label: '名称', required: true },
            { prop: 'agentType', label: '策略类型', type: 'select', required: true, optionsKey: 'agentTypes' },
            { prop: 'agentDesc', label: '描述', type: 'textarea', required: true },
            { prop: 'modelId', label: '模型ID' },
            { prop: 'templateId', label: '模版ID' },
            { prop: 'agentStatus', label: '状态', type: 'switch' }
        ],
        listColumns: [
            { prop: 'agentId', label: 'ID', width: 220 },
            { prop: 'agentName', label: '名称', width: 180 },
            { prop: 'agentType', label: '策略类型', width: 136, type: 'chip' },
            { prop: 'modelName', label: '模型', width: 190 },
            { prop: 'templateId', label: '模版', width: 170 },
            { prop: 'userName', label: '创建人', width: 130 },
            { prop: 'updateTime', label: '改动时间', width: 172, type: 'datetime' }
        ]
    },
    {
        key: 'client',
        label: 'CLIENT',
        group: 'model',
        title: 'CLIENT 管理',
        statusField: 'clientStatus',
        search: ['keyword', 'clientType', 'clientRole', 'modelId'],
        query: { keyword: '', clientType: '', clientRole: '', modelId: '', pageNum: 1, pageSize: 10 },
        formDefaults: () => ({
            clientId: '',
            clientName: '',
            clientType: '',
            clientRole: '',
            modelId: '',
            modelName: '',
            clientStatus: 1
        }),
        fields: [
            { prop: 'clientId', label: 'Client ID', required: true },
            { prop: 'clientName', label: '名称', required: true },
            { prop: 'clientType', label: '类型', type: 'select', required: true, optionsKey: 'clientTypes' },
            { prop: 'clientRole', label: '角色', type: 'select', required: true, optionsKey: 'clientRoles' },
            { prop: 'modelId', label: '模型ID', type: 'select', optionsKey: 'modelIds', required: true },
            { prop: 'clientStatus', label: '状态', type: 'switch' }
        ],
        listColumns: [
            { prop: 'clientId', label: 'ID', width: 280 },
            { prop: 'clientName', label: '名称', width: 196 },
            { prop: 'modelName', label: '模型', width: 180 },
            { prop: 'userName', label: '创建人', width: 130 },
            { prop: 'updateTime', label: '改动时间', width: 172, type: 'datetime' }
        ]
    },
    {
        key: 'template',
        label: 'TEMPLATE',
        group: 'model',
        title: 'TEMPLATE 管理',
        statusField: null,
        search: ['keyword'],
        query: { keyword: '', pageNum: 1, pageSize: 10 },
        formDefaults: () => ({
            templateId: '',
            userId: '',
            agentName: '',
            agentType: '',
            agentDesc: '',
            apiBaseUrl: '',
            apiCompletionUrl: '',
            modelName: '',
            modelType: '',
            snapshot: '{}'
        }),
        fields: [
            { prop: 'templateId', label: '模板ID', required: true },
            { prop: 'userId', label: '用户ID', type: 'number', required: true },
            { prop: 'agentName', label: '名称', required: true },
            { prop: 'agentType', label: '类型', required: true },
            { prop: 'agentDesc', label: '描述', type: 'textarea', required: true },
            { prop: 'apiBaseUrl', label: 'API Base URL', required: true },
            { prop: 'apiCompletionUrl', label: 'API Completion URL', required: true },
            { prop: 'modelName', label: '模型名称', required: true },
            { prop: 'modelType', label: '模型类型', required: true },
            { prop: 'snapshot', label: '快照', type: 'textarea', required: true, rows: 16 }
        ],
        listColumns: [
            { prop: 'templateId', label: 'ID', width: 220 },
            { prop: 'agentName', label: '名称', width: 190 },
            { prop: 'agentType', label: '类型', width: 132, type: 'chip' },
            { prop: 'modelName', label: '模型', width: 184 },
            { prop: 'userName', label: '创建人', width: 130 },
            { prop: 'updateTime', label: '改动时间', width: 172, type: 'datetime' }
        ]
    },
    {
        key: 'api',
        label: 'API',
        group: 'base',
        title: 'API 管理',
        statusField: null,
        search: ['keyword'],
        query: { keyword: '', pageNum: 1, pageSize: 10 },
        formDefaults: () => ({
            apiId: '',
            apiBaseUrl: '',
            apiKey: '',
            apiCompletionsPath: '/v1/chat/completions',
            apiEmbeddingsPath: '/v1/embeddings'
        }),
        fields: [
            { prop: 'apiId', label: 'API ID', required: true },
            { prop: 'apiBaseUrl', label: 'Base URL', required: true },
            { prop: 'apiKey', label: 'Key', required: true },
            { prop: 'apiCompletionsPath', label: '补全路径', required: true },
            { prop: 'apiEmbeddingsPath', label: '向量路径', required: true }
        ],
        listColumns: [
            { prop: 'apiId', label: 'ID', width: 220 },
            { prop: 'apiBaseUrl', label: '基础路径', width: 320 },
            { prop: 'apiCompletionsPath', label: '补全路径', width: 250 },
            { prop: 'userName', label: '创建人', width: 130 },
            { prop: 'updateTime', label: '改动时间', width: 172, type: 'datetime' }
        ]
    },
    {
        key: 'model',
        label: 'MODEL',
        group: 'base',
        title: 'MODEL 管理',
        statusField: null,
        search: ['keyword', 'apiId'],
        query: { keyword: '', apiId: '', pageNum: 1, pageSize: 10 },
        formDefaults: () => ({
            modelId: '',
            apiId: '',
            modelName: '',
            modelType: ''
        }),
        fields: [
            { prop: 'modelId', label: 'Model ID', required: true },
            { prop: 'modelName', label: '模型', required: true },
            { prop: 'modelType', label: '类型', required: true },
            { prop: 'apiId', label: 'API', type: 'select', optionsKey: 'apiIds', required: true }
        ],
        listColumns: [
            { prop: 'modelId', label: 'ID', width: 220 },
            { prop: 'modelName', label: '模型', width: 220 },
            { prop: 'modelType', label: '类型', width: 140, type: 'chip' },
            { prop: 'userName', label: '创建人', width: 130 },
            { prop: 'updateTime', label: '改动时间', width: 172, type: 'datetime' }
        ]
    },
    {
        key: 'mcp',
        label: 'MCP',
        group: 'base',
        title: 'MCP 管理',
        statusField: null,
        search: ['keyword'],
        query: { keyword: '', pageNum: 1, pageSize: 10 },
        formDefaults: () => ({
            mcpId: '',
            mcpName: '',
            mcpType: '',
            mcpParam: '',
            mcpSecret: '',
            mcpDesc: '',
            mcpTimeout: 180
        }),
        fields: [
            { prop: 'mcpId', label: 'MCP ID', required: true },
            { prop: 'mcpName', label: '名称', required: true },
            { prop: 'mcpType', label: '类型', required: true },
            { prop: 'mcpParam', label: '配置', type: 'textarea', required: true, placeholder: 'JSON' },
            { prop: 'mcpSecret', label: '密钥', type: 'textarea', placeholder: 'JSON' },
            { prop: 'mcpDesc', label: '描述', type: 'textarea', required: true },
            { prop: 'mcpTimeout', label: '超时时间', type: 'number' }
        ],
        listColumns: [
            { prop: 'mcpId', label: 'ID', width: 220 },
            { prop: 'mcpName', label: '名称', width: 220 },
            { prop: 'mcpType', label: '类型', width: 140, type: 'chip' },
            { prop: 'userName', label: '创建人', width: 130 },
            { prop: 'updateTime', label: '改动时间', width: 172, type: 'datetime' }
        ]
    },
    {
        key: 'prompt',
        label: 'PROMPT',
        group: 'base',
        title: 'PROMPT 管理',
        statusField: null,
        search: ['keyword'],
        query: { keyword: '', pageNum: 1, pageSize: 10 },
        formDefaults: () => ({
            promptId: '',
            promptName: '',
            systemPrompt: ''
        }),
        fields: [
            { prop: 'promptId', label: 'Prompt ID', required: true },
            { prop: 'promptName', label: '名称', required: true },
            { prop: 'systemPrompt', label: '内容', type: 'textarea', required: true }
        ],
        listColumns: [
            { prop: 'promptId', label: 'ID', width: 220 },
            { prop: 'promptName', label: '名称', width: 210 },
            { prop: 'systemPrompt', label: '内容', width: 360, type: 'ellipsis' },
            { prop: 'updateTime', label: '改动时间', width: 172, type: 'datetime' }
        ]
    },
    {
        key: 'advisor',
        label: 'ADVISOR',
        group: 'base',
        title: 'ADVISOR 管理',
        statusField: null,
        search: ['keyword'],
        query: { keyword: '', pageNum: 1, pageSize: 10 },
        formDefaults: () => ({
            advisorId: '',
            advisorName: '',
            advisorType: '',
            advisorParam: ''
        }),
        fields: [
            { prop: 'advisorId', label: 'Advisor ID', required: true },
            { prop: 'advisorName', label: '名称', required: true },
            { prop: 'advisorType', label: '类型', required: true },
            { prop: 'advisorParam', label: '参数', type: 'textarea' }
        ],
        listColumns: [
            { prop: 'advisorId', label: 'ID', width: 220 },
            { prop: 'advisorName', label: '名称', width: 220 },
            { prop: 'advisorType', label: '类型', width: 140, type: 'chip' },
            { prop: 'updateTime', label: '改动时间', width: 172, type: 'datetime' }
        ]
    },
    {
        key: 'user',
        label: 'USER',
        group: 'user',
        title: 'USER 管理',
        statusField: 'userStatus',
        search: ['keyword', 'userRole'],
        query: { keyword: '', userRole: '', pageNum: 1, pageSize: 10 },
        formDefaults: () => ({
            originUserName: '',
            userName: '',
            password: '',
            userRole: 'account',
            userAvatar: '',
            userStatus: 1
        }),
        fields: [
            { prop: 'userName', label: '用户名', required: true },
            { prop: 'password', label: '密码', type: 'password', required: true },
            {
                prop: 'userRole',
                label: '角色',
                type: 'select',
                options: [
                    { label: 'admin', value: 'admin' },
                    { label: 'account', value: 'account' }
                ],
                required: true
            },
            { prop: 'userAvatar', label: '头像', required: true },
            { prop: 'userStatus', label: '状态', type: 'switch' }
        ],
        listColumns: [
            { prop: 'userId', label: 'ID', width: 120 },
            { prop: 'userName', label: '名称', width: 180 },
            { prop: 'userRole', label: '角色', width: 132, type: 'chip' },
            { prop: 'userAvatar', label: '头像', width: 260 },
            { prop: 'updateTime', label: '改动时间', width: 172, type: 'datetime' }
        ]
    },
    {
        key: 'plaza',
        label: 'PLAZA',
        group: 'other',
        title: 'PLAZA 管理',
        statusField: null,
        search: ['keyword', 'sortBy', 'sortOrder'],
        query: { keyword: '', sortBy: '', sortOrder: '', pageNum: 1, pageSize: 10 },
        formDefaults: () => ({
            plazaId: '',
            templateId: '',
            userId: '',
            agentName: '',
            agentType: '',
            plazaTitle: '',
            plazaDesc: '',
            likeCount: 0,
            favorCount: 0,
            commentCount: 0
        }),
        fields: [
            { prop: 'plazaId', label: '广场ID', required: true },
            { prop: 'templateId', label: '模板ID', required: true },
            { prop: 'userId', label: '用户ID', type: 'number', required: true },
            { prop: 'agentName', label: '智能体名称', required: true },
            { prop: 'agentType', label: '策略', required: true },
            { prop: 'plazaTitle', label: '标题', required: true },
            { prop: 'plazaDesc', label: '描述', type: 'textarea', required: true },
            { prop: 'likeCount', label: '点赞数', type: 'number', required: true },
            { prop: 'favorCount', label: '收藏数', type: 'number', required: true },
            { prop: 'commentCount', label: '评论数', type: 'number', required: true }
        ],
        listColumns: [
            { prop: 'plazaId', label: 'ID', width: 220 },
            { prop: 'templateId', label: '模版', width: 170 },
            { prop: 'plazaTitle', label: '标题', width: 260 },
            { prop: 'engagement', label: '点赞/收藏/评论', width: 210 },
            { prop: 'userName', label: '创建人', width: 130 },
            { prop: 'updateTime', label: '改动时间', width: 172, type: 'datetime' }
        ]
    },
    {
        key: 'task',
        label: 'TASK',
        group: 'other',
        title: 'TASK 管理',
        statusField: 'taskStatus',
        search: ['keyword', 'agentId'],
        query: { keyword: '', agentId: '', pageNum: 1, pageSize: 10 },
        formDefaults: () => ({
            taskId: '',
            agentId: '',
            taskCron: '',
            taskDesc: '',
            taskParam: '',
            taskStatus: 1
        }),
        fields: [
            { prop: 'taskId', label: 'Task ID', required: true },
            { prop: 'agentId', label: '智能体', type: 'select', optionsKey: 'agents', required: true },
            { prop: 'taskCron', label: 'Cron表达式', required: true },
            { prop: 'taskDesc', label: '描述', type: 'textarea', required: true },
            { prop: 'taskParam', label: '参数', type: 'textarea', required: true, placeholder: 'JSON' },
            { prop: 'taskStatus', label: '状态', type: 'switch' }
        ],
        listColumns: [
            { prop: 'taskId', label: 'ID', width: 220 },
            { prop: 'agentId', label: '智能体', width: 220 },
            { prop: 'taskCron', label: 'Cron表达式', width: 220 },
            { prop: 'userName', label: '创建人', width: 130 },
            { prop: 'updateTime', label: '改动时间', width: 172, type: 'datetime' }
        ]
    }
];

const modulesMap = Object.fromEntries(moduleDefs.map((item) => [item.key, item]));
const tableKeys = moduleDefs.map((item) => item.key);
const menuGroups = adminMenuGroups;

const toggleActionModules = new Set(['agent', 'client', 'user', 'task']);

const resolveKeyFromRoute = () => {
    const key = route.params.module;
    if (typeof key === 'string' && tableKeys.includes(key)) {
        return key;
    }
    return 'agent';
};

const currentKey = ref(resolveKeyFromRoute());
const modalVisible = ref(false);
const editingId = ref(null);
const modalError = ref('');
const currentForm = reactive({});

const unwrapResult = (resp, defaultMsg = '操作失败') => {
    if (resp && typeof resp === 'object' && Object.prototype.hasOwnProperty.call(resp, 'code')) {
        if (resp.code !== 200) {
            const err = new Error(resp.info || defaultMsg);
            err.status = 500;
            throw err;
        }
        return resp.data;
    }
    return resp?.data ?? resp?.result ?? resp;
};

const stateMap = reactive(
    Object.fromEntries(
        moduleDefs.map((item) => [
            item.key,
            {
                list: [],
                total: 0,
                pageSum: 1,
                loading: false,
                query: { ...item.query },
                error: ''
            }
        ])
    )
);

const options = reactive({
    apiIds: [],
    modelIds: [],
    agentTypes: [],
    clientTypes: [],
    clientRoles: [],
    roles: [],
    agents: []
});

const currentModule = computed(() => modulesMap[currentKey.value] || modulesMap.agent);
const hasToggleAction = computed(() => toggleActionModules.has(currentKey.value));
const operationWidth = computed(() => (hasToggleAction.value ? 212 : 164));
const tableViewportRef = ref(null);
const tableViewportWidth = ref(0);
let tableResizeObserver = null;

const syncTableViewportWidth = () => {
    tableViewportWidth.value = tableViewportRef.value?.clientWidth || 0;
};

const baseLeftColumnWidth = computed(() =>
    (currentModule.value.listColumns || []).reduce((sum, col) => sum + (Number(col.width) || 0), 0)
);

const resolvedLeftColumnWidths = computed(() => {
    const columns = currentModule.value.listColumns || [];
    const baseWidths = columns.map((col) => Number(col.width) || 0);
    const viewport = tableViewportWidth.value;
    const availableLeft = Math.max(0, viewport - operationWidth.value);
    const baseSum = baseWidths.reduce((sum, width) => sum + width, 0);

    if (!baseSum || availableLeft <= baseSum) {
        return baseWidths;
    }

    const ratio = availableLeft / baseSum;
    const scaled = baseWidths.map((width) => Math.floor(width * ratio));
    const scaledSum = scaled.reduce((sum, width) => sum + width, 0);
    const remain = availableLeft - scaledSum;
    if (scaled.length > 0 && remain > 0) {
        scaled[scaled.length - 1] += remain;
    }
    return scaled;
});

const tableWidth = computed(
    () => resolvedLeftColumnWidths.value.reduce((sum, width) => sum + width, 0) + operationWidth.value
);

const tableStyle = computed(() => ({
    width: `${tableWidth.value}px`,
    minWidth: `${tableWidth.value}px`
}));

const pageCount = computed(() => {
    const state = stateMap[currentKey.value];
    if (!state) return 1;
    return state.pageSum || 1;
});

const isToggleDisabled = (row) => currentKey.value === 'user' && row?.userRole === 'admin';
const isFormToggleDisabled = (field) =>
    currentKey.value === 'user' && field?.prop === 'userStatus' && currentForm.userRole === 'admin';

const isRequiredEmpty = (value) =>
    value === null ||
    value === undefined ||
    (typeof value === 'string' && value.trim() === '') ||
    (Array.isArray(value) && value.length === 0);

const resolveRowKey = (moduleKey, row = {}) => {
    const fieldMap = {
        client: 'clientId',
        api: 'apiId',
        model: 'modelId',
        mcp: 'mcpId',
        advisor: 'advisorId',
        prompt: 'promptId',
        user: 'userName',
        task: 'taskId',
        agent: 'agentId',
        template: 'templateId',
        plaza: 'plazaId'
    };
    const field = fieldMap[moduleKey];
    return field ? row?.[field] || '' : '';
};

const showErrorToast = (error, defaultMsg = '操作失败') => {
    const msg = normalizeError(error).message || defaultMsg;
    notifyAdminError(error, msg);
};

const tryParseJson = (value) => {
    try {
        return { ok: true, value: JSON.parse(value) };
    } catch {
        return { ok: false, value };
    }
};

const parseJsonLayer = (value) => {
    if (typeof value !== 'string') return value;
    let current = value;
    for (let i = 0; i < 3; i += 1) {
        const trimmed = String(current).trim();
        if (!trimmed) break;
        const parsed = tryParseJson(trimmed);
        if (!parsed.ok) break;
        current = parsed.value;
        if (typeof current !== 'string') {
            break;
        }
    }
    return current;
};

const escapeNewlinesInJsonStrings = (text) => {
    if (typeof text !== 'string') return text;
    let result = '';
    let inString = false;
    let escaped = false;
    for (let i = 0; i < text.length; i += 1) {
        const ch = text[i];
        if (escaped) {
            result += ch;
            escaped = false;
            continue;
        }
        if (ch === '\\') {
            result += ch;
            escaped = true;
            continue;
        }
        if (ch === '"') {
            inString = !inString;
            result += ch;
            continue;
        }
        if ((ch === '\n' || ch === '\r') && inString) {
            result += '\\n';
            if (ch === '\r' && text[i + 1] === '\n') {
                i += 1;
            }
            continue;
        }
        result += ch;
    }
    return result;
};

const normalizeSnapshotForEditor = (value) => {
    if (typeof value !== 'string') return value;
    const layered = parseJsonLayer(value);
    if (layered && typeof layered === 'object') {
        return JSON.stringify(layered, null, 2).replace(/\\n/g, '\n');
    }
    const text = String(layered ?? value);
    const pretty = prettifyJsonString(text);
    return String(pretty).replace(/\\n/g, '\n');
};

const normalizeSnapshotForSubmit = (value) => {
    if (typeof value !== 'string') return value;
    const raw = value.trim();
    if (!raw) return value;

    const parsedDirect = tryParseJson(raw);
    if (parsedDirect.ok) {
        return JSON.stringify(parsedDirect.value, null, 2);
    }

    const escaped = escapeNewlinesInJsonStrings(value);
    const parsedEscaped = tryParseJson(String(escaped).trim());
    if (parsedEscaped.ok) {
        return JSON.stringify(parsedEscaped.value, null, 2);
    }

    return value;
};

const prettifyFormJsonFields = (moduleKey, form) => {
    if (!form || typeof form !== 'object') return;
    const jsonFieldsMap = {
        mcp: ['mcpParam', 'mcpSecret'],
        advisor: ['advisorParam'],
        task: ['taskParam'],
        template: ['snapshot']
    };
    (jsonFieldsMap[moduleKey] || []).forEach((field) => {
        if (typeof form[field] === 'string') {
            if (moduleKey === 'template' && field === 'snapshot') {
                form[field] = normalizeSnapshotForEditor(form[field]);
            } else {
                form[field] = prettifyJsonString(form[field]);
            }
        }
    });
};

const getCellRawValue = (row, col) => {
    if (!row || !col) return '';
    if (col.prop === 'updateTime') {
        return row.updateTime || row.createTime || '';
    }
    if (col.prop === 'systemPrompt') {
        return row.systemPrompt || row.systenPrompt || '';
    }
    if (col.prop === 'modelName') {
        if (currentKey.value === 'agent') {
            return row.modelName || '';
        }
        return row.modelName || row.modelId || '';
    }
    if (col.prop === 'engagement') {
        return `${row.likeCount ?? 0}/${row.favorCount ?? 0}/${row.commentCount ?? 0}`;
    }
    return row[col.prop];
};

const getCellText = (row, col) => {
    const raw = getCellRawValue(row, col);
    if (col?.type === 'datetime') {
        return formatDateTime(raw);
    }
    if (raw === null || raw === undefined || raw === '') {
        return '-';
    }
    return String(raw);
};

const getCellTitle = (row, col) => {
    if (col?.type === 'chip' || col?.type === 'datetime') {
        return '';
    }
    return getCellText(row, col);
};

const getCellClass = (col) => {
    if (col?.type === 'ellipsis') {
        return 'max-w-0 truncate whitespace-nowrap';
    }
    if (col?.type === 'chip') {
        return '';
    }
    if (col?.type === 'datetime') {
        return 'whitespace-nowrap';
    }
    return 'whitespace-nowrap';
};

const loadRefs = async () => {
    const loadList = async (setter, fn) => {
        try {
            const res = await fn();
            const payload = res?.data ?? res?.result ?? res;
            setter(payload?.list || payload || []);
        } catch {
            setter([]);
        }
    };

    await Promise.all([
        loadList((val) => (options.clientTypes = val), listClientType),
        loadList((val) => (options.clientRoles = val), listClientRole),
        loadList((val) => (options.agentTypes = val), listAgentType),
        loadList((val) => (options.roles = val), listUserRole),
        loadList((val) => (options.apiIds = val), listApiId),
        loadList((val) => (options.modelIds = val), listModelId),
        (async () => {
            try {
                const res = await adminPage('agent', { pageNum: 1, pageSize: 10 });
                const payload = res?.data ?? res?.result ?? res;
                options.agents =
                    payload?.list?.map((item) => ({
                        value: item.agentId,
                        label: item.agentName || item.agentId
                    })) || [];
            } catch {
                options.agents = [];
            }
        })()
    ]);
};

const fetchList = async (key = currentKey.value) => {
    const state = stateMap[key];
    state.loading = true;
    state.error = '';
    try {
        const params = { ...state.query };
        Object.keys(params).forEach((paramKey) => {
            if (params[paramKey] === '' || params[paramKey] === null || params[paramKey] === undefined) {
                delete params[paramKey];
            }
        });
        const res = await adminPage(key, params);
        const payload = unwrapResult(res, '查询失败');
        state.list = payload?.list || [];
        state.total = payload?.total || 0;
        state.pageSum = payload?.pageSum || 1;
        state.query.pageNum = payload?.pageNum || state.query.pageNum;
    } catch (error) {
        state.error = normalizeError(error).message;
        notifyAdminError(error, state.error || '查询失败');
    } finally {
        state.loading = false;
    }
};

const openCreate = () => {
    editingId.value = null;
    modalError.value = '';
    Object.assign(currentForm, currentModule.value.formDefaults());
    prettifyFormJsonFields(currentKey.value, currentForm);
    modalVisible.value = true;
};

const openEdit = (row) => {
    editingId.value = resolveRowKey(currentKey.value, row);
    modalError.value = '';
    Object.assign(currentForm, currentModule.value.formDefaults(), row);
    if (currentKey.value === 'user') {
        currentForm.originUserName = row.userName || '';
    }
    if (currentKey.value === 'prompt' && row.systemPrompt && !currentForm.systemPrompt) {
        currentForm.systemPrompt = row.systemPrompt;
    }
    prettifyFormJsonFields(currentKey.value, currentForm);
    modalVisible.value = true;
};

const handleDelete = async (row) => {
    if (!window.confirm('确认删除该记录？')) return;
    try {
        const res = await adminDelete(currentKey.value, resolveRowKey(currentKey.value, row));
        unwrapResult(res, '删除失败');
        await fetchList();
    } catch (error) {
        showErrorToast(error, '删除失败');
    }
};

const saveForm = async () => {
    const module = currentModule.value;
    modalError.value = '';
    for (const field of module.fields) {
        if (field.required && isRequiredEmpty(currentForm[field.prop])) {
            modalError.value = `${field.label} 不能为空`;
            return;
        }
        if (field.requiredOnCreate && !editingId.value && isRequiredEmpty(currentForm[field.prop])) {
            modalError.value = `${field.label} 不能为空`;
            return;
        }
    }

    const payload = { ...currentForm };
    if (module.key === 'prompt' && payload.systemPrompt && !payload.systenPrompt) {
        payload.systenPrompt = payload.systemPrompt;
    }
    if (module.key === 'template' && typeof payload.snapshot === 'string') {
        payload.snapshot = normalizeSnapshotForSubmit(payload.snapshot);
    }

    try {
        if (editingId.value) {
            const res = await adminUpdate(module.key, payload);
            unwrapResult(res, '更新失败');
        } else {
            const res = await adminInsert(module.key, payload);
            unwrapResult(res, '创建失败');
        }
        modalVisible.value = false;
        await Promise.all([fetchList(), loadRefs()]);
    } catch (error) {
        showErrorToast(error, '保存失败');
    }
};

const switchStatus = async (row, val) => {
    if (!hasToggleAction.value || isToggleDisabled(row)) {
        return;
    }
    const field = currentModule.value.statusField;
    const oldVal = row[field];
    row[field] = val;
    try {
        const res = await adminToggle(currentKey.value, resolveRowKey(currentKey.value, row), val);
        unwrapResult(res, '更新状态失败');
    } catch (error) {
        row[field] = oldVal;
        showErrorToast(error, '更新状态失败');
    }
};

const changePage = (step) => {
    const state = stateMap[currentKey.value];
    const next = Math.min(Math.max(1, state.query.pageNum + step), pageCount.value);
    state.query.pageNum = next;
    fetchList();
};

const handleRefresh = async () => {
    await Promise.all([loadRefs(), fetchList(currentKey.value)]);
};

watch(
    () => currentKey.value,
    async () => {
        await fetchList();
    }
);

watch(
    () => route.params.module,
    (value) => {
        if (typeof value === 'string' && tableKeys.includes(value)) {
            currentKey.value = value;
        }
    }
);

onMounted(async () => {
    await loadRefs();
    await fetchList(currentKey.value);
    syncTableViewportWidth();
    if (typeof ResizeObserver !== 'undefined') {
        tableResizeObserver = new ResizeObserver(() => {
            syncTableViewportWidth();
        });
        if (tableViewportRef.value) {
            tableResizeObserver.observe(tableViewportRef.value);
        }
    }
});

onBeforeUnmount(() => {
    if (tableResizeObserver) {
        tableResizeObserver.disconnect();
        tableResizeObserver = null;
    }
});

const handleSelectModule = (key) => {
    const target = menuGroups
        .flatMap((group) => group.items)
        .find((item) => item.key === key);
    if (target?.path) {
        router.push(target.path);
    }
};
</script>

<template>
    <div class="admin-font flex h-screen bg-[#f8fafc]">
        <AdminSidebar :groups="menuGroups" :current="currentKey" @select="handleSelectModule" />
        <div class="flex min-w-0 flex-1 flex-col">
            <header class="flex items-center justify-between border-b border-[#e2e8f0] bg-white px-6 py-4 shadow-sm">
                <div class="text-[18px] font-semibold text-[#0f172a]">{{ currentModule.title }}</div>
                <button
                    class="admin-icon-btn h-[34px] w-[34px] rounded-[10px] disabled:cursor-not-allowed disabled:opacity-70"
                    type="button"
                    title="刷新"
                    aria-label="刷新"
                    :disabled="stateMap[currentKey].loading"
                    @click="handleRefresh"
                >
                    <svg viewBox="0 0 24 24" class="h-[16px] w-[16px]" fill="none" stroke="currentColor" stroke-width="1.8" aria-hidden="true">
                        <path d="M20 12a8 8 0 1 1-2.34-5.66" />
                        <path d="M20 4v6h-6" />
                    </svg>
                </button>
            </header>

            <div class="flex-1 overflow-auto p-5">
                <div class="mb-4 grid grid-cols-[1fr_auto] items-center gap-3">
                    <div class="flex flex-wrap items-center gap-3 rounded-[12px] border border-[#e2e8f0] bg-white px-4 py-3">
                        <template v-for="field in currentModule.search" :key="field">
                            <template v-if="field === 'keyword'">
                                <input
                                    v-model="stateMap[currentKey].query.keyword"
                                    class="w-[180px] rounded-[10px] border border-[#e2e8f0] px-3 py-2 text-[13px] outline-none focus:border-[#1d4ed8]"
                                    placeholder="关键字"
                                />
                            </template>
                            <template v-else-if="field === 'agentType'">
                                <AdminSelect
                                    v-model="stateMap[currentKey].query.agentType"
                                    class="w-[160px]"
                                    :options="options.agentTypes"
                                    placeholder="选择类型"
                                />
                            </template>
                            <template v-else-if="field === 'clientType'">
                                <AdminSelect
                                    v-model="stateMap[currentKey].query.clientType"
                                    class="w-[160px]"
                                    :options="options.clientTypes"
                                    placeholder="选择类型"
                                />
                            </template>
                            <template v-else-if="field === 'clientRole'">
                                <AdminSelect
                                    v-model="stateMap[currentKey].query.clientRole"
                                    class="w-[160px]"
                                    :options="options.clientRoles"
                                    placeholder="选择角色"
                                />
                            </template>
                            <template v-else-if="field === 'apiId'">
                                <AdminSelect
                                    v-model="stateMap[currentKey].query.apiId"
                                    class="w-[180px]"
                                    :options="options.apiIds"
                                    placeholder="选择 API"
                                />
                            </template>
                            <template v-else-if="field === 'modelId'">
                                <AdminSelect
                                    v-model="stateMap[currentKey].query.modelId"
                                    class="w-[180px]"
                                    :options="options.modelIds"
                                    placeholder="选择 MODEL"
                                />
                            </template>
                            <template v-else-if="field === 'agentId'">
                                <AdminSelect
                                    v-model="stateMap[currentKey].query.agentId"
                                    class="w-[180px]"
                                    :options="options.agents"
                                    placeholder="全部 AGENT"
                                />
                            </template>
                            <template v-else-if="field === 'sortBy'">
                                <AdminSelect
                                    v-model="stateMap[currentKey].query.sortBy"
                                    class="w-[150px]"
                                    :options="sortByOptions"
                                    placeholder="排序字段"
                                />
                            </template>
                            <template v-else-if="field === 'sortOrder'">
                                <AdminSelect
                                    v-model="stateMap[currentKey].query.sortOrder"
                                    class="w-[150px]"
                                    :options="sortOrderOptions"
                                    placeholder="排序方式"
                                />
                            </template>
                            <template v-else-if="field === 'userRole'">
                                <AdminSelect
                                    v-model="stateMap[currentKey].query.userRole"
                                    class="w-[160px]"
                                    :options="options.roles"
                                    placeholder="选择角色"
                                />
                            </template>
                            <template v-else>
                                <input
                                    v-model="stateMap[currentKey].query[field]"
                                    class="w-[160px] rounded-[10px] border border-[#e2e8f0] px-3 py-2 text-[13px] outline-none focus:border-[#1d4ed8]"
                                    :placeholder="field"
                                />
                            </template>
                        </template>
                        <button
                            class="admin-btn-primary rounded-[10px] px-4 py-2 text-[13px] font-semibold"
                            type="button"
                            @click="() => { stateMap[currentKey].query.pageNum = 1; fetchList(); }"
                        >
                            查询
                        </button>
                        <button
                            class="admin-btn-secondary rounded-[10px] px-4 py-2 text-[13px] font-semibold shadow"
                            type="button"
                            @click="openCreate"
                        >
                            新增
                        </button>
                    </div>
                </div>

                <div class="overflow-hidden rounded-[12px] border border-[#e2e8f0] bg-white">
                    <div ref="tableViewportRef" class="overflow-x-auto">
                        <table class="admin-data-table table-fixed border-collapse text-[13px]" :style="tableStyle">
                            <colgroup>
                                <col
                                    v-for="(col, index) in currentModule.listColumns"
                                    :key="col.prop"
                                    :style="{
                                        width: `${resolvedLeftColumnWidths[index]}px`,
                                        minWidth: `${resolvedLeftColumnWidths[index]}px`,
                                        maxWidth: `${resolvedLeftColumnWidths[index]}px`
                                    }"
                                />
                                <col :style="{ width: `${operationWidth}px`, minWidth: `${operationWidth}px`, maxWidth: `${operationWidth}px` }" />
                            </colgroup>
                            <thead class="bg-[#f8fafc] text-[#475569]">
                                <tr>
                                    <th
                                        v-for="col in currentModule.listColumns"
                                        :key="col.prop"
                                        class="px-3 py-2 text-center font-semibold"
                                    >
                                        {{ col.label }}
                                    </th>
                                    <th
                                        class="admin-op-col sticky right-0 z-[3] border-l border-[#e2e8f0] bg-[#f8fafc] px-2 py-2 text-center font-semibold"
                                        :style="{ width: `${operationWidth}px`, minWidth: `${operationWidth}px`, maxWidth: `${operationWidth}px` }"
                                    >
                                        操作
                                    </th>
                                </tr>
                            </thead>
                            <tbody>
                                <tr v-if="stateMap[currentKey].loading">
                                    <td :colspan="currentModule.listColumns.length + 1" class="px-3 py-4 text-center text-[#94a3b8]">加载中...</td>
                                </tr>
                                <tr v-else-if="stateMap[currentKey].list.length === 0">
                                    <td :colspan="currentModule.listColumns.length + 1" class="px-3 py-4 text-center text-[#94a3b8]">暂无数据</td>
                                </tr>
                                <tr
                                    v-for="row in stateMap[currentKey].list"
                                    :key="resolveRowKey(currentKey, row)"
                                    class="group border-t border-[#e2e8f0] hover:bg-[#f8fafc]"
                                >
                                    <td
                                        v-for="col in currentModule.listColumns"
                                        :key="col.prop"
                                        class="px-3 py-2 align-middle text-center text-[#0f172a]"
                                        :class="getCellClass(col)"
                                        :title="getCellTitle(row, col)"
                                    >
                                        <span v-if="col.type === 'chip'" class="admin-chip">{{ getCellText(row, col) }}</span>
                                        <span v-else>{{ getCellText(row, col) }}</span>
                                    </td>
                                    <td
                                        class="admin-op-col sticky right-0 z-[1] border-l border-[#e2e8f0] bg-white px-2 py-2 align-middle group-hover:bg-[#f8fafc]"
                                        :style="{ width: `${operationWidth}px`, minWidth: `${operationWidth}px`, maxWidth: `${operationWidth}px` }"
                                    >
                                        <div class="admin-actions flex flex-nowrap items-center justify-center gap-2 whitespace-nowrap">
                                            <template v-if="hasToggleAction">
                                                <button
                                                    class="relative h-[22px] w-[50px] rounded-full text-[10px] font-semibold uppercase tracking-[0.5px] transition"
                                                    :class="[
                                                        row[currentModule.statusField] === 1 ? 'bg-[#1d4ed8] text-white' : 'bg-[#cbd5e1] text-[#0f172a]',
                                                        isToggleDisabled(row) ? 'cursor-not-allowed opacity-50' : ''
                                                    ]"
                                                    type="button"
                                                    :disabled="isToggleDisabled(row)"
                                                    @click="switchStatus(row, row[currentModule.statusField] === 1 ? 0 : 1)"
                                                >
                                                    <span
                                                        class="absolute left-[3px] top-[3px] h-[16px] w-[16px] rounded-full bg-white transition"
                                                        :class="row[currentModule.statusField] === 1 ? 'translate-x-[28px]' : ''"
                                                    />
                                                    <span
                                                        class="absolute inset-0 flex items-center px-[6px] transition"
                                                        :class="row[currentModule.statusField] === 1 ? 'justify-start' : 'justify-end'"
                                                    >
                                                        <span>{{ row[currentModule.statusField] === 1 ? 'on' : 'off' }}</span>
                                                    </span>
                                                </button>
                                            </template>
                                            <button
                                                class="rounded-[8px] border border-[#22c55e] px-3 py-1 text-[12px] font-semibold text-[#15803d] transition hover:bg-[#dcfce7]"
                                                type="button"
                                                @click="openEdit(row)"
                                            >
                                                编辑
                                            </button>
                                            <button
                                                class="rounded-[8px] border border-[#fecdd3] px-3 py-1 text-[12px] font-semibold text-[#dc2626]"
                                                type="button"
                                                @click="handleDelete(row)"
                                            >
                                                删除
                                            </button>
                                        </div>
                                    </td>
                                </tr>
                            </tbody>
                        </table>
                    </div>
                </div>

                <div class="mt-3 flex items-center justify-between text-[13px] text-[#475569]">
                    <div>共 {{ stateMap[currentKey].total }} 条，第 {{ stateMap[currentKey].query.pageNum }} / {{ pageCount }} 页</div>
                    <div class="flex gap-2">
                        <button
                            class="admin-btn-page rounded-[8px] px-3 py-2 disabled:cursor-not-allowed disabled:opacity-50"
                            type="button"
                            :disabled="stateMap[currentKey].query.pageNum <= 1"
                            @click="changePage(-1)"
                        >
                            上一页
                        </button>
                        <button
                            class="admin-btn-page rounded-[8px] px-3 py-2 disabled:cursor-not-allowed disabled:opacity-50"
                            type="button"
                            :disabled="stateMap[currentKey].query.pageNum >= pageCount"
                            @click="changePage(1)"
                        >
                            下一页
                        </button>
                    </div>
                </div>
            </div>
            <Footer layout="admin" />
        </div>

        <div
            v-if="modalVisible"
            class="fixed inset-0 z-50 grid place-items-center bg-[rgba(15,23,42,0.35)] px-4"
        >
            <div class="flex h-[760px] w-[760px] max-h-[88vh] max-w-[96vw] flex-col overflow-hidden rounded-[14px] bg-white shadow-lg">
                <div class="flex items-center justify-between border-b border-[#e2e8f0] px-6 py-4">
                    <div class="text-[16px] font-semibold text-[#0f172a]">
                        {{ editingId ? '编辑' : '新增' }} - {{ currentModule.title }}
                    </div>
                    <button class="text-[14px] text-[#94a3b8]" type="button" @click="modalVisible = false">✕</button>
                </div>
                <div class="flex-1 overflow-y-auto px-6 py-4">
                    <div class="flex flex-col gap-3">
                        <div v-for="field in currentModule.fields" :key="field.prop" class="flex flex-col gap-1">
                            <label class="text-[13px] font-semibold text-[#0f172a]">
                                {{ field.label }}
                                <span v-if="field.required && !field.requiredOnCreate" class="text-[#dc2626]">*</span>
                            </label>
                            <template v-if="field.type === 'textarea'">
                                <textarea
                                    v-model="currentForm[field.prop]"
                                    :rows="field.rows || 3"
                                    class="rounded-[10px] border border-[#e2e8f0] px-3 py-2 text-[13px] outline-none focus:border-[#1d4ed8]"
                                    :placeholder="field.placeholder"
                                />
                            </template>
                            <template v-else-if="field.type === 'select'">
                                <AdminSelect
                                    v-model="currentForm[field.prop]"
                                    :options="field.options || options[field.optionsKey || ''] || []"
                                    placeholder="请选择"
                                />
                            </template>
                            <template v-else-if="field.type === 'number'">
                                <input
                                    v-model.number="currentForm[field.prop]"
                                    type="number"
                                    class="rounded-[10px] border border-[#e2e8f0] px-3 py-2 text-[13px] outline-none focus:border-[#1d4ed8]"
                                    :placeholder="field.placeholder"
                                />
                            </template>
                            <template v-else-if="field.type === 'password'">
                                <input
                                    v-model="currentForm[field.prop]"
                                    type="password"
                                    class="rounded-[10px] border border-[#e2e8f0] px-3 py-2 text-[13px] outline-none focus:border-[#1d4ed8]"
                                    :placeholder="field.placeholder"
                                />
                            </template>
                            <template v-else-if="field.type === 'switch'">
                                <button
                                    class="relative h-[24px] w-[54px] rounded-full text-[10px] font-semibold uppercase tracking-[0.5px] transition"
                                    :class="[
                                        currentForm[field.prop] === 1 ? 'bg-[#1d4ed8] text-white' : 'bg-[#cbd5e1] text-[#0f172a]',
                                        isFormToggleDisabled(field) ? 'cursor-not-allowed opacity-50' : ''
                                    ]"
                                    type="button"
                                    :disabled="isFormToggleDisabled(field)"
                                    @click="currentForm[field.prop] = currentForm[field.prop] === 1 ? 0 : 1"
                                >
                                    <span
                                        class="absolute left-[3px] top-[3px] h-[18px] w-[18px] rounded-full bg-white transition"
                                        :class="currentForm[field.prop] === 1 ? 'translate-x-[28px]' : ''"
                                    />
                                    <span
                                        class="absolute inset-0 flex items-center px-[6px] transition"
                                        :class="currentForm[field.prop] === 1 ? 'justify-start' : 'justify-end'"
                                    >
                                        <span>{{ currentForm[field.prop] === 1 ? 'on' : 'off' }}</span>
                                    </span>
                                </button>
                            </template>
                            <template v-else>
                                <input
                                    v-model="currentForm[field.prop]"
                                    class="rounded-[10px] border border-[#e2e8f0] px-3 py-2 text-[13px] outline-none focus:border-[#1d4ed8]"
                                    :placeholder="field.placeholder"
                                />
                            </template>
                        </div>
                        <div v-if="modalError" class="rounded-[10px] bg-[#fef2f2] px-3 py-2 text-[12px] text-[#dc2626]">
                            {{ modalError }}
                        </div>
                    </div>
                </div>
                <div class="flex justify-end gap-3 border-t border-[#e2e8f0] px-6 py-4">
                    <button
                        class="admin-btn-page rounded-[10px] px-4 py-2 text-[13px] font-semibold"
                        type="button"
                        @click="modalVisible = false"
                    >
                        取消
                    </button>
                    <button
                        class="admin-btn-primary rounded-[10px] px-4 py-2 text-[13px] font-semibold"
                        type="button"
                        @click="saveForm"
                    >
                        保存
                    </button>
                </div>
            </div>
        </div>
    </div>
</template>

<style scoped>
.admin-font {
    font-size: 15px;
}

.admin-font .text-\[12px\] {
    font-size: 13px !important;
}

.admin-font .text-\[13px\] {
    font-size: 14px !important;
}

.admin-chip {
    display: inline-flex;
    max-width: 100%;
    align-items: center;
    border-radius: 999px;
    border: 1px solid #c7d2fe;
    background: #eef2ff;
    padding: 2px 10px;
    color: #1e3a8a;
    font-size: 12px;
    line-height: 1.4;
    white-space: nowrap;
}

[data-theme='dark'] .admin-chip {
    border-color: rgba(147, 197, 253, 0.6);
    background: rgba(96, 165, 250, 0.18);
    color: #bfdbfe;
}
</style>
