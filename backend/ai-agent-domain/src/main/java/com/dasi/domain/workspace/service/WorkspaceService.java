package com.dasi.domain.workspace.service;

import com.dasi.domain.workspace.model.dto.AgentPublishDTO;
import com.dasi.domain.workspace.model.dto.AgentBaseUpdateDTO;
import com.dasi.domain.workspace.model.dto.PlazaCommentAreaDTO;
import com.dasi.domain.workspace.model.dto.PlazaCommentDTO;
import com.dasi.domain.workspace.model.dto.PlazaPageDTO;
import com.dasi.domain.workspace.model.dto.AgentSystemPromptUpdateDTO;
import com.dasi.domain.workspace.model.dto.AgentUserPromptUpdateDTO;
import com.dasi.domain.workspace.model.enumeration.RepoType;
import com.dasi.domain.workspace.model.vo.CommentVO;
import com.dasi.domain.workspace.model.vo.PlazaVO;
import com.dasi.domain.workspace.model.vo.RepoVO;
import com.dasi.domain.workspace.model.vo.TemplateVO;
import com.dasi.domain.workspace.repository.IWorkspaceRepository;
import com.dasi.types.result.PageResult;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class WorkspaceService implements IWorkspaceService {

    @Resource
    private IWorkspaceRepository workspaceRepository;

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

}
