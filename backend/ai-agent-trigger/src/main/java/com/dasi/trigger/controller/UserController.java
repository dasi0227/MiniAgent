package com.dasi.trigger.controller;

import com.dasi.api.IUserApi;
import com.dasi.domain.user.model.vo.*;
import com.dasi.domain.user.service.auth.IAuthService;
import com.dasi.domain.user.service.query.IQueryService;
import com.dasi.domain.user.service.setting.ISettingService;
import com.dasi.domain.user.model.dto.AuthDTO;
import com.dasi.domain.user.model.dto.ProfileEditDTO;
import com.dasi.domain.user.model.dto.SettingApiDTO;
import com.dasi.domain.user.model.dto.SettingMcpDTO;
import com.dasi.domain.user.model.dto.SettingTaskDTO;
import com.dasi.types.result.Result;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/user")
public class UserController implements IUserApi {

    @Resource
    private IAuthService authService;

    @Resource
    private ISettingService settingService;

    @Resource
    private IQueryService queryService;

    @PostMapping("/query/work-agent")
    @Override
    public Result<List<QueryWorkAgentVO>> queryWorkAgentList() {
        return Result.success(queryService.queryWorkAgentList());
    }

    @PostMapping("/query/chat-client")
    @Override
    public Result<List<QueryChatClientVO>> queryChatClientList() {
        return Result.success(queryService.queryChatClientList());
    }

    @PostMapping("/query/mcp")
    @Override
    public Result<List<QueryMcpVO>> queryMcpList() {
        return Result.success(queryService.queryMcpList());
    }

    @PostMapping("/query/rag")
    @Override
    public Result<List<QueryRagVO>> queryRagList() {
        return Result.success(queryService.queryRagList());
    }

    @PostMapping("/query/model")
    public Result<List<QueryModelVO>> queryModelList() {
        return Result.success(queryService.queryModelList());
    }

    @PostMapping("/auth/login")
    @Override
    public Result<AuthVO> login(@Valid @RequestBody AuthDTO dto) {
        return Result.success(authService.login(dto));
    }

    @PostMapping("/auth/register")
    @Override
    public Result<AuthVO> register(@Valid @RequestBody AuthDTO dto) {
        return Result.success(authService.register(dto));
    }

    @PostMapping("/profile/query")
    @Override
    public Result<AuthVO> profileQuery() {
        return Result.success(settingService.profileQuery());
    }

    @PostMapping(value = "/profile/edit", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Override
    public Result<AuthVO> profileEdit(@Valid @RequestPart("profile") ProfileEditDTO dto,
                                      @RequestPart(value = "avatar", required = false) MultipartFile avatar) {
        return Result.success(settingService.profileEdit(dto, avatar));
    }

    @PostMapping(value = "/api/list")
    @Override
    public Result<List<UserApiVO>> apiList(@RequestParam(required=false, defaultValue = "") String keyword) {
        return Result.success(settingService.apiList(keyword));
    }

    @PostMapping(value = "/api/insert")
    @Override
    public Result<Void> apiInsert(@Valid @RequestBody SettingApiDTO dto) {
        settingService.apiInsert(dto);
        return Result.success();
    }

    @PostMapping(value = "/api/update")
    @Override
    public Result<Void> apiUpdate(@Valid @RequestBody SettingApiDTO dto) {
        settingService.apiUpdate(dto);
        return Result.success();
    }

    @PostMapping(value = "/api/delete")
    @Override
    public Result<Void> apiDelete(@RequestParam String apiId) {
        settingService.apiDelete(apiId);
        return Result.success();
    }

    @PostMapping(value = "/mcp/list")
    @Override
    public Result<List<UserMcpVO>> mcpList(@RequestParam(required = false, defaultValue = "") String keyword) {
        return Result.success(settingService.mcpList(keyword));
    }

    @PostMapping(value = "/mcp/insert")
    @Override
    public Result<Void> mcpInsert(@Valid @RequestBody SettingMcpDTO dto) {
        settingService.mcpInsert(dto);
        return Result.success();
    }

    @PostMapping(value = "/mcp/update")
    @Override
    public Result<Void> mcpUpdate(@Valid @RequestBody SettingMcpDTO dto) {
        settingService.mcpUpdate(dto);
        return Result.success();
    }

    @PostMapping(value = "/mcp/delete")
    @Override
    public Result<Void> mcpDelete(@RequestParam String mcpId) {
        settingService.mcpDelete(mcpId);
        return Result.success();
    }

    @PostMapping(value = "/task/list")
    @Override
    public Result<List<UserTaskVO>> taskList() {
        return Result.success(settingService.taskList());
    }

    @PostMapping(value = "/task/insert")
    @Override
    public Result<Void> taskInsert(@Valid @RequestBody SettingTaskDTO dto) {
        settingService.taskInsert(dto);
        return Result.success();
    }

    @PostMapping(value = "/task/update")
    @Override
    public Result<Void> taskUpdate(@Valid @RequestBody SettingTaskDTO dto) {
        settingService.taskUpdate(dto);
        return Result.success();
    }

    @PostMapping(value = "/task/delete")
    @Override
    public Result<Void> taskDelete(@RequestParam String taskId) {
        settingService.taskDelete(taskId);
        return Result.success();
    }

    @PostMapping(value = "/task/toggle")
    @Override
    public Result<Void> taskToggle(@RequestParam String taskId, @RequestParam Integer taskStatus) {
        settingService.taskToggle(taskId, taskStatus);
        return Result.success();
    }

}
