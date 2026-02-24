import http, { streamFetch } from './request';
import { trimStrings } from '../utils/StringUtil';

const AI_BASE_PATH = '/api/v1/ai';
const CHAT_COMPLETE_PATH = `${AI_BASE_PATH}/chat/complete`;
const CHAT_STREAM_PATH = `${AI_BASE_PATH}/chat/stream`;
const AGENT_EXECUTE_PATH = `${AI_BASE_PATH}/work/execute`;
const CLIENT_ARMORY_PATH = `${AI_BASE_PATH}/armory`;
const RAG_UPLOAD_PATH = `${AI_BASE_PATH}/rag/file`;
const RAG_GIT_PATH = `${AI_BASE_PATH}/rag/git`;

const USER_BASE_PATH = '/api/v1/user';
const QUERY_BASE_PATH = `${USER_BASE_PATH}/query`;
const CHAT_CLIENTS_PATH = `${QUERY_BASE_PATH}/chat-client-list`;
const CHAT_MCP_PATH = `${QUERY_BASE_PATH}/chat-mcp-list`;
const AGENT_LIST_PATH = `${QUERY_BASE_PATH}/agent-list`;
const RAG_TAGS_PATH = `${QUERY_BASE_PATH}/chat-rag-list`;

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
    config: 'configStatus',
    user: 'userStatus',
    task: 'taskStatus'
};

const USER_MCP_BASE_PATH = `${USER_BASE_PATH}/mcp`;
const USER_MCP_LIST_PATH = `${USER_MCP_BASE_PATH}/list`;
const USER_MCP_INSERT_PATH = `${USER_MCP_BASE_PATH}/insert`;
const USER_MCP_UPDATE_PATH = `${USER_MCP_BASE_PATH}/update`;
const USER_MCP_DELETE_PATH = `${USER_MCP_BASE_PATH}/delete`;

const USER_API_BASE_PATH = `${USER_BASE_PATH}/api`;
const USER_API_LIST_PATH = `${USER_API_BASE_PATH}/list`;
const USER_API_INSERT_PATH = `${USER_API_BASE_PATH}/insert`;
const USER_API_UPDATE_PATH = `${USER_API_BASE_PATH}/update`;
const USER_API_DELETE_PATH = `${USER_API_BASE_PATH}/delete`;

const PLAZA_BASE_PATH = '/api/v1/workspace/plaza';
const PLAZA_PAGE_PATH = `${PLAZA_BASE_PATH}/page`;
const PLAZA_LIKE_PATH = `${PLAZA_BASE_PATH}/like`;
const PLAZA_DISLIKE_PATH = `${PLAZA_BASE_PATH}/dislike`;
const PLAZA_FAVOR_PATH = `${PLAZA_BASE_PATH}/favor`;
const PLAZA_DISFAVOR_PATH = `${PLAZA_BASE_PATH}/disfavor`;
const PLAZA_COMMENT_PATH = `${PLAZA_BASE_PATH}/comment`;
const PLAZA_DISCOMMENT_PATH = `${PLAZA_BASE_PATH}/discomment`;
const PLAZA_COMMENT_AREA_PATH = `${PLAZA_BASE_PATH}/comment-area`;

