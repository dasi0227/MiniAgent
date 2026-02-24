package com.dasi.api;

import com.dasi.domain.user.model.vo.UserApiVO;
import com.dasi.types.dto.request.user.SettingApiRequest;
import com.dasi.types.dto.request.user.AuthRequest;
import com.dasi.types.dto.request.user.ProfileEditRequest;
import com.dasi.domain.user.model.vo.AuthVO;
import com.dasi.types.dto.result.Result;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IUserApi {

    Result<AuthVO> login(AuthRequest request);

    Result<AuthVO> register(AuthRequest request);

    Result<AuthVO> profileQuery();

    Result<AuthVO> profileEdit(ProfileEditRequest request, MultipartFile avatar);

    Result<List<UserApiVO>> apiList(String keyword);

    Result<Void> apiInsert(SettingApiRequest request);

    Result<Void> apiUpdate(SettingApiRequest request);

    Result<Void> apiDelete(Long id);

}
