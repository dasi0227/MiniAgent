package com.dasi.trigger.http;

import com.dasi.api.IUserMcpApi;
import com.dasi.domain.user.service.IUserMcpService;
import com.dasi.types.dto.request.user.mcp.*;
import com.dasi.types.dto.response.user.mcp.UserMcpItemResponse;
import com.dasi.types.dto.result.Result;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/user/mcp")
public class UserMcpController implements IUserMcpApi {

    @Resource
    private IUserMcpService userMcpService;

    @Override
    @PostMapping("/list")
    public Result<List<UserMcpItemResponse>> list(@RequestBody(required = false) UserMcpListRequest request) {
        UserMcpListRequest safeRequest = request == null ? new UserMcpListRequest() : request;
        return Result.success(userMcpService.list(safeRequest));
    }

    @Override
    @PostMapping("/insert")
    public Result<Void> insert(@Valid @RequestBody UserMcpManageRequest request) {
        userMcpService.insert(request);
        return Result.success();
    }

    @Override
    @PostMapping("/update")
    public Result<Void> update(@Valid @RequestBody UserMcpManageRequest request) {
        userMcpService.update(request);
        return Result.success();
    }

    @Override
    @PostMapping("/delete")
    public Result<Void> delete(@RequestParam("id") Long id) {
        userMcpService.delete(id);
        return Result.success();
    }

    @Override
    @PostMapping("/toggle")
    public Result<Void> toggle(@RequestParam("id") Long id, @RequestParam("mcpChat") Integer mcpChat) {
        userMcpService.toggle(id, mcpChat);
        return Result.success();
    }

    @Override
    @PostMapping("/test")
    public Result<Map<String, Object>> test(@Valid @RequestBody UserMcpTestRequest request) {
        return Result.success(userMcpService.test(request));
    }

    @Override
    @PostMapping("/export")
    public Result<Map<String, Object>> export(@Valid @RequestBody UserMcpExportRequest request) {
        return Result.success(userMcpService.export(request));
    }
}
