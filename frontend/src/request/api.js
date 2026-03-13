import http, { streamFetch } from './request';
import { trimStrings, prettifyJsonString } from '../utils/StringUtil';

const AI_BASE_PATH = '/api/v1/ai';
const CHAT_COMPLETE_PATH = `${AI_BASE_PATH}/chat/complete`;
const CHAT_STREAM_PATH = `${AI_BASE_PATH}/chat/stream`;
const AGENT_EXECUTE_PATH = `${AI_BASE_PATH}/work/execute`;
const CLIENT_ARMORY_PATH = `${AI_BASE_PATH}/armory`;
const RAG_UPLOAD_PATH = `${AI_BASE_PATH}/rag/file`;
const RAG_GIT_PATH = `${AI_BASE_PATH}/rag/git`;

const USER_BASE_PATH = '/api/v1/user';
const QUERY_BASE_PATH = `${USER_BASE_PATH}/query`;
const CHAT_CLIENTS_PATH = `${QUERY_BASE_PATH}/chat-client`;
const CHAT_MCP_PATH = `${QUERY_BASE_PATH}/mcp`;
const AGENT_LIST_PATH = `${QUERY_BASE_PATH}/work-agent`;
const RAG_TAGS_PATH = `${QUERY_BASE_PATH}/rag`;
const MODEL_LIST_PATH = `${QUERY_BASE_PATH}/model`;
const ROLE_MAP_PATH = `${QUERY_BASE_PATH}/role`;

const AUTH_BASE_PATH = `${USER_BASE_PATH}/auth`;
const LOGIN_PATH = `${AUTH_BASE_PATH}/login`;
const REGISTER_PATH = `${AUTH_BASE_PATH}/register`;
const PROFILE_QUERY_PATH = `${USER_BASE_PATH}/profile/query`;
const PROFILE_EDIT_PATH = `${USER_BASE_PATH}/profile/edit`;

const SESSION_BASE_PATH = '/api/v1/session';
const SESSION_LIST_PATH = `${SESSION_BASE_PATH}/list`;
const SESSION_INSERT_PATH = `${SESSION_BASE_PATH}/insert`;
const SESSION_UPDATE_PATH = `${SESSION_BASE_PATH}/update`;
const SESSION_DELETE_PATH = `${SESSION_BASE_PATH}/delete`;
const SESSION_CHAT_MESSAGE_PATH = `${SESSION_BASE_PATH}/message/chat`;
const SESSION_WORK_SSE_MESSAGE_PATH = `${SESSION_BASE_PATH}/message/work-sse`;
const SESSION_WORK_ANSWER_MESSAGE_PATH = `${SESSION_BASE_PATH}/message/work-answer`;

const ADMIN_BASE_PATH = '/api/v1/admin';
const ADMIN_DASHBOARD_PATH = `${ADMIN_BASE_PATH}/dashboard`;
const SESSION_ADMIN_LIST_PATH = `${ADMIN_BASE_PATH}/session/list`;
const ADMIN_STATUS_PARAM = {
    client: 'clientStatus',
    agent: 'agentStatus',
    user: 'userStatus',
    task: 'taskStatus'
};
const ADMIN_PRIMARY_PARAM = {
    api: 'apiId',
    model: 'modelId',
    mcp: 'mcpId',
    advisor: 'advisorId',
    prompt: 'promptId',
    client: 'clientId',
    agent: 'agentId',
    user: 'userName',
    task: 'taskId',
    template: 'templateId',
    plaza: 'plazaId'
};

const ADMIN_PAGE_PAYLOAD_FIELDS = {
    api: ['keyword', 'pageNum', 'pageSize'],
    model: ['keyword', 'apiId', 'pageNum', 'pageSize'],
    mcp: ['keyword', 'pageNum', 'pageSize'],
    advisor: ['keyword', 'pageNum', 'pageSize'],
    prompt: ['keyword', 'pageNum', 'pageSize'],
    client: ['keyword', 'modelId', 'clientType', 'clientRole', 'pageNum', 'pageSize'],
    agent: ['keyword', 'agentType', 'pageNum', 'pageSize'],
    user: ['keyword', 'userRole', 'pageNum', 'pageSize'],
    task: ['keyword', 'agentId', 'pageNum', 'pageSize'],
    template: ['keyword', 'pageNum', 'pageSize'],
    plaza: ['keyword', 'sortBy', 'sortOrder', 'pageNum', 'pageSize']
};

