package com.dasi.domain.workspace.service;

import com.dasi.domain.workspace.model.dto.AgentPublishDTO;
import com.dasi.domain.workspace.model.dto.AgentBaseUpdateDTO;
import com.dasi.domain.workspace.model.dto.PlazaCommentAreaDTO;
import com.dasi.domain.workspace.model.dto.PlazaCommentDTO;
import com.dasi.domain.workspace.model.dto.PlazaPageDTO;
import com.dasi.domain.workspace.model.dto.AgentSystemPromptUpdateDTO;
import com.dasi.domain.workspace.model.dto.AgentUserPromptUpdateDTO;
import com.dasi.domain.workspace.model.vo.CommentVO;
import com.dasi.domain.workspace.model.vo.PlazaVO;
import com.dasi.domain.workspace.model.vo.RepoVO;
import com.dasi.domain.workspace.model.vo.TemplateVO;
import com.dasi.domain.workspace.repository.IWorkspaceRepository;
import com.dasi.types.result.PageResult;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class WorkspaceService implements IWorkspaceService {

    @Resource
    private IWorkspaceRepository workspaceRepository;

    @Override
    public PageResult<PlazaVO> pagePlaza(PlazaPageDTO dto) {
        return workspaceRepository.pagePlaza(dto);
    }

    @Override
    public PageResult<CommentVO> plazaCommentArea(PlazaCommentAreaDTO dto) {
        return workspaceRepository.plazaCommentArea(dto);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void plazaLike(String plazaId, boolean liked) {
        workspaceRepository.plazaLike(plazaId, liked);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void plazaFavor(String plazaId, boolean favored) {
        workspaceRepository.plazaFavor(plazaId, favored);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void plazaComment(PlazaCommentDTO dto) {
        workspaceRepository.plazaComment(dto);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void plazaDiscomment(String plazaId, String commentId) {
        workspaceRepository.plazaDiscomment(plazaId, commentId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void plazaDelete(String plazaId) {
        workspaceRepository.plazaDelete(plazaId);
    }

    @Override
    public Map<String, List<RepoVO>> repoMap() {
        return workspaceRepository.repoMap();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void agentPublish(AgentPublishDTO dto) {
        workspaceRepository.agentPublish(dto);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void agentFork(String templateId) {
        workspaceRepository.agentFork(templateId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void agentBaseUpdate(AgentBaseUpdateDTO dto) {
        workspaceRepository.agentBaseUpdate(dto);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void agentUserPromptUpdate(AgentUserPromptUpdateDTO dto) {
        workspaceRepository.agentUserPromptUpdate(dto);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void agentSystemPromptUpdate(AgentSystemPromptUpdateDTO dto) {
        workspaceRepository.agentSystemPromptUpdate(dto);
    }

    @Override
    public TemplateVO agentTemplate(String templateId) {
        return workspaceRepository.agentTemplate(templateId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void agentDelete(String agentId) {
        workspaceRepository.agentDelete(agentId);
    }

}
