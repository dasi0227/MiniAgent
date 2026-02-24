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
public class AiPlazaComment {

    private Long id;
    private String commentId;
    private String plazaId;
    private Long userId;
    private String userName;
    private String commentContent;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

}
