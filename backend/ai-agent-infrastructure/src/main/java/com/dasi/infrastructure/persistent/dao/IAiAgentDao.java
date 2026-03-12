package com.dasi.infrastructure.persistent.dao;

import com.dasi.infrastructure.persistent.po.AiAgent;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface IAiAgentDao {

    AiAgent queryAgentByAgentId(@Param("agentId") String agentId);

    List<AiAgent> queryWorkAgentByUserId(@Param("userId") Long userId);

    List<AiAgent> page(@Param("keyword") String keyword,
                       @Param("agentType") String agentType,
                       @Param("offset") Integer offset,
                       @Param("size") Integer size);

    List<AiAgent> list(@Param("keyword") String keyword,
                       @Param("agentType") String agentType);

    Integer count(@Param("keyword") String keyword,
                  @Param("agentType") String agentType);

    Integer countAll();

    void insert(AiAgent aiAgent);

    void update(AiAgent aiAgent);

    void deleteByAgentId(@Param("agentId") String agentId);

    void toggle(AiAgent aiAgent);
}
