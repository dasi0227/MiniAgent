package com.dasi.domain.workspace.service;

import com.dasi.domain.util.jwt.UserContext;
import com.dasi.domain.util.mq.IMqService;
import com.dasi.domain.util.mq.MqEventDTO;
import com.dasi.domain.util.mq.MqEventType;
import com.dasi.domain.workspace.model.dto.*;
import com.dasi.domain.workspace.model.entity.RolePromptEntity;
import com.dasi.domain.workspace.model.enumeration.RepoType;
import com.dasi.domain.workspace.model.enumeration.RoleType;
import com.dasi.domain.workspace.model.enumeration.StrategyType;
import com.dasi.domain.workspace.model.vo.CommentVO;
import com.dasi.domain.workspace.model.vo.AgentVO;
import com.dasi.domain.workspace.model.vo.PlazaVO;
import com.dasi.domain.workspace.model.vo.RepoVO;
import com.dasi.domain.workspace.model.vo.TemplateVO;
import com.dasi.domain.workspace.repository.IWorkspaceRepository;
import com.dasi.types.exception.WorkException;
import com.dasi.types.result.PageResult;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.dasi.domain.workspace.model.enumeration.PromptType.SYSTEM_PROMPT;
import static com.dasi.domain.workspace.model.enumeration.PromptType.USER_PROMPT;
import static com.dasi.types.constant.ExceptionMessage.ILLEGAL_DATA;

@Slf4j
@Service
public class WorkspaceService implements IWorkspaceService {

    @Resource
    private IWorkspaceRepository workspaceRepository;

    @Resource
    private PromptGenerator promptGenerator;

    @Resource
    private IMqService mqService;

    @Resource
    private UserContext userContext;

    private final PathMatchingResourcePatternResolver RESOURCE_RESOLVER = new PathMatchingResourcePatternResolver();

    @Override
    public PageResult<PlazaVO> pagePlaza(PlazaPageDTO dto) {
        List<PlazaVO> list = workspaceRepository.plazaPage(dto);
        Integer total = workspaceRepository.plazaCount(dto);
        int pageSize = dto.getPageSize();
        int pageSum = (total + pageSize - 1) / pageSize;
        return PageResult.<PlazaVO>builder()
                .list(list)
                .total(total)
                .pageNum(dto.getPageNum())
                .pageSize(pageSize)
                .pageSum(pageSum)
                .build();
    }

    @Override
    public PageResult<CommentVO> plazaCommentArea(PlazaCommentAreaDTO dto) {
        List<CommentVO> list = workspaceRepository.plazaCommentList(dto);
        Integer total = workspaceRepository.plazaCommentCount(dto.getPlazaId());
        int pageSize = dto.getPageSize();
        int pageSum = (total + pageSize - 1) / pageSize;
        return PageResult.<CommentVO>builder()
                .list(list)
                .total(total)
                .pageNum(dto.getPageNum())
                .pageSize(pageSize)
                .pageSum(pageSum)
                .build();
    }

    @Override
    public void plazaLike(String plazaId, boolean liked) {
        MqEventDTO payload = MqEventDTO.builder()
                .plazaId(plazaId)
                .liked(liked)
                .build();
        mqService.sendMain(mqService.buildTask(MqEventType.PLAZA_LIKE, payload, userContext.getUser()));
    }

    @Override
    public void plazaFavor(String plazaId, boolean favored) {
        MqEventDTO payload = MqEventDTO.builder()
                .plazaId(plazaId)
                .favored(favored)
                .build();
        mqService.sendMain(mqService.buildTask(MqEventType.PLAZA_FAVOR, payload, userContext.getUser()));
    }

    @Override
    public void plazaComment(PlazaCommentDTO dto) {
        MqEventDTO payload = MqEventDTO.builder()
                .plazaId(dto.getPlazaId())
                .commentContent(dto.getCommentContent())
                .build();
        mqService.sendMain(mqService.buildTask(MqEventType.PLAZA_COMMENT, payload, userContext.getUser()));
    }

    @Override
    public void plazaDiscomment(String plazaId, String commentId) {
        MqEventDTO payload = MqEventDTO.builder()
                .plazaId(plazaId)
                .commentId(commentId)
                .build();
        mqService.sendMain(mqService.buildTask(MqEventType.PLAZA_DISCOMMENT, payload, userContext.getUser()));
    }

    @Override
    public void plazaDelete(String plazaId) {
        MqEventDTO payload = MqEventDTO.builder()
                .plazaId(plazaId)
                .build();
        mqService.sendMain(mqService.buildTask(MqEventType.PLAZA_DELETE, payload, userContext.getUser()));
    }

    @Override
    public Map<String, List<RepoVO>> repoMap() {
        List<RepoVO> repoVOList = workspaceRepository.repoList();
        Map<String, List<RepoVO>> resultMap = new LinkedHashMap<>();
        resultMap.put(RepoType.SELF.getType(), new ArrayList<>());
        resultMap.put(RepoType.FORK.getType(), new ArrayList<>());
        resultMap.put(RepoType.FAVOR.getType(), new ArrayList<>());
        if (repoVOList == null || repoVOList.isEmpty()) {
            return resultMap;
        }

        Map<String, List<RepoVO>> groupedMap = repoVOList.stream()
                .filter(repoVO -> repoVO != null && repoVO.getRepoType() != null)
                .collect(Collectors.groupingBy(RepoVO::getRepoType, LinkedHashMap::new, Collectors.toList()));
        resultMap.put(RepoType.SELF.getType(), groupedMap.getOrDefault(RepoType.SELF.getType(), new ArrayList<>()));
        resultMap.put(RepoType.FORK.getType(), groupedMap.getOrDefault(RepoType.FORK.getType(), new ArrayList<>()));
        resultMap.put(RepoType.FAVOR.getType(), groupedMap.getOrDefault(RepoType.FAVOR.getType(), new ArrayList<>()));
        return resultMap;
    }

