package com.dasi.domain.workspace.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentCreateDTO {

    @NotBlank
    private String modelId;

    @NotBlank
    private String modelName;

    private Set<String> mcpIdSet;

    @NotBlank
    private String strategy;

    @NotBlank
    private String agentName;

    @NotBlank
    private String agentDesc;

}
