package com.dasi.infrastructure.repository;

import com.dasi.domain.agent.repository.IAgentRepository;
import com.dasi.infrastructure.persistent.dao.*;
import com.dasi.infrastructure.persistent.po.*;
import com.dasi.types.dto.request.plaza.PlazaCommentRequest;
import com.dasi.types.dto.request.plaza.PlazaListRequest;
import com.dasi.types.dto.request.plaza.PlazaPublishRequest;
import com.dasi.types.dto.request.studio.StudioCreateRequest;
import com.dasi.types.dto.request.studio.StudioUpdateRequest;
import com.dasi.types.dto.response.plaza.PlazaCommentResponse;
import com.dasi.types.dto.response.plaza.PlazaDetailResponse;
import com.dasi.types.dto.response.plaza.PlazaItemResponse;
import com.dasi.types.dto.response.repo.RepoItemResponse;
import com.dasi.types.dto.response.studio.StudioAgentResponse;
import com.dasi.types.dto.response.studio.StudioGenerateResponse;
import com.dasi.types.dto.result.PageResult;
import com.dasi.types.exception.AdminException;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Repository
public class AgentRepository implements IAgentRepository {

    private static final DateTimeFormatter AGENT_ID_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyMMddHHmmss");

    @Resource
    private IAiAgentDao aiAgentDao;

    @Resource
    private IAiFlowDao aiFlowDao;

    @Resource
    private IAiRepoDao aiRepoDao;

    @Resource
    private IAiPlazaDao aiPlazaDao;

    @Resource
    private IAiPlazaLikeDao aiPlazaLikeDao;

    @Resource
    private IAiPlazaFavorDao aiPlazaFavorDao;

    @Resource
    private IAiPlazaCommentDao aiPlazaCommentDao;

    @Resource
    private IUserDao userDao;

    // -------------------- Studio --------------------

    @Override
    public StudioGenerateResponse studioGenerate(Long userId, String taskPrompt, String strategy, List<String> mcpIdList) {
        String normalizedStrategy = normalizeStrategy(strategy);
        String trimmedPrompt = taskPrompt == null ? "" : taskPrompt.trim();
        String titleSeed = trimmedPrompt.isEmpty() ? "新建智能体" : trimmedPrompt;
        String agentName = titleSeed.length() > 12 ? titleSeed.substring(0, 12) : titleSeed;

        String flowPrompt = "你是一个" + normalizedStrategy + "策略执行代理，请围绕用户目标进行拆解、执行与反馈。";
        if (!trimmedPrompt.isEmpty()) {
            flowPrompt = flowPrompt + "\n用户目标：" + trimmedPrompt;
        }

        return StudioGenerateResponse.builder()
                .agentName(agentName)
                .agentType(normalizedStrategy)
                .agentDesc("由 Studio 自动生成")
                .flowPrompt(flowPrompt)
                .mcpIdList(mcpIdList == null ? List.of() : mcpIdList)
                .build();
    }

    @Override
    public StudioAgentResponse studioCreate(Long userId, StudioCreateRequest request) {
        String agentId = StringUtils.hasText(request.getAgentId()) ? request.getAgentId() : buildAgentId(userId);

        AiAgent exists = aiAgentDao.queryAgentByAgentIdWithFrom(agentId, userId);
        if (exists != null) {
            throw new AdminException("AGENT 已存在，请修改后重新创建");
        }

        AiAgent aiAgent = AiAgent.builder()
                .agentId(agentId)
                .agentName(request.getAgentName())
                .agentType(normalizeStrategy(request.getAgentType()))
                .agentDesc(defaultDesc(request.getAgentDesc()))
                .agentStatus(1)
                .agentFrom(userId)
                .build();
        aiAgentDao.insert(aiAgent);

        insertDefaultFlow(agentId, aiAgent.getAgentType(), request.getFlowPrompt());

        AiRepo existingRepo = aiRepoDao.queryByUserIdAndAgentId(userId, agentId);
        if (existingRepo == null) {
            aiRepoDao.insert(AiRepo.builder()
                    .repoId(UUID.randomUUID().toString().replace("-", ""))
                    .userId(userId)
                    .agentId(agentId)
                    .repoType("self")
                    .repoStatus(1)
                    .build());
        } else if (!Integer.valueOf(1).equals(existingRepo.getRepoStatus())) {
            aiRepoDao.updateStatusByUserIdAndAgentId(userId, agentId, 1);
        }

        return studioDetail(userId, agentId);
    }

