package com.dasi.infrastructure.repository;

import com.alibaba.fastjson2.JSON;
import com.dasi.domain.util.jwt.UserContext;
import com.dasi.domain.util.random.IRandomUtil;
import com.dasi.domain.util.snapshot.ISnapshotUtil;
import com.dasi.domain.util.snapshot.SnapshotView;
import com.dasi.domain.workspace.model.dto.*;
import com.dasi.domain.workspace.model.entity.RolePromptEntity;
import com.dasi.domain.workspace.model.enumeration.ConfigType;
import com.dasi.domain.workspace.model.enumeration.RepoType;
import com.dasi.domain.workspace.model.vo.AgentVO;
import com.dasi.domain.workspace.model.vo.CommentVO;
import com.dasi.domain.workspace.model.vo.PlazaVO;
import com.dasi.domain.workspace.model.vo.RepoVO;
import com.dasi.domain.workspace.model.vo.TemplateVO;
import com.dasi.domain.workspace.repository.IWorkspaceRepository;
import com.dasi.infrastructure.persistent.dao.*;
import com.dasi.infrastructure.persistent.po.*;
import com.dasi.types.annotation.CacheEvict;
import com.dasi.types.annotation.Cacheable;
import com.dasi.types.enumeration.CacheType;
import com.dasi.types.exception.WorkException;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.dasi.types.constant.ExceptionMessage.ILLEGAL_DATA;
import static com.dasi.types.constant.ExceptionMessage.ILLEGAL_USER;
import static com.dasi.types.constant.RedisConstant.*;

@Slf4j
@Repository
public class WorkspaceRepository implements IWorkspaceRepository {

    private static final String DEFAULT_ADVISOR_ID = "advisor_work_memory";

    @Resource
    private UserContext userContext;

    @Resource
    private IAiPlazaDao aiPlazaDao;

    @Resource
    private IAiPlazaLikeDao aiPlazaLikeDao;

    @Resource
    private IAiPlazaFavorDao aiPlazaFavorDao;

    @Resource
    private IAiPlazaCommentDao aiPlazaCommentDao;

    @Resource
    private IAiTemplateDao aiTemplateDao;

    @Resource
    private IAiRepoDao aiRepoDao;

    @Resource
    private IAiAgentDao aiAgentDao;

    @Resource
    private IAiFlowDao aiFlowDao;

    @Resource
    private IAiConfigDao aiConfigDao;

    @Resource
    private IAiClientDao aiClientDao;

    @Resource
    private IAiTaskDao aiTaskDao;

    @Resource
    private IAiPromptDao aiPromptDao;

    @Resource
    private IAiUserDao aiUserDao;

    @Resource
    private IAiModelDao aiModelDao;

    @Resource
    private IAiApiDao aiApiDao;

    @Resource
    private IAiMcpDao aiMcpDao;

    @Resource
    private IRandomUtil randomUtil;

    @Resource
    private ISnapshotUtil snapshotUtil;

    @Override
    @Cacheable(cachePrefix = WORKSPACE_PLAZA_PREFIX, cacheType = CacheType.LIST, cacheClass = PlazaVO.class)
    public List<PlazaVO> plazaPage(PlazaPageDTO dto) {
        int pageNum = dto.getPageNum();
        int pageSize = dto.getPageSize();
        int offset = (pageNum - 1) * pageSize;

        List<AiPlaza> aiPlazaList = aiPlazaDao.page(dto.getKeyword(), dto.getSortBy(), dto.getSortOrder(), offset, pageSize);
        if (aiPlazaList == null || aiPlazaList.isEmpty()) {
            return List.of();
        }

        Long userId = userContext.getUserId();
        List<String> plazaIdList = aiPlazaList.stream().map(AiPlaza::getPlazaId).filter(StringUtils::hasText).distinct().toList();
        List<String> templateIdList = aiPlazaList.stream().map(AiPlaza::getTemplateId).filter(StringUtils::hasText).distinct().toList();

        Set<String> likedList = aiPlazaLikeDao.queryLikedByUserIdAndPlazaIdList(userId, plazaIdList);
        Set<String> favoredList = aiPlazaFavorDao.queryFavoredByUserIdAndPlazaIdList(userId, plazaIdList);
        Set<String> commentedList = aiPlazaCommentDao.queryCommentedByUserIdAndPlazaIdList(userId, plazaIdList);
        Set<String> forkedList = aiRepoDao.queryForkedByUserIdAndTemplateIdList(userId, templateIdList);

        Map<Long, String> plazaUserNameMap = aiPlazaList.stream()
                .map(AiPlaza::getUserId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toMap(
                        Function.identity(),
                        plazaUserId -> plazaUserId == 0L ? "system" : aiUserDao.queryUserNameById(plazaUserId)
                ));

        return aiPlazaList.stream().map(aiPlaza -> PlazaVO.builder()
                .plazaId(aiPlaza.getPlazaId())
                .templateId(aiPlaza.getTemplateId())
                .agentName(aiPlaza.getAgentName())
                .agentType(aiPlaza.getAgentType())
                .plazaTitle(aiPlaza.getPlazaTitle())
                .plazaDesc(aiPlaza.getPlazaDesc())
                .likeCount(aiPlaza.getLikeCount())
                .favorCount(aiPlaza.getFavorCount())
                .commentCount(aiPlaza.getCommentCount())
                .userName(plazaUserNameMap.get(aiPlaza.getUserId()))
                .liked(likedList.contains(aiPlaza.getPlazaId()))
                .favored(favoredList.contains(aiPlaza.getPlazaId()))
                .commented(commentedList.contains(aiPlaza.getPlazaId()))
                .forked(forkedList.contains(aiPlaza.getTemplateId()))
                .createTime(aiPlaza.getCreateTime())
                .build()).toList();
    }

