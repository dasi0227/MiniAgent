package com.dasi.types.dto.response.repo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RepoItemResponse {

    private String repoId;

    private String agentId;

    private String agentName;

    private String agentDesc;

    private String repoType;

    private String sourceType;

    private LocalDateTime updateTime;
}
