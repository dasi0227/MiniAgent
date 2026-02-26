package com.dasi.domain.user.service.auth;

import com.dasi.domain.user.model.vo.UserVO;
import com.dasi.domain.user.repository.IUserRepository;
import com.dasi.domain.util.jwt.IJwtUtil;
import com.dasi.domain.util.oss.IOssUtil;
import com.dasi.domain.user.model.dto.AuthDTO;
import com.dasi.domain.user.model.vo.AuthVO;
import com.dasi.types.exception.AuthException;
import jakarta.annotation.Resource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import static com.dasi.types.constant.ExceptionMessage.*;

@Service
public class AuthService implements IAuthService {

    @Resource
    private IUserRepository userRepository;

    @Resource
    private IJwtUtil jwtUtil;

    @Resource
    private IOssUtil ossUtil;

    @Resource
    private PasswordEncoder passwordEncoder;

    @Override
    public AuthVO login(AuthDTO request) {

        String userName = request.getUserName();
        String password = request.getPassword();

        UserVO userVO = userRepository.queryUserByUserName(userName);
        if (userVO == null) {
            throw new AuthException(AUTH_USER_NOT_EXISTS);
        }
        if (Integer.valueOf(0).equals(userVO.getUserStatus())) {
            throw new AuthException(AUTH_USER_UNAVAILABLE);
        }
        if (!passwordEncoder.matches(password, userVO.getPassword())) {
            throw new AuthException(AUTH_LOGIN_FAIL);
        }

        return buildAuthResponse(userVO);
    }

    @Override
    public AuthVO register(AuthDTO request) {

        String userName = request.getUserName();
        String password = request.getPassword();

        if (userRepository.queryUserByUserName(userName) != null) {
            throw new AuthException(AUTH_USER_ALREADY_EXISTS);
        }

        String encodedPwd = passwordEncoder.encode(password);
        UserVO userVO = userRepository.insertUser(userName, encodedPwd);
        return buildAuthResponse(userVO);
    }

    private AuthVO buildAuthResponse(UserVO userVO) {
        String token = jwtUtil.generateToken(userVO);
        return AuthVO.builder()
                .token(token)
                .userId(userVO.getId())
                .userName(userVO.getUserName())
                .userRole(userVO.getUserRole())
                .userAvatar(ossUtil.getObjectUrl(userVO.getUserAvatar()))
                .userStatus(userVO.getUserStatus())
                .build();
    }

}
