package com.dasi.types.dto.request.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EditProfileRequest {

    @NotNull(message = "id 不能为空")
    private Long id;

    @NotBlank(message = "用户名不能为空")
    private String userName;

    @Pattern(regexp = "^$|^.{6}$", message = "长度必须为 6 位")
    private String oldPassword;

    @Pattern(regexp = "^$|^.{6}$", message = "长度必须为 6 位")
    private String newPassword;

}
