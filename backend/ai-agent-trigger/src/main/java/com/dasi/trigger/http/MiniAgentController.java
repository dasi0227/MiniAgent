package com.dasi.trigger.http;

import com.dasi.domain.miniagent.model.vo.CommentVO;
import com.dasi.domain.miniagent.model.vo.PlazaVO;
import com.dasi.domain.miniagent.service.IMiniAgentService;
import com.dasi.types.dto.request.plaza.PlazaCommentAreaRequest;
import com.dasi.types.dto.request.plaza.PlazaCommentRequest;
import com.dasi.types.dto.request.plaza.PlazaPageRequest;
import com.dasi.types.dto.result.PageResult;
import com.dasi.types.dto.result.Result;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/miniagent")
public class MiniAgentController {

    @Resource
    private IMiniAgentService miniAgentService;

    @PostMapping("/plaza/page")
    public Result<PageResult<PlazaVO>> plazaPage(@Valid @RequestBody PlazaPageRequest request) {
        return Result.success(miniAgentService.pagePlaza(request));
    }

    @PostMapping("/plaza/like")
    public Result<Void> plazaLike(@NotBlank @RequestParam String plazaId) {
        miniAgentService.plazaLike(plazaId, true);
        return Result.success();
    }

    @PostMapping("/plaza/dislike")
    public Result<Void> plazaDislike(@NotBlank @RequestParam String plazaId) {
        miniAgentService.plazaLike(plazaId, false);
        return Result.success();
    }

    @PostMapping("/plaza/comment")
    public Result<Void> plazaComment(@Valid @RequestBody PlazaCommentRequest request) {
        miniAgentService.plazaComment(request);
        return Result.success();
    }

    @PostMapping("/plaza/discomment")
    public Result<Void> plazaDiscomment(@NotBlank @RequestParam String commentId,
                                        @NotBlank @RequestParam String plazaId) {
        miniAgentService.plazaDiscomment(plazaId, commentId);
        return Result.success();
    }

    @PostMapping("/plaza/comment-area")
    public Result<PageResult<CommentVO>> plazaCommentArea(@Valid @RequestBody PlazaCommentAreaRequest request) {
        return Result.success(miniAgentService.plazaCommentArea(request));
    }

    @PostMapping("/plaza/favor")
    public Result<Void> plazaFavor(@NotBlank @RequestParam String plazaId) {
        miniAgentService.plazaFavor(plazaId, true);
        return Result.success();
    }

    @PostMapping("/plaza/disfavor")
    public Result<Void> plazaDisfavor(@NotBlank @RequestParam String plazaId) {
        miniAgentService.plazaFavor(plazaId, false);
        return Result.success();
    }

}
