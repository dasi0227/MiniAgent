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
public class AgentBaseUpdateDTO {

    @NotBlank
    private String agentId;

    private String agentName;

    private String agentDesc;

}
