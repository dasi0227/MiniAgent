import http, { streamFetch } from './request';
import { trimStrings } from '../utils/StringUtil';

const AI_BASE_PATH = '/api/v1/ai';
const CHAT_COMPLETE_PATH = `${AI_BASE_PATH}/chat/complete`;
const CHAT_STREAM_PATH = `${AI_BASE_PATH}/chat/stream`;
const AGENT_EXECUTE_PATH = `${AI_BASE_PATH}/work/execute`;
const CLIENT_ARMORY_PATH = `${AI_BASE_PATH}/armory`;
const RAG_UPLOAD_PATH = `${AI_BASE_PATH}/rag/file`;
const RAG_GIT_PATH = `${AI_BASE_PATH}/rag/git`;

const QUERY_BASE_PATH = '/api/v1/query';
const CHAT_CLIENTS_PATH = `${QUERY_BASE_PATH}/chat-client-list`;
const CHAT_MCP_PATH = `${QUERY_BASE_PATH}/chat-mcp-list`;
const AGENT_LIST_PATH = `${QUERY_BASE_PATH}/agent-list`;
const RAG_TAGS_PATH = `${QUERY_BASE_PATH}/chat-rag-list`;

const AUTH_BASE_PATH = '/api/v1/auth';
const LOGIN_PATH = `${AUTH_BASE_PATH}/login`;
const REGISTER_PATH = `${AUTH_BASE_PATH}/register`;
const PROFILE_PATH = `${AUTH_BASE_PATH}/profile`;
const PASSWORD_PATH = `${AUTH_BASE_PATH}/password`;

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

const USER_MCP_BASE_PATH = '/api/v1/user/mcp';
const USER_MCP_LIST_PATH = `${USER_MCP_BASE_PATH}/list`;
const USER_MCP_INSERT_PATH = `${USER_MCP_BASE_PATH}/insert`;
const USER_MCP_UPDATE_PATH = `${USER_MCP_BASE_PATH}/update`;
const USER_MCP_DELETE_PATH = `${USER_MCP_BASE_PATH}/delete`;
const USER_MCP_TOGGLE_PATH = `${USER_MCP_BASE_PATH}/toggle`;
const USER_MCP_TEST_PATH = `${USER_MCP_BASE_PATH}/test`;
const USER_MCP_EXPORT_PATH = `${USER_MCP_BASE_PATH}/export`;

const STUDIO_BASE_PATH = '/api/v1/studio';
const STUDIO_GENERATE_PATH = `${STUDIO_BASE_PATH}/generate`;
const STUDIO_CREATE_PATH = `${STUDIO_BASE_PATH}/create`;
const STUDIO_UPDATE_PATH = `${STUDIO_BASE_PATH}/update`;
const STUDIO_DETAIL_PATH = `${STUDIO_BASE_PATH}/detail`;
const STUDIO_LIST_MINE_PATH = `${STUDIO_BASE_PATH}/list-mine`;

const PLAZA_BASE_PATH = '/api/v1/plaza';
const PLAZA_LIST_PATH = `${PLAZA_BASE_PATH}/list`;
const PLAZA_DETAIL_PATH = `${PLAZA_BASE_PATH}/detail`;
const PLAZA_PUBLISH_PATH = `${PLAZA_BASE_PATH}/publish`;
const PLAZA_LIKE_PATH = `${PLAZA_BASE_PATH}/like`;
const PLAZA_FAVOR_PATH = `${PLAZA_BASE_PATH}/favor`;
const PLAZA_COMMENT_PATH = `${PLAZA_BASE_PATH}/comment`;
const PLAZA_COMMENT_COUNT_PATH = `${PLAZA_BASE_PATH}/comment/count`;

const REPO_BASE_PATH = '/api/v1/repo';
const REPO_LIST_PATH = `${REPO_BASE_PATH}/list`;
const REPO_ADD_PATH = `${REPO_BASE_PATH}/add`;
const REPO_REMOVE_PATH = `${REPO_BASE_PATH}/remove`;
const REPO_FORK_PATH = `${REPO_BASE_PATH}/fork`;

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
    // streamFetch will prepend the configured base (dev/prod) itself.
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

export const queryRagTags = async () => http.get(RAG_TAGS_PATH);

export const queryChatModels = async (params = {}) => http.get(CHAT_CLIENTS_PATH, { params: trimStrings(params) });

export const queryChatMcps = async (params = {}) => http.get(CHAT_MCP_PATH, { params: trimStrings(params) });

export const queryAgentList = async () => http.get(AGENT_LIST_PATH);

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
    // streamFetch will prepend the configured base (dev/prod) itself.
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
    http.post(LOGIN_PATH, trimStrings({ username, password }));
export const register = async ({ username, password }) =>
    http.post(REGISTER_PATH, trimStrings({ username, password }));
export const fetchProfile = async () => http.post(PROFILE_PATH);
export const updatePassword = async ({ id, username, oldPassword, newPassword }) =>
    http.post(PASSWORD_PATH, trimStrings({ id, username, oldPassword, newPassword }));

// -------------------- Session --------------------
export const listSessions = async () => http.get(SESSION_LIST_PATH);

export const listAdminSessions = async () => http.get(SESSION_ADMIN_LIST_PATH);

export const insertSession = async ({ sessionTitle, sessionType }) =>
    http.post(SESSION_INSERT_PATH, null, { params: trimStrings({ sessionTitle, sessionType }) });

export const updateSession = async ({ id, sessionTitle }) =>
    http.post(SESSION_UPDATE_PATH, null, { params: trimStrings({ id, sessionTitle }) });

export const deleteSession = async ({ id, sessionId }) =>
    http.post(SESSION_DELETE_PATH, null, { params: { id, sessionId } });

