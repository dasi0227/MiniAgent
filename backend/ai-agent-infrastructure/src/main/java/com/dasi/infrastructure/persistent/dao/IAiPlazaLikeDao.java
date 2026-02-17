package com.dasi.infrastructure.persistent.dao;

import com.dasi.infrastructure.persistent.po.AiPlazaLike;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface IAiPlazaLikeDao {

    AiPlazaLike queryByPlazaIdAndUserId(@Param("plazaId") String plazaId, @Param("userId") Long userId);

    void insert(AiPlazaLike aiPlazaLike);

    void deleteByPlazaIdAndUserId(@Param("plazaId") String plazaId, @Param("userId") Long userId);
}
