package com.dasi.domain.agent.service;

import com.dasi.domain.agent.repository.IAgentRepository;
import com.dasi.domain.util.jwt.AuthContext;
import com.dasi.types.dto.request.plaza.PlazaActionRequest;
import com.dasi.types.dto.request.plaza.PlazaCommentRequest;
import com.dasi.types.dto.request.plaza.PlazaListRequest;
import com.dasi.types.dto.request.plaza.PlazaPublishRequest;
import com.dasi.types.dto.request.repo.RepoActionRequest;
import com.dasi.types.dto.request.repo.RepoForkRequest;
import com.dasi.types.dto.request.studio.StudioCreateRequest;
import com.dasi.types.dto.request.studio.StudioGenerateRequest;
import com.dasi.types.dto.request.studio.StudioUpdateRequest;
import com.dasi.types.dto.response.plaza.PlazaDetailResponse;
import com.dasi.types.dto.response.plaza.PlazaItemResponse;
import com.dasi.types.dto.response.repo.RepoItemResponse;
import com.dasi.types.dto.response.studio.StudioAgentResponse;
import com.dasi.types.dto.response.studio.StudioGenerateResponse;
import com.dasi.types.dto.result.PageResult;
import com.dasi.types.exception.AuthException;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AgentService implements IAgentService {

    @Resource
    private IAgentRepository agentRepository;

    @Resource
    private AuthContext authContext;

    // -------------------- Studio --------------------

    @Override
    public StudioGenerateResponse studioGenerate(StudioGenerateRequest request) {
        return agentRepository.studioGenerate(requiredUserId(), request.getTaskPrompt(), request.getStrategy(), request.getMcpIdList());
    }

    @Override
    public StudioAgentResponse studioCreate(StudioCreateRequest request) {
        return agentRepository.studioCreate(requiredUserId(), request);
    }

    @Override
    public StudioAgentResponse studioUpdate(StudioUpdateRequest request) {
        return agentRepository.studioUpdate(requiredUserId(), request);
    }

    @Override
    public StudioAgentResponse studioDetail(String agentId) {
        return agentRepository.studioDetail(requiredUserId(), agentId);
    }

    @Override
    public List<StudioAgentResponse> studioListMine() {
        return agentRepository.studioListMine(requiredUserId());
    }

    // -------------------- Plaza --------------------

    @Override
    public PageResult<PlazaItemResponse> plazaList(PlazaListRequest request) {
        return agentRepository.plazaList(requiredUserId(), request);
    }

    @Override
    public PlazaDetailResponse plazaDetail(String plazaId) {
        return agentRepository.plazaDetail(requiredUserId(), plazaId);
    }

    @Override
    public void plazaPublish(PlazaPublishRequest request) {
        agentRepository.plazaPublish(requiredUserId(), request);
    }

    @Override
    public void plazaLike(PlazaActionRequest request) {
        agentRepository.plazaLike(requiredUserId(), request.getPlazaId());
    }

    @Override
    public void plazaFavor(PlazaActionRequest request) {
        agentRepository.plazaFavor(requiredUserId(), request.getPlazaId());
    }

    @Override
    public void plazaComment(PlazaCommentRequest request) {
        agentRepository.plazaComment(requiredUserId(), request);
    }

    @Override
    public void plazaCommentCount(PlazaActionRequest request) {
        agentRepository.plazaCommentCount(requiredUserId(), request.getPlazaId());
    }

    // -------------------- Repo --------------------

    @Override
    public List<RepoItemResponse> repoList() {
        return agentRepository.repoList(requiredUserId());
    }

    @Override
    public void repoAdd(RepoActionRequest request) {
        agentRepository.repoAdd(requiredUserId(), request.getAgentId());
    }

    @Override
    public void repoRemove(RepoActionRequest request) {
        agentRepository.repoRemove(requiredUserId(), request.getAgentId());
    }

    @Override
    public void repoFork(RepoForkRequest request) {
        agentRepository.repoFork(requiredUserId(), request.getPlazaId());
    }

    private Long requiredUserId() {
        Long userId = authContext.getId();
        if (userId == null) {
            throw new AuthException("未登录");
        }
        return userId;
    }
}
