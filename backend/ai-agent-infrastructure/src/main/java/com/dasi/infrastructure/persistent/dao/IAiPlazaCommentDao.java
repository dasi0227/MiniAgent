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

    Integer countByPlazaId(@Param("plazaId") String plazaId);

    void insert(AiPlazaComment aiPlazaComment);
}
