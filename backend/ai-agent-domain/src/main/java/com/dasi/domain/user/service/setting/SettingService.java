package com.dasi.domain.user.service.setting;

import com.dasi.domain.user.model.vo.UserApiVO;
import com.dasi.domain.user.model.vo.UserMcpVO;
import com.dasi.domain.user.model.vo.UserVO;
import com.dasi.domain.user.repository.IUserRepository;
import com.dasi.domain.util.jwt.UserContext;
import com.dasi.domain.util.jwt.IJwtUtil;
import com.dasi.domain.util.oss.IOssUtil;
import com.dasi.domain.util.random.IRandomUtil;
import com.dasi.types.dto.request.user.SettingApiRequest;
import com.dasi.types.dto.request.user.ProfileEditRequest;
import com.dasi.domain.user.model.vo.AuthVO;
import com.dasi.types.dto.request.user.SettingMcpRequest;
import com.dasi.types.exception.AuthException;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static com.dasi.types.constant.ExceptionMessage.AUTH_PASSWORD_FAIL;
import static com.dasi.types.constant.ExceptionMessage.AUTH_PASSWORD_WRONG;

@Slf4j
@Service
public class SettingService implements ISettingService {

    @Resource
    private IUserRepository userRepository;

    @Resource
    private UserContext userContext;

    @Resource
    private IJwtUtil jwtUtil;

    @Resource
    private IOssUtil ossUtil;

    @Resource
    private IRandomUtil randomUtil;

    @Resource
    private PasswordEncoder passwordEncoder;

    @Override
    public AuthVO profileQuery() {
        UserVO userVO = userRepository.queryUserById(userContext.getUserId());
        return buildAuthResponse(userVO);
    }

    @Override
    public AuthVO profileEdit(ProfileEditRequest request, MultipartFile avatar) {
        String userName = request.getUserName();
        String oldPassword = request.getOldPassword();
        String newPassword = request.getNewPassword();

        boolean hasOld = StringUtils.hasText(oldPassword);
        boolean hasNew = StringUtils.hasText(newPassword);
        if (hasOld != hasNew) {
            throw new AuthException(AUTH_PASSWORD_FAIL);
        }

        UserVO userVO = userRepository.queryUserById(userContext.getUserId());

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
            newAvatar = ossUtil.uploadObject(avatar);
            if (StringUtils.hasText(oldAvatar)) {
                ossUtil.deleteObject(oldAvatar);
            }
        }

        userVO = userRepository.updateUser(userVO.getId(), userName, newPassword, newAvatar);
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

    @Override
    public List<UserApiVO> apiList(String keyword) {
        return userRepository.apiList(keyword);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void apiInsert(SettingApiRequest request) {
        String apiId = "api_" + randomUtil.userRandom();
        String modelId = "model_" + randomUtil.userRandom();
        userRepository.apiInsert(request, apiId, modelId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void apiUpdate(SettingApiRequest request) {
        userRepository.apiUpdate(request);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void apiDelete(Long id) {
        userRepository.apiDelete(id);
    }


    @Override
    public List<UserMcpVO> mcpList(String keyword) {
        return userRepository.mcpList(keyword);
    }

    @Override
    public void mcpInsert(SettingMcpRequest request) {
        String mcpId = "mcp_" + randomUtil.userRandom();
        userRepository.mcpInsert(request, mcpId);
    }

    @Override
    public void mcpUpdate(SettingMcpRequest request) {
        userRepository.mcpUpdate(request);
    }

    @Override
    public void mcpDelete(Long id) {
        userRepository.mcpDelete(id);
    }
}