    @Override
    @Cacheable(cachePrefix = WORKSPACE_PLAZA_PREFIX, cacheType = CacheType.VALUE, cacheClass = Integer.class)
    public Integer plazaCount(PlazaPageDTO dto) {
        Integer total = aiPlazaDao.count(dto.getKeyword());
        return total == null ? 0 : total;
    }

    @Override
    @Cacheable(cachePrefix = WORKSPACE_COMMENT_PREFIX, cacheType = CacheType.LIST, cacheClass = CommentVO.class)
    public List<CommentVO> plazaCommentList(PlazaCommentAreaDTO dto) {
        Long userId = userContext.getUserId();
        int pageSize = dto.getPageSize();
        int offset = (dto.getPageNum() - 1) * pageSize;
        String plazaId = dto.getPlazaId();

        List<AiPlazaComment> aiPlazaCommentList = aiPlazaCommentDao.listByPlazaId(plazaId, offset, pageSize);
        if (aiPlazaCommentList == null || aiPlazaCommentList.isEmpty()) {
            return List.of();
        }
        return aiPlazaCommentList.stream()
                .map(aiPlazaComment -> CommentVO.builder()
                        .commentId(aiPlazaComment.getCommentId())
                        .plazaId(aiPlazaComment.getPlazaId())
                        .userId(aiPlazaComment.getUserId())
                        .userName(aiPlazaComment.getUserName())
                        .commentContent(aiPlazaComment.getCommentContent())
                        .createTime(aiPlazaComment.getCreateTime())
                        .mine(userId != null && userId.equals(aiPlazaComment.getUserId()))
                        .build())
                .toList();
    }

    @Override
    @Cacheable(cachePrefix = WORKSPACE_COMMENT_PREFIX, cacheType = CacheType.VALUE, cacheClass = Integer.class)
    public Integer plazaCommentCount(String plazaId) {
        Integer total = aiPlazaCommentDao.countByPlazaId(plazaId);
        return total == null ? 0 : total;
    }

    @Override
    @CacheEvict(keyPrefix = {"ai:", "query:", "user:", "workspace:"})
    public void plazaLike(String plazaId, boolean liked) {
        Long userId = userContext.getUserId();
        if (liked) {
            Integer affected = aiPlazaLikeDao.insert(AiPlazaLike.builder().plazaId(plazaId).userId(userId).build());
            if (affected != null && affected > 0) {
                aiPlazaDao.increaseLikeCount(plazaId, 1);
            }
            return;
        }

        Integer affected = aiPlazaLikeDao.delete(plazaId, userId);
        if (affected != null && affected > 0) {
            aiPlazaDao.increaseLikeCount(plazaId, -1);
        }
    }

    @Override
    @CacheEvict(keyPrefix = {"ai:", "query:", "user:", "workspace:"})
    public void plazaFavor(String plazaId, boolean favored) {
        Long userId = userContext.getUserId();
        AiPlaza aiPlaza = aiPlazaDao.queryByPlazaId(plazaId);
        String templateId = aiPlaza.getTemplateId();

        if (favored) {
            AiPlazaFavor aiPlazaFavor = AiPlazaFavor.builder()
                    .plazaId(plazaId)
                    .userId(userId)
                    .build();
            Integer affected = aiPlazaFavorDao.insert(aiPlazaFavor);
            if (affected != null && affected > 0) {
                aiPlazaDao.increaseFavorCount(plazaId, 1);
            }

            AiRepo aiRepo = aiRepoDao.queryByUserIdAndTemplateIdAndRepoType(userId, templateId, RepoType.FAVOR.getType());
            if (aiRepo == null) {
                aiRepo = AiRepo.builder()
                        .repoId(randomUtil.randomRepoId())
                        .userId(userId)
                        .templateId(templateId)
                        .repoType(RepoType.FAVOR.getType())
                        .build();
                aiRepoDao.insert(aiRepo);
            }
        } else {
            Integer affected = aiPlazaFavorDao.delete(plazaId, userId);
            if (affected != null && affected > 0) {
                aiPlazaDao.increaseFavorCount(plazaId, -1);
            }
            aiRepoDao.deleteByUserIdAndTemplateIdAndRepoType(userId, templateId, RepoType.FAVOR.getType());
        }
    }

