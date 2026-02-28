package com.dasi.trigger.http;

import com.dasi.api.IAdminApi;
import com.dasi.domain.admin.model.dto.*;
import com.dasi.domain.admin.model.vo.*;
import com.dasi.domain.admin.service.IAdminService;
import com.dasi.domain.session.model.vo.SessionVO;
import com.dasi.domain.admin.model.vo.DashboardVO;
import com.dasi.types.result.PageResult;
import com.dasi.types.result.Result;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/admin")
public class AdminController implements IAdminApi {

    @Resource
    private IAdminService adminService;

    // -------------------- Dashboard --------------------
    @PostMapping("/dashboard")
    public Result<DashboardVO> dashboard() {
        return Result.success(adminService.dashboard());
    }


    // -------------------- API --------------------
    @PostMapping("/api/page")
    public Result<PageResult<ApiVO>> apiPage(@Valid @RequestBody ApiPageDTO dto) {
        return Result.success(adminService.apiPage(dto));
    }

    @PostMapping("/api/insert")
    public Result<Void> apiInsert(@Valid @RequestBody ApiManageDTO dto) {
        adminService.apiInsert(dto);
        return Result.success();
    }

    @PostMapping("/api/update")
    public Result<Void> apiUpdate(@Valid @RequestBody ApiManageDTO dto) {
        adminService.apiUpdate(dto);
        return Result.success();
    }

    @PostMapping("/api/delete")
    public Result<Void> apiDelete(@RequestParam("apiId") String apiId) {
        adminService.apiDelete(apiId);
        return Result.success();
    }

    // -------------------- Model --------------------
    @PostMapping("/model/page")
    public Result<PageResult<ModelVO>> modelPage(@Valid @RequestBody ModelPageDTO dto) {
        return Result.success(adminService.modelPage(dto));
    }

    @PostMapping("/model/insert")
    public Result<Void> modelInsert(@Valid @RequestBody ModelManageDTO dto) {
        adminService.modelInsert(dto);
        return Result.success();
    }

    @PostMapping("/model/update")
    public Result<Void> modelUpdate(@Valid @RequestBody ModelManageDTO dto) {
        adminService.modelUpdate(dto);
        return Result.success();
    }

    @PostMapping("/model/delete")
    public Result<Void> modelDelete(@RequestParam("modelId") String modelId) {
        adminService.modelDelete(modelId);
        return Result.success();
    }

    // -------------------- MCP --------------------
    @PostMapping("/mcp/page")
    public Result<PageResult<McpVO>> mcpPage(@Valid @RequestBody McpPageDTO dto) {
        return Result.success(adminService.mcpPage(dto));
    }

    @PostMapping("/mcp/insert")
    public Result<Void> mcpInsert(@Valid @RequestBody McpManageDTO dto) {
        adminService.mcpInsert(dto);
        return Result.success();
    }

    @PostMapping("/mcp/update")
    public Result<Void> mcpUpdate(@Valid @RequestBody McpManageDTO dto) {
        adminService.mcpUpdate(dto);
        return Result.success();
    }

    @PostMapping("/mcp/delete")
    public Result<Void> mcpDelete(@RequestParam("mcpId") String mcpId) {
        adminService.mcpDelete(mcpId);
        return Result.success();
    }

    // -------------------- Advisor --------------------
    @PostMapping("/advisor/page")
    public Result<PageResult<AdvisorVO>> advisorPage(@Valid @RequestBody AdvisorPageDTO dto) {
        return Result.success(adminService.advisorPage(dto));
    }

    @PostMapping("/advisor/insert")
    public Result<Void> advisorInsert(@Valid @RequestBody AdvisorManageDTO dto) {
        adminService.advisorInsert(dto);
        return Result.success();
    }

    @PostMapping("/advisor/update")
    public Result<Void> advisorUpdate(@Valid @RequestBody AdvisorManageDTO dto) {
        adminService.advisorUpdate(dto);
        return Result.success();
    }

    @PostMapping("/advisor/delete")
    public Result<Void> advisorDelete(@RequestParam("advisorId") String advisorId) {
        adminService.advisorDelete(advisorId);
        return Result.success();
    }

    // -------------------- Prompt --------------------
    @PostMapping("/prompt/page")
    public Result<PageResult<PromptVO>> promptPage(@Valid @RequestBody PromptPageDTO dto) {
        return Result.success(adminService.promptPage(dto));
    }

