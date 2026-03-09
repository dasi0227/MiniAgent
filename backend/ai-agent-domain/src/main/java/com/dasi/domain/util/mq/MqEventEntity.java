package com.dasi.domain.util.mq;

import com.dasi.domain.util.jwt.UserContext.UserInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MqEventEntity {

    private String eventId;

    private MqEventType eventType;

    private String payload;

    private UserInfo userInfo;

    private Integer retryCount;

    private String errorMessage;

    private LocalDateTime createTime;

}
