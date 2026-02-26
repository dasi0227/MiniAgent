package com.dasi.domain.workspace.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RepoVO {

    private String repoId;

    private String agentId;

    private String agentName;

    private String agentType;

    private String agentDesc;

    private String createTime;

}
