package com.dasi.domain.user.service.setting;

import com.dasi.types.dto.request.user.EditProfileRequest;
import com.dasi.types.dto.response.user.AuthResponse;
import org.springframework.web.multipart.MultipartFile;

public interface ISettingService {

    AuthResponse queryProfile();

    AuthResponse editProfile(EditProfileRequest request, MultipartFile avatar);

}
