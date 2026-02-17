package com.dasi.infrastructure.persistent.dao;

import com.dasi.infrastructure.persistent.po.AiPlaza;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface IAiPlazaDao {

    List<AiPlaza> list(@Param("titleKeyword") String titleKeyword,
                       @Param("offset") Integer offset,
                       @Param("size") Integer size);

    Integer count(@Param("titleKeyword") String titleKeyword);

    AiPlaza queryByPlazaId(@Param("plazaId") String plazaId);

    AiPlaza queryByAgentIdAndUserId(@Param("agentId") String agentId, @Param("userId") Long userId);

    void insert(AiPlaza aiPlaza);

    void increaseLikeCount(@Param("plazaId") String plazaId, @Param("delta") Integer delta);

    void increaseFavorCount(@Param("plazaId") String plazaId, @Param("delta") Integer delta);

    void increaseCommentCount(@Param("plazaId") String plazaId, @Param("delta") Integer delta);
}
