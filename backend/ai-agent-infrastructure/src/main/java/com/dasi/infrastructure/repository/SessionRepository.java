package com.dasi.infrastructure.repository;

import com.dasi.domain.session.model.vo.MessageVO;
import com.dasi.domain.session.model.vo.SessionVO;
import com.dasi.domain.session.repository.ISessionRepository;
import com.dasi.infrastructure.persistent.dao.IAiMessageDao;
import com.dasi.infrastructure.persistent.dao.IAiSessionDao;
import com.dasi.infrastructure.persistent.po.AiMessage;
import com.dasi.infrastructure.persistent.po.AiSession;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;

@Slf4j
@Repository
public class SessionRepository implements ISessionRepository {

    @Resource
    private IAiSessionDao sessionDao;

    @Resource
    private IAiMessageDao messageDao;

    @Override
    public List<SessionVO> listSession(String sessionUser) {
        List<AiSession> list = sessionDao.queryByUser(sessionUser);
        return list.stream().map(this::toSessionVO).toList();
    }

    @Override
    public int countSessionByType(String sessionUser, String sessionType) {
        return sessionDao.countByUserAndType(sessionUser, sessionType);
    }

    @Override
    public void insertSession(String sessionId, String sessionUser, String sessionTitle, String sessionType) {
        AiSession session = AiSession.builder()
                .sessionId(sessionId)
                .sessionUser(sessionUser)
                .sessionTitle(sessionTitle)
                .sessionType(sessionType)
                .build();
        sessionDao.insert(session);
    }

    @Override
    public void updateSessionTitle(Long id, String sessionTitle) {
        sessionDao.updateTitle(id, sessionTitle);
    }

    @Override
    public void deleteSession(Long id, String sessionId) {
        messageDao.deleteBySessionId(sessionId);
        sessionDao.delete(id);
    }

    @Override
    public SessionVO querySessionById(Long id) {
        return toSessionVO(sessionDao.queryById(id));
    }

    @Override
    public SessionVO querySessionBySessionId(String sessionId) {
        return toSessionVO(sessionDao.queryBySessionId(sessionId));
    }

    @Override
    public List<MessageVO> listMessageBySessionAndType(String sessionId, String messageType) {
        List<AiMessage> list = messageDao.queryBySessionAndType(sessionId, messageType);
        return list.stream().map(this::toMessageVO).toList();
    }

    private SessionVO toSessionVO(AiSession session) {
        if (session == null) {
            return null;
        }
        return SessionVO.builder()
                .id(session.getId())
                .sessionId(session.getSessionId())
                .sessionUser(session.getSessionUser())
                .sessionTitle(session.getSessionTitle())
                .sessionType(session.getSessionType())
                .createTime(session.getCreateTime())
                .build();
    }

    private MessageVO toMessageVO(AiMessage message) {
        if (message == null) {
            return null;
        }
        return MessageVO.builder()
                .id(message.getId())
                .messageContent(message.getMessageContent())
                .messageRole(message.getMessageRole())
                .messageType(message.getMessageType())
                .messageSeq(message.getMessageSeq())
                .createTime(message.getCreateTime())
                .build();
    }
}
