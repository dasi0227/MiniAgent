package com.dasi.infrastructure.persistent.dao;

import com.dasi.infrastructure.persistent.po.AiPlazaFavor;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface IAiPlazaFavorDao {

    AiPlazaFavor queryByPlazaIdAndUserId(@Param("plazaId") String plazaId, @Param("userId") Long userId);

    List<String> queryPlazaIdListByUserIdAndPlazaIdList(@Param("userId") Long userId, @Param("plazaIdList") List<String> plazaIdList);

    Integer insert(AiPlazaFavor aiPlazaFavor);

    Integer delete(@Param("plazaId") String plazaId, @Param("userId") Long userId);

}