    @Override
    public StudioAgentResponse studioUpdate(Long userId, StudioUpdateRequest request) {
        AiAgent exists = aiAgentDao.queryAgentByAgentIdByOwner(request.getAgentId(), userId);
        if (exists == null) {
            throw new AdminException("AGENT 不存在或无权限修改");
        }

        AiAgent aiAgent = AiAgent.builder()
                .id(exists.getId())
                .agentName(request.getAgentName())
                .agentDesc(defaultDesc(request.getAgentDesc()))
                .agentType(exists.getAgentType())
                .agentStatus(request.getAgentStatus() == null ? exists.getAgentStatus() : request.getAgentStatus())
                .agentFrom(userId)
                .build();
        aiAgentDao.updateByOwner(aiAgent);

        if (StringUtils.hasText(request.getFlowPrompt())) {
            List<AiFlow> aiFlowList = aiFlowDao.queryByAgentId(request.getAgentId());
            for (AiFlow aiFlow : aiFlowList) {
                aiFlowDao.update(AiFlow.builder().id(aiFlow.getId()).flowPrompt(request.getFlowPrompt()).build());
            }
        }

        return studioDetail(userId, request.getAgentId());
    }

    @Override
    public StudioAgentResponse studioDetail(Long userId, String agentId) {
        AiAgent aiAgent = aiAgentDao.queryAgentByAgentIdByOwner(agentId, userId);
        if (aiAgent == null) {
            return null;
        }
        return toStudioAgentResponse(aiAgent);
    }

    @Override
    public List<StudioAgentResponse> studioListMine(Long userId) {
        List<AiAgent> aiAgentList = aiAgentDao.queryAgentListByFrom(userId);
        if (aiAgentList == null || aiAgentList.isEmpty()) {
            return List.of();
        }

        List<StudioAgentResponse> responseList = new ArrayList<>();
        for (AiAgent aiAgent : aiAgentList) {
            if (!userId.equals(aiAgent.getAgentFrom())) {
                continue;
            }
            responseList.add(toStudioAgentResponse(aiAgent));
        }
        return responseList;
    }

    // -------------------- Plaza --------------------

    @Override
    public PageResult<PlazaItemResponse> plazaList(Long userId, PlazaListRequest request) {
        int pageNum = request.getPageNum() == null || request.getPageNum() < 1 ? 1 : request.getPageNum();
        int pageSize = request.getPageSize() == null || request.getPageSize() < 1 ? 10 : request.getPageSize();
        int offset = (pageNum - 1) * pageSize;

        List<AiPlaza> aiPlazaList = aiPlazaDao.list(request.getTitleKeyword(), offset, pageSize);
        int total = aiPlazaDao.count(request.getTitleKeyword());

        List<PlazaItemResponse> responseList = new ArrayList<>();
        for (AiPlaza aiPlaza : aiPlazaList) {
            boolean liked = aiPlazaLikeDao.queryByPlazaIdAndUserId(aiPlaza.getPlazaId(), userId) != null;
            boolean favored = aiPlazaFavorDao.queryByPlazaIdAndUserId(aiPlaza.getPlazaId(), userId) != null;
            responseList.add(PlazaItemResponse.builder()
                    .plazaId(aiPlaza.getPlazaId())
                    .agentId(aiPlaza.getAgentId())
                    .agentType(resolvePlazaAgentType(aiPlaza))
                    .username(resolvePlazaUsername(aiPlaza))
                    .plazaTitle(aiPlaza.getPlazaTitle())
                    .plazaDesc(aiPlaza.getPlazaDesc())
                    .likeCount(aiPlaza.getLikeCount())
                    .favorCount(aiPlaza.getFavorCount())
                    .commentCount(aiPlaza.getCommentCount())
                    .liked(liked)
                    .favored(favored)
                    .createTime(aiPlaza.getCreateTime())
                    .build());
        }

        int pageSum = (total + pageSize - 1) / pageSize;
        return PageResult.<PlazaItemResponse>builder()
                .list(responseList)
                .total(total)
                .pageNum(pageNum)
                .pageSize(pageSize)
                .pageSum(pageSum)
                .build();
    }

