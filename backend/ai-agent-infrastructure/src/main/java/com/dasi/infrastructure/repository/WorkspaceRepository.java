package com.dasi.infrastructure.repository;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.dasi.domain.util.jwt.UserContext;
import com.dasi.domain.util.random.IRandomUtil;
import com.dasi.domain.workspace.model.dto.PlazaCommentAreaDTO;
import com.dasi.domain.workspace.model.dto.PlazaCommentDTO;
import com.dasi.domain.workspace.model.dto.PlazaPageDTO;
import com.dasi.domain.workspace.model.enumeration.PlazaActionType;
import com.dasi.domain.workspace.model.vo.CommentVO;
import com.dasi.domain.workspace.model.vo.PlazaVO;
import com.dasi.domain.workspace.model.vo.RepoVO;
import com.dasi.domain.workspace.model.vo.TemplateVO;
import com.dasi.domain.workspace.repository.IWorkspaceRepository;
import com.dasi.infrastructure.persistent.dao.IAiAgentDao;
import com.dasi.infrastructure.persistent.dao.IAiApiDao;
import com.dasi.infrastructure.persistent.dao.IAiClientDao;
import com.dasi.infrastructure.persistent.dao.IAiConfigDao;
import com.dasi.infrastructure.persistent.dao.IAiFlowDao;
import com.dasi.infrastructure.persistent.dao.IAiMcpDao;
import com.dasi.infrastructure.persistent.dao.IAiModelDao;
import com.dasi.infrastructure.persistent.dao.IAiPlazaCommentDao;
import com.dasi.infrastructure.persistent.dao.IAiPlazaDao;
import com.dasi.infrastructure.persistent.dao.IAiPlazaFavorDao;
import com.dasi.infrastructure.persistent.dao.IAiPlazaLikeDao;
import com.dasi.infrastructure.persistent.dao.IAiPromptDao;
import com.dasi.infrastructure.persistent.dao.IAiRepoDao;
import com.dasi.infrastructure.persistent.dao.IAiTaskDao;
import com.dasi.infrastructure.persistent.dao.IAiTemplateDao;
import com.dasi.infrastructure.persistent.dao.IAiUserDao;
import com.dasi.infrastructure.persistent.po.AiAgent;
import com.dasi.infrastructure.persistent.po.AiApi;
import com.dasi.infrastructure.persistent.po.AiClient;
import com.dasi.infrastructure.persistent.po.AiConfig;
import com.dasi.infrastructure.persistent.po.AiFlow;
import com.dasi.infrastructure.persistent.po.AiMcp;
import com.dasi.infrastructure.persistent.po.AiModel;
import com.dasi.infrastructure.persistent.po.AiPlaza;
import com.dasi.infrastructure.persistent.po.AiPlazaComment;
import com.dasi.infrastructure.persistent.po.AiPlazaFavor;
import com.dasi.infrastructure.persistent.po.AiPlazaLike;
import com.dasi.infrastructure.persistent.po.AiPrompt;
import com.dasi.infrastructure.persistent.po.AiRepo;
import com.dasi.infrastructure.persistent.po.AiTemplate;
import com.dasi.infrastructure.persistent.po.AiUser;
import com.dasi.types.exception.MiniAgentException;
import com.dasi.types.result.PageResult;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static com.dasi.types.constant.ExceptionMessage.SETTING_USER_ILLEGAL;

@Slf4j
@Repository
public class WorkspaceRepository implements IWorkspaceRepository {

    private static final String REPO_SELF = "self";
    private static final String REPO_FORK = "fork";
    private static final String REPO_FAVOR = "favor";
    private static final String CONFIG_PROMPT = "prompt";
    private static final String CONFIG_MCP = "mcp";

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
    private IAiClientDao aiClientDao;

    @Resource
    private IAiConfigDao aiConfigDao;

    @Resource
    private IAiPromptDao aiPromptDao;

    @Resource
    private IAiMcpDao aiMcpDao;

    @Resource
    private IAiModelDao aiModelDao;

    @Resource
    private IAiApiDao aiApiDao;

    @Resource
    private IAiTaskDao aiTaskDao;

