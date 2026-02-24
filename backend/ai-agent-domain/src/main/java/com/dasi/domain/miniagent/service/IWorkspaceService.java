package com.dasi.domain.miniagent.service;

import com.dasi.domain.miniagent.model.vo.CommentVO;
import com.dasi.domain.miniagent.model.vo.PlazaVO;
import com.dasi.types.dto.request.plaza.PlazaCommentAreaRequest;
import com.dasi.types.dto.request.plaza.PlazaCommentRequest;
import com.dasi.types.dto.request.plaza.PlazaPageRequest;
import com.dasi.types.dto.result.PageResult;

public interface IWorkspaceService {

    PageResult<PlazaVO> pagePlaza(PlazaPageRequest request);

    PageResult<CommentVO> plazaCommentArea(PlazaCommentAreaRequest request);

    void plazaLike(String plazaId, boolean liked);

    void plazaFavor(String plazaId, boolean favored);

    void plazaComment(PlazaCommentRequest request);

    void plazaDiscomment(String plazaId, String commentId);


}
