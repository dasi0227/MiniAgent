package com.dasi.config;

import com.dasi.properties.RabbitMqProperties;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableRabbit
@Slf4j
@EnableConfigurationProperties(RabbitMqProperties.class)
public class RabbitMqConfig {

    @PostConstruct
    public void init() {
        log.info("【初始化配置】RabbitMQ");
    }

    @Resource
    private RabbitMqProperties rabbitMqProperties;

    @Bean
    public DirectExchange workspaceExchange() {
        return new DirectExchange(rabbitMqProperties.getExchange(), true, false);
    }

    @Bean("workspaceMainQueue")
    public Queue workspaceMainQueue() {
        return new Queue(rabbitMqProperties.getMainQueue(), true);
    }

    @Bean("workspaceRetryQueue")
    public Queue workspaceRetryQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-message-ttl", rabbitMqProperties.getRetryDelay());
        args.put("x-dead-letter-exchange", rabbitMqProperties.getExchange());
        args.put("x-dead-letter-routing-key", rabbitMqProperties.getMainRoutingKey());
        return new Queue(rabbitMqProperties.getRetryQueue(), true, false, false, args);
    }

    @Bean("workspaceDeadQueue")
    public Queue workspaceDeadQueue() {
        return new Queue(rabbitMqProperties.getDeadQueue(), true);
    }

    @Bean("workspaceMainBinding")
    public Binding workspaceMainBinding(@Qualifier("workspaceMainQueue") Queue workspaceMainQueue,
                                        @Qualifier("workspaceExchange") DirectExchange workspaceExchange) {
        return BindingBuilder.bind(workspaceMainQueue).to(workspaceExchange).with(rabbitMqProperties.getMainRoutingKey());
    }

    @Bean("workspaceRetryBinding")
    public Binding workspaceRetryBinding(@Qualifier("workspaceRetryQueue") Queue workspaceRetryQueue,
                                         @Qualifier("workspaceExchange") DirectExchange workspaceExchange) {
        return BindingBuilder.bind(workspaceRetryQueue).to(workspaceExchange).with(rabbitMqProperties.getRetryRoutingKey());
    }

    @Bean("workspaceDeadBinding")
    public Binding workspaceDeadBinding(@Qualifier("workspaceDeadQueue") Queue workspaceDeadQueue,
                                        @Qualifier("workspaceExchange") DirectExchange workspaceExchange) {
        return BindingBuilder.bind(workspaceDeadQueue).to(workspaceExchange).with(rabbitMqProperties.getDeadRoutingKey());
    }

}
