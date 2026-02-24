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
import com.dasi.types.dto.request.user.SettingApiRequest;
import com.dasi.types.dto.request.user.SettingMcpRequest;
import com.dasi.types.exception.MiniAgentException;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Repository;

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
        return apiDao.listUserApi(keyword, userId);
    }

    @Override
    public void apiInsert(SettingApiRequest request) {
        Long userId = userContext.getUserId();

        AiApi aiApi = AiApi.builder()
                .apiId(request.getApiId())
                .apiBaseUrl(request.getApiBaseUrl())
                .apiCompletionsPath(request.getApiCompletionPath())
                .apiKey(request.getApiKey())
                .apiFrom(userId)
                .build();
        apiDao.insert(aiApi);

        AiModel aiModel = AiModel.builder()
                .apiId(request.getApiId())
                .modelId(userId + "-" + request.getModelName() + "-" + RandomStringUtils.randomAlphanumeric(6))
                .modelName(request.getModelName())
                .modelType(request.getModelType())
                .modelFrom(userId)
                .build();
        modelDao.insert(aiModel);
    }

    @Override
    public void apiUpdate(SettingApiRequest request) {
        Long userId = userContext.getUserId();

        String apiId = request.getApiId();
        AiApi aiApi = apiDao.queryByApiId(apiId);
        if (!aiApi.getApiFrom().equals(userId)) {
            throw new MiniAgentException(SETTING_USER_ILLEGAL);
        }
        aiApi.setApiBaseUrl(request.getApiBaseUrl());
        aiApi.setApiKey(request.getApiKey());
        aiApi.setApiCompletionsPath(request.getApiCompletionPath());
        apiDao.update(aiApi);

        String modelId = modelDao.queryModelIdByApiId(request.getApiId()).get(0);
        AiModel aiModel = modelDao.queryByModelId(modelId);
        if (!aiModel.getModelFrom().equals(userId)) {
            throw new MiniAgentException(SETTING_USER_ILLEGAL);
        }
        aiModel.setModelName(request.getModelName());
        aiModel.setModelType(request.getModelType());
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
        return mcpDao.listUserMcp(keyword, userId);
    }

    @Override
    public void mcpInsert(SettingMcpRequest request) {
        Long userId = userContext.getUserId();

        AiMcp aiMcp = AiMcp.builder()
                .mcpId(request.getMcpId())
                .mcpName(request.getMcpName())
                .mcpType(request.getMcpType())
                .mcpConfig(request.getMcpConfig())
                .mcpSecret(request.getMcpSecret())
                .mcpDesc(request.getMcpDesc())
                .mcpTimeout(180)
                .mcpChat(0)
                .mcpFrom(userId)
                .build();
        mcpDao.insert(aiMcp);
    }

    @Override
    public void mcpUpdate(SettingMcpRequest request) {
        Long userId = userContext.getUserId();

        AiMcp aiMcp = mcpDao.queryByMcpId(request.getMcpId());
        if (aiMcp == null || !aiMcp.getMcpFrom().equals(userId)) {
            throw new MiniAgentException(SETTING_USER_ILLEGAL);
        }
        aiMcp.setMcpName(request.getMcpName());
        aiMcp.setMcpType(request.getMcpType());
        aiMcp.setMcpConfig(request.getMcpConfig());
        aiMcp.setMcpSecret(request.getMcpSecret());
        aiMcp.setMcpDesc(request.getMcpDesc());
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
