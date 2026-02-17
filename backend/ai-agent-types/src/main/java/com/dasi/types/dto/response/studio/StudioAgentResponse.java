package com.dasi.types.dto.response.studio;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudioAgentResponse {

    private String agentId;

    private String agentName;

    private String agentType;

    private String agentDesc;

    private Integer agentStatus;

    private String sourceType;

    private LocalDateTime updateTime;
}
