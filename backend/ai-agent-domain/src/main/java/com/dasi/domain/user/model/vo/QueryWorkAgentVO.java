package com.dasi.domain.user.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class QueryWorkAgentVO {

    private String agentId;

    private String agentName;

    private String agentDesc;

}
