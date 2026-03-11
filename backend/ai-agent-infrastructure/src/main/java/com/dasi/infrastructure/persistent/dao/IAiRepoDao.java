package com.dasi.infrastructure.persistent.dao;

import com.dasi.infrastructure.persistent.po.AiRepo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

@Mapper
public interface IAiRepoDao {

    List<AiRepo> listByUserId(@Param("userId") Long userId);

    AiRepo queryByUserIdAndAgentIdAndRepoType(@Param("userId") Long userId,
                                              @Param("agentId") String agentId,
                                              @Param("repoType") String repoType);

    AiRepo queryByUserIdAndTemplateIdAndRepoType(@Param("userId") Long userId,
                                                 @Param("templateId") String templateId,
                                                 @Param("repoType") String repoType);

    Integer insert(AiRepo aiRepo);

    Integer update(AiRepo aiRepo);

    Integer deleteByAgentId(@Param("agentId") String agentId);

    Integer deleteByTemplateId(@Param("templateId") String templateId);

    Integer deleteByUserIdAndTemplateIdAndRepoType(@Param("userId") Long userId,
                                                    @Param("templateId") String templateId,
                                                    @Param("repoType") String repoType);

    Set<String> queryForkedByUserIdAndTemplateIdList(@Param("userId") Long userId,
                                                     @Param("templateIdList") List<String> templateIdList);
}
