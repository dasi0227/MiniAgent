package com.dasi.infrastructure.util;

import com.alibaba.fastjson2.JSON;
import com.dasi.domain.util.jwt.UserContext.UserInfo;
import com.dasi.domain.util.mq.IMqService;
import com.dasi.domain.util.mq.MqEventDTO;
import com.dasi.domain.util.mq.MqEventEntity;
import com.dasi.domain.util.mq.MqEventType;
import com.dasi.domain.util.random.IRandomUtil;
import com.dasi.types.exception.WorkException;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

import static com.dasi.types.constant.ExceptionMessage.ILLEGAL_DATA;

@Slf4j
@Service
public class MqService implements IMqService {

    @Resource
    private RabbitTemplate rabbitTemplate;

    @Resource
    private IRandomUtil randomUtil;
    
    @Value("${miniagent.mq.exchange}")
    private String exchange;

    @Value("${miniagent.mq.main-routing-key}")
    private String mainRoutingKey;

    @Value("${miniagent.mq.retry-routing-key}")
    private String retryRoutingKey;

    @Value("${miniagent.mq.dead-routing-key}")
    private String deadRoutingKey;

    @Override
    public MqEventEntity buildTask(MqEventType eventType, Object payload, UserInfo userInfo) {
        if (eventType == null || payload == null || userInfo == null) {
            throw new WorkException(ILLEGAL_DATA);
        }
        return MqEventEntity.builder()
                .eventId(randomUtil.randomTaskId())
                .eventType(eventType)
                .payload(toJson(payload))
                .userInfo(userInfo)
                .retryCount(0)
                .createTime(LocalDateTime.now())
                .build();
    }

    @Override
    public void sendMain(MqEventEntity event) {
        rabbitTemplate.convertAndSend(exchange, mainRoutingKey, toJson(event));
    }

    @Override
    public void sendRetry(MqEventEntity event) {
        rabbitTemplate.convertAndSend(exchange, retryRoutingKey, toJson(event));
    }

    @Override
    public void sendDead(MqEventEntity event) {
        rabbitTemplate.convertAndSend(exchange, deadRoutingKey, toJson(event));
    }

    @Override
    public MqEventDTO parsePayload(MqEventEntity event) {
        if (event == null || !StringUtils.hasText(event.getPayload())) {
            throw new WorkException(ILLEGAL_DATA);
        }
        try {
            return JSON.parseObject(event.getPayload(), MqEventDTO.class);
        } catch (Exception e) {
            log.error("【消息队列】解析 payload 失败：event={}", event, e);
            throw new WorkException(ILLEGAL_DATA);
        }
    }

    private String toJson(Object value) {
        try {
            return JSON.toJSONString(value);
        } catch (Exception e) {
            log.error("【消息队列】转换消息为 JSON 字符串出错：value={}", value, e);
            throw new WorkException(ILLEGAL_DATA);
        }
    }

}