    @Override
    public PlazaDetailResponse plazaDetail(Long userId, String plazaId) {
        AiPlaza aiPlaza = aiPlazaDao.queryByPlazaId(plazaId);
        if (aiPlaza == null) {
            throw new AdminException("广场内容不存在");
        }

        boolean liked = aiPlazaLikeDao.queryByPlazaIdAndUserId(plazaId, userId) != null;
        boolean favored = aiPlazaFavorDao.queryByPlazaIdAndUserId(plazaId, userId) != null;
        PlazaItemResponse plazaItem = PlazaItemResponse.builder()
                .plazaId(aiPlaza.getPlazaId())
                .agentId(aiPlaza.getAgentId())
                .agentType(resolvePlazaAgentType(aiPlaza))
                .username(resolvePlazaUsername(aiPlaza))
                .plazaTitle(aiPlaza.getPlazaTitle())
                .plazaDesc(aiPlaza.getPlazaDesc())
                .likeCount(aiPlaza.getLikeCount())
                .favorCount(aiPlaza.getFavorCount())
                .commentCount(aiPlaza.getCommentCount())
                .liked(liked)
                .favored(favored)
                .createTime(aiPlaza.getCreateTime())
                .build();

        List<AiPlazaComment> aiPlazaCommentList = aiPlazaCommentDao.listByPlazaId(plazaId, 0, 30);
        List<PlazaCommentResponse> commentList = new ArrayList<>();
        for (AiPlazaComment aiPlazaComment : aiPlazaCommentList) {
            User user = userDao.queryById(aiPlazaComment.getUserId());
            commentList.add(PlazaCommentResponse.builder()
                    .commentId(aiPlazaComment.getCommentId())
                    .plazaId(aiPlazaComment.getPlazaId())
                    .userId(aiPlazaComment.getUserId())
                    .username(user == null ? "未知用户" : user.getUsername())
                    .commentContent(aiPlazaComment.getCommentContent())
                    .createTime(aiPlazaComment.getCreateTime())
                    .build());
        }

        return PlazaDetailResponse.builder()
                .plazaItem(plazaItem)
                .commentList(commentList)
                .build();
    }

    @Override
    public void plazaPublish(Long userId, PlazaPublishRequest request) {
        AiAgent aiAgent = aiAgentDao.queryAgentByAgentIdByOwner(request.getAgentId(), userId);
        if (aiAgent == null) {
            throw new AdminException("仅可发布自己创建的 Agent");
        }

        AiPlaza exists = aiPlazaDao.queryByAgentIdAndUserId(request.getAgentId(), userId);
        if (exists != null) {
            throw new AdminException("该 Agent 已发布");
        }

        aiPlazaDao.insert(AiPlaza.builder()
                .plazaId(UUID.randomUUID().toString().replace("-", ""))
                .agentId(request.getAgentId())
                .userId(userId)
                .agentType(aiAgent.getAgentType())
                .username(resolveUsernameByUserId(userId))
                .plazaTitle(request.getPlazaTitle())
                .plazaDesc(StringUtils.hasText(request.getPlazaDesc()) ? request.getPlazaDesc() : "暂无")
                .plazaStatus(1)
                .likeCount(0)
                .favorCount(0)
                .commentCount(0)
                .build());
    }

    @Override
    public void plazaLike(Long userId, String plazaId) {
        AiPlaza aiPlaza = aiPlazaDao.queryByPlazaId(plazaId);
        if (aiPlaza == null) {
            throw new AdminException("广场内容不存在");
        }

        AiPlazaLike exists = aiPlazaLikeDao.queryByPlazaIdAndUserId(plazaId, userId);
        if (exists == null) {
            aiPlazaLikeDao.insert(AiPlazaLike.builder().plazaId(plazaId).userId(userId).build());
            aiPlazaDao.increaseLikeCount(plazaId, 1);
            return;
        }

        aiPlazaLikeDao.deleteByPlazaIdAndUserId(plazaId, userId);
        aiPlazaDao.increaseLikeCount(plazaId, -1);
    }

    @Override
    public void plazaFavor(Long userId, String plazaId) {
        AiPlaza aiPlaza = aiPlazaDao.queryByPlazaId(plazaId);
        if (aiPlaza == null) {
            throw new AdminException("广场内容不存在");
        }

        AiPlazaFavor exists = aiPlazaFavorDao.queryByPlazaIdAndUserId(plazaId, userId);
        if (exists == null) {
            aiPlazaFavorDao.insert(AiPlazaFavor.builder().plazaId(plazaId).userId(userId).build());
            aiPlazaDao.increaseFavorCount(plazaId, 1);
            return;
        }

        aiPlazaFavorDao.deleteByPlazaIdAndUserId(plazaId, userId);
        aiPlazaDao.increaseFavorCount(plazaId, -1);
    }

