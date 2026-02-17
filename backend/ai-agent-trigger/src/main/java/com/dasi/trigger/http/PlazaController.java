package com.dasi.trigger.http;

import com.dasi.api.IPlazaApi;
import com.dasi.domain.plaza.service.IPlazaService;
import com.dasi.types.dto.request.plaza.*;
import com.dasi.types.dto.response.plaza.PlazaDetailResponse;
import com.dasi.types.dto.response.plaza.PlazaItemResponse;
import com.dasi.types.dto.result.PageResult;
import com.dasi.types.dto.result.Result;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/plaza")
public class PlazaController implements IPlazaApi {

    @Resource
    private IPlazaService plazaService;

    @Override
    @GetMapping("/list")
    public Result<PageResult<PlazaItemResponse>> list(@Valid PlazaListRequest request) {
        return Result.success(plazaService.list(request));
    }

    @Override
    @GetMapping("/detail")
    public Result<PlazaDetailResponse> detail(@RequestParam("plazaId") String plazaId) {
        return Result.success(plazaService.detail(plazaId));
    }

    @Override
    @PostMapping("/publish")
    public Result<Void> publish(@Valid @RequestBody PlazaPublishRequest request) {
        plazaService.publish(request);
        return Result.success();
    }

    @Override
    @PostMapping("/like")
    public Result<Void> like(@Valid @RequestBody PlazaActionRequest request) {
        plazaService.like(request);
        return Result.success();
    }

    @Override
    @PostMapping("/favor")
    public Result<Void> favor(@Valid @RequestBody PlazaActionRequest request) {
        plazaService.favor(request);
        return Result.success();
    }

    @Override
    @PostMapping("/comment")
    public Result<Void> comment(@Valid @RequestBody PlazaCommentRequest request) {
        plazaService.comment(request);
        return Result.success();
    }
}
