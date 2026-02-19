package com.dasi.api;

import com.dasi.types.dto.request.user.AuthRequest;
import com.dasi.types.dto.request.user.EditProfileRequest;
import com.dasi.types.dto.response.user.AuthResponse;
import com.dasi.types.dto.result.Result;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestBody;

public interface IUserApi {

    Result<AuthResponse> login(AuthRequest request);

    Result<AuthResponse> register(AuthRequest request);

    Result<AuthResponse> profile();

    Result<AuthResponse> editProfile(@Valid @RequestBody EditProfileRequest request);
}

