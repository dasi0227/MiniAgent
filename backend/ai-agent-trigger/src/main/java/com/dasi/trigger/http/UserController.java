package com.dasi.trigger.http;

import com.dasi.api.IUserApi;
import com.dasi.domain.user.service.auth.IAuthService;
import com.dasi.domain.user.service.edit.IEditService;
import com.dasi.types.dto.request.user.AuthRequest;
import com.dasi.types.dto.request.user.EditProfileRequest;
import com.dasi.types.dto.response.user.AuthResponse;
import com.dasi.types.dto.result.Result;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/auth")
public class UserController implements IUserApi {

    @Resource
    private IAuthService authService;

    @Resource
    private IEditService editService;

    @PostMapping("/login")
    @Override
    public Result<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        AuthResponse response = authService.login(request);
        return Result.success(response);
    }

    @PostMapping("/register")
    @Override
    public Result<AuthResponse> register(@Valid @RequestBody AuthRequest request) {
        return Result.success(authService.register(request));
    }

    @PostMapping("/profile")
    @Override
    public Result<AuthResponse> profile() {
        return Result.success(authService.profile());
    }

    @PostMapping("/password")
    @Override
    public Result<AuthResponse> editProfile(@Valid @RequestBody EditProfileRequest request) {
        return Result.success(editService.editProfile(request));
    }

}
