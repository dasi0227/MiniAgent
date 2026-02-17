package com.dasi.api;

import com.dasi.types.dto.request.user.mcp.*;
import com.dasi.types.dto.response.user.mcp.UserMcpItemResponse;
import com.dasi.types.dto.result.Result;

import java.util.List;
import java.util.Map;

public interface IUserMcpApi {

    Result<List<UserMcpItemResponse>> list(UserMcpListRequest request);

    Result<Void> insert(UserMcpManageRequest request);

    Result<Void> update(UserMcpManageRequest request);

    Result<Void> delete(Long id);

    Result<Void> toggle(Long id, Integer mcpChat);

    Result<Map<String, Object>> test(UserMcpTestRequest request);

    Result<Map<String, Object>> export(UserMcpExportRequest request);
}
