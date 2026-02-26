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
    PageResult<ApiVO> apiPage(ApiPageDTO request);
    void apiInsert(ApiManageDTO request);
    void apiUpdate(ApiManageDTO request);
    void apiDelete(Long id);

    // Model
    PageResult<ModelVO> modelPage(ModelPageDTO request);
    void modelInsert(ModelManageDTO request);
    void modelUpdate(ModelManageDTO request);
    void modelDelete(Long id);

    // Mcp
    PageResult<McpVO> mcpPage(McpPageDTO request);
    void mcpInsert(McpManageDTO request);
    void mcpUpdate(McpManageDTO request);
    void mcpDelete(Long id);

    // Advisor
    PageResult<AdvisorVO> advisorPage(AdvisorPageDTO request);
    void advisorInsert(AdvisorManageDTO request);
    void advisorUpdate(AdvisorManageDTO request);
    void advisorDelete(Long id);

    // Prompt
    PageResult<PromptVO> promptPage(PromptPageDTO request);
    void promptInsert(PromptManageDTO request);
    void promptUpdate(PromptManageDTO request);
    void promptDelete(Long id);

    // Client
    PageResult<ClientVO> clientPage(ClientPageDTO request);
    void clientInsert(ClientManageDTO request);
    void clientUpdate(ClientManageDTO request);
    void clientDelete(Long id);
    void clientToggle(Long id, Integer status);

    // Agent
    PageResult<AgentVO> agentPage(AgentPageDTO request);
    List<AgentVO> agentList(AgentListDTO request);
    void agentInsert(AgentManageDTO request);
    void agentUpdate(AgentManageDTO request);
    void agentDelete(Long id);
    void agentToggle(Long id, Integer status);

    // User
    PageResult<UserVO> userPage(UserPageDTO request);
    void userInsert(UserManageDTO request);
    void userUpdate(UserManageDTO request);
    void userDelete(Long id);
    void userToggle(Long id, Integer status);

    // Config
    Map<String, List<ConfigVO>> configList(ConfigListDTO request);
    void configInsert(ConfigManageDTO request);
    void configUpdate(ConfigManageDTO request);
    void configDelete(Long id);
    void configToggle(Long id, Integer configStatus);

    // Flow
    List<ClientDetailVO> flowClient();
    List<FlowVO> flowAgent(String agentId);
    void flowInsert(FlowManageDTO request);
    void flowUpdate(FlowManageDTO request);
    void flowDelete(Long id);

    // Task
    PageResult<TaskVO> taskPage(TaskPageDTO request);
    void taskInsert(TaskManageDTO request);
    void taskUpdate(TaskManageDTO request);
    void taskDelete(Long id);
    void taskToggle(Long id, Integer status);

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
