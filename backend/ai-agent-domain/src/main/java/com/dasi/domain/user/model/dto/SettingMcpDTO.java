package com.dasi.domain.user.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SettingMcpDTO {

    private Long id;

    @NotNull
    private String mcpName;

    @NotNull
    private String mcpType;

    @NotNull
    private String mcpConfig;

    @NotNull
    private String mcpDesc;

    @NotNull
    private String mcpSecret;

}