const ADMIN_MANAGE_PAYLOAD_FIELDS = {
    api: ['apiId', 'apiBaseUrl', 'apiKey', 'apiCompletionsPath', 'apiEmbeddingsPath'],
    model: ['modelId', 'apiId', 'modelName', 'modelType'],
    mcp: ['mcpId', 'mcpName', 'mcpType', 'mcpDesc', 'mcpParam', 'mcpSecret', 'mcpTimeout'],
    advisor: ['advisorId', 'advisorName', 'advisorType', 'advisorParam'],
    prompt: ['promptId', 'promptName', 'systenPrompt'],
    client: ['clientId', 'clientType', 'clientRole', 'modelId', 'modelName', 'clientName', 'clientStatus'],
    agent: ['agentId', 'agentName', 'agentType', 'agentDesc', 'modelId', 'templateId', 'agentStatus'],
    user: ['originUserName', 'userName', 'password', 'userRole', 'userAvatar', 'userStatus'],
    task: ['taskId', 'agentId', 'taskCron', 'taskDesc', 'taskParam', 'taskStatus'],
    template: ['templateId', 'userId', 'agentName', 'agentType', 'agentDesc', 'apiBaseUrl', 'apiCompletionUrl', 'modelName', 'modelType', 'snapshot'],
    plaza: ['plazaId', 'templateId', 'userId', 'agentName', 'agentType', 'plazaTitle', 'plazaDesc', 'likeCount', 'favorCount', 'commentCount']
};

const ADMIN_LIST_PAYLOAD_FIELDS = {
    agent: ['keyword', 'agentType'],
    config: ['keyword', 'configType']
};

const FLOW_MANAGE_PAYLOAD_FIELDS = ['originAgentId', 'originClientId', 'agentId', 'clientId', 'clientRole', 'userPrompt', 'flowSeq'];
const CONFIG_MANAGE_PAYLOAD_FIELDS = ['originClientId', 'originConfigType', 'originConfigValue', 'clientId', 'configType', 'configValue', 'configStatus'];

const USER_MCP_BASE_PATH = `${USER_BASE_PATH}/mcp`;
const USER_MCP_LIST_PATH = `${USER_MCP_BASE_PATH}/list`;
const USER_MCP_INSERT_PATH = `${USER_MCP_BASE_PATH}/insert`;
const USER_MCP_UPDATE_PATH = `${USER_MCP_BASE_PATH}/update`;
const USER_MCP_DELETE_PATH = `${USER_MCP_BASE_PATH}/delete`;

const USER_MODEL_BASE_PATH = `${USER_BASE_PATH}/model`;
const USER_MODEL_LIST_PATH = `${USER_MODEL_BASE_PATH}/list`;
const USER_MODEL_INSERT_PATH = `${USER_MODEL_BASE_PATH}/insert`;
const USER_MODEL_UPDATE_PATH = `${USER_MODEL_BASE_PATH}/update`;
const USER_MODEL_DELETE_PATH = `${USER_MODEL_BASE_PATH}/delete`;

const USER_TASK_BASE_PATH = `${USER_BASE_PATH}/task`;
const USER_TASK_LIST_PATH = `${USER_TASK_BASE_PATH}/list`;
const USER_TASK_INSERT_PATH = `${USER_TASK_BASE_PATH}/insert`;
const USER_TASK_UPDATE_PATH = `${USER_TASK_BASE_PATH}/update`;
const USER_TASK_DELETE_PATH = `${USER_TASK_BASE_PATH}/delete`;
const USER_TASK_TOGGLE_PATH = `${USER_TASK_BASE_PATH}/toggle`;

const WORKSPACE_BASE_PATH = '/api/v1/workspace';
const PLAZA_BASE_PATH = `${WORKSPACE_BASE_PATH}/plaza`;
const PLAZA_PAGE_PATH = `${PLAZA_BASE_PATH}/page`;
const PLAZA_LIKE_PATH = `${PLAZA_BASE_PATH}/like`;
const PLAZA_DISLIKE_PATH = `${PLAZA_BASE_PATH}/dislike`;
const PLAZA_FAVOR_PATH = `${PLAZA_BASE_PATH}/favor`;
const PLAZA_DISFAVOR_PATH = `${PLAZA_BASE_PATH}/disfavor`;
const PLAZA_COMMENT_PATH = `${PLAZA_BASE_PATH}/comment`;
const PLAZA_DISCOMMENT_PATH = `${PLAZA_BASE_PATH}/discomment`;
const PLAZA_COMMENT_AREA_PATH = `${PLAZA_BASE_PATH}/comment-area`;
const PLAZA_DELETE_PATH = `${PLAZA_BASE_PATH}/delete`;
const REPO_LIST_PATH = `${WORKSPACE_BASE_PATH}/repo/map`;
const WORKSPACE_AGENT_PUBLISH_PATH = `${WORKSPACE_BASE_PATH}/agent/publish`;
const WORKSPACE_AGENT_TEMPLATE_PATH = `${WORKSPACE_BASE_PATH}/agent/template`;
const WORKSPACE_AGENT_DETAIL_PATH = `${WORKSPACE_BASE_PATH}/agent/detail`;
const WORKSPACE_AGENT_DELETE_PATH = `${WORKSPACE_BASE_PATH}/agent/delete`;
const WORKSPACE_AGENT_FORK_PATH = `${WORKSPACE_BASE_PATH}/agent/fork`;
const WORKSPACE_AGENT_CREATE_PATH = `${WORKSPACE_BASE_PATH}/agent/create`;
const WORKSPACE_AGENT_UPDATE_BASE_PATH = `${WORKSPACE_BASE_PATH}/agent/update/base`;
const WORKSPACE_AGENT_UPDATE_MODEL_PATH = `${WORKSPACE_BASE_PATH}/agent/update/model`;
const WORKSPACE_AGENT_UPDATE_MCP_PATH = `${WORKSPACE_BASE_PATH}/agent/update/mcp`;
const WORKSPACE_AGENT_UPDATE_USERPROMPT_PATH = `${WORKSPACE_BASE_PATH}/agent/update/userprompt`;
const WORKSPACE_AGENT_UPDATE_SYSTEMPROMPT_PATH = `${WORKSPACE_BASE_PATH}/agent/update/systemprompt`;

