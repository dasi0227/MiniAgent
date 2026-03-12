package com.dasi.infrastructure.persistent.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiApiModel {

    private Long id;

    private String apiId;

    private String modelId;

    private String modelName;

    private String modelType;

    private String apiBaseUrl;

    private String apiKey;

    private String apiCompletionPath;

}
