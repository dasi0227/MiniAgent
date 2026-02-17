package com.dasi.infrastructure.persistent.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AiRepo {

    private Long id;

    private String repoId;

    private Long userId;

    private String agentId;

    private String repoType;

    private String sourceId;

    private Integer repoStatus;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
