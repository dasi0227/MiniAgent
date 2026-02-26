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
public class SettingApiDTO {

    private Long id;

    @NotNull
    private String modelName;

    @NotNull
    private String modelType;

    @NotNull
    private String apiBaseUrl;

    @NotNull
    private String apiKey;

    @NotNull
    private String apiCompletionPath;

}
