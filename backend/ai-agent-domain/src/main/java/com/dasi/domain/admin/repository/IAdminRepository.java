package com.dasi.domain.admin.repository;

import com.dasi.domain.admin.model.dto.*;
import com.dasi.domain.admin.model.vo.*;
import com.dasi.domain.admin.model.vo.DashboardVO;

import java.util.List;

public interface IAdminRepository {

    // Dashboard
    DashboardVO.CountInfo dashboardCount();
    DashboardVO.GraphInfo dashboardChart();

    // Api
    List<ApiVO> apiPage(ApiPageDTO apiPageDTO);
    Integer apiCount(ApiPageDTO apiPageDTO);
    ApiVO apiQuery(String apiId);
    void apiInsert(ApiManageDTO apiManageDTO);
    void apiUpdate(ApiManageDTO apiManageDTO);
    void apiDelete(String apiId);

    // Model
    List<ModelVO> modelPage(ModelPageDTO dto);
    Integer modelCount(ModelPageDTO dto);
    ModelVO modelQuery(String modelId);
    void modelInsert(ModelManageDTO dto);
    void modelUpdate(ModelManageDTO dto);
    void modelDelete(String modelId);

    // Mcp
    List<McpVO> mcpPage(McpPageDTO dto);
    Integer mcpCount(McpPageDTO dto);
    McpVO mcpQuery(String mcpId);
    void mcpInsert(McpManageDTO dto);
    void mcpUpdate(McpManageDTO dto);
    void mcpDelete(String mcpId);

    // Advisor
    List<AdvisorVO> advisorPage(AdvisorPageDTO dto);
    Integer advisorCount(AdvisorPageDTO dto);
    AdvisorVO advisorQuery(String advisorId);
    void advisorInsert(AdvisorManageDTO dto);
    void advisorUpdate(AdvisorManageDTO dto);
    void advisorDelete(String advisorId);

    // Prompt
    List<PromptVO> promptPage(PromptPageDTO dto);
    Integer promptCount(PromptPageDTO dto);
    PromptVO promptQuery(String promptId);
    void promptInsert(PromptManageDTO dto);
    void promptUpdate(PromptManageDTO dto);
    void promptDelete(String promptId);

    // Client
    List<ClientVO> clientPage(ClientPageDTO dto);
    Integer clientCount(ClientPageDTO dto);
    ClientVO clientQuery(String clientId);
    void clientInsert(ClientManageDTO dto);
    void clientUpdate(ClientManageDTO dto);
    void clientDelete(String clientId);
    void clientToggle(String clientId, Integer status);

    // Agent
    List<AgentVO> agentPage(AgentPageDTO dto);
    List<AgentVO> agentList(AgentListDTO dto);
    Integer agentCount(AgentPageDTO dto);
    AgentVO agentQuery(String agentId);
    void agentInsert(AgentManageDTO dto);
    void agentUpdate(AgentManageDTO dto);
    void agentDelete(String agentId);
    void agentToggle(String agentId, Integer status);

    // User
    List<UserVO> userPage(UserPageDTO dto);
    Integer userCount(UserPageDTO dto);
    UserVO userQuery(String userName);
    void userInsert(UserManageDTO dto);
    void userUpdate(UserManageDTO dto);
    void userDelete(String userName);
    void userToggle(String userName, Integer status);

    // Config
    List<ConfigVO> configList(ConfigListDTO dto);
    ConfigVO configQuery(ConfigManageDTO dto);
    void configInsert(ConfigManageDTO dto);
    void configUpdate(ConfigManageDTO dto);
    void configDelete(String clientId, String configType, String configValue);
    void configToggle(String clientId, String configType, String configValue, Integer status);

    // Flow
    List<ClientDetailVO> flowClient();
    List<FlowVO> flowAgent(String agentId);
    FlowVO flowQuery(String agentId, String clientId);
    void flowInsert(FlowManageDTO dto);
    void flowUpdate(FlowManageDTO dto);
    void flowDelete(String agentId, String clientId);

    // Task
    List<TaskVO> taskPage(TaskPageDTO dto);
    Integer taskCount(TaskPageDTO dto);
    TaskVO taskQuery(String taskId);
    void taskInsert(TaskManageDTO dto);
    void taskUpdate(TaskManageDTO dto);
    void taskDelete(String taskId);
    void taskToggle(String taskId, Integer status);

    // Session
    List<SessionVO> listSession();

    // Template
    List<TemplateVO> templatePage(TemplatePageDTO dto);
    Integer templateCount(TemplatePageDTO dto);
    TemplateVO templateQuery(String templateId);
    void templateInsert(TemplateManageDTO dto);
    void templateUpdate(TemplateManageDTO dto);
    void templateDelete(String templateId);

    // Plaza
    List<PlazaVO> plazaPage(PlazaPageDTO dto);
    Integer plazaCount(PlazaPageDTO dto);
    PlazaVO plazaQuery(String plazaId);
    void plazaInsert(PlazaManageDTO dto);
    void plazaUpdate(PlazaManageDTO dto);
    void plazaDelete(String plazaId);

    // Depend
    List<String> queryClientDependOnPrompt(String promptId);
    List<String> queryClientDependOnAdvisor(String advisorId);
    List<String> queryClientDependOnMcp(String mcpId);
    List<String> queryModelDependOnApi(String apiId);
    List<String> queryClientDependOnModel(String modelId);
    List<String> queryAgentDependOnClient(String clientId);
    List<String> queryPlazaDependOnTemplate(String templateId);

    // Option
    List<String> listApiId();
    List<String> listModelId();

}
