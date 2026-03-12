package com.dasi.infrastructure.persistent.dao;

import com.dasi.infrastructure.persistent.po.AiApi;
import com.dasi.infrastructure.persistent.po.AiApiModel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface IAiApiDao {

    AiApi queryByApiId(@Param("apiId") String apiId);

    List<AiApi> page(@Param("keyword") String keyword,
                     @Param("offset") Integer offset,
                     @Param("size") Integer size);

    Integer count(@Param("keyword") String keyword);

    Integer countAll();

    void insert(AiApi aiApi);

    void update(AiApi aiApi);

    void deleteByApiId(@Param("apiId") String apiId);

    List<String> listApiId();

    List<AiApiModel> listUserApi(@Param("keyword") String keyword, @Param("userId") Long userId);



}
