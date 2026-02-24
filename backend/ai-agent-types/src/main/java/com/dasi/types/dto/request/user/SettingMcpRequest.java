package com.dasi.types.dto.request.user;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SettingMcpRequest {

    @NotNull
    private String mcpId;

    @NotNull
    private String mcpName;

    @NotNull
    private String mcpType;

    @NotNull
    private String mcpConfig;

    @NotNull
    private String mcpDesc;

    @NotNull
    private Map<String, String> mcpSecret;

}
