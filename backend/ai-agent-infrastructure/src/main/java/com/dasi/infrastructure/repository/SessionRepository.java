package com.dasi.infrastructure.repository;

import com.dasi.domain.session.model.vo.MessageVO;
import com.dasi.domain.session.model.vo.SessionVO;
import com.dasi.domain.session.repository.ISessionRepository;
import com.dasi.infrastructure.persistent.dao.IAiMessageDao;
import com.dasi.infrastructure.persistent.dao.IAiSessionDao;
import com.dasi.infrastructure.persistent.dao.IAiUserDao;
import com.dasi.infrastructure.persistent.po.AiMessage;
import com.dasi.infrastructure.persistent.po.AiSession;
import com.dasi.infrastructure.persistent.po.AiUser;
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

    @Resource
    private IAiUserDao userDao;

    @Override
    public List<SessionVO> listSession(Long userId) {
        List<AiSession> list = sessionDao.queryByUserId(userId);
        return list.stream().map(this::toSessionVO).toList();
    }

    @Override
    public int countSessionByType(Long userId, String sessionType) {
        return sessionDao.countByUserIdAndType(userId, sessionType);
    }

    @Override
    public void insertSession(String sessionId, Long userId, String sessionTitle, String sessionType) {
        AiSession session = AiSession.builder()
                .sessionId(sessionId)
                .userId(userId)
                .sessionTitle(sessionTitle)
                .sessionType(sessionType)
                .build();
        sessionDao.insert(session);
    }

    @Override
    public void updateSessionTitle(String sessionId, String sessionTitle) {
        sessionDao.updateTitle(sessionId, sessionTitle);
    }

    @Override
    public void deleteSession(String sessionId) {
        messageDao.deleteBySessionId(sessionId);
        sessionDao.delete(sessionId);
    }

    @Override
    public SessionVO querySessionBySessionId(String sessionId) {
        return toSessionVO(sessionDao.queryBySessionId(sessionId));
    }

    @Override
    public Long querySessionOwnerId(String sessionId) {
        AiSession session = sessionDao.queryBySessionId(sessionId);
        return session == null ? null : session.getUserId();
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
                .sessionId(session.getSessionId())
                .userName(queryUserName(session.getUserId()))
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
                .messageContent(message.getMessageContent())
                .messageRole(message.getMessageRole())
                .messageType(message.getMessageType())
                .messageSeq(message.getMessageSeq())
                .createTime(message.getCreateTime())
                .build();
    }

    private String queryUserName(Long userId) {
        if (userId == null) {
            return null;
        }
        AiUser user = userDao.queryById(userId);
        return user == null ? null : user.getUserName();
    }
}
