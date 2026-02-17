package com.dasi.types.dto.response.user.mcp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserMcpItemResponse {

    private Long id;

    private String mcpId;

    private String mcpName;

    private String mcpType;

    private String mcpConfig;

    private String mcpDesc;

    private Integer mcpTimeout;

    private Integer mcpChat;

    private String sourceType;

    private Boolean editable;

    private Boolean secretConfigured;
}