    @Override
    @CacheEvict(keyPrefix = {"ai:", "query:", "user:", "workspace:"})
    public void plazaComment(PlazaCommentDTO dto) {
        String plazaId = dto.getPlazaId();
        Long userId = userContext.getUserId();
        AiPlazaComment aiPlazaComment = AiPlazaComment.builder()
                .commentId(randomUtil.randomCommentId())
                .plazaId(plazaId)
                .userId(userId)
                .userName(userContext.getUserName())
                .commentContent(dto.getCommentContent())
                .build();
        aiPlazaCommentDao.insert(aiPlazaComment);
        aiPlazaDao.increaseCommentCount(plazaId, 1);
    }

    @Override
    @CacheEvict(keyPrefix = {"ai:", "query:", "user:", "workspace:"})
    public void plazaDiscomment(String plazaId, String commentId) {
        Long userId = userContext.getUserId();
        Integer affected = aiPlazaCommentDao.delete(commentId, userId);
        if (affected != null && affected > 0) {
            aiPlazaDao.increaseCommentCount(plazaId, -1);
        }
    }

    @Override
    @CacheEvict(keyPrefix = {"ai:", "query:", "user:", "workspace:"})
    public void plazaDelete(String plazaId) {
        Long userId = userContext.getUserId();
        AiPlaza aiPlaza = aiPlazaDao.queryByPlazaId(plazaId);
        if (aiPlaza == null || !userId.equals(aiPlaza.getUserId())) {
            throw new WorkException(ILLEGAL_USER);
        }

        aiPlazaLikeDao.deleteByPlazaId(plazaId);
        aiPlazaFavorDao.deleteByPlazaId(plazaId);
        aiPlazaCommentDao.deleteByPlazaId(plazaId);
        aiPlazaDao.deleteByPlazaId(plazaId);
    }

    @Override
    @Cacheable(cachePrefix = WORKSPACE_REPO_PREFIX, cacheType = CacheType.LIST, cacheClass = RepoVO.class)
    public List<RepoVO> repoList() {
        Long userId = userContext.getUserId();
        List<AiRepo> aiRepoList = aiRepoDao.listByUserId(userId);
        if (aiRepoList == null || aiRepoList.isEmpty()) {
            return List.of();
        }

        List<RepoVO> repoVOList = new ArrayList<>();
        for (AiRepo aiRepo : aiRepoList) {
            if (aiRepo == null) {
                continue;
            }

            RepoVO.RepoVOBuilder repoVOBuilder = RepoVO.builder()
                    .repoId(aiRepo.getRepoId())
                    .repoType(aiRepo.getRepoType())
                    .agentId(aiRepo.getAgentId())
                    .templateId(aiRepo.getTemplateId())
                    .createTime(aiRepo.getCreateTime());

            if (StringUtils.hasText(aiRepo.getAgentId())) {
                AiAgent aiAgent = aiAgentDao.queryAgentByAgentId(aiRepo.getAgentId());
                repoVOBuilder.agentName(aiAgent.getAgentName())
                        .agentType(aiAgent.getAgentType())
                        .agentDesc(aiAgent.getAgentDesc());
            } else if (StringUtils.hasText(aiRepo.getTemplateId())) {
                AiTemplate aiTemplate = aiTemplateDao.queryByTemplateId(aiRepo.getTemplateId());
                repoVOBuilder.agentName(aiTemplate.getAgentName())
                        .agentType(aiTemplate.getAgentType())
                        .agentDesc(aiTemplate.getAgentDesc());
            } else {
                throw new WorkException(ILLEGAL_DATA);
            }

            repoVOList.add(repoVOBuilder.build());
        }
        return repoVOList;
    }

