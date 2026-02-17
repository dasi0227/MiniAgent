package com.dasi.domain.studio.service;

import com.dasi.domain.studio.repository.IStudioRepository;
import com.dasi.domain.util.jwt.AuthContext;
import com.dasi.types.dto.request.studio.*;
import com.dasi.types.dto.response.studio.StudioAgentResponse;
import com.dasi.types.dto.response.studio.StudioGenerateResponse;
import com.dasi.types.exception.AuthException;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StudioService implements IStudioService {

    @Resource
    private IStudioRepository studioRepository;

    @Resource
    private AuthContext authContext;

    @Override
    public StudioGenerateResponse generate(StudioGenerateRequest request) {
        return studioRepository.generate(requiredUserId(), request.getTaskPrompt(), request.getStrategy(), request.getMcpIdList());
    }

    @Override
    public StudioAgentResponse create(StudioCreateRequest request) {
        return studioRepository.create(requiredUserId(), request);
    }

    @Override
    public StudioAgentResponse update(StudioUpdateRequest request) {
        return studioRepository.update(requiredUserId(), request);
    }

    @Override
    public StudioAgentResponse detail(String agentId) {
        return studioRepository.detail(requiredUserId(), agentId);
    }

    @Override
    public List<StudioAgentResponse> listMine() {
        return studioRepository.listMine(requiredUserId());
    }

    private Long requiredUserId() {
        Long userId = authContext.getId();
        if (userId == null) {
            throw new AuthException("未登录");
        }
        return userId;
    }
}
