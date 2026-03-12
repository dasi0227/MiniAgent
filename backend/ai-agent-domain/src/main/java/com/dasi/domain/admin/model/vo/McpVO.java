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
public class McpVO {
    private String mcpId;
    private String mcpName;
    private String mcpType;
    private String mcpParam;
    private String mcpSecret;
    private String mcpDesc;
    private Integer mcpTimeout;
    private Integer mcpChat;
    private Long mcpFrom;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
