package com.dasi.types.constant;

public class ExceptionMessage {

    // 头像相关
    public static final String AVATAR_NOT_LEGAL = "头像文件错误，请检查";
    public static final String AVATAR_EXTENSION_NOT_ALLOW = "头像文件格式不允许，只支持 png/jpg";
    public static final String AVATAR_SIZE_NOT_ALLOW = "头像文件大小不允许，只支持 1MB 以下";
    public static final String AVATAR_UPLOAD_FAIL = "上传头像失败，请重新尝试";

    // 鉴权相关
    public static final String AUTH_USER_NOT_EXISTS = "用户不存在";
    public static final String AUTH_USER_UNAVAILABLE = "用户被禁用";
    public static final String AUTH_LOGIN_FAIL = "用户名或密码错误";
    public static final String AUTH_USER_ALREADY_EXISTS = "用户已存在";
    public static final String AUTH_PASSWORD_WRONG = "旧密码不正确";
    public static final String AUTH_PASSWORD_FAIL = "旧密码和新密码不符合规则";

    // Token 相关
    public static final String JWT_USER_INFO_MISSING = "用户信息缺失，无法签发 Token";
    public static final String JWT_TOKEN_MISSING = "Token 缺失";
    public static final String JWT_TOKEN_CLAIM_MISSING = "Token 缺少必要用户信息";

    // 非法情况相关
    public static final String ILLEGAL_USER = "用户行为不合法";
    public static final String ILLEGAL_DATA = "数据不合法";

    // 会话相关
    public static final String SESSION_TYPE_LIMIT_REACHED = "每种类型最多 3 个会话";
    public static final String SESSION_NO_PERMISSION_UPDATE = "无权限修改该会话";
    public static final String SESSION_NO_PERMISSION_ACCESS = "无权限访问该会话";
    public static final String SESSION_USER_INFO_MISSING = "用户信息缺失";
    public static final String SESSION_NOT_FOUND = "会话不存在";
    public static final String SESSION_TYPE_MISMATCH = "会话类型不匹配";
    public static final String SESSION_USER_MESSAGE_LIMIT_REACHED = "当前会话已达到用户消息上限";

    // 管理后台相关
    public static final String ADMIN_ALREADT_EXISTS = "数据已存在，请修改后重新添加";
    public static final String ADMIN_NOT_FOUND = "数据不存在，请确认后重新更改";
    public static final String ADMIN_DEPENDENCY_CONFLICT = "存在依赖，无法执行操作：%s 依赖于 %s";

    // 发布相关
    public static final String PUBLISH_FLOW_EMPTY = "智能体工作流为空，无法发布";
    public static final String PUBLISH_CLIENT_MISSING = "工作流客户端不存在，无法发布";
    public static final String PUBLISH_PROMPT_CONFIG_MISSING = "客户端缺少 Prompt 配置，无法发布";
    public static final String PUBLISH_PROMPT_MISSING = "客户端 Prompt 数据缺失，无法发布";
    public static final String PUBLISH_MCP_MISSING = "客户端 MCP 数据缺失，无法发布";
    public static final String PUBLISH_MODEL_MISSING = "智能体模型配置缺失，无法发布";
    public static final String PUBLISH_API_MISSING = "智能体 API 配置缺失，无法发布";

    // 缓存相关
    public static final String CACHE_KEY_OR_PREFIX_REQUIRED = "cacheKey 和 cachePrefix 必须有且只能有一个有值";

    // 调度相关
    public static final String DISPATCH_ARMORY_STRATEGY_NOT_FOUND = "装配策略不存在";
    public static final String DISPATCH_ARMORY_ENTRY_NOT_FOUND = "装配入口不存在";
    public static final String DISPATCH_ARMORY_FAIL = "装配数据失败";
    public static final String DISPATCH_EXECUTE_STRATEGY_NOT_FOUND = "执行策略不存在";

    // 装配相关
    public static final String ARMORY_EMPTY = "装配内容为空";

    // 执行相关
    public static final String EXECUTE_ANALYZER_RESULT_EMPTY = "Analyzer 结果解析为空";
    public static final String EXECUTE_PERFORMER_RESULT_EMPTY = "Performer 结果解析为空";
    public static final String EXECUTE_SUPERVISOR_RESULT_EMPTY = "Supervisor 结果解析为空";
    public static final String EXECUTE_SUMMARIZER_RESULT_EMPTY = "Summarizer 结果解析为空";
    public static final String EXECUTE_ACTOR_RESULT_EMPTY = "Actor 结果解析为空";
    public static final String EXECUTE_REASONER_RESULT_EMPTY = "Reasoner 结果解析为空";
    public static final String EXECUTE_OBSERVER_RESULT_EMPTY = "Observer 结果解析为空";
    public static final String EXECUTE_EVALUATOR_RESULT_EMPTY = "Evaluator 结果解析为空";
    public static final String EXECUTE_INSPECTOR_RESULT_EMPTY = "Inspector 结果解析为空";
    public static final String EXECUTE_PLANNER_RESULT_EMPTY = "Planner 结果解析为空";
    public static final String EXECUTE_REPLIER_RESULT_EMPTY = "Replier 结果解析为空";
    public static final String EXECUTE_STEP_RETRY_EXCEEDED = "超过最大重试次数 %d，客户端仍然无法执行步骤 %d";

    // 知识库相关
    public static final String RAG_IMPORT_FAIL = "知识库入库失败，请检查 Embedding 服务配置后重试";
    public static final String RAG_IMPORT_FILE_NOT_FOUND = "未找到可入库文件，请检查仓库内容";

    // 枚举相关
    public static final String ENUM_STR_NULL = "字符串为空：type=%s";
    public static final String ENUM_TYPE_UNKNOWN = "未知枚举类型：type=%s，str=%s";

    // MQ 相关
    public static final String MQ_EVENT_UNKNOWN = "未知事件类型：%s";

    // AI 数据相关
    public static final String AI_TASK_PARAM_EMPTY = "taskParam 为空：taskId=%s";
}
