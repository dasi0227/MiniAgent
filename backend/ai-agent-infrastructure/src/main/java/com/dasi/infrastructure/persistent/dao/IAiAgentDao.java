package com.dasi.infrastructure.persistent.dao;

import com.dasi.infrastructure.persistent.po.AiAgent;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface IAiAgentDao {

    AiAgent queryAgentByAgentId(@Param("agentId") String agentId);

    List<AiAgent> queryWorkAgentList(@Param("userId") Long userId);

    List<AiAgent> page(@Param("idKeyword") String idKeyword,
                       @Param("nameKeyword") String nameKeyword,
                       @Param("agentType") String agentType,
                       @Param("offset") Integer offset,
                       @Param("size") Integer size);

    List<AiAgent> list(@Param("idKeyword") String idKeyword,
                       @Param("nameKeyword") String nameKeyword,
                       @Param("agentType") String agentType);

    Integer count(@Param("idKeyword") String idKeyword,
                  @Param("nameKeyword") String nameKeyword,
                  @Param("agentType") String agentType);

    Integer countAll();

    AiAgent queryById(@Param("id") Long id);

    void insert(AiAgent aiAgent);

    void update(AiAgent aiAgent);

    void delete(@Param("id") Long id);

    void deleteByAgentId(@Param("agentId") String agentId);

    void toggle(AiAgent aiAgent);
}
