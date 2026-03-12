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
public class ConfigManageDTO {

    private String originClientId;

    private String originConfigType;

    private String originConfigValue;

    @NotBlank
    private String clientId;

    @NotBlank
    private String configType;

    @NotBlank
    private String configValue;

    @NotNull
    private Integer configStatus;

}
