package com.dasi.domain.user.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserApiVO {

    private Long id;

    private String apiId;

    private String modelName;

    private String modelType;

    private String apiBaseUrl;

    private String apiKey;

    private String apiCompletionPath;

}