    @Override
    @CacheEvict(keyPrefix = {"ai:", "query:", "user:", "workspace:"})
    public void agentDelete(String agentId) {
        Long userId = userContext.getUserId();
        AiAgent aiAgent = aiAgentDao.queryAgentByAgentId(agentId);
        if (!userId.equals(aiAgent.getAgentFrom())) {
            throw new WorkException(ILLEGAL_USER);
        }

        String templateId = aiAgent.getTemplateId();
        AiTemplate aiTemplate = aiTemplateDao.queryByTemplateId(templateId);
        if (aiTemplate != null && userId.equals(aiTemplate.getUserId())) {
            List<AiPlaza> aiPlazaList = aiPlazaDao.listByTemplateId(templateId);
            if (aiPlazaList != null && !aiPlazaList.isEmpty()) {
                for (AiPlaza aiPlaza : aiPlazaList) {
                    String plazaId = aiPlaza.getPlazaId();
                    aiPlazaLikeDao.deleteByPlazaId(plazaId);
                    aiPlazaFavorDao.deleteByPlazaId(plazaId);
                    aiPlazaCommentDao.deleteByPlazaId(plazaId);
                    aiPlazaDao.deleteByPlazaId(plazaId);
                }
            }
            aiRepoDao.deleteByTemplateId(templateId);
            aiTemplateDao.deleteByTemplateId(templateId);
        }

        List<AiFlow> aiFlowList = aiFlowDao.queryByAgentId(agentId);
        for (AiFlow aiFlow : aiFlowList) {
            String clientId = aiFlow.getClientId();
            aiConfigDao.deleteByClientId(clientId);
            aiClientDao.deleteByClientId(clientId);
        }

        aiTaskDao.deleteByAgentId(agentId);
        aiFlowDao.deleteByAgentId(agentId);
        aiRepoDao.deleteByAgentId(agentId);
        aiAgentDao.deleteByAgentId(agentId);
    }

    @Override
    @CacheEvict(keyPrefix = {"ai:", "query:", "user:", "workspace:"})
    public void agentCreate(AgentCreateDTO dto, List<RolePromptEntity> rolePromptList) {
        String agentId = randomUtil.randomAgentId();
        Long userId = userContext.getUserId();
        Set<String> mcpIdSet = dto.getMcpIdSet();

        // 新建 agent
        aiAgentDao.insert(AiAgent.builder()
                .agentId(agentId)
                .agentName(dto.getAgentName())
                .agentType(dto.getStrategy())
                .agentDesc(dto.getAgentDesc())
                .modelId(dto.getModelId())
                .agentStatus(1)
                .agentFrom(userId)
                .build());

        for (RolePromptEntity rolePrompt : rolePromptList) {
            String clientId = randomUtil.randomClientId();
            String promptId = randomUtil.randomPromptId();
            String clientRole = rolePrompt.getClientRole();
            String systemPrompt = rolePrompt.getSystemPrompt();
            String userPrompt = rolePrompt.getUserPrompt();
            Integer flowSeq = rolePrompt.getFlowSeq();

            // 新建 client
            aiClientDao.insert(AiClient.builder()
                    .clientId(clientId)
                    .clientType("work")
                    .clientRole(clientRole)
                    .modelId(dto.getModelId())
                    .modelName(dto.getModelName())
                    .clientName(dto.getAgentName() + "-" + clientRole)
                    .clientStatus(1)
                    .clientFrom(userId)
                    .build());

            // 新建 prompt
            aiPromptDao.insert(AiPrompt.builder()
                    .promptId(promptId)
                    .promptName(clientRole + "_prompt")
                    .systenPrompt(systemPrompt)
                    .build());

            // 新建 config-client
            aiConfigDao.insert(AiConfig.builder()
                    .clientId(clientId)
                    .configType(ConfigType.PROMPT.getType())
                    .configValue(promptId)
                    .configStatus(1)
                    .build());

            // 新建 config-advisor
            aiConfigDao.insert(AiConfig.builder()
                    .clientId(clientId)
                    .configType(ConfigType.ADVISOR.getType())
                    .configValue(DEFAULT_ADVISOR_ID)
                    .configStatus(1)
                    .build());

            // 新建 config-mcp
            if (mcpIdSet != null && !mcpIdSet.isEmpty()) {
                for (String mcpId : mcpIdSet) {
                    aiConfigDao.insert(AiConfig.builder()
                            .clientId(clientId)
                            .configType(ConfigType.MCP.getType())
                            .configValue(mcpId)
                            .configStatus(1)
                            .build());
                }
            }

            // 新建 flow
            aiFlowDao.insert(AiFlow.builder()
                    .agentId(agentId)
                    .clientId(clientId)
                    .clientRole(clientRole)
                    .userPrompt(userPrompt)
                    .flowSeq(flowSeq)
                    .build());
        }

        // 新建 repo
        aiRepoDao.insert(AiRepo.builder()
                .repoId(randomUtil.randomRepoId())
                .userId(userId)
                .agentId(agentId)
                .repoType(RepoType.SELF.getType())
                .build());
    }

