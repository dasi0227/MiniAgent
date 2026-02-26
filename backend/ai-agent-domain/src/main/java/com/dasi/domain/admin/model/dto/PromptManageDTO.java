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
public class PromptManageDTO {

    private Long id;

    @NotBlank
    private String promptId;

    @NotBlank
    private String promptName;

    @NotBlank
    private String promptContent;

    private String promptDesc;

}
