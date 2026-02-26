package com.dasi.infrastructure.repository;

import com.dasi.domain.user.model.vo.UserApiVO;
import com.dasi.domain.user.model.vo.UserMcpVO;
import com.dasi.domain.user.model.vo.UserVO;
import com.dasi.domain.user.repository.IUserRepository;
import com.dasi.domain.util.jwt.UserContext;
import com.dasi.infrastructure.persistent.dao.IAiApiDao;
import com.dasi.infrastructure.persistent.dao.IAiMcpDao;
import com.dasi.infrastructure.persistent.dao.IAiModelDao;
import com.dasi.infrastructure.persistent.dao.IAiUserDao;
import com.dasi.infrastructure.persistent.po.AiApi;
import com.dasi.infrastructure.persistent.po.AiMcp;
import com.dasi.infrastructure.persistent.po.AiModel;
import com.dasi.infrastructure.persistent.po.AiUser;
import com.dasi.infrastructure.persistent.po.AiUserApi;
import com.dasi.domain.user.model.dto.SettingApiDTO;
import com.dasi.domain.user.model.dto.SettingMcpDTO;
import com.dasi.types.exception.MiniAgentException;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

import static com.dasi.domain.user.model.enumeration.UserRoleType.ACCOUNT;
import static com.dasi.types.constant.ExceptionMessage.SETTING_USER_ILLEGAL;

@Repository
public class UserRepository implements IUserRepository {

    @Resource
    private IAiUserDao userDao;

    @Resource
    private IAiApiDao apiDao;

    @Resource
    private IAiModelDao modelDao;

    @Resource
    private IAiMcpDao mcpDao;

    @Resource
    private UserContext userContext;

    @Override
    public UserVO queryUserByUserName(String userName) {
        AiUser user = userDao.queryByUserName(userName);
        return toUserVO(user);
    }

    @Override
    public UserVO queryUserById(Long id) {
        AiUser user = userDao.queryById(id);
        return toUserVO(user);
    }

    @Override
    public UserVO insertUser(String userName, String password) {
        AiUser user = AiUser.builder()
                .userName(userName)
                .password(password)
                .userRole(ACCOUNT.getType())
                .userAvatar("")
                .userStatus(1)
                .build();
        userDao.insert(user);
        return toUserVO(user);
    }

    @Override
    public UserVO updateUser(Long id, String userName, String password, String userAvatar) {
        AiUser user = AiUser.builder()
                .id(id)
                .userName(userName)
                .password(password)
                .userAvatar(userAvatar)
                .build();
        userDao.update(user);
        return toUserVO(userDao.queryById(id));
    }

    private UserVO toUserVO(AiUser user) {
        if (user == null) {
            return null;
        }
        return UserVO.builder()
                .id(user.getId())
                .userName(user.getUserName())
                .password(user.getPassword())
                .userRole(user.getUserRole())
                .userAvatar(user.getUserAvatar())
                .userStatus(user.getUserStatus())
                .build();
    }

    @Override
    public List<UserApiVO> apiList(String keyword) {
        Long userId = userContext.getUserId();
        List<AiUserApi> userApiList = apiDao.listUserApi(keyword, userId);
        List<UserApiVO> userApiVOList = new ArrayList<>();
        if (userApiList == null || userApiList.isEmpty()) {
            return userApiVOList;
        }
        for (AiUserApi userApi : userApiList) {
            userApiVOList.add(UserApiVO.builder()
                    .id(userApi.getId())
                    .apiId(userApi.getApiId())
                    .modelName(userApi.getModelName())
                    .modelType(userApi.getModelType())
                    .apiBaseUrl(userApi.getApiBaseUrl())
                    .apiKey(userApi.getApiKey())
                    .apiCompletionPath(userApi.getApiCompletionPath())
                    .build());
        }
        return userApiVOList;
    }

    @Override
    public void apiInsert(SettingApiDTO dto, String apiId, String modelId) {
        Long userId = userContext.getUserId();

        AiApi aiApi = AiApi.builder()
                .apiId(apiId)
                .apiBaseUrl(dto.getApiBaseUrl())
                .apiCompletionsPath(dto.getApiCompletionPath())
                .apiKey(dto.getApiKey())
                .apiFrom(userId)
                .build();
        apiDao.insert(aiApi);

        AiModel aiModel = AiModel.builder()
                .apiId(apiId)
                .modelId(modelId)
                .modelName(dto.getModelName())
                .modelType(dto.getModelType())
                .modelFrom(userId)
                .build();
        modelDao.insert(aiModel);
    }

