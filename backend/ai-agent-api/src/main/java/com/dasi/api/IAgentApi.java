package com.dasi.api;

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
import com.dasi.types.dto.result.Result;

import java.util.List;

public interface IAgentApi {

    Result<StudioGenerateResponse> studioGenerate(StudioGenerateRequest request);

    Result<StudioAgentResponse> studioCreate(StudioCreateRequest request);

    Result<StudioAgentResponse> studioUpdate(StudioUpdateRequest request);

    Result<StudioAgentResponse> studioDetail(String agentId);

    Result<List<StudioAgentResponse>> studioListMine();

    Result<PageResult<PlazaItemResponse>> plazaList(PlazaListRequest request);

    Result<PlazaDetailResponse> plazaDetail(String plazaId);

    Result<Void> plazaPublish(PlazaPublishRequest request);

    Result<Void> plazaLike(PlazaActionRequest request);

    Result<Void> plazaFavor(PlazaActionRequest request);

    Result<Void> plazaComment(PlazaCommentRequest request);

    Result<List<RepoItemResponse>> repoList();

    Result<Void> repoAdd(RepoActionRequest request);

    Result<Void> repoRemove(RepoActionRequest request);

    Result<Void> repoFork(RepoForkRequest request);
}
