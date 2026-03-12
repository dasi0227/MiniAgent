package com.dasi.infrastructure.repository;

import com.dasi.domain.user.model.vo.UserApiModelVO;
import com.dasi.domain.user.model.vo.UserMcpVO;
import com.dasi.domain.user.model.vo.UserTaskVO;
import com.dasi.domain.user.model.vo.UserVO;
import com.dasi.domain.user.repository.IUserRepository;
import com.dasi.domain.util.jwt.UserContext;
import com.dasi.domain.util.random.IRandomUtil;
import com.dasi.infrastructure.persistent.dao.IAiApiDao;
import com.dasi.infrastructure.persistent.dao.IAiAgentDao;
import com.dasi.infrastructure.persistent.dao.IAiClientDao;
import com.dasi.infrastructure.persistent.dao.IAiMcpDao;
import com.dasi.infrastructure.persistent.dao.IAiModelDao;
import com.dasi.infrastructure.persistent.dao.IAiTaskDao;
import com.dasi.infrastructure.persistent.dao.IAiUserDao;
import com.dasi.infrastructure.persistent.po.AiAgent;
import com.dasi.infrastructure.persistent.po.AiApi;
import com.dasi.infrastructure.persistent.po.AiClient;
import com.dasi.infrastructure.persistent.po.AiMcp;
import com.dasi.infrastructure.persistent.po.AiModel;
import com.dasi.infrastructure.persistent.po.AiTask;
import com.dasi.infrastructure.persistent.po.AiUser;
import com.dasi.infrastructure.persistent.po.AiApiModel;
import com.dasi.domain.user.model.dto.SettingApiModelDTO;
import com.dasi.domain.user.model.dto.SettingMcpDTO;
import com.dasi.domain.user.model.dto.SettingTaskDTO;
import com.dasi.types.annotation.CacheEvict;
import com.dasi.types.annotation.Cacheable;
import com.dasi.types.enumeration.CacheType;
import com.dasi.types.exception.MiniAgentException;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

import static com.dasi.domain.user.model.enumeration.UserRoleType.ACCOUNT;
import static com.dasi.types.constant.ExceptionMessage.ILLEGAL_USER;
import static com.dasi.types.constant.ExceptionMessage.LACK_PARAM;
import static com.dasi.types.constant.RedisConstant.*;

@Repository
public class UserRepository implements IUserRepository {

    @Resource
    private IAiUserDao userDao;

    @Resource
    private IAiApiDao apiDao;

    @Resource
    private IAiModelDao modelDao;

    @Resource
    private IAiClientDao clientDao;

    @Resource
    private IAiMcpDao mcpDao;

    @Resource
    private IAiTaskDao taskDao;

    @Resource
    private IAiAgentDao agentDao;

    @Resource
    private UserContext userContext;

    @Resource
    private IRandomUtil randomUtil;

    @Override
    public UserVO queryUserByUserName(String userName) {
        AiUser user = userDao.queryByUserName(userName);
        return toUserVO(user);
    }

