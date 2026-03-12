package com.dasi.infrastructure.persistent.dao;

import com.dasi.infrastructure.persistent.po.AiMcp;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface IAiMcpDao {

    AiMcp queryByMcpId(@Param("mcpId") String mcpId);

    List<AiMcp> queryMcpByUserId(@Param("userId") Long userId);

    List<AiMcp> queryByMcpIdList(@Param("mcpIdList") List<String> mcpIdList);

    List<AiMcp> page(@Param("keyword") String keyword,
                     @Param("offset") Integer offset,
                     @Param("size") Integer size);

    Integer count(@Param("keyword") String keyword);

    Integer countAll();

    void insert(AiMcp aiMcp);

    void update(AiMcp aiMcp);

    void deleteByMcpId(@Param("mcpId") String mcpId);

    List<AiMcp> listUserMcp(@Param("keyword") String keyword, @Param("userId") Long userId);

}
