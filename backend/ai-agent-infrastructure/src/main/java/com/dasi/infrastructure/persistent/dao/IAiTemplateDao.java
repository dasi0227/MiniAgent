package com.dasi.infrastructure.persistent.dao;

import com.dasi.infrastructure.persistent.po.AiTemplate;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface IAiTemplateDao {

    AiTemplate queryByTemplateId(@Param("templateId") String templateId);

    Integer insert(AiTemplate aiTemplate);

    Integer update(AiTemplate aiTemplate);

    Integer deleteByTemplateIdAndUserId(@Param("templateId") String templateId, @Param("userId") Long userId);

}
