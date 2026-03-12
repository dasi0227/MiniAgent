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
public class PlazaManageDTO {

    @NotBlank
    private String plazaId;

    @NotBlank
    private String templateId;

    @NotNull
    private Long userId;

    @NotBlank
    private String agentName;

    @NotBlank
    private String agentType;

    @NotBlank
    private String plazaTitle;

    @NotBlank
    private String plazaDesc;

    @NotNull
    private Integer likeCount;

    @NotNull
    private Integer favorCount;

    @NotNull
    private Integer commentCount;

}
