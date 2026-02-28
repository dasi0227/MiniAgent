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
public class AgentManageDTO {

    @NotBlank
    private String agentId;

    @NotBlank
    private String agentName;

    @NotBlank
    private String agentType;

    private String agentDesc;

    private String modelId;

    @Builder.Default
    private Integer agentStatus = 1;
}