    @Override
    public void agentPublish(AgentPublishDTO dto) {
        MqEventDTO payload = MqEventDTO.builder()
                .agentId(dto.getAgentId())
                .plazaTitle(dto.getPlazaTitle())
                .plazaDesc(dto.getPlazaDesc())
                .build();
        mqService.sendMain(mqService.buildTask(MqEventType.AGENT_PUBLISH, payload, userContext.getUser()));
    }

    @Override
    public void agentFork(String templateId) {
        MqEventDTO payload = MqEventDTO.builder()
                .templateId(templateId)
                .build();
        mqService.sendMain(mqService.buildTask(MqEventType.AGENT_FORK, payload, userContext.getUser()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void agentUpdateBase(AgentUpdateBaseDTO dto) {
        workspaceRepository.agentUpdateBase(dto);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void agentUpdateModel(AgentUpdateModelDTO dto) {
        workspaceRepository.agentUpdateModel(dto);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void agentUpdateMcp(AgentUpdateMcpDTO dto) {
        workspaceRepository.agentUpdateMcp(dto);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void agentUpdateUserPrompt(AgentUpdateUserPromptDTO dto) {
        workspaceRepository.agentUpdateUserPrompt(dto);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void agentUpdateSystemPrompt(AgentUpdateSystemPromptDTO dto) {
        workspaceRepository.agentUpdateSystemPrompt(dto);
    }

    @Override
    public TemplateVO agentTemplate(String templateId) {
        return workspaceRepository.agentTemplate(templateId);
    }

    @Override
    public AgentVO agentDetail(String agentId) {
        return workspaceRepository.agentDetail(agentId);
    }

    @Override
    public void agentDelete(String agentId) {
        MqEventDTO payload = MqEventDTO.builder()
                .agentId(agentId)
                .build();
        mqService.sendMain(mqService.buildTask(MqEventType.AGENT_DELETE, payload, userContext.getUser()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void agentCreate(AgentCreateDTO dto) {
        String agentDesc = dto.getAgentDesc();
        StrategyType strategyType = StrategyType.from(dto.getStrategy());
        String strategy = strategyType.getType();

        // 获取策略下的角色
        List<RoleType> roleTypeList = RoleType.queryByStrategy(strategyType);

        List<RolePromptEntity> rolePromptEntityList = new ArrayList<>();
        for (RoleType roleType : roleTypeList) {
            // 读取文件拿到 prompt 内容
            String systemPrompt;
            String userPrompt;
            try {
                String systemPath = "classpath:template/" + strategy + "/" + SYSTEM_PROMPT.getType() + "/" + roleType.getTemplateName() + ".md";
                systemPrompt = StreamUtils.copyToString(RESOURCE_RESOLVER.getResource(systemPath).getInputStream(), StandardCharsets.UTF_8);
                if (!StringUtils.hasText(systemPrompt)) {
                    throw new WorkException(ILLEGAL_DATA);
                }

                String userPath = "classpath:template/" + strategy + "/" + USER_PROMPT.getType() + "/" + roleType.getTemplateName() + ".md";
                userPrompt = StreamUtils.copyToString(RESOURCE_RESOLVER.getResource(userPath).getInputStream(), StandardCharsets.UTF_8);
                if (!StringUtils.hasText(userPrompt)) {
                    throw new WorkException(ILLEGAL_DATA);
                }
            } catch (Exception e) {
                log.error("【加载模版】加载失败：strategy={}, roleType={}", strategy, roleType, e);
                throw new WorkException(ILLEGAL_DATA);
            }

            // 获取每个角色要补充的约束内容
            String strategyDesc = strategyType.getStrategyDesc();
            String clientRole = roleType.getClientRole();
            String roleDesc = roleType.getRoleDesc();
            String roleConstraint = promptGenerator.generateRoleConstraint(strategyDesc, clientRole, roleDesc, agentDesc);
            if (!StringUtils.hasText(roleConstraint)) {
                throw new WorkException(ILLEGAL_DATA);
            }

            // 放入列表收集
            RolePromptEntity rolePromptEntity = RolePromptEntity.builder()
                    .clientRole(clientRole)
                    .flowSeq(roleType.getFlowSeq())
                    .systemPrompt(systemPrompt)
                    .userPrompt(userPrompt.formatted(roleConstraint))
                    .build();
            rolePromptEntityList.add(rolePromptEntity);
        }

        workspaceRepository.agentCreate(dto, rolePromptEntityList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void executePlazaLike(String plazaId, boolean liked) {
        workspaceRepository.plazaLike(plazaId, liked);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void executePlazaFavor(String plazaId, boolean favored) {
        workspaceRepository.plazaFavor(plazaId, favored);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void executePlazaComment(PlazaCommentDTO dto) {
        workspaceRepository.plazaComment(dto);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void executePlazaDiscomment(String plazaId, String commentId) {
        workspaceRepository.plazaDiscomment(plazaId, commentId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void executePlazaDelete(String plazaId) {
        workspaceRepository.plazaDelete(plazaId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void executeAgentPublish(AgentPublishDTO dto) {
        workspaceRepository.agentPublish(dto);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void executeAgentFork(String templateId) {
        workspaceRepository.agentFork(templateId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void executeAgentDelete(String agentId) {
        workspaceRepository.agentDelete(agentId);
    }

}
