package com.dasi.infrastructure.persistent.dao;

import com.dasi.infrastructure.persistent.po.AiMcp;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface IAiMcpDao {

    AiMcp queryByMcpId(@Param("mcpId") String mcpId);

    AiMcp queryByMcpIdWithFrom(@Param("mcpId") String mcpId, @Param("userId") Long userId);

    AiMcp queryByMcpIdByOwner(@Param("mcpId") String mcpId, @Param("userId") Long userId);

    List<AiMcp> queryChatMcpList();

    List<AiMcp> queryChatMcpListByFrom(@Param("userId") Long userId);

    List<AiMcp> queryByMcpIdList(@Param("mcpIdList") List<String> mcpIdList);

    List<AiMcp> queryByMcpIdListWithFrom(@Param("mcpIdList") List<String> mcpIdList, @Param("userId") Long userId);

    List<AiMcp> queryVisibleList(@Param("userId") Long userId,
                                 @Param("idKeyword") String idKeyword,
                                 @Param("nameKeyword") String nameKeyword);

    List<AiMcp> page(@Param("idKeyword") String idKeyword,
                     @Param("nameKeyword") String nameKeyword,
                     @Param("offset") Integer offset,
                     @Param("size") Integer size);

    Integer count(@Param("idKeyword") String idKeyword,
                  @Param("nameKeyword") String nameKeyword);

    Integer countAll();

    AiMcp queryById(@Param("id") Long id);

    AiMcp queryByIdWithOwner(@Param("id") Long id, @Param("userId") Long userId);

    void insert(AiMcp aiMcp);

    void update(AiMcp aiMcp);

    void updateByOwner(AiMcp aiMcp);

    void delete(@Param("id") Long id);

    void deleteByOwner(@Param("id") Long id, @Param("userId") Long userId);

    void toggleByOwner(AiMcp aiMcp);

}
