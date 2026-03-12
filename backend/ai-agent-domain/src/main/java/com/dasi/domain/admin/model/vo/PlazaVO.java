package com.dasi.domain.admin.model.vo;

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
    private Long userId;
    private String userName;
    private String agentName;
    private String agentType;
    private String plazaTitle;
    private String plazaDesc;
    private Integer likeCount;
    private Integer favorCount;
    private Integer commentCount;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
