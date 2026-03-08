package com.dasi.domain.workspace.service;

import com.dasi.domain.workspace.model.dto.*;
import com.dasi.domain.workspace.model.vo.CommentVO;
import com.dasi.domain.workspace.model.vo.PlazaVO;
import com.dasi.domain.workspace.model.vo.RepoVO;
import com.dasi.domain.workspace.model.vo.TemplateVO;
import com.dasi.types.result.PageResult;

import java.util.List;
import java.util.Map;

public interface IWorkspaceService {

    PageResult<PlazaVO> pagePlaza(PlazaPageDTO dto);

    PageResult<CommentVO> plazaCommentArea(PlazaCommentAreaDTO dto);

    void plazaLike(String plazaId, boolean liked);

    void plazaFavor(String plazaId, boolean favored);

    void plazaComment(PlazaCommentDTO dto);

    void plazaDiscomment(String plazaId, String commentId);

    void plazaDelete(String plazaId);

    Map<String, List<RepoVO>> repoMap();

    void agentPublish(AgentPublishDTO dto);

    void agentFork(String templateId);

    void agentBaseUpdate(AgentBaseUpdateDTO dto);

    void agentUserPromptUpdate(AgentUserPromptUpdateDTO dto);

    void agentSystemPromptUpdate(AgentSystemPromptUpdateDTO dto);

    TemplateVO agentTemplate(String templateId);

    void agentDelete(String agentId);

    void agentCreate(AgentCreateDTO dto);
}
