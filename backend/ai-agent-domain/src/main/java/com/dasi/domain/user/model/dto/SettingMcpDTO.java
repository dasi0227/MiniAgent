package com.dasi.domain.user.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SettingMcpDTO {

    private String mcpId;

    @NotBlank
    private String mcpName;

    @NotBlank
    private String mcpType;

    @NotBlank
    private String mcpDesc;

    private String mcpParam;

    private String mcpSecret;

}
