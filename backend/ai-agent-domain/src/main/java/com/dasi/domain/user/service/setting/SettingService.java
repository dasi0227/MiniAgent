package com.dasi.domain.user.service.setting;

import com.dasi.domain.user.model.vo.UserVO;
import com.dasi.domain.user.repository.IUserRepository;
import com.dasi.domain.util.jwt.AuthContext;
import com.dasi.domain.util.jwt.IJwtService;
import com.dasi.domain.util.oss.IOssService;
import com.dasi.types.dto.request.user.EditProfileRequest;
import com.dasi.types.dto.response.user.AuthResponse;
import com.dasi.types.exception.AuthException;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import static com.dasi.types.constant.ExceptionMessage.AUTH_PASSWORD_ILLEGAL;
import static com.dasi.types.constant.ExceptionMessage.AUTH_PASSWORD_WRONG;

@Slf4j
@Service
public class SettingService implements ISettingService {

    @Resource
    private IUserRepository userRepository;

    @Resource
    private AuthContext authContext;

    @Resource
    private IJwtService jwtService;

    @Resource
    private IOssService ossService;

    @Resource
    private PasswordEncoder passwordEncoder;

    @Override
    public AuthResponse queryProfile() {
        UserVO userVO = userRepository.queryById(authContext.getUserId());
        return buildAuthResponse(userVO);
    }

    @Override
    public AuthResponse editProfile(EditProfileRequest request, MultipartFile avatar) {
        String userName = request.getUserName();
        String oldPassword = request.getOldPassword();
        String newPassword = request.getNewPassword();

        boolean hasOld = StringUtils.hasText(oldPassword);
        boolean hasNew = StringUtils.hasText(newPassword);
        if (hasOld != hasNew) {
            throw new AuthException(AUTH_PASSWORD_ILLEGAL);
        }

        UserVO userVO = userRepository.queryById(authContext.getUserId());

        String originalPassword = userVO.getPassword();
        if (hasOld) {
            if (!passwordEncoder.matches(oldPassword, originalPassword)) {
                throw new AuthException(AUTH_PASSWORD_WRONG);
            }
            newPassword = passwordEncoder.encode(newPassword);
        } else {
            newPassword = originalPassword;
        }

        String oldAvatar = userVO.getUserAvatar();
        String newAvatar = oldAvatar;
        if (avatar != null && !avatar.isEmpty()) {
            newAvatar = ossService.uploadObject(avatar);
            if (StringUtils.hasText(oldAvatar)) {
                ossService.deleteObject(oldAvatar);
            }
        }

        userVO = userRepository.updateUser(userVO.getId(), userName, newPassword, newAvatar);
        return buildAuthResponse(userVO);
    }

    private AuthResponse buildAuthResponse(UserVO userVO) {
        String token = jwtService.generateToken(userVO);
        return AuthResponse.builder()
                .token(token)
                .userId(userVO.getId())
                .userName(userVO.getUserName())
                .userRole(userVO.getUserRole())
                .userAvatar( ossService.getObjectUrl(userVO.getUserAvatar()))
                .userStatus(userVO.getUserStatus())
                .build();
    }
}
