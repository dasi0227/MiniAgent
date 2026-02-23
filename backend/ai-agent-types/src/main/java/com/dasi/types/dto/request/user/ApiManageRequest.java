package com.dasi.types.dto.request.user;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ApiManageRequest {

    @NotNull
    private String apiId;

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
