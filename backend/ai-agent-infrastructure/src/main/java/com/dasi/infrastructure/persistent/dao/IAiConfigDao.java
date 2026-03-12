package com.dasi.infrastructure.persistent.dao;

import com.dasi.infrastructure.persistent.po.AiConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface IAiConfigDao {

    List<AiConfig> queryByClientIdAndConfigType(@Param("clientId") String clientId,
                                                @Param("configType") String configType);

    List<AiConfig> queryByClientId(@Param("clientId") String clientId);

    List<String> queryClientIdListByConfigType(@Param("configType") String configType);

    List<String> queryClientIdListByConfigTypeAndValue(@Param("configType") String configType,
                                                       @Param("configValue") String configValue);

    List<AiConfig> list(@Param("keyword") String keyword,
                        @Param("configType") String configType);

    Integer count(@Param("keyword") String keyword,
                  @Param("configType") String configType);

    Integer countAll();

    AiConfig queryByUniqueKey(@Param("clientId") String clientId,
                              @Param("configType") String configType,
                              @Param("configValue") String configValue);

    void insert(AiConfig aiConfig);

    void update(AiConfig aiConfig);

    void delete(@Param("id") Long id);

    Integer deleteByClientId(@Param("clientId") String clientId);

    Integer deleteByClientIdAndConfigType(@Param("clientId") String clientId,
                                          @Param("configType") String configType);

    void toggle(@Param("id") Long id, @Param("status") Integer status);

}
