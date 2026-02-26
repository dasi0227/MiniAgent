package com.dasi.infrastructure.persistent.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiPlaza {

    private Long id;
    private String plazaId;
    private String templateId;
    private String agentId;
    private Long userId;
    private String agentType;
    private String userName;
    private String plazaTitle;
    private String plazaDesc;
    private Integer likeCount;
    private Integer favorCount;
    private Integer commentCount;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

}
