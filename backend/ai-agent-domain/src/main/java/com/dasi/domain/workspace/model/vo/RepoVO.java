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
public class RepoVO {

    private String repoId;

    private String repoType;

    private String agentId;

    private String templateId;

    private String agentName;

    private String agentType;

    private String agentDesc;

    private LocalDateTime createTime;
}
