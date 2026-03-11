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
public class AgentUpdateSystemPromptDTO {

    @NotBlank
    private String agentId;

    @NotBlank
    private String promptId;

    @NotBlank
    private String systemPrompt;

}