    @Override
    @CacheEvict(keyPrefix = {"ai:", "query:", "user:", "workspace:"})
    public void agentPublish(AgentPublishDTO dto) {
        String agentId = dto.getAgentId();
        Long userId = userContext.getUserId();
        AiAgent aiAgent = aiAgentDao.queryAgentByAgentId(agentId);
        if (!userId.equals(aiAgent.getAgentFrom())) {
            throw new WorkException(ILLEGAL_USER);
        }

        String snapshot = snapshotUtil.buildSnapshot(agentId);

        AiModel aiModel = aiModelDao.queryByModelId(aiAgent.getModelId());
        if (aiModel == null) {
            throw new WorkException(com.dasi.types.constant.ExceptionMessage.PUBLISH_MODEL_MISSING);
        }
        String modelName = aiModel.getModelName();
        String modelType = aiModel.getModelType();

        AiApi aiApi = aiApiDao.queryByApiId(aiModel.getApiId());
        if (aiApi == null) {
            throw new WorkException(com.dasi.types.constant.ExceptionMessage.PUBLISH_API_MISSING);
        }
        String apiBaseUrl = aiApi.getApiBaseUrl();
        String apiCompletionUrl = aiApi.getApiCompletionsPath();

        String templateId = aiAgent.getTemplateId();
        if (!StringUtils.hasText(templateId)) {
            templateId = randomUtil.randomTemplateId();
            aiAgentDao.update(AiAgent.builder()
                    .id(aiAgent.getId())
                    .templateId(templateId)
                    .build());
        }

        AiTemplate aiTemplate = aiTemplateDao.queryByTemplateId(templateId);
        if (aiTemplate == null) {
            aiTemplate = AiTemplate.builder()
                    .templateId(randomUtil.randomTemplateId())
                    .userId(userId)
                    .agentName(aiAgent.getAgentName())
                    .agentType(aiAgent.getAgentType())
                    .agentDesc(aiAgent.getAgentDesc())
                    .apiBaseUrl(apiBaseUrl)
                    .apiCompletionUrl(apiCompletionUrl)
                    .modelName(modelName)
                    .modelType(modelType)
                    .snapshot(snapshot)
                    .build();
            aiTemplateDao.insert(aiTemplate);
        }
        // 更新 template
        else {
            aiTemplate = aiTemplateDao.queryByTemplateId(templateId);
            aiTemplate.setUserId(userId);
            aiTemplate.setAgentName(aiAgent.getAgentName());
            aiTemplate.setAgentType(aiAgent.getAgentType());
            aiTemplate.setAgentDesc(aiAgent.getAgentDesc());
            aiTemplate.setApiBaseUrl(apiBaseUrl);
            aiTemplate.setApiCompletionUrl(apiCompletionUrl);
            aiTemplate.setModelName(modelName);
            aiTemplate.setModelType(modelType);
            aiTemplate.setSnapshot(snapshot);
            aiTemplateDao.update(aiTemplate);
        }

        // 新增或更新 plaza
        AiPlaza aiPlaza = aiPlazaDao.queryByTemplateId(aiTemplate.getTemplateId());
        if (aiPlaza == null) {
            aiPlaza = AiPlaza.builder()
                    .plazaId(randomUtil.randomPlazaId())
                    .templateId(aiTemplate.getTemplateId())
                    .userId(userId)
                    .agentName(aiAgent.getAgentName())
                    .agentType(aiAgent.getAgentType())
                    .plazaTitle(dto.getPlazaTitle())
                    .plazaDesc(dto.getPlazaDesc())
                    .likeCount(0)
                    .favorCount(0)
                    .commentCount(0)
                    .build();
            aiPlazaDao.insert(aiPlaza);
        } else {
            aiPlaza.setTemplateId(aiTemplate.getTemplateId());
            aiPlaza.setUserId(userId);
            aiPlaza.setAgentName(aiAgent.getAgentName());
            aiPlaza.setAgentType(aiAgent.getAgentType());
            aiPlaza.setPlazaTitle(dto.getPlazaTitle());
            aiPlaza.setPlazaDesc(dto.getPlazaDesc());
            aiPlazaDao.update(aiPlaza);
        }

        // 新增或更新 repo
        AiRepo aiRepo = aiRepoDao.queryByUserIdAndAgentIdAndRepoType(userId, aiAgent.getAgentId(), RepoType.SELF.getType());
        if (aiRepo == null) {
            aiRepoDao.insert(AiRepo.builder()
                    .repoId(randomUtil.randomRepoId())
                    .userId(userId)
                    .agentId(aiAgent.getAgentId())
                    .templateId(aiTemplate.getTemplateId())
                    .repoType(RepoType.SELF.getType())
                    .build());
        } else {
            aiRepo.setTemplateId(aiTemplate.getTemplateId());
            aiRepoDao.update(aiRepo);
        }
    }

