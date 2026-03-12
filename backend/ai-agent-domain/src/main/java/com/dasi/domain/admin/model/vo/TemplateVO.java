package com.dasi.domain.admin.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TemplateVO {

    private String templateId;
    private Long userId;
    private String userName;
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
