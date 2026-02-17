package com.dasi.trigger.http;

import com.dasi.api.IStudioApi;
import com.dasi.domain.studio.service.IStudioService;
import com.dasi.types.dto.request.studio.*;
import com.dasi.types.dto.response.studio.StudioAgentResponse;
import com.dasi.types.dto.response.studio.StudioGenerateResponse;
import com.dasi.types.dto.result.Result;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/studio")
public class StudioController implements IStudioApi {

    @Resource
    private IStudioService studioService;

    @Override
    @PostMapping("/generate")
    public Result<StudioGenerateResponse> generate(@Valid @RequestBody StudioGenerateRequest request) {
        return Result.success(studioService.generate(request));
    }

    @Override
    @PostMapping("/create")
    public Result<StudioAgentResponse> create(@Valid @RequestBody StudioCreateRequest request) {
        return Result.success(studioService.create(request));
    }

    @Override
    @PostMapping("/update")
    public Result<StudioAgentResponse> update(@Valid @RequestBody StudioUpdateRequest request) {
        return Result.success(studioService.update(request));
    }

    @Override
    @GetMapping("/detail")
    public Result<StudioAgentResponse> detail(@RequestParam("agentId") String agentId) {
        return Result.success(studioService.detail(agentId));
    }

    @Override
    @GetMapping("/list-mine")
    public Result<List<StudioAgentResponse>> listMine() {
        return Result.success(studioService.listMine());
    }
}
