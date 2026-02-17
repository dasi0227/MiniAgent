package com.dasi.infrastructure.repository;

import com.dasi.domain.studio.repository.IStudioRepository;
import com.dasi.infrastructure.persistent.dao.IAiAgentDao;
import com.dasi.infrastructure.persistent.dao.IAiFlowDao;
import com.dasi.infrastructure.persistent.dao.IAiRepoDao;
import com.dasi.infrastructure.persistent.po.AiAgent;
import com.dasi.infrastructure.persistent.po.AiFlow;
import com.dasi.infrastructure.persistent.po.AiRepo;
import com.dasi.types.dto.request.studio.StudioCreateRequest;
import com.dasi.types.dto.request.studio.StudioUpdateRequest;
import com.dasi.types.dto.response.studio.StudioAgentResponse;
import com.dasi.types.dto.response.studio.StudioGenerateResponse;
import com.dasi.types.exception.AdminException;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Repository
public class StudioRepository implements IStudioRepository {

    private static final DateTimeFormatter AGENT_ID_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyMMddHHmmss");

    @Resource
    private IAiAgentDao aiAgentDao;

    @Resource
    private IAiFlowDao aiFlowDao;

    @Resource
    private IAiRepoDao aiRepoDao;

    @Override
    public StudioGenerateResponse generate(Long userId, String taskPrompt, String strategy, List<String> mcpIdList) {
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
    public StudioAgentResponse create(Long userId, StudioCreateRequest request) {
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

        return detail(userId, agentId);
    }

    @Override
    public StudioAgentResponse update(Long userId, StudioUpdateRequest request) {
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

        return detail(userId, request.getAgentId());
    }

    @Override
    public StudioAgentResponse detail(Long userId, String agentId) {
        AiAgent aiAgent = aiAgentDao.queryAgentByAgentIdByOwner(agentId, userId);
        if (aiAgent == null) {
            return null;
        }
        return toStudioAgentResponse(aiAgent);
    }

    @Override
    public List<StudioAgentResponse> listMine(Long userId) {
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
}
