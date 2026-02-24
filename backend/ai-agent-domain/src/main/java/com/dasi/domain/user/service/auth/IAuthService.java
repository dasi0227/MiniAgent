package com.dasi.domain.user.service.auth;

import com.dasi.types.dto.request.user.AuthRequest;
import com.dasi.domain.user.model.vo.AuthVO;

public interface IAuthService {

    AuthVO login(AuthRequest request);

    AuthVO register(AuthRequest request);

}