    @Override
    public void plazaComment(Long userId, PlazaCommentRequest request) {
        AiPlaza aiPlaza = aiPlazaDao.queryByPlazaId(request.getPlazaId());
        if (aiPlaza == null) {
            throw new AdminException("广场内容不存在");
        }

        aiPlazaCommentDao.insert(AiPlazaComment.builder()
                .commentId(UUID.randomUUID().toString().replace("-", ""))
                .plazaId(request.getPlazaId())
                .userId(userId)
                .commentContent(request.getCommentContent())
                .commentStatus(1)
                .build());
        aiPlazaDao.increaseCommentCount(request.getPlazaId(), 1);
    }

    @Override
    public void plazaCommentCount(Long userId, String plazaId) {
        AiPlaza aiPlaza = aiPlazaDao.queryByPlazaId(plazaId);
        if (aiPlaza == null) {
            throw new AdminException("广场内容不存在");
        }
        aiPlazaDao.increaseCommentCount(plazaId, 1);
    }

    // -------------------- Repo --------------------

    @Override
    public List<RepoItemResponse> repoList(Long userId) {
        List<AiRepo> aiRepoList = aiRepoDao.queryByUserIdAndStatus(userId, 1);
        if (aiRepoList == null || aiRepoList.isEmpty()) {
            return List.of();
        }

        List<String> agentIdList = aiRepoList.stream().map(AiRepo::getAgentId).distinct().toList();
        Map<String, AiAgent> aiAgentMap = aiAgentDao.queryAgentListByIdList(agentIdList).stream()
                .collect(Collectors.toMap(AiAgent::getAgentId, Function.identity(), (a, b) -> a));

        List<RepoItemResponse> resultList = new ArrayList<>();
        for (AiRepo aiRepo : aiRepoList) {
            AiAgent aiAgent = aiAgentMap.get(aiRepo.getAgentId());
            if (aiAgent == null || !Integer.valueOf(1).equals(aiAgent.getAgentStatus())) {
                continue;
            }
            resultList.add(RepoItemResponse.builder()
                    .repoId(aiRepo.getRepoId())
                    .agentId(aiRepo.getAgentId())
                    .agentName(aiAgent.getAgentName())
                    .agentDesc(aiAgent.getAgentDesc())
                    .repoType(aiRepo.getRepoType())
                    .sourceType(resolveSourceType(aiRepo.getRepoType()))
                    .updateTime(aiRepo.getUpdateTime())
                    .build());
        }

        return resultList;
    }

    @Override
    public void repoAdd(Long userId, String agentId) {
        AiAgent aiAgent = aiAgentDao.queryAgentByAgentIdWithFrom(agentId, userId);
        if (aiAgent == null || !Integer.valueOf(1).equals(aiAgent.getAgentStatus())) {
            throw new AdminException("AGENT 不存在或不可用");
        }

        String repoType = resolveRepoType(userId, aiAgent, null);
        upsertRepo(userId, agentId, repoType, null);
    }

    @Override
    public void repoRemove(Long userId, String agentId) {
        AiRepo exists = aiRepoDao.queryByUserIdAndAgentId(userId, agentId);
        if (exists == null) {
            return;
        }
        aiRepoDao.updateStatusByUserIdAndAgentId(userId, agentId, 0);
    }

    @Override
    public void repoFork(Long userId, String plazaId) {
        AiPlaza aiPlaza = aiPlazaDao.queryByPlazaId(plazaId);
        if (aiPlaza == null) {
            throw new AdminException("广场内容不存在");
        }

        AiAgent aiAgent = aiAgentDao.queryAgentByAgentIdWithFrom(aiPlaza.getAgentId(), userId);
        if (aiAgent == null || !Integer.valueOf(1).equals(aiAgent.getAgentStatus())) {
            throw new AdminException("AGENT 不存在或不可用");
        }

        String repoType = resolveRepoType(userId, aiAgent, aiPlaza);
        upsertRepo(userId, aiPlaza.getAgentId(), repoType, plazaId);
    }

    // -------------------- Studio Helper --------------------

    private StudioAgentResponse toStudioAgentResponse(AiAgent aiAgent) {
        return StudioAgentResponse.builder()
                .agentId(aiAgent.getAgentId())
                .agentName(aiAgent.getAgentName())
                .agentType(aiAgent.getAgentType())
                .agentDesc(aiAgent.getAgentDesc())
                .agentStatus(aiAgent.getAgentStatus())
                .sourceType(aiAgent.getAgentFrom() != null && aiAgent.getAgentFrom() > 0 ? "mine" : "system")
                .updateTime(aiAgent.getUpdateTime())
                .build();
    }