const unsupportedApi = (name) => Promise.reject(new Error(`${name} 暂未开放`));
const isBlank = (value) => typeof value === 'string' && value.trim() === '';
const isUnset = (value) => value === null || value === undefined || isBlank(value);

const isResultEnvelope = (resp) =>
    resp && typeof resp === 'object' && Object.prototype.hasOwnProperty.call(resp, 'code');

const pickPayloadFields = (source = {}, fields = []) =>
    fields.reduce((acc, field) => {
        if (Object.prototype.hasOwnProperty.call(source, field)) {
            acc[field] = source[field];
        }
        return acc;
    }, {});

const normalizePageSize = (value) => {
    const num = Number(value);
    if (!Number.isFinite(num) || num < 1) {
        return 10;
    }
    return Math.min(Math.floor(num), 10);
};

const normalizePositiveInt = (value, fallback = 1) => {
    const num = Number(value);
    if (!Number.isFinite(num) || num < 1) {
        return fallback;
    }
    return Math.floor(num);
};

const normalizeNumber = (value, fallback) => {
    if (value === '' || value === null || value === undefined) {
        return fallback;
    }
    const num = Number(value);
    return Number.isFinite(num) ? num : fallback;
};

const mapResultData = (resp, mapper) => {
    if (!resp || typeof mapper !== 'function') {
        return resp;
    }
    if (isResultEnvelope(resp)) {
        return {
            ...resp,
            data: mapper(resp.data)
        };
    }
    return mapper(resp);
};

const resolveRequestConfig = (options = {}) => {
    if (!options || typeof options !== 'object') return {};
    const requestConfig = options.requestConfig && typeof options.requestConfig === 'object' ? { ...options.requestConfig } : {};
    if (options.toast === false) {
        requestConfig.toast = false;
    }
    return requestConfig;
};

const buildParamsConfig = (params = {}, options = {}) => {
    const requestConfig = resolveRequestConfig(options);
    return {
        ...requestConfig,
        params: {
            ...(requestConfig.params || {}),
            ...params
        }
    };
};

const normalizeAdminPayload = (moduleKey, payload = {}, scene = 'manage') => {
    const source = trimStrings(payload);
    if (!source || typeof source !== 'object') {
        return source;
    }

    const working = { ...source };

    if (moduleKey === 'mcp' && isUnset(working.mcpParam) && !isUnset(working.mcpConfig)) {
        working.mcpParam = working.mcpConfig;
    }
    if (moduleKey === 'prompt' && isUnset(working.systenPrompt) && !isUnset(working.promptContent)) {
        working.systenPrompt = working.promptContent;
    }
    if (moduleKey === 'prompt' && isUnset(working.systenPrompt) && !isUnset(working.systemPrompt)) {
        working.systenPrompt = working.systemPrompt;
    }

    const fieldMap = scene === 'page' ? ADMIN_PAGE_PAYLOAD_FIELDS : ADMIN_MANAGE_PAYLOAD_FIELDS;
    const fields = fieldMap[moduleKey] || [];
    const normalized = fields.length > 0 ? pickPayloadFields(working, fields) : { ...working };

    if (scene === 'page') {
        normalized.pageNum = normalizePositiveInt(normalized.pageNum, 1);
        normalized.pageSize = normalizePageSize(normalized.pageSize);
        return normalized;
    }

    if (moduleKey === 'api') {
        if (isUnset(normalized.apiCompletionsPath)) {
            normalized.apiCompletionsPath = '/v1/chat/completions';
        }
        if (isUnset(normalized.apiEmbeddingsPath)) {
            normalized.apiEmbeddingsPath = '/v1/embeddings';
        }
    }

    if (moduleKey === 'mcp') {
        if (isUnset(normalized.mcpParam)) {
            normalized.mcpParam = '{}';
        }
        if (isUnset(normalized.mcpDesc)) {
            normalized.mcpDesc = '暂无描述';
        }
        normalized.mcpTimeout = normalizeNumber(normalized.mcpTimeout, 180);
    }

    if (moduleKey === 'client') {
        if (isUnset(normalized.modelName) && !isUnset(normalized.modelId)) {
            normalized.modelName = normalized.modelId;
        }
        normalized.clientStatus = normalizeNumber(normalized.clientStatus, 1);
    }

    if (moduleKey === 'agent') {
        if (isUnset(normalized.agentDesc)) {
            normalized.agentDesc = '暂无描述';
        }
        normalized.agentStatus = normalizeNumber(normalized.agentStatus, 1);
    }

    if (moduleKey === 'task') {
        if (isUnset(normalized.taskDesc)) {
            normalized.taskDesc = '暂无描述';
        }
        if (isUnset(normalized.taskParam)) {
            normalized.taskParam = '{}';
        }
        normalized.taskStatus = normalizeNumber(normalized.taskStatus, 1);
    }

    if (moduleKey === 'template') {
        normalized.userId = normalizeNumber(normalized.userId, normalized.userId);
        if (isUnset(normalized.snapshot)) {
            normalized.snapshot = '{}';
        }
    }

    if (moduleKey === 'plaza') {
        normalized.userId = normalizeNumber(normalized.userId, normalized.userId);
        normalized.likeCount = normalizeNumber(normalized.likeCount, 0);
        normalized.favorCount = normalizeNumber(normalized.favorCount, 0);
        normalized.commentCount = normalizeNumber(normalized.commentCount, 0);
    }

    if (moduleKey === 'user') {
        if (isUnset(normalized.userAvatar)) {
            normalized.userAvatar = 'avatar/default.png';
        }
        normalized.userStatus = normalizeNumber(normalized.userStatus, 1);
    }

    return normalized;
};

