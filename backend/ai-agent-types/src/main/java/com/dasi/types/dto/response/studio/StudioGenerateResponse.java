package com.dasi.types.dto.response.studio;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudioGenerateResponse {

    private String agentName;

    private String agentType;

    private String agentDesc;

    private String flowPrompt;

    private List<String> mcpIdList;
}
