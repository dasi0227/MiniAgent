package com.dasi.domain.util.mq;

import com.dasi.domain.util.jwt.UserContext.UserInfo;

public interface IMqService {

    MqEventEntity buildTask(MqEventType type, Object payload, UserInfo userInfo);

    void sendMain(MqEventEntity event);

    void sendRetry(MqEventEntity event);

    void sendDead(MqEventEntity event);

    MqEventDTO parsePayload(MqEventEntity event);

}
