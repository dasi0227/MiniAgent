package com.dasi.infrastructure.util;

import com.dasi.domain.session.model.enumeration.MessageType;
import com.dasi.domain.util.persist.IPersistUtil;
import com.dasi.infrastructure.persistent.dao.IAiMessageDao;
import com.dasi.infrastructure.persistent.dao.IAiSessionDao;
import com.dasi.infrastructure.persistent.po.AiMessage;
import com.dasi.infrastructure.persistent.po.AiSession;
import com.dasi.types.exception.SessionException;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static com.dasi.domain.session.model.enumeration.MessageRole.ASSISTANT;
import static com.dasi.domain.session.model.enumeration.MessageRole.USER;
import static com.dasi.domain.session.model.enumeration.MessageType.WORK_ANSWER;
import static com.dasi.domain.session.model.enumeration.SessionType.CHAT;
import static com.dasi.domain.session.model.enumeration.SessionType.WORK;
import static com.dasi.types.constant.ChatConstant.CHAT_USER_LIMIT;
import static com.dasi.types.constant.ChatConstant.WORK_USER_LIMIT;
import static com.dasi.types.constant.ExceptionMessage.SESSION_NOT_FOUND;
import static com.dasi.types.constant.ExceptionMessage.SESSION_USER_MESSAGE_LIMIT_REACHED;

@Slf4j
@Service
public class PersistUtil implements IPersistUtil {

    @Resource
    private IAiMessageDao messageDao;

    @Resource
    private IAiSessionDao sessionDao;

    @Override
    public void saveChatUserMessage(String sessionId, String messageContent) {
        saveMessage(sessionId, MessageType.CHAT.getType(), USER.getRole(), messageContent);
    }

    @Override
    public void saveChatAssistantMessage(String sessionId, String messageContent) {
        saveMessage(sessionId, MessageType.CHAT.getType(), ASSISTANT.getRole(), messageContent);
    }

    @Override
    public void saveWorkSseMessage(String sessionId, String messageContent) {
        saveMessage(sessionId, MessageType.WORK_SSE.getType(), ASSISTANT.getRole(), messageContent);
    }

    @Override
    public void saveWorkAssistantMessage(String sessionId, String messageContent) {
        saveMessage(sessionId, WORK_ANSWER.getType(), ASSISTANT.getRole(), messageContent);
    }

    @Override
    public void saveWorkUserMessage(String sessionId, String messageContent) {
        saveMessage(sessionId, WORK_ANSWER.getType(), USER.getRole(), messageContent);
    }
    private void saveMessage(String sessionId, String messageType, String messageRole, String messageContent) {
        AiSession session = sessionDao.queryBySessionId(sessionId);
        if (session == null) {
            throw new SessionException(SESSION_NOT_FOUND);
        }

        checkUserLimit(session, messageRole);
        Integer maxSeq = messageDao.maxSeqBySessionAndType(sessionId, messageType);
        int nextSeq = maxSeq == null ? 1 : maxSeq + 1;

        AiMessage message = AiMessage.builder()
                .sessionId(sessionId)
                .messageContent(messageContent == null ? "" : messageContent)
                .messageRole(messageRole)
                .messageType(messageType)
                .messageSeq(nextSeq)
                .build();
        messageDao.insert(message);
    }


    private void checkUserLimit(AiSession session, String messageRole) {
        if (!USER.getRole().equalsIgnoreCase(messageRole)) {
            return;
        }

        int limit;
        String messageType;
        if (session.getSessionType().equals(WORK.getType())) {
            limit = WORK_USER_LIMIT;
            messageType = WORK_ANSWER.getType();
        } else {
            limit = CHAT_USER_LIMIT;
            messageType = CHAT.getType();
        }

        int count = messageDao.countBySessionAndType(session.getSessionId(), messageType);
        if (count >= limit) {
            throw new SessionException(SESSION_USER_MESSAGE_LIMIT_REACHED);
        }
    }
}