    @Resource
    private IAiUserDao aiUserDao;

    @Resource
    private IRandomUtil randomUtil;

    @Override
    public PageResult<PlazaVO> pagePlaza(PlazaPageDTO dto) {
        int pageNum = dto.getPageNum();
        int pageSize = dto.getPageSize();
        int offset = (pageNum - 1) * pageSize;

        List<AiPlaza> aiPlazaList = aiPlazaDao.page(dto.getKeyword(), dto.getSortBy(), dto.getSortOrder(), offset, pageSize);
        Integer total = aiPlazaDao.count(dto.getKeyword());
        if (total == null) {
            total = 0;
        }

        List<PlazaVO> plazaVOList = List.of();
        if (aiPlazaList != null && !aiPlazaList.isEmpty()) {
            Long userId = userContext.getUserId();
            List<String> plazaIdList = aiPlazaList.stream().map(AiPlaza::getPlazaId).toList();
            Set<String> likedSet = queryUserPlazaSet(userId, plazaIdList, PlazaActionType.LIKE);
            Set<String> favoredSet = queryUserPlazaSet(userId, plazaIdList, PlazaActionType.FAVOR);
            Set<String> commentedSet = queryUserPlazaSet(userId, plazaIdList, PlazaActionType.COMMENT);

            plazaVOList = aiPlazaList.stream().map(aiPlaza -> PlazaVO.builder()
                    .plazaId(aiPlaza.getPlazaId())
                    .templateId(aiPlaza.getTemplateId())
                    .agentName(aiPlaza.getAgentName())
                    .agentType(aiPlaza.getAgentType())
                    .userName(aiPlaza.getUserName())
                    .plazaTitle(aiPlaza.getPlazaTitle())
                    .plazaDesc(aiPlaza.getPlazaDesc())
                    .likeCount(aiPlaza.getLikeCount())
                    .favorCount(aiPlaza.getFavorCount())
                    .commentCount(aiPlaza.getCommentCount())
                    .liked(likedSet.contains(aiPlaza.getPlazaId()))
                    .favored(favoredSet.contains(aiPlaza.getPlazaId()))
                    .commented(commentedSet.contains(aiPlaza.getPlazaId()))
                    .createTime(aiPlaza.getCreateTime())
                    .build()).toList();
        }

        int pageSum = pageSize == 0 ? 0 : (total + pageSize - 1) / pageSize;
        return PageResult.<PlazaVO>builder()
                .list(plazaVOList)
                .total(total)
                .pageNum(pageNum)
                .pageSize(pageSize)
                .pageSum(pageSum)
                .build();
    }

    @Override
    public PageResult<CommentVO> plazaCommentArea(PlazaCommentAreaDTO dto) {
        Long userId = userContext.getUserId();
        int pageNum = dto.getPageNum();
        int pageSize = dto.getPageSize();
        int offset = (pageNum - 1) * pageSize;
        String plazaId = dto.getPlazaId();

        List<AiPlazaComment> aiPlazaCommentList = aiPlazaCommentDao.listByPlazaId(plazaId, offset, pageSize);
        List<CommentVO> commentVOList = List.of();
        if (aiPlazaCommentList != null && !aiPlazaCommentList.isEmpty()) {
            commentVOList = aiPlazaCommentList.stream().map(aiPlazaComment -> {
                Long commentUserId = aiPlazaComment.getUserId();
                return CommentVO.builder()
                        .commentId(aiPlazaComment.getCommentId())
                        .plazaId(aiPlazaComment.getPlazaId())
                        .userId(commentUserId)
                        .userName(aiPlazaComment.getUserName())
                        .commentContent(aiPlazaComment.getCommentContent())
                        .createTime(aiPlazaComment.getCreateTime())
                        .mine(userId != null && userId.equals(commentUserId))
                        .build();
            }).toList();
        }

        Integer total = aiPlazaCommentDao.countByPlazaId(plazaId);
        if (total == null) {
            total = 0;
        }
        int pageSum = (total + pageSize - 1) / pageSize;
        return PageResult.<CommentVO>builder()
                .list(commentVOList)
                .total(total)
                .pageNum(pageNum)
                .pageSize(pageSize)
                .pageSum(pageSum)
                .build();
    }

