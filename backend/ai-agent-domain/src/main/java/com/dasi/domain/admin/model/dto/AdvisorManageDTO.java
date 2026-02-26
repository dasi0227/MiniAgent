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
public class AdvisorManageDTO {

    private Long id;

    @NotBlank
    private String advisorId;

    @NotBlank
    private String advisorName;

    @NotBlank
    private String advisorType;

    private String advisorParam;

}
