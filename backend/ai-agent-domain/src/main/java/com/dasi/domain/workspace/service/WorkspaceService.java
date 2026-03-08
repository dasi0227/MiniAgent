package com.dasi.domain.workspace.service;

import com.dasi.domain.util.jwt.UserContext;
import com.dasi.domain.workspace.model.dto.*;
import com.dasi.domain.workspace.model.entity.RolePromptEntity;
import com.dasi.domain.workspace.model.enumeration.RoleType;
import com.dasi.domain.workspace.model.enumeration.RepoType;
import com.dasi.domain.workspace.model.enumeration.StrategyType;
import com.dasi.domain.workspace.model.vo.CommentVO;
import com.dasi.domain.workspace.model.vo.PlazaVO;
import com.dasi.domain.workspace.model.vo.RepoVO;
import com.dasi.domain.workspace.model.vo.TemplateVO;
import com.dasi.domain.workspace.repository.IWorkspaceRepository;
import com.dasi.types.exception.MiniAgentException;
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
import java.util.Set;
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
    private UserContext userContext;

    @Resource
    private PromptGenerator promptGenerator;

    private static final PathMatchingResourcePatternResolver RESOURCE_RESOLVER = new PathMatchingResourcePatternResolver();

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
    @Transactional(rollbackFor = Exception.class)
    public void plazaLike(String plazaId, boolean liked) {
        workspaceRepository.plazaLike(plazaId, liked);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void plazaFavor(String plazaId, boolean favored) {
        workspaceRepository.plazaFavor(plazaId, favored);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void plazaComment(PlazaCommentDTO dto) {
        workspaceRepository.plazaComment(dto);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void plazaDiscomment(String plazaId, String commentId) {
        workspaceRepository.plazaDiscomment(plazaId, commentId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void plazaDelete(String plazaId) {
        workspaceRepository.plazaDelete(plazaId);
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
    @Transactional(rollbackFor = Exception.class)
    public void agentPublish(AgentPublishDTO dto) {
        workspaceRepository.agentPublish(dto);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void agentFork(String templateId) {
        workspaceRepository.agentFork(templateId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void agentBaseUpdate(AgentBaseUpdateDTO dto) {
        workspaceRepository.agentBaseUpdate(dto);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void agentUserPromptUpdate(AgentUserPromptUpdateDTO dto) {
        workspaceRepository.agentUserPromptUpdate(dto);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void agentSystemPromptUpdate(AgentSystemPromptUpdateDTO dto) {
        workspaceRepository.agentSystemPromptUpdate(dto);
    }

    @Override
    public TemplateVO agentTemplate(String templateId) {
        return workspaceRepository.agentTemplate(templateId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void agentDelete(String agentId) {
        workspaceRepository.agentDelete(agentId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void agentCreate(AgentCreateDTO dto) {
        Long userId = userContext.getUserId();
        String agentDesc = dto.getAgentDesc();
        StrategyType strategyType = StrategyType.from(dto.getStrategy());
        String strategy = strategyType.getType();
        Set<String> mcpIdSet = dto.getMcpIdSet() == null ? Set.of() : dto.getMcpIdSet();

        // 获取策略下的角色
        List<RoleType> roleTypeList = RoleType.queryByStrategy(strategyType);

        List<RolePromptEntity> rolePromptEntityList = new ArrayList<>();
        for (RoleType roleType : roleTypeList) {
            // 读取文件拿到 prompt 内容
            String systemPrompt = loadTemplateContent(strategy, SYSTEM_PROMPT.getType(), roleType.getTemplateName());
            String userPrompt = loadTemplateContent(strategy, USER_PROMPT.getType(), roleType.getTemplateName());
            if (!StringUtils.hasText(systemPrompt)) {
                throw new MiniAgentException(ILLEGAL_DATA);
            }
            if (!StringUtils.hasText(userPrompt)) {
                throw new MiniAgentException(ILLEGAL_DATA);
            }

            // 获取每个角色要补充的约束内容
            String strategyDesc = strategyType.getStrategyDesc();
            String clientRole = roleType.getClientRole();
            String roleDesc = roleType.getRoleDesc();
            String roleConstraint = promptGenerator.generateRoleConstraint(strategyDesc, clientRole, roleDesc, agentDesc);
            if (!StringUtils.hasText(roleConstraint)) {
                throw new MiniAgentException(ILLEGAL_DATA);
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

        workspaceRepository.agentCreate(dto, userId, mcpIdSet, rolePromptEntityList);
    }

    private String loadTemplateContent(String strategy, String promptType, String templateName) {
        try {
            String path = "classpath:template/" + strategy + "/" + promptType + "/" + templateName + ".md";
            return StreamUtils.copyToString(RESOURCE_RESOLVER.getResource(path).getInputStream(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("【Workspace】加载模板失败：strategy={}, promptType={}, templateName={}", strategy, promptType, templateName, e);
            throw new MiniAgentException(ILLEGAL_DATA);
        }
    }

}
