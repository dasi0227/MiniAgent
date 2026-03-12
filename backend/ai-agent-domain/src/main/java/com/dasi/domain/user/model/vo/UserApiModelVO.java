package com.dasi.domain.user.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserApiModelVO {

    private String apiId;

    private String modelId;

    private String modelName;

    private String modelType;

    private String apiKey;

    private String apiBaseUrl;

    private String apiCompletionPath;

}
