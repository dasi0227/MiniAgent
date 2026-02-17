package com.dasi.infrastructure.repository;

import com.dasi.domain.repo.repository.IRepoRepository;
import com.dasi.infrastructure.persistent.dao.IAiAgentDao;
import com.dasi.infrastructure.persistent.dao.IAiPlazaDao;
import com.dasi.infrastructure.persistent.dao.IAiRepoDao;
import com.dasi.infrastructure.persistent.po.AiAgent;
import com.dasi.infrastructure.persistent.po.AiPlaza;
import com.dasi.infrastructure.persistent.po.AiRepo;
import com.dasi.types.dto.response.repo.RepoItemResponse;
import com.dasi.types.exception.AdminException;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Repository
public class RepoRepository implements IRepoRepository {

    @Resource
    private IAiRepoDao aiRepoDao;

    @Resource
    private IAiAgentDao aiAgentDao;

    @Resource
    private IAiPlazaDao aiPlazaDao;

    @Override
    public List<RepoItemResponse> list(Long userId) {
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
    public void add(Long userId, String agentId) {
        AiAgent aiAgent = aiAgentDao.queryAgentByAgentIdWithFrom(agentId, userId);
        if (aiAgent == null || !Integer.valueOf(1).equals(aiAgent.getAgentStatus())) {
            throw new AdminException("AGENT 不存在或不可用");
        }

        String repoType = resolveRepoType(userId, aiAgent, null);
        upsertRepo(userId, agentId, repoType, null);
    }

    @Override
    public void remove(Long userId, String agentId) {
        AiRepo exists = aiRepoDao.queryByUserIdAndAgentId(userId, agentId);
        if (exists == null) {
            return;
        }
        aiRepoDao.updateStatusByUserIdAndAgentId(userId, agentId, 0);
    }

    @Override
    public void fork(Long userId, String plazaId) {
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
}
