package com.dasi.domain.workspace.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentDetailVO {

    private String id;

    private String agentId;

    private String agentName;

    private String agentType;

    private String agentDesc;

    private String agentFrom;

    private LocalDateTime publishTime;


}
