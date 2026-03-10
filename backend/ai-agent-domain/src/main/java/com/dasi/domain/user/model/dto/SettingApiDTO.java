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
public class SettingApiDTO {

    private String apiId;

    @NotBlank
    private String modelName;

    @NotBlank
    private String modelType;

    @NotBlank
    private String apiBaseUrl;

    @NotBlank
    private String apiKey;

    @NotBlank
    private String apiCompletionPath;

}