    @Override
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
    public void plazaFavor(String plazaId, boolean favored) {
        Long userId = userContext.getUserId();
        if (favored) {
            Integer affected = aiPlazaFavorDao.insert(AiPlazaFavor.builder().plazaId(plazaId).userId(userId).build());
            if (affected != null && affected > 0) {
                aiPlazaDao.increaseFavorCount(plazaId, 1);
            }
            return;
        }

        Integer affected = aiPlazaFavorDao.delete(plazaId, userId);
        if (affected != null && affected > 0) {
            aiPlazaDao.increaseFavorCount(plazaId, -1);
        }
    }

    @Override
    public void plazaComment(PlazaCommentDTO dto) {
        String plazaId = dto.getPlazaId();
        Long userId = userContext.getUserId();
        aiPlazaCommentDao.insert(AiPlazaComment.builder()
                .commentId(randomUtil.userRandom())
                .plazaId(plazaId)
                .userId(userId)
                .userName(userContext.getUserName())
                .commentContent(dto.getCommentContent())
                .build());
        aiPlazaDao.increaseCommentCount(plazaId, 1);
    }

    @Override
    public void plazaDiscomment(String plazaId, String commentId) {
        Long userId = userContext.getUserId();
        Integer affected = aiPlazaCommentDao.delete(commentId, userId);
        if (affected != null && affected > 0) {
            aiPlazaDao.increaseCommentCount(plazaId, -1);
        }
    }

    @Override
    public void plazaDelete(String plazaId) {
        Long userId = userContext.getUserId();
        AiPlaza aiPlaza = aiPlazaDao.queryByPlazaId(plazaId);
        if (aiPlaza == null || !Objects.equals(aiPlaza.getUserId(), userId)) {
            throw new MiniAgentException(SETTING_USER_ILLEGAL);
        }

        aiPlazaLikeDao.deleteByPlazaId(plazaId);
        aiPlazaFavorDao.deleteByPlazaId(plazaId);
        aiPlazaCommentDao.deleteByPlazaId(plazaId);
        aiPlazaDao.deleteByPlazaId(plazaId);
    }

    @Override
    public Map<String, RepoVO> repoList() {
        Long userId = userContext.getUserId();
        List<AiRepo> aiRepoList = aiRepoDao.listByUserId(userId);

        Map<String, RepoVO> resultMap = initRepoMap();
        if (aiRepoList == null || aiRepoList.isEmpty()) {
            return resultMap;
        }

        for (AiRepo aiRepo : aiRepoList) {
            if (aiRepo == null || !resultMap.containsKey(aiRepo.getRepoType())) {
                continue;
            }
            RepoVO repoVO = resultMap.get(aiRepo.getRepoType());
            RepoVO.RepoItem.RepoItemBuilder itemBuilder = RepoVO.RepoItem.builder()
                    .repoId(aiRepo.getRepoId())
                    .agentId(aiRepo.getAgentId())
                    .templateId(aiRepo.getTemplateId())
                    .createTime(aiRepo.getCreateTime());

            AiAgent aiAgent = StringUtils.hasText(aiRepo.getAgentId()) ? aiAgentDao.queryAgentByAgentId(aiRepo.getAgentId()) : null;
            if (aiAgent != null) {
                itemBuilder.agentName(aiAgent.getAgentName())
                        .agentType(aiAgent.getAgentType())
                        .agentDesc(aiAgent.getAgentDesc());
            } else if (StringUtils.hasText(aiRepo.getTemplateId())) {
                AiTemplate aiTemplate = aiTemplateDao.queryByTemplateId(aiRepo.getTemplateId());
                if (aiTemplate != null) {
                    itemBuilder.agentName(aiTemplate.getAgentName())
                            .agentType(aiTemplate.getAgentType())
                            .agentDesc(aiTemplate.getAgentDesc());
                }
            }

            repoVO.getList().add(itemBuilder.build());
        }

        resultMap.values().forEach(vo -> vo.setTotal(vo.getList().size()));
        return resultMap;
    }

