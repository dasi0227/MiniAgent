package com.dasi.domain.user.service.auth;

import com.dasi.domain.user.model.vo.UserVO;
import com.dasi.domain.user.repository.IUserRepository;
import com.dasi.domain.util.jwt.AuthContext;
import com.dasi.domain.util.jwt.IJwtService;
import com.dasi.types.dto.request.user.AuthRequest;
import com.dasi.types.dto.response.user.AuthResponse;
import com.dasi.types.exception.AuthException;
import jakarta.annotation.Resource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService implements IAuthService {

    @Resource
    private IUserRepository userRepository;

    @Resource
    private IJwtService jwtService;

    @Resource
    private PasswordEncoder passwordEncoder;

    @Resource
    private AuthContext authContext;

    @Override
    public AuthResponse login(AuthRequest request) {

        String username = request.getUsername();
        String password = request.getPassword();

        UserVO userVO = userRepository.queryByUsername(username);
        if (userVO == null) {
            throw new AuthException("用户不存在");
        }
        if (!passwordEncoder.matches(password, userVO.getPassword())) {
            throw new AuthException("用户名或密码错误");
        }
        if (Integer.valueOf(0).equals(userVO.getUserStatus())) {
            throw new AuthException("账号已被禁用");
        }

        return buildAuthResponse(userVO);
    }

    @Override
    public AuthResponse register(AuthRequest request) {

        String username = request.getUsername();
        String password = request.getPassword();

        if (userRepository.queryByUsername(username) != null) {
            throw new AuthException("用户名已存在");
        }

        String encodedPwd = passwordEncoder.encode(password);
        UserVO userVO = userRepository.insertUser(username, encodedPwd);
        return buildAuthResponse(userVO);
    }

    @Override
    public AuthResponse profile() {
        if (authContext.getUserId() == null) {
            throw new AuthException("未登录");
        }

        UserVO userVO = userRepository.queryById(authContext.getUserId());
        if (userVO == null) {
            throw new AuthException("用户不存在或已被删除");
        }
        if (Integer.valueOf(0).equals(userVO.getUserStatus())) {
            throw new AuthException("账号已被禁用");
        }

        return buildAuthResponse(userVO);
    }

    private AuthResponse buildAuthResponse(UserVO userVO) {
        String token = jwtService.generateToken(userVO);
        return AuthResponse.builder()
                .token(token)
                .userId(userVO.getId())
                .username(userVO.getUsername())
                .userrole(userVO.getUserrole())
                .userStatus(userVO.getUserStatus())
                .build();
    }

}
