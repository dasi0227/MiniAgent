package com.dasi.infrastructure.persistent.dao;

import com.dasi.infrastructure.persistent.po.AiRepo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface IAiRepoDao {

    List<AiRepo> queryByUserId(@Param("userId") Long userId);

    List<AiRepo> queryByUserIdAndStatus(@Param("userId") Long userId, @Param("repoStatus") Integer repoStatus);

    List<String> queryAgentIdListByUserIdAndStatus(@Param("userId") Long userId, @Param("repoStatus") Integer repoStatus);

    AiRepo queryByUserIdAndAgentId(@Param("userId") Long userId, @Param("agentId") String agentId);

    void insert(AiRepo aiRepo);

    void deleteByUserIdAndAgentId(@Param("userId") Long userId, @Param("agentId") String agentId);

    void updateStatusByUserIdAndAgentId(@Param("userId") Long userId,
                                        @Param("agentId") String agentId,
                                        @Param("repoStatus") Integer repoStatus);
}
