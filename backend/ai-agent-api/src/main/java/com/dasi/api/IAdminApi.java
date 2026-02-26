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

    Result<Void> apiDelete(Long id);

    Result<PageResult<ModelVO>> modelPage(ModelPageDTO dto);

    Result<Void> modelInsert(ModelManageDTO dto);

    Result<Void> modelUpdate(ModelManageDTO dto);

    Result<Void> modelDelete(Long id);

    Result<PageResult<McpVO>> mcpPage(McpPageDTO dto);

    Result<Void> mcpInsert(McpManageDTO dto);

    Result<Void> mcpUpdate(McpManageDTO dto);

    Result<Void> mcpDelete(Long id);

    Result<PageResult<AdvisorVO>> advisorPage(AdvisorPageDTO dto);

    Result<Void> advisorInsert(AdvisorManageDTO dto);

    Result<Void> advisorUpdate(AdvisorManageDTO dto);

    Result<Void> advisorDelete(Long id);

    Result<PageResult<PromptVO>> promptPage(PromptPageDTO dto);

    Result<Void> promptInsert(PromptManageDTO dto);

    Result<Void> promptUpdate(PromptManageDTO dto);

    Result<Void> promptDelete(Long id);

    Result<PageResult<ClientVO>> clientPage(ClientPageDTO dto);

    Result<Void> clientInsert(ClientManageDTO dto);

    Result<Void> clientUpdate(ClientManageDTO dto);

    Result<Void> clientDelete(Long id);

    Result<Void> clientToggle(Long id, Integer clientStatus);

    Result<PageResult<AgentVO>> agentPage(AgentPageDTO dto);

    Result<List<AgentVO>> agentList(AgentListDTO dto);

    Result<Void> agentInsert(AgentManageDTO dto);

    Result<Void> agentUpdate(AgentManageDTO dto);

    Result<Void> agentDelete(Long id);

    Result<Void> agentToggle(Long id, Integer agentStatus);

    Result<PageResult<UserVO>> userPage(UserPageDTO dto);

    Result<Void> userInsert(UserManageDTO dto);

    Result<Void> userUpdate(UserManageDTO dto);

    Result<Void> userDelete(Long id);

    Result<Void> userToggle(Long id, Integer userStatus);

    Result<Map<String, List<ConfigVO>>> configList(ConfigListDTO dto);

    Result<Void> configInsert(ConfigManageDTO dto);

    Result<Void> configUpdate(ConfigManageDTO dto);

    Result<Void> configDelete(Long id);

    Result<Void> configToggle(Long id, Integer configStatus);

    Result<List<ClientDetailVO>> flowClient();

    Result<List<FlowVO>> flowAgent(String agentId);

    Result<Void> flowInsert(FlowManageDTO dto);

    Result<Void> flowUpdate(FlowManageDTO dto);

    Result<Void> flowDelete(Long id);

    Result<PageResult<TaskVO>> taskPage(TaskPageDTO dto);

    Result<Void> taskInsert(TaskManageDTO dto);

    Result<Void> taskUpdate(TaskManageDTO dto);

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
