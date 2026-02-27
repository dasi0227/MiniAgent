package com.dasi.infrastructure.persistent.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiTemplate {

    private Long id;
    private String templateId;
    private Long userId;
    private String agentId;
    private String agentName;
    private String agentType;
    private String agentDesc;
    private String apiBaseUrl;
    private String apiCompletionUrl;
    private String modelName;
    private String modelType;
    private String snapshot;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

}
