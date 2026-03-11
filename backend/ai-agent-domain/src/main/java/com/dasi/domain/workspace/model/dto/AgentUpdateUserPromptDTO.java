package com.dasi.domain.workspace.model.dto;

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
public class AgentUpdateUserPromptDTO {

    @NotBlank
    private String agentId;

    @NotNull
    private Long flowId;

    @NotBlank
    private String userPrompt;

}
