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

    Result<PageResult<ApiVO>> apiPage(ApiPageDTO dto);

    Result<Void> apiInsert(ApiManageDTO dto);

    Result<Void> apiUpdate(ApiManageDTO dto);

    Result<Void> apiDelete(String apiId);

    Result<PageResult<ModelVO>> modelPage(ModelPageDTO dto);

    Result<Void> modelInsert(ModelManageDTO dto);

    Result<Void> modelUpdate(ModelManageDTO dto);

    Result<Void> modelDelete(String modelId);

    Result<PageResult<McpVO>> mcpPage(McpPageDTO dto);

    Result<Void> mcpInsert(McpManageDTO dto);

    Result<Void> mcpUpdate(McpManageDTO dto);

    Result<Void> mcpDelete(String mcpId);

    Result<PageResult<AdvisorVO>> advisorPage(AdvisorPageDTO dto);

    Result<Void> advisorInsert(AdvisorManageDTO dto);

    Result<Void> advisorUpdate(AdvisorManageDTO dto);

    Result<Void> advisorDelete(String advisorId);

    Result<PageResult<PromptVO>> promptPage(PromptPageDTO dto);

    Result<Void> promptInsert(PromptManageDTO dto);

    Result<Void> promptUpdate(PromptManageDTO dto);

    Result<Void> promptDelete(String promptId);

    Result<PageResult<ClientVO>> clientPage(ClientPageDTO dto);

    Result<Void> clientInsert(ClientManageDTO dto);

    Result<Void> clientUpdate(ClientManageDTO dto);

    Result<Void> clientDelete(String clientId);

    Result<Void> clientToggle(String clientId, Integer clientStatus);

    Result<PageResult<AgentVO>> agentPage(AgentPageDTO dto);

    Result<List<AgentVO>> agentList(AgentListDTO dto);

    Result<Void> agentInsert(AgentManageDTO dto);

    Result<Void> agentUpdate(AgentManageDTO dto);

    Result<Void> agentDelete(String agentId);

    Result<Void> agentToggle(String agentId, Integer agentStatus);

    Result<PageResult<UserVO>> userPage(UserPageDTO dto);

    Result<Void> userInsert(UserManageDTO dto);

    Result<Void> userUpdate(UserManageDTO dto);

    Result<Void> userDelete(String userName);

    Result<Void> userToggle(String userName, Integer userStatus);

    Result<Map<String, List<ConfigVO>>> configList(ConfigListDTO dto);

    Result<Void> configInsert(ConfigManageDTO dto);

    Result<Void> configUpdate(ConfigManageDTO dto);

    Result<Void> configDelete(String clientId, String configType, String configValue);

    Result<Void> configToggle(String clientId, String configType, String configValue, Integer configStatus);

    Result<List<ClientDetailVO>> flowClient();

    Result<List<FlowVO>> flowAgent(String agentId);

    Result<Void> flowInsert(FlowManageDTO dto);

    Result<Void> flowUpdate(FlowManageDTO dto);

    Result<Void> flowDelete(String agentId, String clientId);

    Result<PageResult<TaskVO>> taskPage(TaskPageDTO dto);

    Result<Void> taskInsert(TaskManageDTO dto);

    Result<Void> taskUpdate(TaskManageDTO dto);

    Result<Void> taskDelete(String taskId);

    Result<Void> taskToggle(String taskId, Integer taskStatus);

    Result<List<SessionVO>> listSession();

    Result<List<String>> listClientType();

    Result<List<String>> listAgentType();

    Result<List<String>> listConfigType();

    Result<List<String>> listClientRole();

    Result<List<String>> listUserRole();

    Result<List<String>> listApiId();

    Result<List<String>> listModelId();
}