const normalizeAdminListPayload = (moduleKey, payload = {}) => {
    const source = trimStrings(payload);
    if (!source || typeof source !== 'object') {
        return source;
    }
    const fields = ADMIN_LIST_PAYLOAD_FIELDS[moduleKey] || [];
    return fields.length > 0 ? pickPayloadFields(source, fields) : source;
};

const normalizeConfigPayload = (payload = {}) => {
    const source = trimStrings(payload);
    if (!source || typeof source !== 'object') {
        return source;
    }
    const normalized = pickPayloadFields(source, CONFIG_MANAGE_PAYLOAD_FIELDS);
    normalized.configStatus = normalizeNumber(normalized.configStatus, 1);
    return normalized;
};

const normalizeAdminItem = (moduleKey, item = {}) => {
    if (!item || typeof item !== 'object') {
        return item;
    }
    if (moduleKey === 'api') {
        return {
            ...item,
            apiCompletionsPath: item.apiCompletionsPath || '/v1/chat/completions',
            apiEmbeddingsPath: item.apiEmbeddingsPath || '/v1/embeddings'
        };
    }
    if (moduleKey === 'mcp') {
        return {
            ...item,
            mcpDesc: item.mcpDesc || '暂无描述',
            mcpTimeout: item.mcpTimeout ?? 180,
            mcpParam: prettifyJsonString(item.mcpParam || item.mcpConfig || ''),
            mcpSecret: prettifyJsonString(item.mcpSecret || '')
        };
    }
    if (moduleKey === 'prompt') {
        const systemPrompt = item.systemPrompt || item.systenPrompt || '';
        return {
            ...item,
            systemPrompt,
            systenPrompt: item.systenPrompt || systemPrompt
        };
    }
    if (moduleKey === 'client') {
        return {
            ...item,
            modelName: item.modelName || item.modelId || ''
        };
    }
    if (moduleKey === 'agent') {
        return {
            ...item,
            agentDesc: item.agentDesc || '暂无描述',
            agentStatus: item.agentStatus ?? 1
        };
    }
    if (moduleKey === 'task') {
        return {
            ...item,
            taskDesc: item.taskDesc || '暂无描述',
            taskStatus: item.taskStatus ?? 1,
            taskParam: prettifyJsonString(item.taskParam || '{}')
        };
    }
    if (moduleKey === 'user') {
        return {
            ...item,
            userAvatar: item.userAvatar || 'avatar/default.png',
            userStatus: item.userStatus ?? 1
        };
    }
    if (moduleKey === 'template') {
        return {
            ...item,
            snapshot: prettifyJsonString(item.snapshot || '{}')
        };
    }
    if (moduleKey === 'plaza') {
        return {
            ...item,
            likeCount: item.likeCount ?? 0,
            favorCount: item.favorCount ?? 0,
            commentCount: item.commentCount ?? 0
        };
    }
    if (moduleKey === 'advisor') {
        return {
            ...item,
            advisorParam: prettifyJsonString(item.advisorParam || '')
        };
    }
    return item;
};

