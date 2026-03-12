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
public class ApiManageDTO {

    @NotBlank
    private String apiId;

    @NotBlank
    private String apiBaseUrl;

    @NotBlank
    private String apiKey;

    @NotBlank
    private String apiCompletionsPath;

    @NotBlank
    private String apiEmbeddingsPath;

}