    @Override
    @Cacheable(cachePrefix = WORKSPACE_TEMPLATE_PREFIX, cacheType = CacheType.VALUE, cacheClass = TemplateVO.class)
    public TemplateVO agentTemplate(String templateId) {
        AiTemplate aiTemplate = aiTemplateDao.queryByTemplateId(templateId);
        if (aiTemplate == null) {
            throw new WorkException(ILLEGAL_DATA);
        }

        AiPlaza aiPlaza = aiPlazaDao.queryByTemplateId(templateId);
        String userName = aiUserDao.queryUserNameById(aiTemplate.getUserId());
        SnapshotView snapshotView = snapshotUtil.parseSnapshot(aiTemplate.getSnapshot());

        return TemplateVO.builder()
                .templateId(aiTemplate.getTemplateId())
                .createTime(aiTemplate.getCreateTime())
                .userName(StringUtils.hasText(userName) ? userName : "system")
                .plazaTitle(aiPlaza.getPlazaTitle())
                .plazaDesc(aiPlaza.getPlazaDesc())
                .likeCount(aiPlaza.getLikeCount())
                .favorCount(aiPlaza.getFavorCount())
                .commentCount(aiPlaza.getCommentCount())
                .agentName(aiTemplate.getAgentName())
                .agentType(aiTemplate.getAgentType())
                .agentDesc(aiTemplate.getAgentDesc())
                .apiUrl(aiTemplate.getApiBaseUrl())
                .apiCompletionUrl(aiTemplate.getApiCompletionUrl())
                .modelName(aiTemplate.getModelName())
                .modelType(aiTemplate.getModelType())
                .mcpInfoList(snapshotUtil.toTemplateMcpInfoList(snapshotView.getMcps()))
                .clientInfoList(snapshotUtil.toTemplateClientInfoList(snapshotView.getPrompts()))
                .build();
    }

    @Override
    @Cacheable(cachePrefix = WORKSPACE_AGENT_DETAIL_PREFIX, cacheType = CacheType.VALUE, cacheClass = AgentVO.class)
    public AgentVO agentDetail(String agentId) {
        Long userId = userContext.getUserId();
        AiAgent aiAgent = aiAgentDao.queryAgentByAgentId(agentId);
        if (!userId.equals(aiAgent.getAgentFrom())) {
            throw new WorkException(ILLEGAL_USER);
        }

        AiModel aiModel = aiModelDao.queryByModelId(aiAgent.getModelId());
        AiApi aiApi = aiApiDao.queryByApiId(aiModel.getApiId());
        List<AiFlow> aiFlowList = aiFlowDao.queryByAgentId(agentId)
                .stream()
                .sorted(Comparator.comparing(AiFlow::getFlowSeq))
                .toList();

        List<AgentVO.ClientInfo> clientInfoList = new ArrayList<>();
        List<AgentVO.McpInfo> mcpInfoList = new ArrayList<>();
        Set<String> mcpIdSet = new HashSet<>();
        for (AiFlow aiFlow : aiFlowList) {

            // 拿到 userPrompt
            String userPrompt = aiFlow.getUserPrompt();

            // 拿到 systemPrompt
            String clientId = aiFlow.getClientId();
            String promptId = aiConfigDao.queryByClientIdAndConfigType(clientId, ConfigType.PROMPT.getType()).get(0).getConfigValue();
            String systemPrompt = aiPromptDao.queryByPromptId(promptId).getSystenPrompt();

            // 构造 clientInfo
            AgentVO.ClientInfo clientInfo = AgentVO.ClientInfo.builder()
                    .flowId(aiFlow.getId())
                    .clientId(clientId)
                    .clientRole(aiFlow.getClientRole())
                    .promptId(promptId)
                    .systemPrompt(systemPrompt)
                    .userPrompt(userPrompt)
                    .build();
            clientInfoList.add(clientInfo);

            // 构造 mcpInfo
            List<AiConfig> mcpConfigList = aiConfigDao.queryByClientIdAndConfigType(clientId, ConfigType.MCP.getType());
            for (AiConfig mcpConfig : mcpConfigList) {
                String mcpId = mcpConfig.getConfigValue();
                if (mcpIdSet.contains(mcpId)) {
                    continue;
                }
                AiMcp aiMcp = aiMcpDao.queryByMcpId(mcpId);
                AgentVO.McpInfo mcpInfo = AgentVO.McpInfo.builder()
                        .mcpId(aiMcp.getMcpId())
                        .mcpName(aiMcp.getMcpName())
                        .mcpType(aiMcp.getMcpType())
                        .mcpParam(aiMcp.getMcpParam())
                        .mcpDesc(aiMcp.getMcpDesc())
                        .mcpSecret(aiMcp.getMcpSecret())
                        .build();
                mcpInfoList.add(mcpInfo);
                mcpIdSet.add(mcpId);
            }
        }

        return AgentVO.builder()
                .agentId(aiAgent.getAgentId())
                .agentName(aiAgent.getAgentName())
                .agentType(aiAgent.getAgentType())
                .agentDesc(aiAgent.getAgentDesc())
                .createTime(aiAgent.getCreateTime())
                .modelId(aiAgent.getModelId())
                .modelName(aiModel.getModelName())
                .modelType(aiModel.getModelType())
                .apiBaseUrl(aiApi.getApiBaseUrl())
                .apiCompletionUrl(aiApi.getApiCompletionsPath())
                .mcpInfoList(mcpInfoList)
                .clientInfoList(clientInfoList)
                .build();
    }

