package com.dasi.types.dto.request.user.mcp;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserMcpManageRequest {

    private Long id;

    @NotBlank
    private String mcpId;

    @NotBlank
    private String mcpName;

    @NotBlank
    private String mcpType;

    @NotBlank
    private String mcpConfig;

    private String mcpDesc;

    private Integer mcpTimeout;

    private Integer mcpChat;

    private Map<String, String> secretMap;
}
