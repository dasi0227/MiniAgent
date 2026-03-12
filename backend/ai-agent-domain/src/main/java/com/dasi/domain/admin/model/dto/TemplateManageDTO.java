package com.dasi.domain.admin.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TemplateManageDTO {

    @NotBlank
    private String templateId;

    @NotNull
    private Long userId;

    @NotBlank
    private String agentName;

    @NotBlank
    private String agentType;

    @NotBlank
    private String agentDesc;

    @NotBlank
    private String apiBaseUrl;

    @NotBlank
    private String apiCompletionUrl;

    @NotBlank
    private String modelName;

    @NotBlank
    private String modelType;

    @NotBlank
    private String snapshot;
}
