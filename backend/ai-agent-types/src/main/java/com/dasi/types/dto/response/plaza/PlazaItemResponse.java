package com.dasi.types.dto.response.plaza;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlazaItemResponse {

    private String plazaId;

    private String agentId;

    private String agentType;

    private String username;

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
