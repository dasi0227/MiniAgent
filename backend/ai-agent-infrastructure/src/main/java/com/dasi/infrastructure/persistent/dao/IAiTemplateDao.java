package com.dasi.infrastructure.persistent.dao;

import com.dasi.infrastructure.persistent.po.AiTemplate;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface IAiTemplateDao {

    AiTemplate queryByTemplateId(@Param("templateId") String templateId);

    AiTemplate queryByAgentIdAndUserId(@Param("agentId") String agentId, @Param("userId") Long userId);

    List<AiTemplate> listByAgentIdAndUserId(@Param("agentId") String agentId, @Param("userId") Long userId);

    Integer insert(AiTemplate aiTemplate);

    Integer update(AiTemplate aiTemplate);

    Integer deleteByAgentIdAndUserId(@Param("agentId") String agentId, @Param("userId") Long userId);

}
