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
public class ConfigVO {
    private String clientId;
    private String configType;
    private String configValue;
    private Integer configStatus;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
