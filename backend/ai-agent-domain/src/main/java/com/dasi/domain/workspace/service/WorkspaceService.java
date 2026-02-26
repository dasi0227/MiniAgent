package com.dasi.domain.workspace.service;

import com.dasi.domain.workspace.model.dto.PlazaCommentAreaDTO;
import com.dasi.domain.workspace.model.dto.PlazaCommentDTO;
import com.dasi.domain.workspace.model.dto.PlazaPageDTO;
import com.dasi.domain.workspace.model.vo.CommentVO;
import com.dasi.domain.workspace.model.vo.PlazaVO;
import com.dasi.domain.workspace.model.vo.TemplateVO;
import com.dasi.domain.workspace.repository.IWorkspaceRepository;
import com.dasi.types.result.PageResult;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public void agentPublish(String agentId) {
        // workspace template 链路尚在建设中，先保持接口可用。
    }

    @Override
    public TemplateVO agentTemplate(String templateId) {
        // workspace template 链路尚在建设中，先返回空结构。
        return TemplateVO.builder().build();
    }

}
