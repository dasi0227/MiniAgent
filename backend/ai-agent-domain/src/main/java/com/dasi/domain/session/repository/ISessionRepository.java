package com.dasi.domain.session.repository;

import com.dasi.domain.session.model.vo.MessageVO;
import com.dasi.domain.session.model.vo.SessionVO;

import java.util.List;

public interface ISessionRepository {

    List<SessionVO> listSession(Long userId);

    int countSessionByType(Long userId, String sessionType);

    void insertSession(String sessionId, Long userId, String sessionTitle, String sessionType);

    void updateSessionTitle(String sessionId, String sessionTitle);

    void deleteSession(String sessionId);

    List<MessageVO> listMessageBySessionAndType(String sessionId, String messageType);

    SessionVO querySessionBySessionId(String sessionId);

    Long querySessionOwnerId(String sessionId);
}
