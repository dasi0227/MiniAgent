package com.dasi.infrastructure.persistent.dao;

import com.dasi.infrastructure.persistent.po.AiApi;
import com.dasi.infrastructure.persistent.po.AiUserApi;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface IAiApiDao {

    AiApi queryByApiId(@Param("apiId") String apiId);

    List<AiApi> page(@Param("idKeyword") String idKeyword,
                     @Param("offset") Integer offset,
                     @Param("size") Integer size);

    Integer count(@Param("idKeyword") String idKeyword);

    Integer countAll();

    void insert(AiApi aiApi);

    void update(AiApi aiApi);

    void deleteByApiId(@Param("apiId") String apiId);

    List<String> listApiId();

    List<AiUserApi> listUserApi(@Param("keyword") String keyword, @Param("userId") Long userId);



}
