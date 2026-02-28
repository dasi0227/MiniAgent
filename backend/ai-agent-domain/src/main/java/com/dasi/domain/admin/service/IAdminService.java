package com.dasi.domain.admin.service;

import com.dasi.domain.admin.model.dto.*;
import com.dasi.domain.admin.model.vo.*;
import com.dasi.domain.session.model.vo.SessionVO;
import com.dasi.domain.admin.model.vo.DashboardVO;
import com.dasi.types.result.PageResult;

import java.util.List;
import java.util.Map;

public interface IAdminService {

    // Api
    PageResult<ApiVO> apiPage(ApiPageDTO dto);
    void apiInsert(ApiManageDTO dto);
    void apiUpdate(ApiManageDTO dto);
    void apiDelete(String apiId);

    // Model
    PageResult<ModelVO> modelPage(ModelPageDTO dto);
    void modelInsert(ModelManageDTO dto);
    void modelUpdate(ModelManageDTO dto);
    void modelDelete(String modelId);

    // Mcp
    PageResult<McpVO> mcpPage(McpPageDTO dto);
    void mcpInsert(McpManageDTO dto);
    void mcpUpdate(McpManageDTO dto);
    void mcpDelete(String mcpId);

    // Advisor
    PageResult<AdvisorVO> advisorPage(AdvisorPageDTO dto);
    void advisorInsert(AdvisorManageDTO dto);
    void advisorUpdate(AdvisorManageDTO dto);
    void advisorDelete(String advisorId);

    // Prompt
    PageResult<PromptVO> promptPage(PromptPageDTO dto);
    void promptInsert(PromptManageDTO dto);
    void promptUpdate(PromptManageDTO dto);
    void promptDelete(String promptId);

    // Client
    PageResult<ClientVO> clientPage(ClientPageDTO dto);
    void clientInsert(ClientManageDTO dto);
    void clientUpdate(ClientManageDTO dto);
    void clientDelete(String clientId);
    void clientToggle(String clientId, Integer status);

    // Agent
    PageResult<AgentVO> agentPage(AgentPageDTO dto);
    List<AgentVO> agentList(AgentListDTO dto);
    void agentInsert(AgentManageDTO dto);
    void agentUpdate(AgentManageDTO dto);
    void agentDelete(String agentId);
    void agentToggle(String agentId, Integer status);

    // User
    PageResult<UserVO> userPage(UserPageDTO dto);
    void userInsert(UserManageDTO dto);
    void userUpdate(UserManageDTO dto);
    void userDelete(String userName);
    void userToggle(String userName, Integer status);

    // Config
    Map<String, List<ConfigVO>> configList(ConfigListDTO dto);
    void configInsert(ConfigManageDTO dto);
    void configUpdate(ConfigManageDTO dto);
    void configDelete(String clientId, String configType, String configValue);
    void configToggle(String clientId, String configType, String configValue, Integer configStatus);

    // Flow
    List<ClientDetailVO> flowClient();
    List<FlowVO> flowAgent(String agentId);
    void flowInsert(FlowManageDTO dto);
    void flowUpdate(FlowManageDTO dto);
    void flowDelete(String agentId, String clientId);

    // Task
    PageResult<TaskVO> taskPage(TaskPageDTO dto);
    void taskInsert(TaskManageDTO dto);
    void taskUpdate(TaskManageDTO dto);
    void taskDelete(String taskId);
    void taskToggle(String taskId, Integer status);

    // Session
    List<SessionVO> listSession();

    // List
    List<String> listClientType();
    List<String> listAgentType();
    List<String> listClientRole();
    List<String> listUserRole();
    List<String> listApiId();
    List<String> listModelId();
    List<String> listConfigType();

    // Dashboard
    DashboardVO dashboard();
}
