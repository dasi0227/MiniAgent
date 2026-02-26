package com.dasi.trigger.http;

import com.dasi.domain.workspace.model.dto.PlazaCommentAreaDTO;
import com.dasi.domain.workspace.model.dto.PlazaCommentDTO;
import com.dasi.domain.workspace.model.dto.PlazaPageDTO;
import com.dasi.domain.workspace.model.vo.CommentVO;
import com.dasi.domain.workspace.model.vo.PlazaVO;
import com.dasi.domain.workspace.service.IWorkspaceService;
import com.dasi.types.result.PageResult;
import com.dasi.types.result.Result;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/workspace")
public class WorkspaceController {

    @Resource
    private IWorkspaceService workspaceService;

    @PostMapping("/plaza/page")
    public Result<PageResult<PlazaVO>> plazaPage(@Valid @RequestBody PlazaPageDTO request) {
        return Result.success(workspaceService.pagePlaza(request));
    }

    @PostMapping("/plaza/like")
    public Result<Void> plazaLike(@NotBlank @RequestParam String plazaId) {
        workspaceService.plazaLike(plazaId, true);
        return Result.success();
    }

    @PostMapping("/plaza/dislike")
    public Result<Void> plazaDislike(@NotBlank @RequestParam String plazaId) {
        workspaceService.plazaLike(plazaId, false);
        return Result.success();
    }

    @PostMapping("/plaza/comment")
    public Result<Void> plazaComment(@Valid @RequestBody PlazaCommentDTO request) {
        workspaceService.plazaComment(request);
        return Result.success();
    }

    @PostMapping("/plaza/discomment")
    public Result<Void> plazaDiscomment(@NotBlank @RequestParam String commentId,
                                        @NotBlank @RequestParam String plazaId) {
        workspaceService.plazaDiscomment(plazaId, commentId);
        return Result.success();
    }

    @PostMapping("/plaza/comment-area")
    public Result<PageResult<CommentVO>> plazaCommentArea(@Valid @RequestBody PlazaCommentAreaDTO request) {
        return Result.success(workspaceService.plazaCommentArea(request));
    }

    @PostMapping("/plaza/favor")
    public Result<Void> plazaFavor(@NotBlank @RequestParam String plazaId) {
        workspaceService.plazaFavor(plazaId, true);
        return Result.success();
    }

    @PostMapping("/plaza/disfavor")
    public Result<Void> plazaDisfavor(@NotBlank @RequestParam String plazaId) {
        workspaceService.plazaFavor(plazaId, false);
        return Result.success();
    }

//    @PostMapping("/agent/detail")
//    public Result<AgentDetailVO> agentDetail(@NotBlank @RequestParam String agentId) {
//        return Result.success(workspaceService.agentDetail(agentId));
//    }

}
