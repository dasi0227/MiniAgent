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
    void apiDelete(Long id);

    // Model
    PageResult<ModelVO> modelPage(ModelPageDTO dto);
    void modelInsert(ModelManageDTO dto);
    void modelUpdate(ModelManageDTO dto);
    void modelDelete(Long id);

    // Mcp
    PageResult<McpVO> mcpPage(McpPageDTO dto);
    void mcpInsert(McpManageDTO dto);
    void mcpUpdate(McpManageDTO dto);
    void mcpDelete(Long id);

    // Advisor
    PageResult<AdvisorVO> advisorPage(AdvisorPageDTO dto);
    void advisorInsert(AdvisorManageDTO dto);
    void advisorUpdate(AdvisorManageDTO dto);
    void advisorDelete(Long id);

    // Prompt
    PageResult<PromptVO> promptPage(PromptPageDTO dto);
    void promptInsert(PromptManageDTO dto);
    void promptUpdate(PromptManageDTO dto);
    void promptDelete(Long id);

    // Client
    PageResult<ClientVO> clientPage(ClientPageDTO dto);
    void clientInsert(ClientManageDTO dto);
    void clientUpdate(ClientManageDTO dto);
    void clientDelete(Long id);
    void clientToggle(Long id, Integer status);

    // Agent
    PageResult<AgentVO> agentPage(AgentPageDTO dto);
    List<AgentVO> agentList(AgentListDTO dto);
    void agentInsert(AgentManageDTO dto);
    void agentUpdate(AgentManageDTO dto);
    void agentDelete(Long id);
    void agentToggle(Long id, Integer status);

    // User
    PageResult<UserVO> userPage(UserPageDTO dto);
    void userInsert(UserManageDTO dto);
    void userUpdate(UserManageDTO dto);
    void userDelete(Long id);
    void userToggle(Long id, Integer status);

    // Config
    Map<String, List<ConfigVO>> configList(ConfigListDTO dto);
    void configInsert(ConfigManageDTO dto);
    void configUpdate(ConfigManageDTO dto);
    void configDelete(Long id);
    void configToggle(Long id, Integer configStatus);

    // Flow
    List<ClientDetailVO> flowClient();
    List<FlowVO> flowAgent(String agentId);
    void flowInsert(FlowManageDTO dto);
    void flowUpdate(FlowManageDTO dto);
    void flowDelete(Long id);

    // Task
    PageResult<TaskVO> taskPage(TaskPageDTO dto);
    void taskInsert(TaskManageDTO dto);
    void taskUpdate(TaskManageDTO dto);
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
