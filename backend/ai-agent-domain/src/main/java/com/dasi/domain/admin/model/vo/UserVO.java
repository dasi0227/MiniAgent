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
public class UserVO {
    private Long userId;
    private String userName;
    private String userRole;
    private String userAvatar;
    private Integer userStatus;
    private LocalDateTime updateTime;
}