    @Override
    public void agentPublish(String agentId) {
        Long userId = userContext.getUserId();
        AiAgent aiAgent = requireUserAgent(agentId, userId);

        List<AiFlow> aiFlowList = aiFlowDao.queryByAgentId(agentId);
        if (aiFlowList == null || aiFlowList.isEmpty()) {
            throw new MiniAgentException("Agent 缺少 Flow 配置，无法发布");
        }
        aiFlowList = aiFlowList.stream()
                .sorted(Comparator.comparing(AiFlow::getFlowSeq, Comparator.nullsLast(Integer::compareTo)))
                .toList();

        SnapshotBuildResult snapshotBuildResult = buildSnapshotByFlowList(aiFlowList);
        AiTemplate aiTemplate = upsertTemplate(aiAgent, userId, snapshotBuildResult);
        upsertPlaza(aiAgent, userId, aiTemplate.getTemplateId());
        upsertSelfRepo(userId, aiAgent.getAgentId(), aiTemplate.getTemplateId());
    }

    @Override
    public TemplateVO agentTemplate(String templateId) {
        AiTemplate aiTemplate = aiTemplateDao.queryByTemplateId(templateId);
        if (aiTemplate == null) {
            throw new MiniAgentException("Template 不存在");
        }

        AiPlaza aiPlaza = aiPlazaDao.queryByTemplateId(templateId);
        AiUser aiUser = aiUserDao.queryById(aiTemplate.getUserId());
        SnapshotView snapshotView = parseSnapshot(aiTemplate.getSnapshot());

        return TemplateVO.builder()
                .templateId(aiTemplate.getTemplateId())
                .userName(aiUser == null ? "" : aiUser.getUserName())
                .plazaTitle(aiPlaza == null ? aiTemplate.getAgentName() : aiPlaza.getPlazaTitle())
                .plazaDesc(aiPlaza == null ? aiTemplate.getAgentDesc() : aiPlaza.getPlazaDesc())
                .likeCount(aiPlaza == null ? 0 : aiPlaza.getLikeCount())
                .favorCount(aiPlaza == null ? 0 : aiPlaza.getFavorCount())
                .commentCount(aiPlaza == null ? 0 : aiPlaza.getCommentCount())
                .agentName(aiTemplate.getAgentName())
                .agentType(aiTemplate.getAgentType())
                .agentDesc(aiTemplate.getAgentDesc())
                .apiUrl(aiTemplate.getApiUrl())
                .modelName(aiTemplate.getModelName())
                .modelType(aiTemplate.getModelType())
                .mcpInfoList(snapshotView.mcpInfoList())
                .systemPrompt(snapshotView.systemPrompt())
                .userPrompt(snapshotView.userPrompt())
                .build();
    }

    @Override
    public void agentDelete(String agentId) {
        Long userId = userContext.getUserId();
        AiAgent aiAgent = requireUserAgent(agentId, userId);

        List<AiTemplate> aiTemplateList = aiTemplateDao.listByAgentIdAndUserId(agentId, userId);
        if (aiTemplateList != null && !aiTemplateList.isEmpty()) {
            for (AiTemplate aiTemplate : aiTemplateList) {
                String templateId = aiTemplate.getTemplateId();
                List<AiPlaza> aiPlazaList = aiPlazaDao.listByTemplateId(templateId);
                if (aiPlazaList != null && !aiPlazaList.isEmpty()) {
                    for (AiPlaza aiPlaza : aiPlazaList) {
                        deletePlazaCascade(aiPlaza.getPlazaId());
                    }
                }
                aiRepoDao.deleteByTemplateId(templateId);
            }
        }
        aiTemplateDao.deleteByAgentIdAndUserId(agentId, userId);

        List<AiFlow> aiFlowList = aiFlowDao.queryByAgentId(agentId);
        Set<String> clientIdSet = new LinkedHashSet<>();
        if (aiFlowList != null && !aiFlowList.isEmpty()) {
            aiFlowList.stream()
                    .map(AiFlow::getClientId)
                    .filter(StringUtils::hasText)
                    .forEach(clientIdSet::add);
        }

        aiTaskDao.deleteByAgentId(agentId);
        aiFlowDao.deleteByAgentId(agentId);

        for (String clientId : clientIdSet) {
            AiClient aiClient = aiClientDao.queryByClientId(clientId);
            if (aiClient == null || !Objects.equals(aiClient.getClientFrom(), userId)) {
                continue;
            }
            aiConfigDao.deleteByClientId(clientId);
            aiClientDao.deleteByClientId(clientId);
        }

        aiRepoDao.deleteByAgentId(agentId);
        aiAgentDao.delete(aiAgent.getId());
    }

