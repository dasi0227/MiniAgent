package com.dasi.domain.workspace.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RepoVO {

    private String repoType;

    private Integer total;

    private List<RepoItem> list;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RepoItem {
        private String repoId;
        private String agentId;
        private String templateId;
        private String agentName;
        private String agentType;
        private String agentDesc;
        private LocalDateTime createTime;
    }

}
