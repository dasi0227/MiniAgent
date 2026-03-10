package com.dasi.domain.admin.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserManageDTO {

    private String originUserName;

    @NotBlank
    private String userName;

    @NotBlank
    private String password;

    @NotBlank
    private String userRole;

    @NotBlank
    private String userAvatar;

    @NotNull
    private Integer userStatus;
}