    private Map<String, RepoVO> initRepoMap() {
        Map<String, RepoVO> resultMap = new LinkedHashMap<>();
        resultMap.put(REPO_SELF, RepoVO.builder().repoType(REPO_SELF).total(0).list(new ArrayList<>()).build());
        resultMap.put(REPO_FORK, RepoVO.builder().repoType(REPO_FORK).total(0).list(new ArrayList<>()).build());
        resultMap.put(REPO_FAVOR, RepoVO.builder().repoType(REPO_FAVOR).total(0).list(new ArrayList<>()).build());
        return resultMap;
    }

    private void deletePlazaCascade(String plazaId) {
        aiPlazaLikeDao.deleteByPlazaId(plazaId);
        aiPlazaFavorDao.deleteByPlazaId(plazaId);
        aiPlazaCommentDao.deleteByPlazaId(plazaId);
        aiPlazaDao.deleteByPlazaId(plazaId);
    }

    private AiAgent requireUserAgent(String agentId, Long userId) {
        AiAgent aiAgent = aiAgentDao.queryAgentByAgentId(agentId);
        if (aiAgent == null || !Objects.equals(aiAgent.getAgentFrom(), userId)) {
            throw new MiniAgentException(SETTING_USER_ILLEGAL);
        }
        return aiAgent;
    }

    private SnapshotBuildResult buildSnapshotByFlowList(List<AiFlow> aiFlowList) {
        String apiUrl = "";
        String modelName = "";
        String modelType = "";

        List<JSONObject> mcpSnapshotList = new ArrayList<>();
        Set<String> mcpIdSet = new LinkedHashSet<>();
        List<JSONObject> systemPromptList = new ArrayList<>();
        List<JSONObject> userPromptList = new ArrayList<>();

        for (AiFlow aiFlow : aiFlowList) {
            AiClient aiClient = aiClientDao.queryByClientId(aiFlow.getClientId());
            if (aiClient == null) {
                continue;
            }

            if (!StringUtils.hasText(apiUrl)) {
                AiModel aiModel = aiModelDao.queryByModelId(aiClient.getModelId());
                if (aiModel != null) {
                    modelName = aiModel.getModelName();
                    modelType = aiModel.getModelType();
                    AiApi aiApi = aiApiDao.queryByApiId(aiModel.getApiId());
                    if (aiApi != null) {
                        apiUrl = buildApiUrl(aiApi.getApiBaseUrl(), aiApi.getApiCompletionsPath());
                    }
                }
            }

            systemPromptList.add(buildSystemPromptItem(aiFlow.getClientRole(), loadSystemPrompt(aiClient.getClientId())));
            userPromptList.add(buildUserPromptItem(aiFlow.getFlowSeq(), aiFlow.getUserPrompt()));

            List<AiConfig> mcpConfigList = aiConfigDao.queryByClientIdAndConfigType(aiClient.getClientId(), CONFIG_MCP);
            if (mcpConfigList == null || mcpConfigList.isEmpty()) {
                continue;
            }
            for (AiConfig mcpConfig : mcpConfigList) {
                if (mcpConfig == null || !Objects.equals(mcpConfig.getConfigStatus(), 1)) {
                    continue;
                }
                String mcpId = mcpConfig.getConfigValue();
                if (!StringUtils.hasText(mcpId) || mcpIdSet.contains(mcpId)) {
                    continue;
                }

                AiMcp aiMcp = aiMcpDao.queryByMcpId(mcpId);
                if (aiMcp == null) {
                    continue;
                }

                JSONObject mcpInfo = new JSONObject();
                mcpInfo.put("mcpName", aiMcp.getMcpName());
                mcpInfo.put("mcpType", aiMcp.getMcpType());
                mcpInfo.put("mcpDesc", aiMcp.getMcpDesc());
                mcpInfo.put("mcpParamTemplate", parseJsonOrRaw(aiMcp.getMcpParam()));
                mcpInfo.put("requiredSecrets", extractSecretKeyList(aiMcp.getMcpSecret()));
                mcpSnapshotList.add(mcpInfo);
                mcpIdSet.add(mcpId);
            }
        }

        JSONObject snapshot = new JSONObject();
        snapshot.put("version", 1);
        snapshot.put("mcps", mcpSnapshotList);
        snapshot.put("systemPrompts", systemPromptList);
        snapshot.put("userPrompts", userPromptList);

        return new SnapshotBuildResult(JSON.toJSONString(snapshot), apiUrl, modelName, modelType);
    }

