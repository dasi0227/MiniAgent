package com.dasi.infrastructure.persistent.dao;

import com.dasi.infrastructure.persistent.po.AiClient;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface IAiClientDao {
    AiClient queryByClientId(@Param("clientId") String clientId);

    List<AiClient> queryChatClientByUserId(@Param("userId") Long userId);

    AiClient queryChatClientByModelIdAndUserId(@Param("modelId") String modelId, @Param("clientFrom") Long clientFrom);

    List<AiClient> queryWorkClientList();

    List<AiClient> page(@Param("keyword") String keyword,
                        @Param("modelId") String modelId,
                        @Param("clientType") String clientType,
                        @Param("clientRole") String clientRole,
                        @Param("offset") Integer offset,
                        @Param("size") Integer size);

    Integer count(@Param("keyword") String keyword,
                  @Param("modelId") String modelId,
                  @Param("clientType") String clientType,
                  @Param("clientRole") String clientRole);

    Integer countAll();

    void insert(AiClient aiClient);

    void update(AiClient aiClient);

    Integer deleteByClientId(@Param("clientId") String clientId);

    void toggle(AiClient aiClient);

    List<String> queryClientIdByModelId(@Param("modelId") String modelId);

}