const normalizeAdminPageResponse = (moduleKey, resp) =>
    mapResultData(resp, (data) => {
        if (!data || typeof data !== 'object' || !Array.isArray(data.list)) {
            return data;
        }
        return {
            ...data,
            list: data.list.map((item) => normalizeAdminItem(moduleKey, item))
        };
    });

const normalizeFlowPayload = (payload = {}) => {
    const source = trimStrings(payload);
    if (!source || typeof source !== 'object') {
        return source;
    }
    if (!source.userPrompt && source.flowPrompt) {
        source.userPrompt = source.flowPrompt;
    }
    const normalized = pickPayloadFields(source, FLOW_MANAGE_PAYLOAD_FIELDS);
    normalized.flowSeq = normalizePositiveInt(normalized.flowSeq, normalized.flowSeq || 1);
    return normalized;
};

const normalizeFlowResponse = (resp) =>
    mapResultData(resp, (data) => {
        const list = Array.isArray(data) ? data : [];
        return list.map((item) => ({
            ...(item || {}),
            flowPrompt: item?.flowPrompt || item?.userPrompt || ''
        }));
    });

const normalizeUserMcpPayload = (payload = {}) => {
    const normalized = trimStrings(payload);
    if (!normalized || typeof normalized !== 'object') {
        return normalized;
    }
    if (!normalized.mcpParam && normalized.mcpConfig) {
        normalized.mcpParam = normalized.mcpConfig;
    }
    if (isUnset(normalized.mcpParam)) {
        normalized.mcpParam = null;
    }
    if (isUnset(normalized.mcpSecret)) {
        normalized.mcpSecret = null;
    }
    delete normalized.mcpConfig;
    return normalized;
};

const normalizeUserMcpResponse = (resp) =>
    mapResultData(resp, (data) => {
        const list = Array.isArray(data) ? data : [];
        return list.map((item) => {
            const mcpParam = item?.mcpParam || item?.mcpConfig || '';
            return {
                ...(item || {}),
                mcpParam,
                mcpConfig: mcpParam
            };
        });
    });

const normalizeUserApiPayload = (payload = {}) => {
    const normalized = trimStrings(payload);
    if (!normalized || typeof normalized !== 'object') {
        return normalized;
    }
    if (!normalized.modelType) {
        normalized.modelType = 'chat';
    }
    if (isUnset(normalized.apiCompletionPath)) {
        normalized.apiCompletionPath = '/v1/chat/completions';
    }
    return normalized;
};

const normalizeUserTaskPayload = (payload = {}) => {
    const normalized = trimStrings(payload);
    if (!normalized || typeof normalized !== 'object') {
        return normalized;
    }
    if (isUnset(normalized.taskDesc)) {
        normalized.taskDesc = '暂无描述';
    }
    if (isUnset(normalized.taskParam)) {
        normalized.taskParam = '{}';
    }
    if (normalized.taskStatus === '' || normalized.taskStatus === null || normalized.taskStatus === undefined) {
        normalized.taskStatus = 1;
    }
    return normalized;
};

const normalizePlazaQueryPayload = (payload = {}) => {
    const normalized = trimStrings(payload);
    if (!normalized || typeof normalized !== 'object') {
        return normalized;
    }
    if (!normalized.sortBy && normalized.sortField) {
        normalized.sortBy = normalized.sortField;
    }
    if (!normalized.pageNum) {
        normalized.pageNum = 1;
    }
    if (!normalized.pageSize) {
        normalized.pageSize = 10;
    }
    delete normalized.sortField;
    return normalized;
};

export const fetchComplete = async ({
    clientId,
    userMessage,
    ragTag,
    mcpIdList,
    temperature,
    presencePenalty,
    maxCompletionTokens,
    sessionId,
    signal
}) => {
    return http.post(
        CHAT_COMPLETE_PATH,
        {
            clientId,
            userMessage,
            temperature,
            presencePenalty,
            maxCompletionTokens,
            mcpIdList,
            sessionId,
            ragTag
        },
        { signal }
    );
};

export const fetchStream = async ({
    clientId,
    userMessage,
    ragTag,
    mcpIdList,
    temperature,
    presencePenalty,
    maxCompletionTokens,
    sessionId,
    onData,
    onError,
    onDone,
    signal
}) => {
    const url = CHAT_STREAM_PATH;
    return streamFetch(
        url,
        {
            clientId,
            userMessage,
            temperature,
            presencePenalty,
            maxCompletionTokens,
            mcpIdList,
            sessionId,
            ragTag
        },
        onData,
        onError,
        onDone,
        signal
    );
};

export const pickContentFromResult = (result) => {
    if (!result) {
        return '';
    }
    if (typeof result === 'string') {
        return result;
    }
    return (
        result?.output?.content ||
        result?.output?.text ||
        result?.data ||
        result?.result?.output?.content ||
        result?.result?.output?.text ||
        result?.result ||
        ''
    );
};

export const queryRagTags = async () => http.post(RAG_TAGS_PATH);

