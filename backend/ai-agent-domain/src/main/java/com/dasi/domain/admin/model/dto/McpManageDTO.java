package com.dasi.domain.admin.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class McpManageDTO {

    @NotBlank
    private String mcpId;

    @NotBlank
    private String mcpName;

    @NotBlank
    private String mcpType;

    @NotBlank
    private String mcpDesc;

    @NotBlank
    private String mcpParam;

    @NotNull
    private Integer mcpTimeout;

    @NotNull
    private Integer mcpChat;

}