    @Override
    public UserVO queryUserById(Long userId) {
        AiUser user = userDao.queryById(userId);
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
    public UserVO updateUser(Long userId, String userName, String password, String userAvatar) {
        AiUser user = AiUser.builder()
                .id(userId)
                .userName(userName)
                .password(password)
                .userAvatar(userAvatar)
                .build();
        userDao.update(user);
        return toUserVO(userDao.queryById(userId));
    }

    private UserVO toUserVO(AiUser user) {
        if (user == null) {
            return null;
        }
        return UserVO.builder()
                .userId(user.getId())
                .userName(user.getUserName())
                .password(user.getPassword())
                .userRole(user.getUserRole())
                .userAvatar(user.getUserAvatar())
                .userStatus(user.getUserStatus())
                .build();
    }

    // -------------------- API/Model --------------------
    @Override
    @Cacheable(cachePrefix = USER_API_PREFIX, cacheClass = UserApiModelVO.class, cacheType = CacheType.LIST)
    public List<UserApiModelVO> apiModelList(String keyword) {
        Long userId = userContext.getUserId();
        List<AiApiModel> aiApiModelList = apiDao.listUserApi(keyword, userId);
        List<UserApiModelVO> userApiModelVOList = new ArrayList<>();
        if (aiApiModelList == null || aiApiModelList.isEmpty()) {
            return userApiModelVOList;
        }
        for (AiApiModel aiApiModel : aiApiModelList) {
            userApiModelVOList.add(UserApiModelVO.builder()
                    .apiId(aiApiModel.getApiId())
                    .modelId(aiApiModel.getModelId())
                    .modelName(aiApiModel.getModelName())
                    .modelType(aiApiModel.getModelType())
                    .apiBaseUrl(aiApiModel.getApiBaseUrl())
                    .apiKey(aiApiModel.getApiKey())
                    .apiCompletionPath(aiApiModel.getApiCompletionPath())
                    .build());
        }
        return userApiModelVOList;
    }

    @Override
    @CacheEvict(keyPrefix = {"ai:", "query:", "user:", "workspace:"})
    public void apiModelInsert(SettingApiModelDTO dto, String apiId, String modelId) {
        Long userId = userContext.getUserId();

        // 新增 api
        AiApi aiApi = AiApi.builder()
                .apiId(apiId)
                .apiBaseUrl(dto.getApiBaseUrl())
                .apiCompletionsPath(dto.getApiCompletionPath())
                .apiKey(dto.getApiKey())
                .apiFrom(userId)
                .build();
        apiDao.insert(aiApi);

        // 新增 model
        AiModel aiModel = AiModel.builder()
                .apiId(apiId)
                .modelId(modelId)
                .modelName(dto.getModelName())
                .modelType(dto.getModelType())
                .modelFrom(userId)
                .build();
        modelDao.insert(aiModel);

        // 新增 client
        AiClient aiClient = AiClient.builder()
                .clientId(randomUtil.randomClientId())
                .clientType("chat")
                .clientRole("chatclient")
                .modelId(modelId)
                .modelName(dto.getModelName())
                .clientName(dto.getModelName())
                .clientStatus(1)
                .clientFrom(userId)
                .build();
        clientDao.insert(aiClient);
    }

    @Override
    @CacheEvict(keyPrefix = {"ai:", "query:", "user:", "workspace:"})
    public void apiModelUpdate(SettingApiModelDTO dto) {
        Long userId = userContext.getUserId();
        if (dto.getApiId() == null) {
            throw new MiniAgentException(LACK_PARAM);
        }

        // 更改 api
        AiApi aiApi = apiDao.queryByApiId(dto.getApiId());
        if (aiApi == null || !aiApi.getApiFrom().equals(userId)) {
            throw new MiniAgentException(ILLEGAL_USER);
        }
        aiApi.setApiBaseUrl(dto.getApiBaseUrl());
        aiApi.setApiKey(dto.getApiKey());
        aiApi.setApiCompletionsPath(dto.getApiCompletionPath());
        apiDao.update(aiApi);

        // 更改 model
        String modelId = modelDao.queryModelIdByApiId(aiApi.getApiId()).get(0);
        AiModel aiModel = modelDao.queryByModelId(modelId);
        if (!aiModel.getModelFrom().equals(userId)) {
            throw new MiniAgentException(ILLEGAL_USER);
        }
        aiModel.setModelName(dto.getModelName());
        aiModel.setModelType(dto.getModelType());
        aiModel.setModelFrom(userId);
        modelDao.update(aiModel);

        // 更改 client
        AiClient chatClient = clientDao.queryChatClientByModelIdAndUserId(modelId, userId);
        if (chatClient != null) {
            chatClient.setModelName(dto.getModelName());
            chatClient.setClientName(dto.getModelName());
            chatClient.setClientStatus(1);
            clientDao.update(chatClient);
        } else {
            AiClient aiClient = AiClient.builder()
                    .clientId(randomUtil.randomClientId())
                    .clientType("chat")
                    .clientRole("chatclient")
                    .modelId(modelId)
                    .modelName(dto.getModelName())
                    .clientName(dto.getModelName())
                    .clientStatus(1)
                    .clientFrom(userId)
                    .build();
            clientDao.insert(aiClient);
        }

    }

    @Override
    @CacheEvict(keyPrefix = {"ai:", "query:", "user:", "workspace:"})
    public void apiModelDelete(String apiId) {
        Long userId = userContext.getUserId();

        AiApi aiApi = apiDao.queryByApiId(apiId);
        if (!aiApi.getApiFrom().equals(userId)) {
            throw new MiniAgentException(ILLEGAL_USER);
        }
        apiDao.deleteByApiId(apiId);

        AiModel aiModel = modelDao.queryByApiId(apiId);
        String modelId = aiModel.getModelId();
        if (!aiModel.getModelFrom().equals(userId)) {
            throw new MiniAgentException(ILLEGAL_USER);
        }
        modelDao.deleteByModelId(modelId);

        AiClient aiClient = clientDao.queryChatClientByModelIdAndUserId(modelId, userId);
        if (aiClient != null) {
            if (!aiClient.getClientFrom().equals(userId)) {
                throw new MiniAgentException(ILLEGAL_USER);
            }
            clientDao.deleteByClientId(aiClient.getClientId());
        }
    }

    // -------------------- MCP --------------------
    @Override
    @Cacheable(cachePrefix = USER_MCP_PREFIX, cacheClass = UserMcpVO.class, cacheType = CacheType.LIST)
    public List<UserMcpVO> mcpList(String keyword) {
        Long userId = userContext.getUserId();
        List<AiMcp> mcpList = mcpDao.listUserMcp(keyword, userId);
        List<UserMcpVO> userMcpVOList = new ArrayList<>();
        if (mcpList == null || mcpList.isEmpty()) {
            return userMcpVOList;
        }
        for (AiMcp aiMcp : mcpList) {
            userMcpVOList.add(UserMcpVO.builder()
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
    @CacheEvict(keyPrefix = {"ai:", "query:", "user:", "workspace:"})
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
    @CacheEvict(keyPrefix = {"ai:", "query:", "user:", "workspace:"})
    public void mcpUpdate(SettingMcpDTO dto) {
        Long userId = userContext.getUserId();
        if (dto.getMcpId() == null) {
            throw new MiniAgentException(LACK_PARAM);
        }

        AiMcp aiMcp = mcpDao.queryByMcpId(dto.getMcpId());
        if (aiMcp == null || !aiMcp.getMcpFrom().equals(userId)) {
            throw new MiniAgentException(ILLEGAL_USER);
        }
        aiMcp.setMcpName(dto.getMcpName());
        aiMcp.setMcpType(dto.getMcpType());
        aiMcp.setMcpParam(dto.getMcpParam());
        aiMcp.setMcpSecret(dto.getMcpSecret());
        aiMcp.setMcpDesc(dto.getMcpDesc());
        mcpDao.update(aiMcp);
    }

    @Override
    @CacheEvict(keyPrefix = {"ai:", "query:", "user:", "workspace:"})
    public void mcpDelete(String mcpId) {
        Long userId = userContext.getUserId();

        AiMcp aiMcp = mcpDao.queryByMcpId(mcpId);
        if (aiMcp == null || !aiMcp.getMcpFrom().equals(userId)) {
            throw new MiniAgentException(ILLEGAL_USER);
        }
        mcpDao.deleteByMcpId(mcpId);
    }

    // -------------------- Task --------------------
    @Override
    @Cacheable(cachePrefix = USER_TASK_PREFIX, cacheClass = UserTaskVO.class, cacheType = CacheType.LIST)
    public List<UserTaskVO> taskList() {
        Long userId = userContext.getUserId();
        List<AiTask> taskList = taskDao.queryByTaskFrom(userId);
        List<UserTaskVO> userTaskVOList = new ArrayList<>();
        if (taskList == null || taskList.isEmpty()) {
            return userTaskVOList;
        }
        for (AiTask aiTask : taskList) {
            userTaskVOList.add(toUserTaskVO(aiTask));
        }
        return userTaskVOList;
    }

    @Override
    @CacheEvict(keyPrefix = {"ai:", "query:", "user:", "workspace:"})
    public void taskInsert(SettingTaskDTO dto, String taskId) {
        Long userId = userContext.getUserId();
        validateOwnedAgent(dto.getAgentId(), userId);

        AiTask aiTask = AiTask.builder()
                .taskId(taskId)
                .agentId(dto.getAgentId())
                .taskCron(dto.getTaskCron())
                .taskDesc(dto.getTaskDesc())
                .taskParam(dto.getTaskParam())
                .taskStatus(dto.getTaskStatus())
                .taskFrom(userId)
                .build();
        taskDao.insert(aiTask);
    }

    @Override
    @CacheEvict(keyPrefix = {"ai:", "query:", "user:", "workspace:"})
    public void taskUpdate(SettingTaskDTO dto) {
        Long userId = userContext.getUserId();
        if (dto.getTaskId() == null) {
            throw new MiniAgentException(LACK_PARAM);
        }

        AiTask aiTask = taskDao.queryByTaskIdAndFrom(dto.getTaskId(), userId);
        if (aiTask == null) {
            throw new MiniAgentException(ILLEGAL_USER);
        }
        validateOwnedAgent(dto.getAgentId(), userId);

        aiTask.setAgentId(dto.getAgentId());
        aiTask.setTaskCron(dto.getTaskCron());
        aiTask.setTaskDesc(dto.getTaskDesc());
        aiTask.setTaskParam(dto.getTaskParam());
        aiTask.setTaskStatus(dto.getTaskStatus());
        taskDao.update(aiTask);
    }

    @Override
    @CacheEvict(keyPrefix = {"ai:", "query:", "user:", "workspace:"})
    public void taskDelete(String taskId) {
        Long userId = userContext.getUserId();
        AiTask aiTask = taskDao.queryByTaskIdAndFrom(taskId, userId);
        if (aiTask == null) {
            throw new MiniAgentException(ILLEGAL_USER);
        }
        taskDao.delete(aiTask.getId());
    }

    @Override
    @CacheEvict(keyPrefix = {"ai:", "query:", "user:", "workspace:"})
    public void taskToggle(String taskId, Integer taskStatus) {
        Long userId = userContext.getUserId();
        AiTask aiTask = taskDao.queryByTaskIdAndFrom(taskId, userId);
        if (aiTask == null) {
            throw new MiniAgentException(ILLEGAL_USER);
        }
        aiTask.setTaskStatus(taskStatus);
        taskDao.toggle(aiTask);
    }

    private void validateOwnedAgent(String agentId, Long userId) {
        AiAgent aiAgent = agentDao.queryAgentByAgentId(agentId);
        if (aiAgent == null || !userId.equals(aiAgent.getAgentFrom())) {
            throw new MiniAgentException(ILLEGAL_USER);
        }
    }

    private UserTaskVO toUserTaskVO(AiTask aiTask) {
        if (aiTask == null) {
            return null;
        }
        return UserTaskVO.builder()
                .taskId(aiTask.getTaskId())
                .agentId(aiTask.getAgentId())
                .taskCron(aiTask.getTaskCron())
                .taskDesc(aiTask.getTaskDesc())
                .taskParam(aiTask.getTaskParam())
                .taskStatus(aiTask.getTaskStatus())
                .updateTime(aiTask.getUpdateTime())
                .build();
    }

}