export const queryChatModels = async () => http.post(CHAT_CLIENTS_PATH);

export const queryChatMcps = async () => http.post(CHAT_MCP_PATH);

export const queryAgentList = async () => http.post(AGENT_LIST_PATH);

export const queryModelList = async () => http.post(MODEL_LIST_PATH);
export const queryRoleMap = async () => http.post(ROLE_MAP_PATH);

export const dispatchArmory = async ({ armoryType, armoryId }) => {
    return http.post(
        CLIENT_ARMORY_PATH,
        {
            armoryType,
            armoryId
        }
    );
};

export const executeAgentStream = async ({
    agentId,
    aiAgentId,
    agentDesc,
    userMessage,
    sessionId,
    maxRound,
    maxRetry,
    maxPace,
    onData,
    onError,
    onDone,
    signal
}) => {
    const url = AGENT_EXECUTE_PATH;
    const resolvedAgentId = (agentId || aiAgentId || '').trim();
    const resolvedAgentDesc = (agentDesc || '').trim() || '暂无';
    const normalizedMaxPace = Number(maxPace);
    const clampedMaxPace = Number.isFinite(normalizedMaxPace)
        ? Math.min(5, Math.max(3, Math.floor(normalizedMaxPace)))
        : 3;
    return streamFetch(
        url,
        {
            agentId: resolvedAgentId,
            agentDesc: resolvedAgentDesc,
            userMessage,
            sessionId,
            maxRound,
            maxRetry,
            maxPace: clampedMaxPace
        },
        onData,
        onError,
        onDone,
        signal
    );
};

export const uploadRagFile = async ({ ragTag, file }) => {
    const formData = new FormData();
    formData.append('ragTag', ragTag);
    formData.append('fileList', file);
    return http.post(RAG_UPLOAD_PATH, formData);
};

export const uploadRagGit = async ({ repoUrl, username, password }) => {
    return http.post(RAG_GIT_PATH, {
        repoUrl,
        username,
        password
    });
};

// Auth
export const login = async ({ username, password }) =>
    http.post(LOGIN_PATH, trimStrings({ userName: username, password }));
export const register = async ({ username, password }) =>
    http.post(REGISTER_PATH, trimStrings({ userName: username, password }));
export const fetchProfile = async () => http.post(PROFILE_QUERY_PATH);
export const updatePassword = async ({ username, userName, oldPassword, newPassword, avatar }) => {
    const profilePayload = trimStrings({
        userName: userName || username,
        oldPassword,
        newPassword
    });
    const formData = new FormData();
    formData.append('profile', new Blob([JSON.stringify(profilePayload)], { type: 'application/json' }));
    if (avatar instanceof File) {
        formData.append('avatar', avatar);
    }
    return http.post(PROFILE_EDIT_PATH, formData);
};

// -------------------- Session --------------------
export const listSessions = async (options = {}) => http.post(SESSION_LIST_PATH, null, resolveRequestConfig(options));

export const listAdminSessions = async () => http.post(SESSION_ADMIN_LIST_PATH);

export const insertSession = async ({ sessionTitle, sessionType }, options = {}) =>
    http.post(SESSION_INSERT_PATH, null, buildParamsConfig(trimStrings({ sessionTitle, sessionType }), options));

export const updateSession = async ({ sessionId, sessionTitle }, options = {}) =>
    http.post(SESSION_UPDATE_PATH, null, buildParamsConfig(trimStrings({ sessionId, sessionTitle }), options));

export const deleteSession = async ({ sessionId }) =>
    http.post(SESSION_DELETE_PATH, null, { params: { sessionId } });

export const listChatMessages = async ({ sessionId }) =>
    http.post(SESSION_CHAT_MESSAGE_PATH, null, { params: { sessionId } });

export const listWorkSseMessages = async ({ sessionId }) =>
    http.post(SESSION_WORK_SSE_MESSAGE_PATH, null, { params: { sessionId } });

export const listWorkAnswerMessages = async ({ sessionId }) =>
    http.post(SESSION_WORK_ANSWER_MESSAGE_PATH, null, { params: { sessionId } });

// -------------------- Admin --------------------
export const fetchAdminDashboard = async () => http.post(ADMIN_DASHBOARD_PATH);

const buildAdminPath = (moduleKey, action) => `${ADMIN_BASE_PATH}/${moduleKey}/${action}`;

export const adminPage = async (moduleKey, payload = {}) =>
    normalizeAdminPageResponse(moduleKey, await http.post(buildAdminPath(moduleKey, 'page'), normalizeAdminPayload(moduleKey, payload, 'page')));

export const adminInsert = async (moduleKey, payload = {}) =>
    http.post(buildAdminPath(moduleKey, 'insert'), normalizeAdminPayload(moduleKey, payload, 'manage'));

export const adminUpdate = async (moduleKey, payload = {}) =>
    http.post(buildAdminPath(moduleKey, 'update'), normalizeAdminPayload(moduleKey, payload, 'manage'));