    @PostMapping("/prompt/insert")
    public Result<Void> promptInsert(@Valid @RequestBody PromptManageDTO dto) {
        adminService.promptInsert(dto);
        return Result.success();
    }

    @PostMapping("/prompt/update")
    public Result<Void> promptUpdate(@Valid @RequestBody PromptManageDTO dto) {
        adminService.promptUpdate(dto);
        return Result.success();
    }

    @PostMapping("/prompt/delete")
    public Result<Void> promptDelete(@RequestParam("promptId") String promptId) {
        adminService.promptDelete(promptId);
        return Result.success();
    }

    // -------------------- Client --------------------
    @PostMapping("/client/page")
    public Result<PageResult<ClientVO>> clientPage(@Valid @RequestBody ClientPageDTO dto) {
        return Result.success(adminService.clientPage(dto));
    }

    @PostMapping("/client/insert")
    public Result<Void> clientInsert(@Valid @RequestBody ClientManageDTO dto) {
        adminService.clientInsert(dto);
        return Result.success();
    }

    @PostMapping("/client/update")
    public Result<Void> clientUpdate(@Valid @RequestBody ClientManageDTO dto) {
        adminService.clientUpdate(dto);
        return Result.success();
    }

    @PostMapping("/client/delete")
    public Result<Void> clientDelete(@RequestParam("clientId") String clientId) {
        adminService.clientDelete(clientId);
        return Result.success();
    }

    @PostMapping("/client/toggle")
    public Result<Void> clientToggle(@RequestParam("clientId") String clientId, @RequestParam("clientStatus") Integer clientStatus) {
        adminService.clientToggle(clientId, clientStatus);
        return Result.success();
    }

    // -------------------- Agent --------------------
    @PostMapping("/agent/page")
    public Result<PageResult<AgentVO>> agentPage(@Valid @RequestBody AgentPageDTO dto) {
        return Result.success(adminService.agentPage(dto));
    }

    @PostMapping("/agent/list")
    public Result<List<AgentVO>> agentList(@Valid @RequestBody AgentListDTO dto) {
        return Result.success(adminService.agentList(dto));
    }

    @PostMapping("/agent/insert")
    public Result<Void> agentInsert(@Valid @RequestBody AgentManageDTO dto) {
        adminService.agentInsert(dto);
        return Result.success();
    }

    @PostMapping("/agent/update")
    public Result<Void> agentUpdate(@Valid @RequestBody AgentManageDTO dto) {
        adminService.agentUpdate(dto);
        return Result.success();
    }

    @PostMapping("/agent/delete")
    public Result<Void> agentDelete(@RequestParam("agentId") String agentId) {
        adminService.agentDelete(agentId);
        return Result.success();
    }

    @PostMapping("/agent/toggle")
    public Result<Void> agentToggle(@RequestParam("agentId") String agentId, @RequestParam("agentStatus") Integer agentStatus) {
        adminService.agentToggle(agentId, agentStatus);
        return Result.success();
    }

    // -------------------- User --------------------
    @PostMapping("/user/page")
    public Result<PageResult<UserVO>> userPage(@Valid @RequestBody UserPageDTO dto) {
        return Result.success(adminService.userPage(dto));
    }

    @PostMapping("/user/insert")
    public Result<Void> userInsert(@Valid @RequestBody UserManageDTO dto) {
        adminService.userInsert(dto);
        return Result.success();
    }

    @PostMapping("/user/update")
    public Result<Void> userUpdate(@Valid @RequestBody UserManageDTO dto) {
        adminService.userUpdate(dto);
        return Result.success();
    }

    @PostMapping("/user/delete")
    public Result<Void> userDelete(@RequestParam("userName") String userName) {
        adminService.userDelete(userName);
        return Result.success();
    }

    @PostMapping("/user/toggle")
    public Result<Void> userToggle(@RequestParam("userName") String userName, @RequestParam("userStatus") Integer userStatus) {
        adminService.userToggle(userName, userStatus);
        return Result.success();
    }

    // -------------------- Config --------------------
    @PostMapping("/config/list")
    public Result<Map<String, List<ConfigVO>>> configList(@Valid @RequestBody ConfigListDTO dto) {
        return Result.success(adminService.configList(dto));
    }

