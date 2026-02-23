package com.dasi.domain.user.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserVO {

    private Long id;

    private String userName;

    private String password;

    private String userRole;

    private String userAvatar;

    private Integer userStatus;

}
