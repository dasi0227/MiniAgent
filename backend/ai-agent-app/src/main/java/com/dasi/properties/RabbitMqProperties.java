package com.dasi.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "miniagent.mq", ignoreInvalidFields = true)
public class RabbitMqProperties {

    private String exchange;

    private String mainQueue;

    private String retryQueue;

    private String deadQueue;

    private String mainRoutingKey;

    private String retryRoutingKey;

    private String deadRoutingKey;

    private Integer maxRetry;

    private Long retryDelay;

}