    private String buildAgentId(Long userId) {
        return "agent_u" + userId + "_" + LocalDateTime.now().format(AGENT_ID_TIME_FORMATTER);
    }

    private String defaultDesc(String desc) {
        return StringUtils.hasText(desc) ? desc : "暂无";
    }

    private String normalizeStrategy(String strategy) {
        String value = (strategy == null ? "" : strategy).trim().toLowerCase(Locale.ROOT);
        if ("step".equals(value) || "loop".equals(value) || "react".equals(value)) {
            return value;
        }
        return "react";
    }

    private void insertDefaultFlow(String agentId, String agentType, String flowPrompt) {
        String prompt = StringUtils.hasText(flowPrompt) ? flowPrompt : "请根据用户输入执行对应任务。";

        List<FlowTemplate> flowTemplateList;
        if ("step".equals(agentType)) {
            flowTemplateList = List.of(
                    new FlowTemplate("client_inspector_article", "inspector", 1),
                    new FlowTemplate("client_planner_article", "planner", 2),
                    new FlowTemplate("client_runner_article", "runner", 3),
                    new FlowTemplate("client_replier_article", "replier", 4)
            );
        } else {
            flowTemplateList = List.of(
                    new FlowTemplate("client_analyzer_web", "analyzer", 1),
                    new FlowTemplate("client_performer_web", "performer", 2),
                    new FlowTemplate("client_supervisor_web", "supervisor", 3),
                    new FlowTemplate("client_summarizer_web", "summarizer", 4)
            );
        }

        for (FlowTemplate flowTemplate : flowTemplateList) {
            aiFlowDao.insert(AiFlow.builder()
                    .agentId(agentId)
                    .clientId(flowTemplate.clientId)
                    .clientRole(flowTemplate.clientRole)
                    .flowSeq(flowTemplate.flowSeq)
                    .flowPrompt(prompt)
                    .build());
        }
    }

    private record FlowTemplate(String clientId, String clientRole, Integer flowSeq) {
    }

    // -------------------- Repo Helper --------------------

    private void upsertRepo(Long userId, String agentId, String repoType, String sourceId) {
        AiRepo exists = aiRepoDao.queryByUserIdAndAgentId(userId, agentId);
        if (exists == null) {
            aiRepoDao.insert(AiRepo.builder()
                    .repoId(UUID.randomUUID().toString().replace("-", ""))
                    .userId(userId)
                    .agentId(agentId)
                    .repoType(repoType)
                    .sourceId(sourceId)
                    .repoStatus(1)
                    .build());
            return;
        }

        if (!Integer.valueOf(1).equals(exists.getRepoStatus())) {
            aiRepoDao.updateStatusByUserIdAndAgentId(userId, agentId, 1);
        }
    }

    private String resolveRepoType(Long userId, AiAgent aiAgent, AiPlaza aiPlaza) {
        if (aiAgent.getAgentFrom() != null && aiAgent.getAgentFrom().equals(userId)) {
            return "self";
        }
        if (aiAgent.getAgentFrom() != null && aiAgent.getAgentFrom() == 0L) {
            return "system";
        }
        if (aiPlaza != null) {
            return "fork";
        }
        return "favor";
    }

    private String resolveSourceType(String repoType) {
        if ("self".equalsIgnoreCase(repoType)) {
            return "mine";
        }
        if ("system".equalsIgnoreCase(repoType)) {
            return "system";
        }
        return "fork";
    }

    private String resolvePlazaAgentType(AiPlaza aiPlaza) {
        if (StringUtils.hasText(aiPlaza.getAgentType())) {
            return aiPlaza.getAgentType();
        }
        AiAgent aiAgent = aiAgentDao.queryAgentByAgentIdWithFrom(aiPlaza.getAgentId(), aiPlaza.getUserId());
        return aiAgent == null ? "react" : aiAgent.getAgentType();
    }

    private String resolvePlazaUsername(AiPlaza aiPlaza) {
        if (StringUtils.hasText(aiPlaza.getUsername())) {
            return aiPlaza.getUsername();
        }
        return resolveUsernameByUserId(aiPlaza.getUserId());
    }

    private String resolveUsernameByUserId(Long userId) {
        User user = userDao.queryById(userId);
        return user == null ? "未知用户" : user.getUsername();
    }
}