export const listChatMessages = async ({ sessionId }) =>
    http.get(SESSION_CHAT_MESSAGE_PATH, { params: { sessionId } });

export const listWorkSseMessages = async ({ sessionId }) =>
    http.get(SESSION_WORK_SSE_MESSAGE_PATH, { params: { sessionId } });

export const listWorkAnswerMessages = async ({ sessionId }) =>
    http.get(SESSION_WORK_ANSWER_MESSAGE_PATH, { params: { sessionId } });

// -------------------- Admin --------------------
export const fetchAdminDashboard = async () => http.get(ADMIN_DASHBOARD_PATH);

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

// flow 专用
export const flowClients = async () => http.post(`${ADMIN_BASE_PATH}/flow/client`);
export const flowAgent = async (agentId) => http.post(`${ADMIN_BASE_PATH}/flow/agent`, null, { params: { agentId } });
export const flowInsert = async (payload = {}) => http.post(`${ADMIN_BASE_PATH}/flow/insert`, trimStrings(payload));
export const flowUpdate = async (payload = {}) => http.post(`${ADMIN_BASE_PATH}/flow/update`, trimStrings(payload));
export const flowDelete = async (id) => http.post(`${ADMIN_BASE_PATH}/flow/delete`, null, { params: { id } });

export const adminAgentList = async (payload = {}) => http.post(`${ADMIN_BASE_PATH}/agent/list`, trimStrings(payload));

// config 专用（非分页 Map）
export const configList = async (payload = {}) => http.post(`${ADMIN_BASE_PATH}/config/list`, trimStrings(payload));
export const configInsert = async (payload = {}) => http.post(`${ADMIN_BASE_PATH}/config/insert`, trimStrings(payload));
export const configUpdate = async (payload = {}) => http.post(`${ADMIN_BASE_PATH}/config/update`, trimStrings(payload));
export const configDelete = async (id) => http.post(`${ADMIN_BASE_PATH}/config/delete`, null, { params: { id } });
export const configToggle = async (id, status) =>
    http.post(`${ADMIN_BASE_PATH}/config/toggle`, null, { params: { id, configStatus: status } });

// ---- admin option lists ----
const ADMIN_LIST_BASE = `${ADMIN_BASE_PATH}/list`;
export const listClientType = async () => http.get(`${ADMIN_LIST_BASE}/clientType`);
export const listAgentType = async () => http.get(`${ADMIN_LIST_BASE}/agentType`);
export const listConfigType = async () => http.get(`${ADMIN_LIST_BASE}/configType`);
export const listUserRole = async () => http.get(`${ADMIN_LIST_BASE}/userRole`);
export const listApiId = async () => http.get(`${ADMIN_LIST_BASE}/apiId`);
export const listModelId = async () => http.get(`${ADMIN_LIST_BASE}/modelId`);
export const listClientRole = async () => http.get(`${ADMIN_LIST_BASE}/clientRole`);

// -------------------- User MCP --------------------
export const userMcpList = async (payload = {}) => http.post(USER_MCP_LIST_PATH, trimStrings(payload));
export const userMcpInsert = async (payload = {}) => http.post(USER_MCP_INSERT_PATH, trimStrings(payload));
export const userMcpUpdate = async (payload = {}) => http.post(USER_MCP_UPDATE_PATH, trimStrings(payload));
export const userMcpDelete = async (id) => http.post(USER_MCP_DELETE_PATH, null, { params: { id } });
export const userMcpToggle = async (id, mcpChat) => http.post(USER_MCP_TOGGLE_PATH, null, { params: { id, mcpChat } });
export const userMcpTest = async (payload = {}) => http.post(USER_MCP_TEST_PATH, trimStrings(payload));
export const userMcpExport = async (payload = {}) => http.post(USER_MCP_EXPORT_PATH, trimStrings(payload));

// -------------------- Studio --------------------
export const studioGenerate = async (payload = {}) => http.post(STUDIO_GENERATE_PATH, trimStrings(payload));
export const studioCreate = async (payload = {}) => http.post(STUDIO_CREATE_PATH, trimStrings(payload));
export const studioUpdate = async (payload = {}) => http.post(STUDIO_UPDATE_PATH, trimStrings(payload));
export const studioDetail = async (agentId) => http.get(STUDIO_DETAIL_PATH, { params: { agentId } });
export const studioListMine = async () => http.get(STUDIO_LIST_MINE_PATH);

// -------------------- Plaza --------------------
export const plazaList = async (params = {}) => http.get(PLAZA_LIST_PATH, { params: trimStrings(params) });
export const plazaDetail = async (plazaId) => http.get(PLAZA_DETAIL_PATH, { params: { plazaId } });
export const plazaPublish = async (payload = {}) => http.post(PLAZA_PUBLISH_PATH, trimStrings(payload));
export const plazaLike = async (payload = {}) => http.post(PLAZA_LIKE_PATH, trimStrings(payload));
export const plazaFavor = async (payload = {}) => http.post(PLAZA_FAVOR_PATH, trimStrings(payload));
export const plazaComment = async (payload = {}) => http.post(PLAZA_COMMENT_PATH, trimStrings(payload));
export const plazaCommentCount = async (payload = {}) => http.post(PLAZA_COMMENT_COUNT_PATH, trimStrings(payload));

// -------------------- Repository --------------------
export const repoList = async () => http.get(REPO_LIST_PATH);
export const repoAdd = async (payload = {}) => http.post(REPO_ADD_PATH, trimStrings(payload));
export const repoRemove = async (payload = {}) => http.post(REPO_REMOVE_PATH, trimStrings(payload));
export const repoFork = async (payload = {}) => http.post(REPO_FORK_PATH, trimStrings(payload));
