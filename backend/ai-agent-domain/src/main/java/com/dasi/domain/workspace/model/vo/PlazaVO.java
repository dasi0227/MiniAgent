package com.dasi.domain.workspace.model.vo;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlazaVO {

    private String plazaId;
    private String agentId;
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
    private LocalDateTime createTime;

}
