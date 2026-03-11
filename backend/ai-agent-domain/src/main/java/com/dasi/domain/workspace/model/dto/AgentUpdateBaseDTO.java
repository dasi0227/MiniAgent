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
public class AgentUpdateBaseDTO {

    @NotBlank
    private String agentId;

    @NotBlank
    private String agentName;

    @NotBlank
    private String agentDesc;

}
