package com.dasi.api;

import com.dasi.domain.user.model.vo.UserApiVO;
import com.dasi.types.dto.request.user.ApiManageRequest;
import com.dasi.types.dto.request.user.AuthRequest;
import com.dasi.types.dto.request.user.ProfileEditRequest;
import com.dasi.types.dto.response.user.AuthResponse;
import com.dasi.types.dto.result.Result;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IUserApi {

    Result<AuthResponse> login(AuthRequest request);

    Result<AuthResponse> register(AuthRequest request);

    Result<AuthResponse> profileQuery();

    Result<AuthResponse> profileEdit(ProfileEditRequest request, MultipartFile avatar);

    Result<List<UserApiVO>> apiList(String keyword);

    Result<Void> apiInsert(ApiManageRequest request);

    Result<Void> apiUpdate(ApiManageRequest request);

    Result<Void> apiDelete(Long id);

}
