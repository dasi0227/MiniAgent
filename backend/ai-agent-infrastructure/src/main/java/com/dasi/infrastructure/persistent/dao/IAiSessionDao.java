package com.dasi.infrastructure.persistent.dao;

import com.dasi.infrastructure.persistent.po.AiSession;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface IAiSessionDao {

    List<AiSession> queryAll();

    List<AiSession> queryByUser(@Param("sessionUser") String sessionUser);

    AiSession queryById(@Param("id") Long id);

    AiSession queryBySessionId(@Param("sessionId") String sessionId);

    int countByUserAndType(@Param("sessionUser") String sessionUser, @Param("sessionType") String sessionType);

    int countAll();

    int countByType(@Param("sessionType") String sessionType);

    void insert(AiSession session);

    void updateTitle(@Param("id") Long id, @Param("sessionTitle") String sessionTitle);

    void delete(@Param("id") Long id);

}
