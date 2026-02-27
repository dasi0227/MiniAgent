package com.dasi.domain.workspace.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentPublishDTO {

    @NotBlank
    private String agentId;

    @NotBlank
    private String plazaTitle;

    @NotBlank
    private String plazaDesc;

}
