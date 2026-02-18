package com.dasi.trigger.http;

import com.dasi.api.IAgentApi;
import com.dasi.domain.agent.service.IAgentService;
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
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class AgentController implements IAgentApi {

    @Resource
    private IAgentService agentService;

    // -------------------- Studio --------------------

    @Override
    @PostMapping("/studio/generate")
    public Result<StudioGenerateResponse> studioGenerate(@Valid @RequestBody StudioGenerateRequest request) {
        return Result.success(agentService.studioGenerate(request));
    }

    @Override
    @PostMapping("/studio/create")
    public Result<StudioAgentResponse> studioCreate(@Valid @RequestBody StudioCreateRequest request) {
        return Result.success(agentService.studioCreate(request));
    }

    @Override
    @PostMapping("/studio/update")
    public Result<StudioAgentResponse> studioUpdate(@Valid @RequestBody StudioUpdateRequest request) {
        return Result.success(agentService.studioUpdate(request));
    }

    @Override
    @GetMapping("/studio/detail")
    public Result<StudioAgentResponse> studioDetail(@RequestParam("agentId") String agentId) {
        return Result.success(agentService.studioDetail(agentId));
    }

    @Override
    @GetMapping("/studio/list-mine")
    public Result<List<StudioAgentResponse>> studioListMine() {
        return Result.success(agentService.studioListMine());
    }

    // -------------------- Plaza --------------------

    @Override
    @GetMapping("/plaza/list")
    public Result<PageResult<PlazaItemResponse>> plazaList(@Valid PlazaListRequest request) {
        return Result.success(agentService.plazaList(request));
    }

    @Override
    @GetMapping("/plaza/detail")
    public Result<PlazaDetailResponse> plazaDetail(@RequestParam("plazaId") String plazaId) {
        return Result.success(agentService.plazaDetail(plazaId));
    }

    @Override
    @PostMapping("/plaza/publish")
    public Result<Void> plazaPublish(@Valid @RequestBody PlazaPublishRequest request) {
        agentService.plazaPublish(request);
        return Result.success();
    }

    @Override
    @PostMapping("/plaza/like")
    public Result<Void> plazaLike(@Valid @RequestBody PlazaActionRequest request) {
        agentService.plazaLike(request);
        return Result.success();
    }

    @Override
    @PostMapping("/plaza/favor")
    public Result<Void> plazaFavor(@Valid @RequestBody PlazaActionRequest request) {
        agentService.plazaFavor(request);
        return Result.success();
    }

    @Override
    @PostMapping("/plaza/comment")
    public Result<Void> plazaComment(@Valid @RequestBody PlazaCommentRequest request) {
        agentService.plazaComment(request);
        return Result.success();
    }

    // -------------------- Repo --------------------

    @Override
    @GetMapping("/repo/list")
    public Result<List<RepoItemResponse>> repoList() {
        return Result.success(agentService.repoList());
    }

    @Override
    @PostMapping("/repo/add")
    public Result<Void> repoAdd(@Valid @RequestBody RepoActionRequest request) {
        agentService.repoAdd(request);
        return Result.success();
    }

    @Override
    @PostMapping("/repo/remove")
    public Result<Void> repoRemove(@Valid @RequestBody RepoActionRequest request) {
        agentService.repoRemove(request);
        return Result.success();
    }

    @Override
    @PostMapping("/repo/fork")
    public Result<Void> repoFork(@Valid @RequestBody RepoForkRequest request) {
        agentService.repoFork(request);
        return Result.success();
    }
}