const unsupportedApi = (name) => Promise.reject(new Error(`${name} 暂未开放`));

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
    aiAgentId,
    userMessage,
    sessionId,
    maxRound,
    maxRetry,
    onData,
    onError,
    onDone,
    signal
}) => {
    const url = AGENT_EXECUTE_PATH;
    return streamFetch(
        url,
        {
            aiAgentId,
            userMessage,
            sessionId,
            maxRound,
            maxRetry
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
export const updatePassword = async ({ id, username, userName, oldPassword, newPassword, avatar }) => {
    const profilePayload = trimStrings({
        id,
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
export const listSessions = async () => http.post(SESSION_LIST_PATH);

export const listAdminSessions = async () => http.post(SESSION_ADMIN_LIST_PATH);

export const insertSession = async ({ sessionTitle, sessionType }) =>
    http.post(SESSION_INSERT_PATH, null, { params: trimStrings({ sessionTitle, sessionType }) });

export const updateSession = async ({ id, sessionTitle }) =>
    http.post(SESSION_UPDATE_PATH, null, { params: trimStrings({ id, sessionTitle }) });

export const deleteSession = async ({ id, sessionId }) =>
    http.post(SESSION_DELETE_PATH, null, { params: { id, sessionId } });

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
    http.post(buildAdminPath(moduleKey, 'page'), trimStrings(payload));

export const adminInsert = async (moduleKey, payload = {}) =>
    http.post(buildAdminPath(moduleKey, 'insert'), trimStrings(payload));

export const adminUpdate = async (moduleKey, payload = {}) =>
    http.post(buildAdminPath(moduleKey, 'update'), trimStrings(payload));

export const adminDelete = async (moduleKey, id) =>
    http.post(buildAdminPath(moduleKey, 'delete'), null, { params: { id } });

export const adminToggle = async (moduleKey, id, status) => {
    const statusKey = ADMIN_STATUS_PARAM[moduleKey];
    return http.post(buildAdminPath(moduleKey, 'toggle'), null, { params: { id, [statusKey]: status } });
};

export const flowClients = async () => http.post(`${ADMIN_BASE_PATH}/flow/client`);
export const flowAgent = async (agentId) => http.post(`${ADMIN_BASE_PATH}/flow/agent`, null, { params: { agentId } });
export const flowInsert = async (payload = {}) => http.post(`${ADMIN_BASE_PATH}/flow/insert`, trimStrings(payload));
export const flowUpdate = async (payload = {}) => http.post(`${ADMIN_BASE_PATH}/flow/update`, trimStrings(payload));
export const flowDelete = async (id) => http.post(`${ADMIN_BASE_PATH}/flow/delete`, null, { params: { id } });

export const adminAgentList = async (payload = {}) => http.post(`${ADMIN_BASE_PATH}/agent/list`, trimStrings(payload));

export const configList = async (payload = {}) => http.post(`${ADMIN_BASE_PATH}/config/list`, trimStrings(payload));
export const configInsert = async (payload = {}) => http.post(`${ADMIN_BASE_PATH}/config/insert`, trimStrings(payload));
export const configUpdate = async (payload = {}) => http.post(`${ADMIN_BASE_PATH}/config/update`, trimStrings(payload));
export const configDelete = async (id) => http.post(`${ADMIN_BASE_PATH}/config/delete`, null, { params: { id } });
export const configToggle = async (id, status) =>
    http.post(`${ADMIN_BASE_PATH}/config/toggle`, null, { params: { id, configStatus: status } });

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
    http.post(USER_API_LIST_PATH, null, { params: { keyword: (keyword || '').trim() } });
export const userApiInsert = async (payload = {}) => http.post(USER_API_INSERT_PATH, trimStrings(payload));
export const userApiUpdate = async (payload = {}) => http.post(USER_API_UPDATE_PATH, trimStrings(payload));
export const userApiDelete = async (id) => http.post(USER_API_DELETE_PATH, null, { params: { id } });

export const userMcpList = async (keyword = '') =>
    http.post(USER_MCP_LIST_PATH, null, { params: { keyword: (keyword || '').trim() } });
export const userMcpInsert = async (payload = {}) => http.post(USER_MCP_INSERT_PATH, trimStrings(payload));
export const userMcpUpdate = async (payload = {}) => http.post(USER_MCP_UPDATE_PATH, trimStrings(payload));
export const userMcpDelete = async (id) => http.post(USER_MCP_DELETE_PATH, null, { params: { id } });

// 以下接口后端当前未实现，保留函数供调用方降级处理
export const userMcpToggle = async () => unsupportedApi('MCP 启停');
export const userMcpTest = async () => unsupportedApi('MCP 测试');
export const userMcpExport = async () => unsupportedApi('MCP 导出');

// -------------------- Studio（后端未实现） --------------------
export const studioGenerate = async () => unsupportedApi('Studio 生成');
export const studioCreate = async () => unsupportedApi('Studio 创建');
export const studioUpdate = async () => unsupportedApi('Studio 更新');
export const studioDetail = async () => unsupportedApi('Studio 详情');
export const studioListMine = async () => unsupportedApi('Studio 我的列表');

// -------------------- Plaza --------------------
export const plazaList = async (payload = {}) => http.post(PLAZA_PAGE_PATH, trimStrings(payload));
export const plazaDetail = async () => unsupportedApi('Plaza 详情');
export const plazaPublish = async () => unsupportedApi('Plaza 发布');
export const plazaLike = async ({ plazaId }) => http.post(PLAZA_LIKE_PATH, null, { params: { plazaId } });
export const plazaDislike = async ({ plazaId }) => http.post(PLAZA_DISLIKE_PATH, null, { params: { plazaId } });
export const plazaFavor = async ({ plazaId }) => http.post(PLAZA_FAVOR_PATH, null, { params: { plazaId } });
export const plazaDisfavor = async ({ plazaId }) => http.post(PLAZA_DISFAVOR_PATH, null, { params: { plazaId } });
export const plazaComment = async (payload = {}) => http.post(PLAZA_COMMENT_PATH, trimStrings(payload));
export const plazaDiscomment = async ({ plazaId, commentId }) =>
    http.post(PLAZA_DISCOMMENT_PATH, null, { params: { plazaId, commentId } });
export const plazaCommentArea = async (payload = {}) => http.post(PLAZA_COMMENT_AREA_PATH, trimStrings(payload));
export const plazaCommentCount = async () => unsupportedApi('Plaza 评论计数');

// -------------------- Repository（后端未实现） --------------------
export const repoList = async () => unsupportedApi('Repository 列表');
export const repoAdd = async () => unsupportedApi('Repository 添加');
export const repoRemove = async () => unsupportedApi('Repository 移除');
export const repoFork = async () => unsupportedApi('Repository Fork');
