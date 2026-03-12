package com.dasi.domain.session.service;

import com.dasi.domain.session.model.enumeration.UserRoleType;
import com.dasi.domain.session.model.vo.MessageVO;
import com.dasi.domain.session.model.vo.SessionVO;
import com.dasi.domain.session.repository.ISessionRepository;
import com.dasi.domain.util.jwt.UserContext;
import com.dasi.domain.util.random.IRandomUtil;
import com.dasi.types.exception.SessionException;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static com.dasi.domain.session.model.enumeration.MessageType.WORK_ANSWER;
import static com.dasi.domain.session.model.enumeration.MessageType.WORK_SSE;
import static com.dasi.domain.session.model.enumeration.SessionType.CHAT;
import static com.dasi.types.constant.ChatConstant.CHAT_SESSION_LIMIT;
import static com.dasi.types.constant.ExceptionMessage.*;

@Slf4j
@Service
public class SessionService implements ISessionService {

    @Resource
    private ISessionRepository sessionRepository;

    @Resource
    private UserContext userContext;

    @Resource
    private IRandomUtil randomUtil;

    @Override
    public List<SessionVO> listSession() {
        return sessionRepository.listSession(requireUserId());
    }

    @Override
    public SessionVO insertSession(String sessionTitle, String sessionType) {
        Long userId = requireUserId();

        int count = sessionRepository.countSessionByType(userId, sessionType);
        if (count >= CHAT_SESSION_LIMIT) {
            throw new SessionException(SESSION_TYPE_LIMIT_REACHED);
        }

        String sessionId = generateSessionId(sessionType);
        sessionRepository.insertSession(sessionId, userId, sessionTitle, sessionType);
        return sessionRepository.querySessionBySessionId(sessionId);
    }

    @Override
    public void updateSession(String sessionId, String sessionTitle) {
        requireSession(sessionId);
        Long userId = requireUserId();
        if (!userId.equals(requireOwnerId(sessionId))) {
            throw new SessionException(SESSION_NO_PERMISSION_UPDATE);
        }

        String title = StringUtils.hasText(sessionTitle) ? sessionTitle.trim() : "未命名会话";
        sessionRepository.updateSessionTitle(sessionId, title);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteSession(String sessionId) {
        requireSession(sessionId);
        Long userId = requireUserId();
        if (!userId.equals(requireOwnerId(sessionId))) {
            throw new SessionException(SESSION_NO_PERMISSION_UPDATE);
        }

        sessionRepository.deleteSession(sessionId);
    }

    @Override
    public List<MessageVO> listChatMessage(String sessionId) {
        requireSession(sessionId);
        Long userId = requireUserId();
        if (notAdmin() && !userId.equals(requireOwnerId(sessionId))) {
            throw new SessionException(SESSION_NO_PERMISSION_UPDATE);
        }

        return sessionRepository.listMessageBySessionAndType(sessionId, CHAT.getType());
    }

    @Override
    public List<MessageVO> listWorkSseMessage(String sessionId) {
        requireSession(sessionId);
        Long userId = requireUserId();
        if (notAdmin() && !userId.equals(requireOwnerId(sessionId))) {
            throw new SessionException(SESSION_NO_PERMISSION_UPDATE);
        }

        return sessionRepository.listMessageBySessionAndType(sessionId, WORK_SSE.getType());
    }

    @Override
    public List<MessageVO> listWorkAnswerMessage(String sessionId) {
        requireSession(sessionId);
        Long userId = requireUserId();
        if (notAdmin() && !userId.equals(requireOwnerId(sessionId))) {
            throw new SessionException(SESSION_NO_PERMISSION_UPDATE);
        }

        return sessionRepository.listMessageBySessionAndType(sessionId, WORK_ANSWER.getType());
    }

    @Override
    public String validateSessionAccess(String sessionId, String expectedSessionType) {
        if (!StringUtils.hasText(sessionId)) {
            return SESSION_NOT_FOUND;
        }

        SessionVO sessionVO = sessionRepository.querySessionBySessionId(sessionId);
        if (sessionVO == null) {
            return SESSION_NOT_FOUND;
        }

        if (StringUtils.hasText(expectedSessionType) && !expectedSessionType.equalsIgnoreCase(sessionVO.getSessionType())) {
            return SESSION_TYPE_MISMATCH;
        }

        String role = userContext.getUserRole();
        if (!StringUtils.hasText(role)) {
            return SESSION_USER_INFO_MISSING;
        }

        if (!UserRoleType.ADMIN.getType().equals(role)) {
            Long userId = userContext.getUserId();
            if (userId == null) {
                return SESSION_USER_INFO_MISSING;
            }
            if (!userId.equals(requireOwnerId(sessionId))) {
                return SESSION_NO_PERMISSION_ACCESS;
            }
        }

        return null;
    }

    private boolean notAdmin() {
        String role = userContext.getUserRole();
        if (!StringUtils.hasText(role)) {
            throw new SessionException(SESSION_USER_INFO_MISSING);
        }
        return !role.equals(UserRoleType.ADMIN.getType());
    }


    private SessionVO requireSession(String sessionId) {
        SessionVO session = sessionRepository.querySessionBySessionId(sessionId);
        if (session == null) {
            throw new SessionException(SESSION_NOT_FOUND);
        }
        return session;
    }

    private Long requireOwnerId(String sessionId) {
        Long ownerId = sessionRepository.querySessionOwnerId(sessionId);
        if (ownerId == null) {
            throw new SessionException(SESSION_NOT_FOUND);
        }
        return ownerId;
    }

    private Long requireUserId() {
        Long userId = userContext.getUserId();
        if (userId == null) {
            throw new SessionException(SESSION_USER_INFO_MISSING);
        }
        return userId;
    }

    private String generateSessionId(String type) {
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String uuid = randomUtil.uuid().substring(0, 10);
        return "%s_%s_%s".formatted(date, type, uuid);
    }

}
