package com.dasi.domain.ai.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AiUploadDTO {

    private String repoUrl;

    private String username;

    private String password;

}
