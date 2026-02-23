package com.dasi.trigger.http;

import com.dasi.api.IUserApi;
import com.dasi.domain.user.service.auth.IAuthService;
import com.dasi.domain.user.service.setting.ISettingService;
import com.dasi.types.dto.request.user.AuthRequest;
import com.dasi.types.dto.request.user.EditProfileRequest;
import com.dasi.types.dto.response.user.AuthResponse;
import com.dasi.types.dto.result.Result;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/user")
public class UserController implements IUserApi {

    @Resource
    private IAuthService authService;

    @Resource
    private ISettingService settingService;

    @PostMapping("/auth/login")
    @Override
    public Result<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        return Result.success(authService.login(request));
    }

    @PostMapping("/auth/register")
    @Override
    public Result<AuthResponse> register(@Valid @RequestBody AuthRequest request) {
        return Result.success(authService.register(request));
    }

    @PostMapping("/profile/query")
    @Override
    public Result<AuthResponse> queryProfile() {
        return Result.success(settingService.queryProfile());
    }

    @PostMapping(value = "/profile/edit", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Override
    public Result<AuthResponse> editProfile(@Valid @RequestPart("profile") EditProfileRequest request,
                                            @RequestPart(value = "avatar", required = false) MultipartFile avatar) {
        return Result.success(settingService.editProfile(request, avatar));
    }

}
