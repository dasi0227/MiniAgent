package com.dasi.domain.miniagent.service;

import com.dasi.domain.miniagent.model.vo.CommentVO;
import com.dasi.domain.miniagent.model.vo.PlazaVO;
import com.dasi.domain.miniagent.repository.IWorkspaceRepository;
import com.dasi.types.dto.request.plaza.PlazaCommentAreaRequest;
import com.dasi.types.dto.request.plaza.PlazaCommentRequest;
import com.dasi.types.dto.request.plaza.PlazaPageRequest;
import com.dasi.types.dto.result.PageResult;
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
    public PageResult<PlazaVO> pagePlaza(PlazaPageRequest request) {
        return workspaceRepository.pagePlaza(request);
    }

    @Override
    public PageResult<CommentVO> plazaCommentArea(PlazaCommentAreaRequest request) {
        return workspaceRepository.plazaCommentArea(request);
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
    public void plazaComment(PlazaCommentRequest request) {
        workspaceRepository.plazaComment(request);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void plazaDiscomment(String plazaId, String commentId) {
        workspaceRepository.plazaDiscomment(plazaId, commentId);
    }

}