    @PostMapping("/config/insert")
    public Result<Void> configInsert(@Valid @RequestBody ConfigManageDTO dto) {
        adminService.configInsert(dto);
        return Result.success();
    }

    @PostMapping("/config/update")
    public Result<Void> configUpdate(@Valid @RequestBody ConfigManageDTO dto) {
        adminService.configUpdate(dto);
        return Result.success();
    }

    @PostMapping("/config/delete")
    public Result<Void> configDelete(@RequestParam("clientId") String clientId,
                                     @RequestParam("configType") String configType,
                                     @RequestParam("configValue") String configValue) {
        adminService.configDelete(clientId, configType, configValue);
        return Result.success();
    }

    @PostMapping("/config/toggle")
    public Result<Void> configToggle(@RequestParam("clientId") String clientId,
                                     @RequestParam("configType") String configType,
                                     @RequestParam("configValue") String configValue,
                                     @RequestParam("configStatus") Integer configStatus) {
        adminService.configToggle(clientId, configType, configValue, configStatus);
        return Result.success();
    }

    // -------------------- Flow --------------------
    @PostMapping("/flow/client")
    public Result<List<ClientDetailVO>> flowClient() {
        return Result.success(adminService.flowClient());
    }

    @PostMapping("/flow/agent")
    public Result<List<FlowVO>> flowAgent(@RequestParam("agentId") String agentId) {
        return Result.success(adminService.flowAgent(agentId));
    }

    @PostMapping("/flow/insert")
    public Result<Void> flowInsert(@Valid @RequestBody FlowManageDTO dto) {
        adminService.flowInsert(dto);
        return Result.success();
    }

    @PostMapping("/flow/update")
    public Result<Void> flowUpdate(@Valid @RequestBody FlowManageDTO dto) {
        adminService.flowUpdate(dto);
        return Result.success();
    }

    @PostMapping("/flow/delete")
    public Result<Void> flowDelete(@RequestParam("agentId") String agentId, @RequestParam("clientId") String clientId) {
        adminService.flowDelete(agentId, clientId);
        return Result.success();
    }

    // -------------------- Task --------------------
    @PostMapping("/task/page")
    public Result<PageResult<TaskVO>> taskPage(@Valid @RequestBody TaskPageDTO dto) {
        return Result.success(adminService.taskPage(dto));
    }

    @PostMapping("/task/insert")
    public Result<Void> taskInsert(@Valid @RequestBody TaskManageDTO dto) {
        adminService.taskInsert(dto);
        return Result.success();
    }

    @PostMapping("/task/update")
    public Result<Void> taskUpdate(@Valid @RequestBody TaskManageDTO dto) {
        adminService.taskUpdate(dto);
        return Result.success();
    }

    @PostMapping("/task/delete")
    public Result<Void> taskDelete(@RequestParam("taskId") String taskId) {
        adminService.taskDelete(taskId);
        return Result.success();
    }

    @PostMapping("/task/toggle")
    public Result<Void> taskToggle(@RequestParam("taskId") String taskId, @RequestParam("taskStatus") Integer taskStatus) {
        adminService.taskToggle(taskId, taskStatus);
        return Result.success();
    }

    // -------------------- Session --------------------
    @PostMapping("/session/list")
    public Result<List<SessionVO>> listSession() {
        return Result.success(adminService.listSession());
    }

    // -------------------- List --------------------
    @PostMapping("/list/clientType")
    public Result<List<String>> listClientType() {
        return Result.success(adminService.listClientType());
    }

    @PostMapping("/list/agentType")
    public Result<List<String>> listAgentType() {
        return Result.success(adminService.listAgentType());
    }

    @PostMapping("/list/configType")
    public Result<List<String>> listConfigType() {
        return Result.success(adminService.listConfigType());
    }

    @PostMapping("/list/clientRole")
    public Result<List<String>> listClientRole() {
        return Result.success(adminService.listClientRole());
    }

    @PostMapping("/list/userRole")
    public Result<List<String>> listUserRole() {
        return Result.success(adminService.listUserRole());
    }

    @PostMapping("/list/apiId")
    public Result<List<String>> listApiId() {
        return Result.success(adminService.listApiId());
    }

    @PostMapping("/list/modelId")
    public Result<List<String>> listModelId() {
        return Result.success(adminService.listModelId());
    }

}
