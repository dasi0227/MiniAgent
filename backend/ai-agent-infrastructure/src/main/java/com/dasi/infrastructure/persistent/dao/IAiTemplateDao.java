package com.dasi.infrastructure.persistent.dao;

import com.dasi.infrastructure.persistent.po.AiTemplate;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface IAiTemplateDao {

    AiTemplate queryByTemplateId(@Param("templateId") String templateId);

    List<AiTemplate> page(@Param("keyword") String keyword,
                          @Param("offset") Integer offset,
                          @Param("size") Integer size);

    Integer count(@Param("keyword") String keyword);

    Integer countAll();

    Integer insert(AiTemplate aiTemplate);

    Integer update(AiTemplate aiTemplate);

    Integer deleteByTemplateId(@Param("templateId") String templateId);

}