    private AiTemplate upsertTemplate(AiAgent aiAgent, Long userId, SnapshotBuildResult snapshotBuildResult) {
        AiTemplate existed = aiTemplateDao.queryByAgentIdAndUserId(aiAgent.getAgentId(), userId);
        if (existed == null) {
            AiTemplate toInsert = AiTemplate.builder()
                    .templateId(randomUtil.userRandom())
                    .userId(userId)
                    .agentId(aiAgent.getAgentId())
                    .agentName(aiAgent.getAgentName())
                    .agentType(aiAgent.getAgentType())
                    .agentDesc(defaultText(aiAgent.getAgentDesc()))
                    .apiUrl(snapshotBuildResult.apiUrl())
                    .modelName(snapshotBuildResult.modelName())
                    .modelType(snapshotBuildResult.modelType())
                    .snapshot(snapshotBuildResult.snapshot())
                    .build();
            aiTemplateDao.insert(toInsert);
            return toInsert;
        }

        existed.setUserId(userId);
        existed.setAgentId(aiAgent.getAgentId());
        existed.setAgentName(aiAgent.getAgentName());
        existed.setAgentType(aiAgent.getAgentType());
        existed.setAgentDesc(defaultText(aiAgent.getAgentDesc()));
        existed.setApiUrl(snapshotBuildResult.apiUrl());
        existed.setModelName(snapshotBuildResult.modelName());
        existed.setModelType(snapshotBuildResult.modelType());
        existed.setSnapshot(snapshotBuildResult.snapshot());
        aiTemplateDao.update(existed);
        return existed;
    }

    private void upsertPlaza(AiAgent aiAgent, Long userId, String templateId) {
        AiPlaza existed = aiPlazaDao.queryByTemplateId(templateId);
        if (existed == null) {
            aiPlazaDao.insert(AiPlaza.builder()
                    .plazaId(randomUtil.userRandom())
                    .templateId(templateId)
                    .userId(userId)
                    .agentName(aiAgent.getAgentName())
                    .agentType(aiAgent.getAgentType())
                    .plazaTitle(buildPlazaTitle(aiAgent.getAgentName(), aiAgent.getAgentType()))
                    .plazaDesc(defaultText(aiAgent.getAgentDesc()))
                    .likeCount(0)
                    .favorCount(0)
                    .commentCount(0)
                    .build());
            return;
        }

        existed.setTemplateId(templateId);
        existed.setUserId(userId);
        existed.setAgentName(aiAgent.getAgentName());
        existed.setAgentType(aiAgent.getAgentType());
        existed.setPlazaTitle(buildPlazaTitle(aiAgent.getAgentName(), aiAgent.getAgentType()));
        existed.setPlazaDesc(defaultText(aiAgent.getAgentDesc()));
        aiPlazaDao.update(existed);
    }

    private void upsertSelfRepo(Long userId, String agentId, String templateId) {
        AiRepo existed = aiRepoDao.queryByUserIdAndAgentIdAndRepoType(userId, agentId, REPO_SELF);
        if (existed == null) {
            aiRepoDao.insert(AiRepo.builder()
                    .repoId(randomUtil.userRandom())
                    .userId(userId)
                    .agentId(agentId)
                    .templateId(templateId)
                    .repoType(REPO_SELF)
                    .build());
            return;
        }
        existed.setTemplateId(templateId);
        aiRepoDao.update(existed);
    }

