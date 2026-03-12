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
public class ClientVO {
    private String clientId;
    private String clientType;
    private String clientRole;
    private String modelId;
    private String modelName;
    private String clientName;
    private Integer clientStatus;
    private Long clientFrom;
    private String userName;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
