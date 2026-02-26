package com.dasi.domain.workspace.service;

import com.dasi.domain.workspace.model.dto.PlazaCommentAreaDTO;
import com.dasi.domain.workspace.model.dto.PlazaCommentDTO;
import com.dasi.domain.workspace.model.dto.PlazaPageDTO;
import com.dasi.domain.workspace.model.vo.CommentVO;
import com.dasi.domain.workspace.model.vo.PlazaVO;
import com.dasi.domain.workspace.model.vo.TemplateVO;
import com.dasi.types.result.PageResult;

public interface IWorkspaceService {

    PageResult<PlazaVO> pagePlaza(PlazaPageDTO dto);

    PageResult<CommentVO> plazaCommentArea(PlazaCommentAreaDTO dto);

    void plazaLike(String plazaId, boolean liked);

    void plazaFavor(String plazaId, boolean favored);

    void plazaComment(PlazaCommentDTO dto);

    void plazaDiscomment(String plazaId, String commentId);

    void agentPublish(String agentId);

    TemplateVO agentTemplate(String templateId);


}