    private SnapshotView parseSnapshot(String snapshotRaw) {
        if (!StringUtils.hasText(snapshotRaw)) {
            return SnapshotView.empty();
        }

        try {
            JSONObject snapshot = JSON.parseObject(snapshotRaw);
            if (snapshot == null) {
                return SnapshotView.empty();
            }

            List<TemplateVO.McpInfo> mcpInfoList = parseSnapshotMcpList(snapshot.getJSONArray("mcps"));
            Map<String, String> systemPrompt = parseSnapshotSystemPrompt(snapshot.getJSONArray("systemPrompts"));
            List<String> userPrompt = parseSnapshotUserPrompt(snapshot.getJSONArray("userPrompts"));
            return new SnapshotView(mcpInfoList, systemPrompt, userPrompt);
        } catch (Exception e) {
            log.warn("解析 Template 快照失败，snapshot={}", snapshotRaw, e);
            return SnapshotView.empty();
        }
    }

    private List<TemplateVO.McpInfo> parseSnapshotMcpList(JSONArray mcpArray) {
        if (mcpArray == null || mcpArray.isEmpty()) {
            return List.of();
        }
        List<TemplateVO.McpInfo> result = new ArrayList<>();
        for (int i = 0; i < mcpArray.size(); i++) {
            JSONObject mcp = mcpArray.getJSONObject(i);
            if (mcp == null) {
                continue;
            }
            result.add(TemplateVO.McpInfo.builder()
                    .mcpName(mcp.getString("mcpName"))
                    .mcpType(mcp.getString("mcpType"))
                    .mcpDesc(mcp.getString("mcpDesc"))
                    .mcpParamTemplate(mcp.get("mcpParamTemplate"))
                    .requiredSecrets(parseStringList(mcp.getJSONArray("requiredSecrets")))
                    .build());
        }
        return result;
    }

    private Map<String, String> parseSnapshotSystemPrompt(JSONArray systemPromptArray) {
        Map<String, String> systemPromptMap = new LinkedHashMap<>();
        if (systemPromptArray == null || systemPromptArray.isEmpty()) {
            return systemPromptMap;
        }
        for (int i = 0; i < systemPromptArray.size(); i++) {
            JSONObject item = systemPromptArray.getJSONObject(i);
            if (item == null) {
                continue;
            }
            String clientRole = item.getString("clientRole");
            if (!StringUtils.hasText(clientRole)) {
                continue;
            }
            systemPromptMap.put(clientRole, defaultText(item.getString("content")));
        }
        return systemPromptMap;
    }

    private List<String> parseSnapshotUserPrompt(JSONArray userPromptArray) {
        if (userPromptArray == null || userPromptArray.isEmpty()) {
            return List.of();
        }
        List<JSONObject> tempList = new ArrayList<>();
        for (int i = 0; i < userPromptArray.size(); i++) {
            JSONObject item = userPromptArray.getJSONObject(i);
            if (item != null) {
                tempList.add(item);
            }
        }
        tempList.sort(Comparator.comparing(item -> item.getInteger("seq"), Comparator.nullsLast(Integer::compareTo)));

        List<String> result = new ArrayList<>();
        for (JSONObject item : tempList) {
            result.add(defaultText(item.getString("content")));
        }
        return result;
    }

    private String loadSystemPrompt(String clientId) {
        List<AiConfig> promptConfigList = aiConfigDao.queryByClientIdAndConfigType(clientId, CONFIG_PROMPT);
        if (promptConfigList == null || promptConfigList.isEmpty()) {
            return "";
        }
        for (AiConfig promptConfig : promptConfigList) {
            if (promptConfig == null || !Objects.equals(promptConfig.getConfigStatus(), 1)) {
                continue;
            }
            String promptId = promptConfig.getConfigValue();
            if (!StringUtils.hasText(promptId)) {
                continue;
            }
            AiPrompt aiPrompt = aiPromptDao.queryByPromptId(promptId);
            if (aiPrompt != null) {
                return defaultText(aiPrompt.getSystenPrompt());
            }
        }
        return "";
    }

