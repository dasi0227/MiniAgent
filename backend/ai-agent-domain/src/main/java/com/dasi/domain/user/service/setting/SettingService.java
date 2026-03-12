package com.dasi.domain.user.service.setting;

import com.dasi.domain.user.model.vo.UserApiModelVO;
import com.dasi.domain.user.model.vo.UserMcpVO;
import com.dasi.domain.user.model.vo.UserTaskVO;
import com.dasi.domain.user.model.vo.UserVO;
import com.dasi.domain.user.repository.IUserRepository;
import com.dasi.domain.util.jwt.UserContext;
import com.dasi.domain.util.jwt.IJwtUtil;
import com.dasi.domain.util.oss.IOssUtil;
import com.dasi.domain.util.random.IRandomUtil;
import com.dasi.domain.user.model.dto.SettingApiModelDTO;
import com.dasi.domain.user.model.dto.ProfileEditDTO;
import com.dasi.domain.user.model.vo.AuthVO;
import com.dasi.domain.user.model.dto.SettingMcpDTO;
import com.dasi.domain.user.model.dto.SettingTaskDTO;
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
    public AuthVO profileEdit(ProfileEditDTO dto, MultipartFile avatar) {
        String userName = dto.getUserName();
        String oldPassword = dto.getOldPassword();
        String newPassword = dto.getNewPassword();

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

        userVO = userRepository.updateUser(userVO.getUserId(), userName, newPassword, newAvatar);
        return buildAuthResponse(userVO);
    }

    private AuthVO buildAuthResponse(UserVO userVO) {
        String token = jwtUtil.generateToken(userVO);
        return AuthVO.builder()
                .token(token)
                .userName(userVO.getUserName())
                .userRole(userVO.getUserRole())
                .userAvatar(ossUtil.getObjectUrl(userVO.getUserAvatar()))
                .userStatus(userVO.getUserStatus())
                .build();
    }

    @Override
    public List<UserApiModelVO> apiModelList(String keyword) {
        return userRepository.apiModelList(keyword);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void apiModelInsert(SettingApiModelDTO dto) {
        String apiId = randomUtil.randomApiId();
        String modelId = randomUtil.randomModelId();
        userRepository.apiModelInsert(dto, apiId, modelId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void apiModelUpdate(SettingApiModelDTO dto) {
        userRepository.apiModelUpdate(dto);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void apiModelDelete(String apiId) {
        userRepository.apiModelDelete(apiId);
    }


    @Override
    public List<UserMcpVO> mcpList(String keyword) {
        return userRepository.mcpList(keyword);
    }

    @Override
    public void mcpInsert(SettingMcpDTO dto) {
        String mcpId = randomUtil.randomMcpId();
        userRepository.mcpInsert(dto, mcpId);
    }

    @Override
    public void mcpUpdate(SettingMcpDTO dto) {
        userRepository.mcpUpdate(dto);
    }

    @Override
    public void mcpDelete(String mcpId) {
        userRepository.mcpDelete(mcpId);
    }

    @Override
    public List<UserTaskVO> taskList() {
        return userRepository.taskList();
    }

    @Override
    public void taskInsert(SettingTaskDTO dto) {
        String taskId = randomUtil.randomTaskId();
        userRepository.taskInsert(dto, taskId);
    }

    @Override
    public void taskUpdate(SettingTaskDTO dto) {
        userRepository.taskUpdate(dto);
    }

    @Override
    public void taskDelete(String taskId) {
        userRepository.taskDelete(taskId);
    }

    @Override
    public void taskToggle(String taskId, Integer taskStatus) {
        userRepository.taskToggle(taskId, taskStatus);
    }
}
