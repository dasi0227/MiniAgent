package com.dasi.domain.agent.repository;

import com.dasi.types.dto.request.plaza.PlazaCommentRequest;
import com.dasi.types.dto.request.plaza.PlazaListRequest;
import com.dasi.types.dto.request.plaza.PlazaPublishRequest;
import com.dasi.types.dto.request.studio.StudioCreateRequest;
import com.dasi.types.dto.request.studio.StudioUpdateRequest;
import com.dasi.types.dto.response.plaza.PlazaDetailResponse;
import com.dasi.types.dto.response.plaza.PlazaItemResponse;
import com.dasi.types.dto.response.repo.RepoItemResponse;
import com.dasi.types.dto.response.studio.StudioAgentResponse;
import com.dasi.types.dto.response.studio.StudioGenerateResponse;
import com.dasi.types.dto.result.PageResult;

import java.util.List;

public interface IAgentRepository {

    StudioGenerateResponse studioGenerate(Long userId, String taskPrompt, String strategy, List<String> mcpIdList);

    StudioAgentResponse studioCreate(Long userId, StudioCreateRequest request);

    StudioAgentResponse studioUpdate(Long userId, StudioUpdateRequest request);

    StudioAgentResponse studioDetail(Long userId, String agentId);

    List<StudioAgentResponse> studioListMine(Long userId);

    PageResult<PlazaItemResponse> plazaList(Long userId, PlazaListRequest request);

    PlazaDetailResponse plazaDetail(Long userId, String plazaId);

    void plazaPublish(Long userId, PlazaPublishRequest request);

    void plazaLike(Long userId, String plazaId);

    void plazaFavor(Long userId, String plazaId);

    void plazaComment(Long userId, PlazaCommentRequest request);

    List<RepoItemResponse> repoList(Long userId);

    void repoAdd(Long userId, String agentId);

    void repoRemove(Long userId, String agentId);

    void repoFork(Long userId, String plazaId);
}
