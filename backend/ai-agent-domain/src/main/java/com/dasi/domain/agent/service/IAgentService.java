package com.dasi.domain.agent.service;

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

import java.util.List;

public interface IAgentService {

    StudioGenerateResponse studioGenerate(StudioGenerateRequest request);

    StudioAgentResponse studioCreate(StudioCreateRequest request);

    StudioAgentResponse studioUpdate(StudioUpdateRequest request);

    StudioAgentResponse studioDetail(String agentId);

    List<StudioAgentResponse> studioListMine();

    PageResult<PlazaItemResponse> plazaList(PlazaListRequest request);

    PlazaDetailResponse plazaDetail(String plazaId);

    void plazaPublish(PlazaPublishRequest request);

    void plazaLike(PlazaActionRequest request);

    void plazaFavor(PlazaActionRequest request);

    void plazaComment(PlazaCommentRequest request);

    List<RepoItemResponse> repoList();

    void repoAdd(RepoActionRequest request);

    void repoRemove(RepoActionRequest request);

    void repoFork(RepoForkRequest request);
}
