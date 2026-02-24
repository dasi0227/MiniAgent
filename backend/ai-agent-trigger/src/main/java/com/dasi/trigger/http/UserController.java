package com.dasi.trigger.http;

import com.dasi.api.IUserApi;
import com.dasi.domain.user.model.vo.*;
import com.dasi.domain.user.service.auth.IAuthService;
import com.dasi.domain.user.service.query.IQueryService;
import com.dasi.domain.user.service.setting.ISettingService;
import com.dasi.types.dto.request.user.AuthRequest;
import com.dasi.types.dto.request.user.ProfileEditRequest;
import com.dasi.types.dto.request.user.SettingApiRequest;
import com.dasi.types.dto.request.user.SettingMcpRequest;
import com.dasi.types.dto.result.Result;
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

    @PostMapping("/query/chat-client-list")
    @Override
    public Result<List<ChatClientVO>> queryChatClientVOList() {
        return Result.success(queryService.queryChatClientVOList());
    }

    @PostMapping("/query/chat-mcp-list")
    @Override
    public Result<List<ChatMcpVO>> queryChatMcpVOList() {
        return Result.success(queryService.queryChatMcpVOList());
    }

    @PostMapping("/query/chat-rag-list")
    @Override
    public Result<List<ChatRagVO>> queryRagVOList() {
        return Result.success(queryService.queryRagVOList());
    }

    @PostMapping("/query/agent-list")
    @Override
    public Result<List<WorkAgentVO>> queryWorkAgentVOList() {
        return Result.success(queryService.queryWorkAgentVOList());
    }

    @PostMapping("/auth/login")
    @Override
    public Result<AuthVO> login(@Valid @RequestBody AuthRequest request) {
        return Result.success(authService.login(request));
    }

    @PostMapping("/auth/register")
    @Override
    public Result<AuthVO> register(@Valid @RequestBody AuthRequest request) {
        return Result.success(authService.register(request));
    }

    @PostMapping("/profile/query")
    @Override
    public Result<AuthVO> profileQuery() {
        return Result.success(settingService.profileQuery());
    }

    @PostMapping(value = "/profile/edit", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Override
    public Result<AuthVO> profileEdit(@Valid @RequestPart("profile") ProfileEditRequest request,
                                      @RequestPart(value = "avatar", required = false) MultipartFile avatar) {
        return Result.success(settingService.profileEdit(request, avatar));
    }

    @PostMapping(value = "/api/list")
    @Override
    public Result<List<UserApiVO>> apiList(@RequestParam(required=false, defaultValue = "") String keyword) {
        return Result.success(settingService.apiList(keyword));
    }

    @PostMapping(value = "/api/insert")
    @Override
    public Result<Void> apiInsert(@Valid @RequestBody SettingApiRequest request) {
        settingService.apiInsert(request);
        return Result.success();
    }

    @PostMapping(value = "/api/update")
    @Override
    public Result<Void> apiUpdate(@Valid @RequestBody SettingApiRequest request) {
        settingService.apiUpdate(request);
        return Result.success();
    }

    @PostMapping(value = "/api/delete")
    @Override
    public Result<Void> apiDelete(@RequestParam Long id) {
        settingService.apiDelete(id);
        return Result.success();
    }

    @PostMapping(value = "/mcp/list")
    @Override
    public Result<List<UserMcpVO>> mcpList(@RequestParam(required = false, defaultValue = "") String keyword) {
        return Result.success(settingService.mcpList(keyword));
    }

    @PostMapping(value = "/mcp/insert")
    @Override
    public Result<Void> mcpInsert(@Valid @RequestBody SettingMcpRequest request) {
        settingService.mcpInsert(request);
        return Result.success();
    }

    @PostMapping(value = "/mcp/update")
    @Override
    public Result<Void> mcpUpdate(@Valid @RequestBody SettingMcpRequest request) {
        settingService.mcpUpdate(request);
        return Result.success();
    }

    @PostMapping(value = "/mcp/delete")
    @Override
    public Result<Void> mcpDelete(@RequestParam Long id) {
        settingService.mcpDelete(id);
        return Result.success();
    }

}
