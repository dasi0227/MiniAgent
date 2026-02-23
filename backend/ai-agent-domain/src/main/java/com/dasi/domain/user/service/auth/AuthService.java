package com.dasi.domain.user.service.auth;

import com.dasi.domain.user.model.vo.UserVO;
import com.dasi.domain.user.repository.IUserRepository;
import com.dasi.domain.util.jwt.IJwtService;
import com.dasi.domain.util.oss.IOssService;
import com.dasi.types.dto.request.user.AuthRequest;
import com.dasi.types.dto.response.user.AuthResponse;
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
    private IJwtService jwtService;

    @Resource
    private IOssService ossService;

    @Resource
    private PasswordEncoder passwordEncoder;

    @Override
    public AuthResponse login(AuthRequest request) {

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
    public AuthResponse register(AuthRequest request) {

        String userName = request.getUserName();
        String password = request.getPassword();

        if (userRepository.queryUserByUserName(userName) != null) {
            throw new AuthException(AUTH_USER_ALREADY_EXISTS);
        }

        String encodedPwd = passwordEncoder.encode(password);
        UserVO userVO = userRepository.insertUser(userName, encodedPwd);
        return buildAuthResponse(userVO);
    }

    private AuthResponse buildAuthResponse(UserVO userVO) {
        String token = jwtService.generateToken(userVO);
        return AuthResponse.builder()
                .token(token)
                .userId(userVO.getId())
                .userName(userVO.getUserName())
                .userRole(userVO.getUserRole())
                .userAvatar(ossService.getObjectUrl(userVO.getUserAvatar()))
                .userStatus(userVO.getUserStatus())
                .build();
    }

}
