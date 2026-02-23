package com.dasi.api;

import com.dasi.types.dto.request.user.AuthRequest;
import com.dasi.types.dto.request.user.EditProfileRequest;
import com.dasi.types.dto.response.user.AuthResponse;
import com.dasi.types.dto.result.Result;
import org.springframework.web.multipart.MultipartFile;

public interface IUserApi {

    Result<AuthResponse> login(AuthRequest request);

    Result<AuthResponse> register(AuthRequest request);

    Result<AuthResponse> queryProfile();

    Result<AuthResponse> editProfile(EditProfileRequest request, MultipartFile avatar);

}
