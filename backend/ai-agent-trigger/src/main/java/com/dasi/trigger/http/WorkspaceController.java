package com.dasi.trigger.http;

import com.dasi.domain.workspace.model.dto.AgentPublishDTO;
import com.dasi.domain.workspace.model.dto.PlazaCommentAreaDTO;
import com.dasi.domain.workspace.model.dto.PlazaCommentDTO;
import com.dasi.domain.workspace.model.dto.PlazaPageDTO;
import com.dasi.domain.workspace.model.vo.CommentVO;
import com.dasi.domain.workspace.model.vo.PlazaVO;
import com.dasi.domain.workspace.model.vo.RepoVO;
import com.dasi.domain.workspace.model.vo.TemplateVO;
import com.dasi.domain.workspace.service.IWorkspaceService;
import com.dasi.types.result.PageResult;
import com.dasi.types.result.Result;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/workspace")
public class WorkspaceController {

    @Resource
    private IWorkspaceService workspaceService;

    @PostMapping("/plaza/page")
    public Result<PageResult<PlazaVO>> plazaPage(@Valid @RequestBody PlazaPageDTO dto) {
        return Result.success(workspaceService.pagePlaza(dto));
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
    public Result<Void> plazaComment(@Valid @RequestBody PlazaCommentDTO dto) {
        workspaceService.plazaComment(dto);
        return Result.success();
    }

    @PostMapping("/plaza/discomment")
    public Result<Void> plazaDiscomment(@NotBlank @RequestParam String commentId,
                                        @NotBlank @RequestParam String plazaId) {
        workspaceService.plazaDiscomment(plazaId, commentId);
        return Result.success();
    }

    @PostMapping("/plaza/comment-area")
    public Result<PageResult<CommentVO>> plazaCommentArea(@Valid @RequestBody PlazaCommentAreaDTO dto) {
        return Result.success(workspaceService.plazaCommentArea(dto));
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

    @PostMapping("/plaza/delete")
    public Result<Void> plazaDelete(@NotBlank @RequestParam String plazaId) {
        workspaceService.plazaDelete(plazaId);
        return Result.success();
    }

    @PostMapping("/repo/map")
    public Result<Map<String, List<RepoVO>>> repoMap() {
        return Result.success(workspaceService.repoMap());
    }

    @PostMapping("/agent/publish")
    public Result<Void> agentPublish(@Valid @RequestBody AgentPublishDTO dto) {
        workspaceService.agentPublish(dto);
        return Result.success();
    }

    @PostMapping("/agent/template")
    public Result<TemplateVO> agentTemplate(@NotBlank @RequestParam String templateId) {
        return Result.success(workspaceService.agentTemplate(templateId));
    }

    @PostMapping("/agent/delete")
    public Result<Void> agentDelete(@NotBlank @RequestParam String agentId) {
        workspaceService.agentDelete(agentId);
        return Result.success();
    }

}
