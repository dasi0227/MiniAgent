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
public class AiPlaza {

    private Long id;

    private String plazaId;

    private String agentId;

    private Long userId;

    private String plazaTitle;

    private String plazaDesc;

    private Integer plazaStatus;

    private Integer likeCount;

    private Integer favorCount;

    private Integer commentCount;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
