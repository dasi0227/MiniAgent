package com.dasi.domain.user.service.auth;

import com.dasi.domain.user.model.dto.AuthDTO;
import com.dasi.domain.user.model.vo.AuthVO;

public interface IAuthService {

    AuthVO login(AuthDTO request);

    AuthVO register(AuthDTO request);

}
