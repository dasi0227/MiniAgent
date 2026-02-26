package com.dasi.api;

import com.dasi.domain.admin.model.dto.*;
import com.dasi.domain.admin.model.vo.*;
import com.dasi.domain.session.model.vo.SessionVO;
import com.dasi.domain.admin.model.vo.DashboardVO;
import com.dasi.types.result.PageResult;
import com.dasi.types.result.Result;

import java.util.List;
import java.util.Map;

public interface IAdminApi {

    Result<DashboardVO> dashboard();

    Result<PageResult<ApiVO>> apiPage(ApiPageDTO request);

    Result<Void> apiInsert(ApiManageDTO request);

    Result<Void> apiUpdate(ApiManageDTO request);

    Result<Void> apiDelete(Long id);

    Result<PageResult<ModelVO>> modelPage(ModelPageDTO request);

    Result<Void> modelInsert(ModelManageDTO request);

    Result<Void> modelUpdate(ModelManageDTO request);

    Result<Void> modelDelete(Long id);

    Result<PageResult<McpVO>> mcpPage(McpPageDTO request);

    Result<Void> mcpInsert(McpManageDTO request);

    Result<Void> mcpUpdate(McpManageDTO request);

    Result<Void> mcpDelete(Long id);

    Result<PageResult<AdvisorVO>> advisorPage(AdvisorPageDTO request);

    Result<Void> advisorInsert(AdvisorManageDTO request);

    Result<Void> advisorUpdate(AdvisorManageDTO request);

    Result<Void> advisorDelete(Long id);

    Result<PageResult<PromptVO>> promptPage(PromptPageDTO request);

    Result<Void> promptInsert(PromptManageDTO request);

    Result<Void> promptUpdate(PromptManageDTO request);

    Result<Void> promptDelete(Long id);

    Result<PageResult<ClientVO>> clientPage(ClientPageDTO request);

    Result<Void> clientInsert(ClientManageDTO request);

    Result<Void> clientUpdate(ClientManageDTO request);

    Result<Void> clientDelete(Long id);

    Result<Void> clientToggle(Long id, Integer clientStatus);

    Result<PageResult<AgentVO>> agentPage(AgentPageDTO request);

    Result<List<AgentVO>> agentList(AgentListDTO request);

    Result<Void> agentInsert(AgentManageDTO request);

    Result<Void> agentUpdate(AgentManageDTO request);

    Result<Void> agentDelete(Long id);

    Result<Void> agentToggle(Long id, Integer agentStatus);

    Result<PageResult<UserVO>> userPage(UserPageDTO request);

    Result<Void> userInsert(UserManageDTO request);

    Result<Void> userUpdate(UserManageDTO request);

    Result<Void> userDelete(Long id);

    Result<Void> userToggle(Long id, Integer userStatus);

    Result<Map<String, List<ConfigVO>>> configList(ConfigListDTO request);

    Result<Void> configInsert(ConfigManageDTO request);

    Result<Void> configUpdate(ConfigManageDTO request);

    Result<Void> configDelete(Long id);

    Result<Void> configToggle(Long id, Integer configStatus);

    Result<List<ClientDetailVO>> flowClient();

    Result<List<FlowVO>> flowAgent(String agentId);

    Result<Void> flowInsert(FlowManageDTO request);

    Result<Void> flowUpdate(FlowManageDTO request);

    Result<Void> flowDelete(Long id);

    Result<PageResult<TaskVO>> taskPage(TaskPageDTO request);

    Result<Void> taskInsert(TaskManageDTO request);

    Result<Void> taskUpdate(TaskManageDTO request);

    Result<Void> taskDelete(Long id);

    Result<Void> taskToggle(Long id, Integer taskStatus);

    Result<List<SessionVO>> listSession();

    Result<List<String>> listClientType();

    Result<List<String>> listAgentType();

    Result<List<String>> listConfigType();

    Result<List<String>> listClientRole();

    Result<List<String>> listUserRole();

    Result<List<String>> listApiId();

    Result<List<String>> listModelId();
}