export const adminDelete = async (moduleKey, value) => {
    const paramKey = ADMIN_PRIMARY_PARAM[moduleKey];
    if (!paramKey) {
        return unsupportedApi(`${moduleKey} 删除`);
    }
    return http.post(buildAdminPath(moduleKey, 'delete'), null, { params: { [paramKey]: value } });
};

export const adminToggle = async (moduleKey, value, status) => {
    const statusKey = ADMIN_STATUS_PARAM[moduleKey];
    const paramKey = ADMIN_PRIMARY_PARAM[moduleKey];
    return http.post(buildAdminPath(moduleKey, 'toggle'), null, { params: { [paramKey]: value, [statusKey]: status } });
};

export const flowClients = async () => http.post(`${ADMIN_BASE_PATH}/flow/client`);
export const flowAgent = async (agentId) =>
    normalizeFlowResponse(await http.post(`${ADMIN_BASE_PATH}/flow/agent`, null, { params: { agentId } }));
export const flowInsert = async (payload = {}) => http.post(`${ADMIN_BASE_PATH}/flow/insert`, normalizeFlowPayload(payload));
export const flowUpdate = async (payload = {}) => http.post(`${ADMIN_BASE_PATH}/flow/update`, normalizeFlowPayload(payload));
export const flowDelete = async ({ agentId, clientId }) =>
    http.post(`${ADMIN_BASE_PATH}/flow/delete`, null, { params: { agentId, clientId } });

export const adminAgentList = async (payload = {}) => http.post(`${ADMIN_BASE_PATH}/agent/list`, normalizeAdminListPayload('agent', payload));

export const configList = async (payload = {}) => http.post(`${ADMIN_BASE_PATH}/config/list`, normalizeAdminListPayload('config', payload));
export const configInsert = async (payload = {}) => http.post(`${ADMIN_BASE_PATH}/config/insert`, normalizeConfigPayload(payload));
export const configUpdate = async (payload = {}) => http.post(`${ADMIN_BASE_PATH}/config/update`, normalizeConfigPayload(payload));
export const configDelete = async ({ clientId, configType, configValue }) =>
    http.post(`${ADMIN_BASE_PATH}/config/delete`, null, { params: { clientId, configType, configValue } });
export const configToggle = async ({ clientId, configType, configValue }, status) =>
    http.post(`${ADMIN_BASE_PATH}/config/toggle`, null, { params: { clientId, configType, configValue, configStatus: status } });

const ADMIN_LIST_BASE = `${ADMIN_BASE_PATH}/list`;
export const listClientType = async () => http.post(`${ADMIN_LIST_BASE}/clientType`);
export const listAgentType = async () => http.post(`${ADMIN_LIST_BASE}/agentType`);
export const listConfigType = async () => http.post(`${ADMIN_LIST_BASE}/configType`);
export const listUserRole = async () => http.post(`${ADMIN_LIST_BASE}/userRole`);
export const listApiId = async () => http.post(`${ADMIN_LIST_BASE}/apiId`);
export const listModelId = async () => http.post(`${ADMIN_LIST_BASE}/modelId`);
export const listClientRole = async () => http.post(`${ADMIN_LIST_BASE}/clientRole`);

// -------------------- User API/MCP --------------------
export const userApiList = async (keyword = '') =>
    http.post(USER_MODEL_LIST_PATH, null, { params: { keyword: (keyword || '').trim() } });
export const userApiInsert = async (payload = {}) => http.post(USER_MODEL_INSERT_PATH, normalizeUserApiPayload(payload));
export const userApiUpdate = async (payload = {}) => http.post(USER_MODEL_UPDATE_PATH, normalizeUserApiPayload(payload));
export const userApiDelete = async (apiId) => http.post(USER_MODEL_DELETE_PATH, null, { params: { apiId } });
export const userModelList = userApiList;
export const userModelInsert = userApiInsert;
export const userModelUpdate = userApiUpdate;
export const userModelDelete = userApiDelete;

export const userMcpList = async (keyword = '') =>
    normalizeUserMcpResponse(await http.post(USER_MCP_LIST_PATH, null, { params: { keyword: (keyword || '').trim() } }));
export const userMcpInsert = async (payload = {}) => http.post(USER_MCP_INSERT_PATH, normalizeUserMcpPayload(payload));
export const userMcpUpdate = async (payload = {}) => http.post(USER_MCP_UPDATE_PATH, normalizeUserMcpPayload(payload));
export const userMcpDelete = async (mcpId) => http.post(USER_MCP_DELETE_PATH, null, { params: { mcpId } });

