package com.dasi.domain.user.service.edit;

import com.dasi.domain.user.model.vo.UserVO;
import com.dasi.domain.user.repository.IUserRepository;
import com.dasi.domain.util.jwt.AuthContext;
import com.dasi.domain.util.jwt.IJwtService;
import com.dasi.types.dto.request.user.EditProfileRequest;
import com.dasi.types.dto.response.user.AuthResponse;
import com.dasi.types.exception.AuthException;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EditService implements IEditService {

    @Resource
    private IUserRepository userRepository;

    @Resource
    private AuthContext authContext;

    @Resource
    private IJwtService jwtService;

    @Resource
    private PasswordEncoder passwordEncoder;

    @Override
    public AuthResponse editProfile(EditProfileRequest request) {
        
        String username = request.getUsername();
        String newPassword = request.getNewPassword();
        String oldPassword = request.getOldPassword();

        UserVO userVO = userRepository.queryById(authContext.getUserId());
        if (userVO == null) {
            throw new AuthException("用户不存在或已被删除");
        }

        if (Integer.valueOf(0).equals(userVO.getUserStatus())) {
            throw new AuthException("账号已被禁用");
        }

        String originalPassword = userVO.getPassword();
        if (!passwordEncoder.matches(oldPassword, originalPassword)) {
            throw new AuthException("旧密码不正确");
        }

        String encodedPwd = passwordEncoder.encode(newPassword);
        userVO = userRepository.updateUser(userVO.getId(), username, encodedPwd);
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
