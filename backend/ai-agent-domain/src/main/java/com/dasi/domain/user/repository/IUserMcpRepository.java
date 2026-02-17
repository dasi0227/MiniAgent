package com.dasi.domain.user.repository;

import com.dasi.types.dto.request.user.mcp.UserMcpListRequest;
import com.dasi.types.dto.request.user.mcp.UserMcpManageRequest;
import com.dasi.types.dto.response.user.mcp.UserMcpItemResponse;

import java.util.List;
import java.util.Map;

public interface IUserMcpRepository {

    List<UserMcpItemResponse> list(Long userId, UserMcpListRequest request);

    UserMcpItemResponse queryByMcpId(Long userId, String mcpId);

    UserMcpItemResponse queryById(Long userId, Long id);

    void insert(Long userId, UserMcpManageRequest request);

    void update(Long userId, UserMcpManageRequest request);

    void delete(Long userId, Long id);

    void toggle(Long userId, Long id, Integer mcpChat);

    Map<String, Object> test(Long userId, String mcpId);

    Map<String, Object> export(Long userId, String mcpId);

    Map<String, String> querySecretPlainMap(Long userId, String mcpId);
}
