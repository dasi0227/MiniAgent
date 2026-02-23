package com.dasi.domain.user.service.setting;

import com.dasi.domain.user.model.vo.UserApiVO;
import com.dasi.types.dto.request.user.ApiManageRequest;
import com.dasi.types.dto.request.user.ProfileEditRequest;
import com.dasi.types.dto.response.user.AuthResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ISettingService {

    AuthResponse profileQuery();

    AuthResponse profileEdit(ProfileEditRequest request, MultipartFile avatar);

    List<UserApiVO> apiList(String keyword);

    void apiInsert(ApiManageRequest request);

    void apiUpdate(ApiManageRequest request);

    void apiDelete(Long id);

}