    @Override
    @CacheEvict(keyPrefix = {"ai:", "query:", "user:", "workspace:"})
    public void agentFork(String templateId) {
        Long userId = userContext.getUserId();
        AiTemplate aiTemplate = aiTemplateDao.queryByTemplateId(templateId);
        if (aiTemplate == null) {
            throw new WorkException(ILLEGAL_DATA);
        }

        AiRepo existedFork = aiRepoDao.queryByUserIdAndTemplateIdAndRepoType(userId, templateId, RepoType.FORK.getType());
        if (existedFork != null) {
            return;
        }

        SnapshotView snapshotView = snapshotUtil.parseSnapshot(aiTemplate.getSnapshot());
        List<SnapshotView.PromptView> promptViewList = snapshotView.getPrompts();
        if (promptViewList == null || promptViewList.isEmpty()) {
            throw new WorkException(ILLEGAL_DATA);
        }

        // 新建 api
        String apiId = randomUtil.randomApiId();
        aiApiDao.insert(AiApi.builder()
                .apiId(apiId)
                .apiBaseUrl(aiTemplate.getApiBaseUrl())
                .apiCompletionsPath(aiTemplate.getApiCompletionUrl())
                .apiFrom(userId)
                .build());

        // 新建 model
        String modelId = randomUtil.randomModelId();
        aiModelDao.insert(AiModel.builder()
                .modelId(modelId)
                .apiId(apiId)
                .modelName(aiTemplate.getModelName())
                .modelType(aiTemplate.getModelType())
                .modelFrom(userId)
                .build());

        // 新建 mcp
        List<String> mcpIdList = new ArrayList<>();
        List<SnapshotView.McpView> mcpViewList = snapshotView.getMcps();
        for (SnapshotView.McpView mcpInfo : mcpViewList) {
            String mcpId = randomUtil.randomMcpId();
            LinkedHashMap<String, String> secretMap = new LinkedHashMap<>();
            for (String secretKey : mcpInfo.getRequiredSecrets()) {
                if (StringUtils.hasText(secretKey)) {
                    secretMap.put(secretKey, "");
                }
            }
            String mcpSecret = JSON.toJSONString(secretMap);

            aiMcpDao.insert(AiMcp.builder()
                    .mcpId(mcpId)
                    .mcpName(mcpInfo.getMcpName())
                    .mcpType(mcpInfo.getMcpType())
                    .mcpParam(mcpInfo.getMcpParam())
                    .mcpSecret(mcpSecret)
                    .mcpTimeout(180)
                    .mcpChat(0)
                    .mcpFrom(userId)
                    .mcpDesc(mcpInfo.getMcpDesc())
                    .build());
            mcpIdList.add(mcpId);
        }

        // 新建 agent
        String agentId = randomUtil.randomAgentId();
        aiAgentDao.insert(AiAgent.builder()
                .agentId(agentId)
                .agentName(aiTemplate.getAgentName())
                .agentType(aiTemplate.getAgentType())
                .agentDesc(aiTemplate.getAgentDesc())
                .modelId(modelId)
                .agentStatus(1)
                .agentFrom(userId)
                .build());

        for (int i = 0; i < promptViewList.size(); i++) {
            SnapshotView.PromptView promptView = promptViewList.get(i);
            String clientRole = promptView.getClientRole();
            String systemPrompt = promptView.getSystemPrompt();
            String userPrompt = promptView.getUserPrompt();

            // 新建 client
            String clientId = randomUtil.randomClientId();
            aiClientDao.insert(AiClient.builder()
                    .clientId(clientId)
                    .clientType("work")
                    .clientRole(clientRole)
                    .modelId(modelId)
                    .modelName(aiTemplate.getModelName())
                    .clientName(aiTemplate.getAgentName() + "-" + clientRole)
                    .clientStatus(1)
                    .clientFrom(userId)
                    .build());

            // 新建 flow
            aiFlowDao.insert(AiFlow.builder()
                    .agentId(agentId)
                    .clientId(clientId)
                    .clientRole(clientRole)
                    .userPrompt(userPrompt)
                    .flowSeq(i + 1)
                    .build());

            // 新建 prompt
            String promptId = randomUtil.randomPromptId();
            aiPromptDao.insert(AiPrompt.builder()
                    .promptId(promptId)
                    .promptName(clientRole + "_prompt")
                    .systenPrompt(systemPrompt)
                    .build());

            // 新建 config-prompt
            aiConfigDao.insert(AiConfig.builder()
                    .clientId(clientId)
                    .configType(ConfigType.PROMPT.getType())
                    .configValue(promptId)
                    .configStatus(1)
                    .build());

            // 新建 config-advisor
            aiConfigDao.insert(AiConfig.builder()
                    .clientId(clientId)
                    .configType(ConfigType.ADVISOR.getType())
                    .configValue(DEFAULT_ADVISOR_ID)
                    .configStatus(1)
                    .build());

            // 新建 config-mcp
            for (String mcpId : mcpIdList) {
                aiConfigDao.insert(AiConfig.builder()
                        .clientId(clientId)
                        .configType(ConfigType.MCP.getType())
                        .configValue(mcpId)
                        .configStatus(1)
                        .build());
            }
        }

        // 新建 repo
        aiRepoDao.insert(AiRepo.builder()
                .repoId(randomUtil.randomRepoId())
                .userId(userId)
                .agentId(agentId)
                .templateId(templateId)
                .repoType(RepoType.FORK.getType())
                .build());
    }