    @Override
    public void apiUpdate(SettingApiDTO dto) {
        Long userId = userContext.getUserId();
        if (dto.getId() == null) {
            throw new MiniAgentException(SETTING_USER_ILLEGAL);
        }

        AiApi aiApi = apiDao.queryById(dto.getId());
        if (aiApi == null || !aiApi.getApiFrom().equals(userId)) {
            throw new MiniAgentException(SETTING_USER_ILLEGAL);
        }
        aiApi.setApiBaseUrl(dto.getApiBaseUrl());
        aiApi.setApiKey(dto.getApiKey());
        aiApi.setApiCompletionsPath(dto.getApiCompletionPath());
        apiDao.update(aiApi);

        String modelId = modelDao.queryModelIdByApiId(aiApi.getApiId()).get(0);
        AiModel aiModel = modelDao.queryByModelId(modelId);
        if (!aiModel.getModelFrom().equals(userId)) {
            throw new MiniAgentException(SETTING_USER_ILLEGAL);
        }
        aiModel.setModelName(dto.getModelName());
        aiModel.setModelType(dto.getModelType());
        aiModel.setModelFrom(userId);
        modelDao.update(aiModel);
    }

    @Override
    public void apiDelete(Long id) {
        Long userId = userContext.getUserId();

        AiApi aiApi = apiDao.queryById(id);
        if (!aiApi.getApiFrom().equals(userId)) {
            throw new MiniAgentException(SETTING_USER_ILLEGAL);
        }
        apiDao.deleteById(id);

        String modelId = modelDao.queryModelIdByApiId(aiApi.getApiId()).get(0);
        AiModel aiModel = modelDao.queryByModelId(modelId);
        if (!aiModel.getModelFrom().equals(userId)) {
            throw new MiniAgentException(SETTING_USER_ILLEGAL);
        }
        modelDao.deleteByModelId(modelId);
    }

    @Override
    public List<UserMcpVO> mcpList(String keyword) {
        Long userId = userContext.getUserId();
        List<AiMcp> mcpList = mcpDao.listUserMcp(keyword, userId);
        List<UserMcpVO> userMcpVOList = new ArrayList<>();
        if (mcpList == null || mcpList.isEmpty()) {
            return userMcpVOList;
        }
        for (AiMcp aiMcp : mcpList) {
            userMcpVOList.add(UserMcpVO.builder()
                    .id(aiMcp.getId())
                    .mcpId(aiMcp.getMcpId())
                    .mcpName(aiMcp.getMcpName())
                    .mcpType(aiMcp.getMcpType())
                    .mcpDesc(aiMcp.getMcpDesc())
                    .mcpParam(aiMcp.getMcpParam())
                    .mcpSecret(aiMcp.getMcpSecret())
                    .build());
        }
        return userMcpVOList;
    }

    @Override
    public void mcpInsert(SettingMcpDTO dto, String mcpId) {
        Long userId = userContext.getUserId();

        AiMcp aiMcp = AiMcp.builder()
                .mcpId(mcpId)
                .mcpName(dto.getMcpName())
                .mcpType(dto.getMcpType())
                .mcpParam(dto.getMcpParam())
                .mcpSecret(dto.getMcpSecret())
                .mcpDesc(dto.getMcpDesc())
                .mcpTimeout(180)
                .mcpChat(0)
                .mcpFrom(userId)
                .build();
        mcpDao.insert(aiMcp);
    }

    @Override
    public void mcpUpdate(SettingMcpDTO dto) {
        Long userId = userContext.getUserId();
        if (dto.getId() == null) {
            throw new MiniAgentException(SETTING_USER_ILLEGAL);
        }

        AiMcp aiMcp = mcpDao.queryById(dto.getId());
        if (aiMcp == null || !aiMcp.getMcpFrom().equals(userId)) {
            throw new MiniAgentException(SETTING_USER_ILLEGAL);
        }
        aiMcp.setMcpName(dto.getMcpName());
        aiMcp.setMcpType(dto.getMcpType());
        aiMcp.setMcpParam(dto.getMcpParam());
        aiMcp.setMcpSecret(dto.getMcpSecret());
        aiMcp.setMcpDesc(dto.getMcpDesc());
        mcpDao.update(aiMcp);
    }

    @Override
    public void mcpDelete(Long id) {
        Long userId = userContext.getUserId();

        AiMcp aiMcp = mcpDao.queryById(id);
        if (aiMcp == null || !aiMcp.getMcpFrom().equals(userId)) {
            throw new MiniAgentException(SETTING_USER_ILLEGAL);
        }
        mcpDao.delete(id);
    }

}
