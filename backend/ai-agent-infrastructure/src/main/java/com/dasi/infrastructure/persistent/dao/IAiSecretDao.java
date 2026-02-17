package com.dasi.infrastructure.persistent.dao;

import com.dasi.infrastructure.persistent.po.AiSecret;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface IAiSecretDao {

    AiSecret queryByUserRefAndKey(@Param("userId") Long userId,
                                  @Param("refType") String refType,
                                  @Param("refId") String refId,
                                  @Param("secretKey") String secretKey);

    List<AiSecret> queryByUserRef(@Param("userId") Long userId,
                                  @Param("refType") String refType,
                                  @Param("refId") String refId);

    void insert(AiSecret aiSecret);

    void update(AiSecret aiSecret);

    void deleteByUserRef(@Param("userId") Long userId,
                         @Param("refType") String refType,
                         @Param("refId") String refId);
}
