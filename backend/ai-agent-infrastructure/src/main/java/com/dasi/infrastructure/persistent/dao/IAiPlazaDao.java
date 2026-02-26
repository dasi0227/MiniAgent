package com.dasi.infrastructure.persistent.dao;

import com.dasi.infrastructure.persistent.po.AiPlaza;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface IAiPlazaDao {

    List<AiPlaza> page(@Param("keyword") String keyword,
                       @Param("sortBy") String sortBy,
                       @Param("sortOrder") String sortOrder,
                       @Param("offset") Integer offset,
                       @Param("size") Integer size);

    Integer count(@Param("keyword") String keyword);

    AiPlaza queryByPlazaId(@Param("plazaId") String plazaId);

    void insert(AiPlaza aiPlaza);

    void update(AiPlaza aiPlaza);

    Integer increaseLikeCount(@Param("plazaId") String plazaId, @Param("delta") Integer delta);

    Integer increaseFavorCount(@Param("plazaId") String plazaId, @Param("delta") Integer delta);

    Integer increaseCommentCount(@Param("plazaId") String plazaId, @Param("delta") Integer delta);

}
