package com.dasi.types.dto.request.studio;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudioCreateRequest {

    private String agentId;

    @NotBlank
    private String agentName;

    @NotBlank
    private String agentType;

    private String agentDesc;

    private String flowPrompt;

    private List<String> mcpIdList;

    private Integer maxRound;

    private Integer maxRetry;
}
