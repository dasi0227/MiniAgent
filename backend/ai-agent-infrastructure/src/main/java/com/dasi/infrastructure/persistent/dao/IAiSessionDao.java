package com.dasi.infrastructure.persistent.dao;

import com.dasi.infrastructure.persistent.po.AiSession;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface IAiSessionDao {

    List<AiSession> queryAll();

    List<AiSession> queryByUserId(@Param("userId") Long userId);

    AiSession queryBySessionId(@Param("sessionId") String sessionId);

    int countByUserIdAndType(@Param("userId") Long userId, @Param("sessionType") String sessionType);

    int countAll();

    int countByType(@Param("sessionType") String sessionType);

    void insert(AiSession session);

    void updateTitle(@Param("sessionId") String sessionId, @Param("sessionTitle") String sessionTitle);

    void delete(@Param("sessionId") String sessionId);

}
