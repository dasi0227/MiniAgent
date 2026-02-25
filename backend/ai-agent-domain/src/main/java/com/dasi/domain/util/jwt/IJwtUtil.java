package com.dasi.domain.util.jwt;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.dasi.domain.user.model.vo.UserVO;

public interface IJwtUtil {

    String generateToken(UserVO userVO);

    UserVO parseToken(String token);

    DecodedJWT verifyToken(String token);

}
