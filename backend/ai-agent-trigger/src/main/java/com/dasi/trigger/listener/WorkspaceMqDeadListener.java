package com.dasi.trigger.listener;

import com.alibaba.fastjson2.JSON;
import com.dasi.domain.util.mq.MqEventEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class WorkspaceMqDeadListener {

    @RabbitListener(queues = "${miniagent.mq.dead-queue}")
    public void onDeadMessage(String message) {
        try {
            MqEventEntity task = JSON.parseObject(message, MqEventEntity.class);
            log.info("【Listener】接收到死信消息，待人工处理：task={}", task);
        } catch (Exception e) {
            log.error("【Listener】死信消息解析失败：message={}", message, e);
        }
    }

}
