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
public class AiPlazaFavor {

    private Long id;

    private String plazaId;

    private Long userId;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
