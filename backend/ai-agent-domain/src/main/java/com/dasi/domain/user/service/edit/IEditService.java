package com.dasi.domain.user.service.edit;

import com.dasi.types.dto.request.user.EditProfileRequest;
import com.dasi.types.dto.response.user.AuthResponse;

public interface IEditService {

    AuthResponse editProfile(EditProfileRequest request);

}
