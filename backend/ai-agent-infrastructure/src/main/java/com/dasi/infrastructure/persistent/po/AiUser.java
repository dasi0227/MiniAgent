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
public class AiUser {

    private Long id;

    private String userName;

    private String password;

    private String userRole;

    private String userAvatar;

    private Integer userStatus;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

}
