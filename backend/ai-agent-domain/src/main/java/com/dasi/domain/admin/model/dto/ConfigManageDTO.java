package com.dasi.domain.admin.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ConfigManageDTO {

    private Long id;

    @NotBlank
    private String clientId;

    @NotBlank
    private String configType;

    @NotBlank
    private String configValue;

    private String configParam;

    @Builder.Default
    private Integer configStatus = 1;

}
