package com.dasi.domain.user.service;

import com.dasi.domain.user.repository.IUserMcpRepository;
import com.dasi.domain.util.jwt.AuthContext;
import com.dasi.types.dto.request.user.mcp.*;
import com.dasi.types.dto.response.user.mcp.UserMcpItemResponse;
import com.dasi.types.exception.AuthException;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class UserMcpService implements IUserMcpService {

    @Resource
    private IUserMcpRepository userMcpRepository;

    @Resource
    private AuthContext authContext;

    @Override
    public List<UserMcpItemResponse> list(UserMcpListRequest request) {
        return userMcpRepository.list(requiredUserId(), request);
    }

    @Override
    public void insert(UserMcpManageRequest request) {
        if (request.getMcpChat() == null) {
            request.setMcpChat(1);
        }
        userMcpRepository.insert(requiredUserId(), request);
    }

    @Override
    public void update(UserMcpManageRequest request) {
        userMcpRepository.update(requiredUserId(), request);
    }

    @Override
    public void delete(Long id) {
        userMcpRepository.delete(requiredUserId(), id);
    }

    @Override
    public void toggle(Long id, Integer mcpChat) {
        userMcpRepository.toggle(requiredUserId(), id, mcpChat);
    }

    @Override
    public Map<String, Object> test(UserMcpTestRequest request) {
        return userMcpRepository.test(requiredUserId(), request.getMcpId());
    }

    @Override
    public Map<String, Object> export(UserMcpExportRequest request) {
        return userMcpRepository.export(requiredUserId(), request.getMcpId());
    }

    private Long requiredUserId() {
        Long userId = authContext.getId();
        if (userId == null) {
            throw new AuthException("未登录");
        }
        return userId;
    }
}
