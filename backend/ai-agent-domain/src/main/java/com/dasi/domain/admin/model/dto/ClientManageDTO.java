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
public class ClientManageDTO {

    @NotBlank
    private String clientId;

    @NotBlank
    private String clientType;

    @NotBlank
    private String clientRole;

    @NotBlank
    private String modelId;

    @NotBlank
    private String modelName;

    @NotBlank
    private String clientName;

    @NotNull
    private Integer clientStatus;
}
