package com.dasi.infrastructure.persistent.dao;

import com.dasi.infrastructure.persistent.po.AiUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface IAiUserDao {

    AiUser queryById(@Param("id") Long id);

    AiUser queryByUsername(@Param("username") String username);

    List<AiUser> page(@Param("username") String username,
                    @Param("userrole") String userrole,
                    @Param("offset") Integer offset,
                    @Param("size") Integer size);

    Long count(@Param("username") String username,
               @Param("userrole") String userrole);

    Long countAll();

    void insert(AiUser aiUser);

    void update(AiUser aiUser);

    void delete(@Param("id") Long id);

    void toggle(@Param("id") Long id, @Param("status") Integer status);

}
