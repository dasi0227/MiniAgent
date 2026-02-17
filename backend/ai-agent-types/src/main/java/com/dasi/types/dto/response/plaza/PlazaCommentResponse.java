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
public class PlazaCommentResponse {

    private String commentId;

    private String plazaId;

    private Long userId;

    private String username;

    private String commentContent;

    private LocalDateTime createTime;
}
