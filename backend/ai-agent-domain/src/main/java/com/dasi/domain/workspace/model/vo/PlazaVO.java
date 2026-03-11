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
public class PlazaVO {

    private String plazaId;
    private String templateId;
    private String agentName;
    private String agentType;
    private String userName;
    private String plazaTitle;
    private String plazaDesc;
    private Integer likeCount;
    private Integer favorCount;
    private Integer commentCount;
    private Boolean liked;
    private Boolean favored;
    private Boolean commented;
    private Boolean forked;
    private LocalDateTime createTime;

}
