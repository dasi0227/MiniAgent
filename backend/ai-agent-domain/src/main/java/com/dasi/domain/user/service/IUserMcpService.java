package com.dasi.domain.user.service;

import com.dasi.types.dto.request.user.mcp.*;
import com.dasi.types.dto.response.user.mcp.UserMcpItemResponse;

import java.util.List;
import java.util.Map;

public interface IUserMcpService {

    List<UserMcpItemResponse> list(UserMcpListRequest request);

    void insert(UserMcpManageRequest request);

    void update(UserMcpManageRequest request);

    void delete(Long id);

    void toggle(Long id, Integer mcpChat);

    Map<String, Object> test(UserMcpTestRequest request);

    Map<String, Object> export(UserMcpExportRequest request);
}
