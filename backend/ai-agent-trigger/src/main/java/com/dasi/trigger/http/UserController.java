package com.dasi.trigger.http;

import com.dasi.api.IUserApi;
import com.dasi.domain.user.model.vo.UserApiVO;
import com.dasi.domain.user.service.auth.IAuthService;
import com.dasi.domain.user.service.setting.ISettingService;
import com.dasi.types.dto.request.user.ApiManageRequest;
import com.dasi.types.dto.request.user.AuthRequest;
import com.dasi.types.dto.request.user.ProfileEditRequest;
import com.dasi.types.dto.response.user.AuthResponse;
import com.dasi.types.dto.result.Result;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

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
    public Result<AuthResponse> profileQuery() {
        return Result.success(settingService.profileQuery());
    }

    @PostMapping(value = "/profile/edit", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Override
    public Result<AuthResponse> profileEdit(@Valid @RequestPart("profile") ProfileEditRequest request,
                                            @RequestPart(value = "avatar", required = false) MultipartFile avatar) {
        return Result.success(settingService.profileEdit(request, avatar));
    }

    @PostMapping(value = "/api/list")
    @Override
    public Result<List<UserApiVO>> apiList(@RequestParam(required=false, defaultValue = "") String keyword) {
        return Result.success(settingService.apiList(keyword));
    }

    @PostMapping(value = "/api/insert")
    @Override
    public Result<Void> apiInsert(@Valid @RequestBody ApiManageRequest request) {
        settingService.apiInsert(request);
        return Result.success();
    }

    @PostMapping(value = "/api/update")
    @Override
    public Result<Void> apiUpdate(@Valid @RequestBody ApiManageRequest request) {
        settingService.apiUpdate(request);
        return Result.success();
    }

    @PostMapping(value = "/api/delete")
    @Override
    public Result<Void> apiDelete(@RequestParam Long id) {
        settingService.apiDelete(id);
        return Result.success();
    }

}
