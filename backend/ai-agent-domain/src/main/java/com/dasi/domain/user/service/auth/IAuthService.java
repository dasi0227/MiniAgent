package com.dasi.domain.user.service.auth;

import com.dasi.types.dto.request.user.AuthRequest;
import com.dasi.types.dto.response.user.AuthResponse;

public interface IAuthService {

    AuthResponse login(AuthRequest request);

    AuthResponse register(AuthRequest request);

}