    private JSONObject buildSystemPromptItem(String clientRole, String promptContent) {
        JSONObject systemPrompt = new JSONObject();
        systemPrompt.put("clientRole", clientRole);
        systemPrompt.put("content", defaultText(promptContent));
        return systemPrompt;
    }

    private JSONObject buildUserPromptItem(Integer seq, String userPrompt) {
        JSONObject prompt = new JSONObject();
        prompt.put("seq", seq);
        prompt.put("content", defaultText(userPrompt));
        return prompt;
    }

    private String buildApiUrl(String baseUrl, String completionPath) {
        if (!StringUtils.hasText(baseUrl)) {
            return "";
        }
        String cleanBase = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        String cleanPath = defaultText(completionPath);
        if (!StringUtils.hasText(cleanPath)) {
            return cleanBase;
        }
        cleanPath = cleanPath.startsWith("/") ? cleanPath : "/" + cleanPath;
        return cleanBase + cleanPath;
    }

    private String buildPlazaTitle(String agentName, String agentType) {
        String name = defaultText(agentName);
        String type = defaultText(agentType);
        if (!StringUtils.hasText(type)) {
            return name;
        }
        return name + "（" + type + "）";
    }

    private Object parseJsonOrRaw(String raw) {
        if (!StringUtils.hasText(raw)) {
            return "";
        }
        String text = raw.trim();
        try {
            if (text.startsWith("{")) {
                return JSON.parseObject(text);
            }
            if (text.startsWith("[")) {
                return JSON.parseArray(text);
            }
        } catch (Exception e) {
            log.warn("解析 JSON 参数失败，按原文返回：{}", raw, e);
        }
        return raw;
    }

    private List<String> extractSecretKeyList(String secretRaw) {
        if (!StringUtils.hasText(secretRaw)) {
            return List.of();
        }
        String text = secretRaw.trim();
        try {
            if (text.startsWith("{")) {
                JSONObject secretObj = JSON.parseObject(text);
                if (secretObj == null || secretObj.isEmpty()) {
                    return List.of();
                }
                return new ArrayList<>(secretObj.keySet());
            }
            if (text.startsWith("[")) {
                JSONArray secretArray = JSON.parseArray(text);
                return parseStringList(secretArray);
            }
        } catch (Exception e) {
            log.warn("解析 mcp_secret 失败，secret={}", secretRaw, e);
        }
        return List.of();
    }

    private List<String> parseStringList(JSONArray jsonArray) {
        if (jsonArray == null || jsonArray.isEmpty()) {
            return List.of();
        }
        List<String> result = new ArrayList<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            String value = jsonArray.getString(i);
            if (StringUtils.hasText(value)) {
                result.add(value);
            }
        }
        return result;
    }

    private String defaultText(String text) {
        return text == null ? "" : text;
    }

    private Set<String> queryUserPlazaSet(Long userId, List<String> plazaIdList, PlazaActionType actionType) {
        if (userId == null || plazaIdList == null || plazaIdList.isEmpty()) {
            return new HashSet<>();
        }

        List<String> dataList;
        switch (actionType) {
            case LIKE -> dataList = aiPlazaLikeDao.queryPlazaIdListByUserIdAndPlazaIdList(userId, plazaIdList);
            case FAVOR -> dataList = aiPlazaFavorDao.queryPlazaIdListByUserIdAndPlazaIdList(userId, plazaIdList);
            case COMMENT -> dataList = aiPlazaCommentDao.queryPlazaIdListByUserIdAndPlazaIdList(userId, plazaIdList);
            default -> dataList = List.of();
        }
        if (dataList == null || dataList.isEmpty()) {
            return new HashSet<>();
        }
        return new HashSet<>(dataList);
    }

    private record SnapshotBuildResult(String snapshot, String apiUrl, String modelName, String modelType) {
    }

    private record SnapshotView(List<TemplateVO.McpInfo> mcpInfoList,
                                Map<String, String> systemPrompt,
                                List<String> userPrompt) {
        private static SnapshotView empty() {
            return new SnapshotView(List.of(), new LinkedHashMap<>(), List.of());
        }
    }

}