export const userTaskList = async () => http.post(USER_TASK_LIST_PATH);
export const userTaskInsert = async (payload = {}) => http.post(USER_TASK_INSERT_PATH, normalizeUserTaskPayload(payload));
export const userTaskUpdate = async (payload = {}) => http.post(USER_TASK_UPDATE_PATH, normalizeUserTaskPayload(payload));
export const userTaskDelete = async (taskId) => http.post(USER_TASK_DELETE_PATH, null, { params: { taskId } });
export const userTaskToggle = async (taskId, taskStatus) =>
    http.post(USER_TASK_TOGGLE_PATH, null, { params: { taskId, taskStatus } });

// 以下接口后端当前未实现，保留函数供调用方降级处理
// -------------------- Studio / Workspace Agent --------------------
export const studioGenerate = async () => unsupportedApi('Studio 生成');
export const workspaceAgentCreate = async (payload = {}) =>
    http.post(WORKSPACE_AGENT_CREATE_PATH, trimStrings(payload));
export const workspaceAgentBaseUpdate = async (payload = {}) =>
    http.post(WORKSPACE_AGENT_UPDATE_BASE_PATH, trimStrings(payload));
export const workspaceAgentModelUpdate = async (payload = {}) =>
    http.post(WORKSPACE_AGENT_UPDATE_MODEL_PATH, trimStrings(payload));
export const workspaceAgentMcpUpdate = async (payload = {}) =>
    http.post(WORKSPACE_AGENT_UPDATE_MCP_PATH, trimStrings(payload));
export const workspaceAgentUserPromptUpdate = async (payload = {}) =>
    http.post(WORKSPACE_AGENT_UPDATE_USERPROMPT_PATH, trimStrings(payload));
export const workspaceAgentSystemPromptUpdate = async (payload = {}) =>
    http.post(WORKSPACE_AGENT_UPDATE_SYSTEMPROMPT_PATH, trimStrings(payload));
export const workspaceAgentDetail = async ({ agentId }) =>
    http.post(WORKSPACE_AGENT_DETAIL_PATH, null, { params: { agentId } });

// 兼容历史命名，实际复用 workspace/agent 接口
export const studioCreate = async (payload = {}) => workspaceAgentCreate(payload);
export const studioUpdate = async (payload = {}) => {
    const normalized = trimStrings(payload || {});
    if (normalized && typeof normalized === 'object' && normalized.clientRole) {
        if (Object.prototype.hasOwnProperty.call(normalized, 'systemPrompt')) {
            return workspaceAgentSystemPromptUpdate(normalized);
        }
        if (Object.prototype.hasOwnProperty.call(normalized, 'userPrompt')) {
            return workspaceAgentUserPromptUpdate(normalized);
        }
    }
    return workspaceAgentBaseUpdate(normalized);
};
export const studioDetail = async (payload = {}) =>
    http.post(WORKSPACE_AGENT_TEMPLATE_PATH, null, { params: { templateId: payload?.templateId || '' } });
export const studioListMine = async () =>
    mapResultData(await http.post(REPO_LIST_PATH), (data) => (Array.isArray(data?.self) ? data.self : []));

// -------------------- Plaza --------------------
export const plazaList = async (payload = {}) => http.post(PLAZA_PAGE_PATH, normalizePlazaQueryPayload(payload));
export const plazaDetail = async ({ templateId }) =>
    http.post(WORKSPACE_AGENT_TEMPLATE_PATH, null, { params: { templateId } });
export const plazaPublish = async (payload = {}) =>
    http.post(WORKSPACE_AGENT_PUBLISH_PATH, trimStrings(payload));
export const plazaLike = async ({ plazaId }) => http.post(PLAZA_LIKE_PATH, null, { params: { plazaId } });
export const plazaDislike = async ({ plazaId }) => http.post(PLAZA_DISLIKE_PATH, null, { params: { plazaId } });
export const plazaFavor = async ({ plazaId }) => http.post(PLAZA_FAVOR_PATH, null, { params: { plazaId } });
export const plazaDisfavor = async ({ plazaId }) => http.post(PLAZA_DISFAVOR_PATH, null, { params: { plazaId } });
export const plazaComment = async (payload = {}) => http.post(PLAZA_COMMENT_PATH, trimStrings(payload));
export const plazaDiscomment = async ({ plazaId, commentId }) =>
    http.post(PLAZA_DISCOMMENT_PATH, null, { params: { plazaId, commentId } });
export const plazaCommentArea = async (payload = {}) => http.post(PLAZA_COMMENT_AREA_PATH, trimStrings(payload));
export const plazaDelete = async ({ plazaId }) => http.post(PLAZA_DELETE_PATH, null, { params: { plazaId } });

// -------------------- Repository --------------------
export const repoList = async () => http.post(REPO_LIST_PATH);
export const repoFork = async ({ templateId }) =>
    http.post(WORKSPACE_AGENT_FORK_PATH, null, { params: { templateId } });
export const repoDeleteMineAgent = async ({ agentId }) =>
    http.post(WORKSPACE_AGENT_DELETE_PATH, null, { params: { agentId } });
