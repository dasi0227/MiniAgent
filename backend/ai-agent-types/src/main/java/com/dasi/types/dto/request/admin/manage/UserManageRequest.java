package com.dasi.types.dto.request.admin.manage;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserManageRequest {

    private Long id;

    @NotBlank
    private String userName;

    @NotBlank
    private String password;

    @NotBlank
    private String userRole;

    private String userAvatar;

    @Builder.Default
    private Integer userStatus = 1;
}
