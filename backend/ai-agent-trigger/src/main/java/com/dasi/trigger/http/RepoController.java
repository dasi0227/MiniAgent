package com.dasi.trigger.http;

import com.dasi.api.IRepoApi;
import com.dasi.domain.repo.service.IRepoService;
import com.dasi.types.dto.request.repo.RepoActionRequest;
import com.dasi.types.dto.request.repo.RepoForkRequest;
import com.dasi.types.dto.response.repo.RepoItemResponse;
import com.dasi.types.dto.result.Result;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/repo")
public class RepoController implements IRepoApi {

    @Resource
    private IRepoService repoService;

    @Override
    @GetMapping("/list")
    public Result<List<RepoItemResponse>> list() {
        return Result.success(repoService.list());
    }

    @Override
    @PostMapping("/add")
    public Result<Void> add(@Valid @RequestBody RepoActionRequest request) {
        repoService.add(request);
        return Result.success();
    }

    @Override
    @PostMapping("/remove")
    public Result<Void> remove(@Valid @RequestBody RepoActionRequest request) {
        repoService.remove(request);
        return Result.success();
    }

    @Override
    @PostMapping("/fork")
    public Result<Void> fork(@Valid @RequestBody RepoForkRequest request) {
        repoService.fork(request);
        return Result.success();
    }
}
