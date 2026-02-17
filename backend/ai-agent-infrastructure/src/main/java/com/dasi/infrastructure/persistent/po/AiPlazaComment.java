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
public class AiPlazaComment {

    private Long id;

    private String commentId;

    private String plazaId;

    private Long userId;

    private String commentContent;

    private Integer commentStatus;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
