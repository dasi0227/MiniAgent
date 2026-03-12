package com.dasi.domain.admin.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SessionVO {
    private String sessionId;
    private String userName;
    private String sessionTitle;
    private String sessionType;
    private LocalDateTime createTime;
}
