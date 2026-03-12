package com.dasi.infrastructure.persistent.dao;

import com.dasi.infrastructure.persistent.po.AiAdvisor;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface IAiAdvisorDao {

    AiAdvisor queryByAdvisorId(@Param("advisorId") String advisorId);

    List<AiAdvisor> page(@Param("keyword") String keyword,
                         @Param("offset") Integer offset,
                         @Param("size") Integer size);

    Integer count(@Param("keyword") String keyword);

    Integer countAll();

    void insert(AiAdvisor aiAdvisor);

    void update(AiAdvisor aiAdvisor);

    void delete(@Param("id") Long id);

}
