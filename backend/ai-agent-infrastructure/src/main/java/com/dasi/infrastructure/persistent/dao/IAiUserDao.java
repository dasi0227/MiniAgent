package com.dasi.infrastructure.persistent.dao;

import com.dasi.infrastructure.persistent.po.AiUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface IAiUserDao {

    AiUser queryById(@Param("id") Long id);

    String queryUserNameById(@Param("id") Long id);

    AiUser queryByUserName(@Param("userName") String userName);

    List<AiUser> page(@Param("keyword") String keyword,
                      @Param("userRole") String userRole,
                      @Param("offset") Integer offset,
                      @Param("size") Integer size);

    Long count(@Param("keyword") String keyword,
               @Param("userRole") String userRole);

    Long countAll();

    void insert(AiUser aiUser);

    void update(AiUser aiUser);

    void delete(@Param("id") Long id);

    void toggle(@Param("id") Long id, @Param("status") Integer status);

}
