package com.dasi.domain.admin.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentListDTO {

    private String idKeyword;

    private String nameKeyword;

    private String agentType;

}
