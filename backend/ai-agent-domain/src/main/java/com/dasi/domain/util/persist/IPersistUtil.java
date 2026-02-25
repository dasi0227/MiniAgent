package com.dasi.domain.util.persist;

public interface IPersistUtil {

    void saveChatUserMessage(String sessionId, String messageContent);

    void saveChatAssistantMessage(String sessionId, String messageContent);

    void saveWorkSseMessage(String sessionId, String messageContent);

    void saveWorkAssistantMessage(String sessionId, String messageContent);

    void saveWorkUserMessage(String sessionId, String messageContent);

}
