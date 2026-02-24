package com.dasi.infrastructure.persistent.dao;

import com.dasi.infrastructure.persistent.po.AiPlazaLike;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface IAiPlazaLikeDao {

    AiPlazaLike queryByPlazaIdAndUserId(@Param("plazaId") String plazaId, @Param("userId") Long userId);

    List<String> queryPlazaIdListByUserIdAndPlazaIdList(@Param("userId") Long userId, @Param("plazaIdList") List<String> plazaIdList);

    Integer insert(AiPlazaLike aiPlazaLike);

    Integer delete(@Param("plazaId") String plazaId, @Param("userId") Long userId);

}