    @Override
    @CacheEvict(keyPrefix = {"ai:", "query:", "user:", "workspace:"})
    public void agentUpdateBase(AgentUpdateBaseDTO dto) {
        Long userId = userContext.getUserId();
        AiAgent aiAgent = aiAgentDao.queryAgentByAgentId(dto.getAgentId());
        if (!userId.equals(aiAgent.getAgentFrom())) {
            throw new WorkException(ILLEGAL_USER);
        }

        aiAgentDao.update(AiAgent.builder()
                .id(aiAgent.getId())
                .agentName(dto.getAgentName())
                .agentDesc(dto.getAgentDesc())
                .build());
    }

    @Override
    @CacheEvict(keyPrefix = {"ai:", "query:", "user:", "workspace:"})
    public void agentUpdateModel(AgentUpdateModelDTO dto) {
        Long userId = userContext.getUserId();
        AiAgent aiAgent = aiAgentDao.queryAgentByAgentId(dto.getAgentId());
        if (!userId.equals(aiAgent.getAgentFrom())) {
            throw new WorkException(ILLEGAL_USER);
        }

        String modelId = dto.getModelId();
        AiModel aiModel = aiModelDao.queryByModelId(modelId);
        if (aiModel == null) {
            throw new WorkException(ILLEGAL_DATA);
        }

        aiAgentDao.update(AiAgent.builder()
                .id(aiAgent.getId())
                .modelId(modelId)
                .build());

        for (String clientId : dto.getClientIdList()) {
            AiClient aiClient = aiClientDao.queryByClientId(clientId);
            aiClientDao.update(AiClient.builder()
                    .id(aiClient.getId())
                    .modelId(modelId)
                    .modelName(aiModel.getModelName())
                    .build());
        }
    }

    @Override
    @CacheEvict(keyPrefix = {"ai:", "query:", "user:", "workspace:"})
    public void agentUpdateMcp(AgentUpdateMcpDTO dto) {
        Long userId = userContext.getUserId();
        AiAgent aiAgent = aiAgentDao.queryAgentByAgentId(dto.getAgentId());
        if (!userId.equals(aiAgent.getAgentFrom())) {
            throw new WorkException(ILLEGAL_USER);
        }

        for (String clientId : dto.getClientIdList()) {
            aiConfigDao.deleteByClientIdAndConfigType(clientId, ConfigType.MCP.getType());
            for (String mcpId : dto.getMcpIdList()) {
                AiConfig aiConfig = AiConfig.builder()
                        .clientId(clientId)
                        .configType(ConfigType.MCP.getType())
                        .configValue(mcpId)
                        .configStatus(1)
                        .build();
                aiConfigDao.insert(aiConfig);
            }
        }
    }

    @Override
    @CacheEvict(keyPrefix = {"ai:", "query:", "user:", "workspace:"})
    public void agentUpdateUserPrompt(AgentUpdateUserPromptDTO dto) {
        Long userId = userContext.getUserId();
        AiAgent aiAgent = aiAgentDao.queryAgentByAgentId(dto.getAgentId());
        if (!userId.equals(aiAgent.getAgentFrom())) {
            throw new WorkException(ILLEGAL_USER);
        }

        AiFlow aiFlow = aiFlowDao.queryById(dto.getFlowId());
        if (aiFlow == null || !dto.getAgentId().equals(aiFlow.getAgentId())) {
            throw new WorkException(ILLEGAL_DATA);
        }

        aiFlowDao.update(AiFlow.builder()
                .id(aiFlow.getId())
                .userPrompt(dto.getUserPrompt())
                .build());
    }

    @Override
    @CacheEvict(keyPrefix = {"ai:", "query:", "user:", "workspace:"})
    public void agentUpdateSystemPrompt(AgentUpdateSystemPromptDTO dto) {
        Long userId = userContext.getUserId();
        AiAgent aiAgent = aiAgentDao.queryAgentByAgentId(dto.getAgentId());
        if (!userId.equals(aiAgent.getAgentFrom())) {
            throw new WorkException(ILLEGAL_USER);
        }

        AiPrompt aiPrompt = aiPromptDao.queryByPromptId(dto.getPromptId());
        if (aiPrompt == null) {
            throw new WorkException(ILLEGAL_DATA);
        }

        aiPromptDao.update(AiPrompt.builder()
                .id(aiPrompt.getId())
                .systenPrompt(dto.getSystemPrompt())
                .build());
    }

}
