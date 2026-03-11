package com.dasi.domain.workspace.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentUpdateMcpDTO {

    @NotBlank
    private String agentId;

    @NotEmpty
    private List<String> clientIdList;

    private List<String> mcpIdList;

}
