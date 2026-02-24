package com.dasi.infrastructure.persistent.dao;

import com.dasi.infrastructure.persistent.po.AiPlazaComment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface IAiPlazaCommentDao {

    List<AiPlazaComment> listByPlazaId(@Param("plazaId") String plazaId,
                                       @Param("offset") Integer offset,
                                       @Param("size") Integer size);

    Integer countByPlazaIdAndUserId(@Param("plazaId") String plazaId, @Param("userId") Long userId);

    Integer countByPlazaId(@Param("plazaId") String plazaId);

    List<String> queryPlazaIdListByUserIdAndPlazaIdList(@Param("userId") Long userId, @Param("plazaIdList") List<String> plazaIdList);

    AiPlazaComment queryByCommentId(@Param("commentId") String commentId);

    Integer insert(AiPlazaComment aiPlazaComment);

    Integer delete(@Param("commentId") String commentId, @Param("userId") Long userId);

}
