package com.dasi.domain.admin.repository;

import com.dasi.domain.admin.model.dto.*;
import com.dasi.domain.admin.model.vo.*;
import com.dasi.domain.session.model.vo.SessionVO;
import com.dasi.domain.admin.model.vo.DashboardVO;

import java.util.List;

public interface IAdminRepository {

    // Dashboard
    DashboardVO.CountInfo dashboardCount();
    DashboardVO.GraphInfo dashboardChart();

    // Api
    List<ApiVO> apiPage(ApiPageDTO apiPageDTO);
    Integer apiCount(ApiPageDTO apiPageDTO);
    ApiVO apiQuery(Long id);
    ApiVO apiQuery(String apiId);
    void apiInsert(ApiManageDTO apiManageDTO);
    void apiUpdate(ApiManageDTO apiManageDTO);
    void apiDelete(Long id);

    // Model
    List<ModelVO> modelPage(ModelPageDTO dto);
    Integer modelCount(ModelPageDTO dto);
    ModelVO modelQuery(Long id);
    ModelVO modelQuery(String modelId);
    void modelInsert(ModelManageDTO dto);
    void modelUpdate(ModelManageDTO dto);
    void modelDelete(Long id);

    // Mcp
    List<McpVO> mcpPage(McpPageDTO dto);
    Integer mcpCount(McpPageDTO dto);
    McpVO mcpQuery(Long id);
    McpVO mcpQuery(String mcpId);
    void mcpInsert(McpManageDTO dto);
    void mcpUpdate(McpManageDTO dto);
    void mcpDelete(Long id);

    // Advisor
    List<AdvisorVO> advisorPage(AdvisorPageDTO dto);
    Integer advisorCount(AdvisorPageDTO dto);
    AdvisorVO advisorQuery(Long id);
    AdvisorVO advisorQuery(String advisorId);
    void advisorInsert(AdvisorManageDTO dto);
    void advisorUpdate(AdvisorManageDTO dto);
    void advisorDelete(Long id);

    // Prompt
    List<PromptVO> promptPage(PromptPageDTO dto);
    Integer promptCount(PromptPageDTO dto);
    PromptVO promptQuery(Long id);
    PromptVO promptQuery(String promptId);
    void promptInsert(PromptManageDTO dto);
    void promptUpdate(PromptManageDTO dto);
    void promptDelete(Long id);

    // Client
    List<ClientVO> clientPage(ClientPageDTO dto);
    Integer clientCount(ClientPageDTO dto);
    ClientVO clientQuery(Long id);
    ClientVO clientQuery(String clientId);
    void clientInsert(ClientManageDTO dto);
    void clientUpdate(ClientManageDTO dto);
    void clientDelete(Long id);
    void clientToggle(Long id, Integer status);

    // Agent
    List<AgentVO> agentPage(AgentPageDTO dto);
    List<AgentVO> agentList(AgentListDTO dto);
    Integer agentCount(AgentPageDTO dto);
    AgentVO agentQuery(Long id);
    AgentVO agentQuery(String agentId);
    void agentInsert(AgentManageDTO dto);
    void agentUpdate(AgentManageDTO dto);
    void agentDelete(Long id);
    void agentToggle(Long id, Integer status);

    // User
    List<UserVO> userPage(UserPageDTO dto);
    Integer userCount(UserPageDTO dto);
    UserVO userQuery(Long id);
    UserVO userQuery(String userName);
    void userInsert(UserManageDTO dto);
    void userUpdate(UserManageDTO dto);
    void userDelete(Long id);
    void userToggle(Long id, Integer status);

    // Config
    List<ConfigVO> configList(ConfigListDTO dto);
    ConfigVO configQuery(ConfigManageDTO dto);
    ConfigVO configQuery(Long id);
    void configInsert(ConfigManageDTO dto);
    void configUpdate(ConfigManageDTO dto);
    void configDelete(Long id);
    void configToggle(Long id, Integer status);

    // Flow
    List<ClientDetailVO> flowClient();
    List<FlowVO> flowAgent(String agentId);
    FlowVO flowQuery(String agentId, String clientId);
    FlowVO flowQuery(Long id);
    void flowInsert(FlowManageDTO dto);
    void flowUpdate(FlowManageDTO dto);
    void flowDelete(Long id);

    // Task
    List<TaskVO> taskPage(TaskPageDTO dto);
    Integer taskCount(TaskPageDTO dto);
    TaskVO taskQuery(Long id);
    TaskVO taskQuery(String taskId);
    void taskInsert(TaskManageDTO dto);
    void taskUpdate(TaskManageDTO dto);
    void taskDelete(Long id);
    void taskToggle(Long id, Integer status);

    // Session
    List<SessionVO> listSession();

    // Depend
    List<String> queryClientDependOnPrompt(String promptId);
    List<String> queryClientDependOnAdvisor(String advisorId);
    List<String> queryClientDependOnMcp(String mcpId);
    List<String> queryModelDependOnApi(String apiId);
    List<String> queryClientDependOnModel(String modelId);
    List<String> queryAgentDependOnClient(String clientId);

    // Option
    List<String> listApiId();
    List<String> listModelId();

}
