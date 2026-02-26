package com.dasi.domain.workspace.service;

import com.dasi.domain.workspace.model.vo.CommentVO;
import com.dasi.domain.workspace.model.vo.PlazaVO;
import com.dasi.domain.workspace.model.dto.PlazaCommentAreaDTO;
import com.dasi.domain.workspace.model.dto.PlazaCommentDTO;
import com.dasi.domain.workspace.model.dto.PlazaPageDTO;
import com.dasi.types.result.PageResult;

public interface IWorkspaceService {

    PageResult<PlazaVO> pagePlaza(PlazaPageDTO request);

    PageResult<CommentVO> plazaCommentArea(PlazaCommentAreaDTO request);

    void plazaLike(String plazaId, boolean liked);

    void plazaFavor(String plazaId, boolean favored);

    void plazaComment(PlazaCommentDTO request);

    void plazaDiscomment(String plazaId, String commentId);


}
