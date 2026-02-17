package com.dasi.infrastructure.persistent.dao;

import com.dasi.infrastructure.persistent.po.AiPlazaFavor;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface IAiPlazaFavorDao {

    AiPlazaFavor queryByPlazaIdAndUserId(@Param("plazaId") String plazaId, @Param("userId") Long userId);

    void insert(AiPlazaFavor aiPlazaFavor);

    void deleteByPlazaIdAndUserId(@Param("plazaId") String plazaId, @Param("userId") Long userId);
}
