package com.dasi.domain.workspace.service;

import com.dasi.domain.workspace.model.dto.*;
import com.dasi.domain.workspace.model.vo.CommentVO;
import com.dasi.domain.workspace.model.vo.AgentVO;
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

    void agentUpdateBase(AgentUpdateBaseDTO dto);

    void agentUpdateModel(AgentUpdateModelDTO dto);

    void agentUpdateMcp(AgentUpdateMcpDTO dto);

    void agentUpdateUserPrompt(AgentUpdateUserPromptDTO dto);

    void agentUpdateSystemPrompt(AgentUpdateSystemPromptDTO dto);

    TemplateVO agentTemplate(String templateId);

    AgentVO agentDetail(String agentId);

    void agentDelete(String agentId);

    void agentCreate(AgentCreateDTO dto);

    void executePlazaLike(String plazaId, boolean liked);

    void executePlazaFavor(String plazaId, boolean favored);

    void executePlazaComment(PlazaCommentDTO dto);

    void executePlazaDiscomment(String plazaId, String commentId);

    void executePlazaDelete(String plazaId);

    void executeAgentPublish(AgentPublishDTO dto);

    void executeAgentFork(String templateId);

    void executeAgentDelete(String agentId);
}
