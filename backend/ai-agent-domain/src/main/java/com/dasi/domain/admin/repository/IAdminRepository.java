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
    List<ModelVO> modelPage(ModelPageDTO request);
    Integer modelCount(ModelPageDTO request);
    ModelVO modelQuery(Long id);
    ModelVO modelQuery(String modelId);
    void modelInsert(ModelManageDTO request);
    void modelUpdate(ModelManageDTO request);
    void modelDelete(Long id);

    // Mcp
    List<McpVO> mcpPage(McpPageDTO request);
    Integer mcpCount(McpPageDTO request);
    McpVO mcpQuery(Long id);
    McpVO mcpQuery(String mcpId);
    void mcpInsert(McpManageDTO request);
    void mcpUpdate(McpManageDTO request);
    void mcpDelete(Long id);

    // Advisor
    List<AdvisorVO> advisorPage(AdvisorPageDTO request);
    Integer advisorCount(AdvisorPageDTO request);
    AdvisorVO advisorQuery(Long id);
    AdvisorVO advisorQuery(String advisorId);
    void advisorInsert(AdvisorManageDTO request);
    void advisorUpdate(AdvisorManageDTO request);
    void advisorDelete(Long id);

    // Prompt
    List<PromptVO> promptPage(PromptPageDTO request);
    Integer promptCount(PromptPageDTO request);
    PromptVO promptQuery(Long id);
    PromptVO promptQuery(String promptId);
    void promptInsert(PromptManageDTO request);
    void promptUpdate(PromptManageDTO request);
    void promptDelete(Long id);

    // Client
    List<ClientVO> clientPage(ClientPageDTO request);
    Integer clientCount(ClientPageDTO request);
    ClientVO clientQuery(Long id);
    ClientVO clientQuery(String clientId);
    void clientInsert(ClientManageDTO request);
    void clientUpdate(ClientManageDTO request);
    void clientDelete(Long id);
    void clientToggle(Long id, Integer status);

    // Agent
    List<AgentVO> agentPage(AgentPageDTO request);
    List<AgentVO> agentList(AgentListDTO request);
    Integer agentCount(AgentPageDTO request);
    AgentVO agentQuery(Long id);
    AgentVO agentQuery(String agentId);
    void agentInsert(AgentManageDTO request);
    void agentUpdate(AgentManageDTO request);
    void agentDelete(Long id);
    void agentToggle(Long id, Integer status);

    // User
    List<UserVO> userPage(UserPageDTO request);
    Integer userCount(UserPageDTO request);
    UserVO userQuery(Long id);
    UserVO userQuery(String userName);
    void userInsert(UserManageDTO request);
    void userUpdate(UserManageDTO request);
    void userDelete(Long id);
    void userToggle(Long id, Integer status);

    // Config
    List<ConfigVO> configList(ConfigListDTO request);
    ConfigVO configQuery(ConfigManageDTO request);
    ConfigVO configQuery(Long id);
    void configInsert(ConfigManageDTO request);
    void configUpdate(ConfigManageDTO request);
    void configDelete(Long id);
    void configToggle(Long id, Integer status);

    // Flow
    List<ClientDetailVO> flowClient();
    List<FlowVO> flowAgent(String agentId);
    FlowVO flowQuery(String agentId, String clientId);
    FlowVO flowQuery(Long id);
    void flowInsert(FlowManageDTO request);
    void flowUpdate(FlowManageDTO request);
    void flowDelete(Long id);

    // Task
    List<TaskVO> taskPage(TaskPageDTO request);
    Integer taskCount(TaskPageDTO request);
    TaskVO taskQuery(Long id);
    TaskVO taskQuery(String taskId);
    void taskInsert(TaskManageDTO request);
    void taskUpdate(TaskManageDTO request);
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
