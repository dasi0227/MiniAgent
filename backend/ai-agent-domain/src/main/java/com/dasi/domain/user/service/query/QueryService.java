package com.dasi.domain.user.service.query;

import com.dasi.domain.user.model.enumeration.ClientRoleType;
import com.dasi.domain.user.model.vo.*;
import com.dasi.domain.user.repository.IQueryRepository;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class QueryService implements IQueryService {

    @Resource
    private IQueryRepository queryRepository;

    @Override
    public List<QueryWorkAgentVO> queryWorkAgentList() {
        return queryRepository.queryWorkAgentList();
    }

    @Override
    public List<QueryChatClientVO> queryChatClientList() {
        return queryRepository.queryChatClientList();
    }

    @Override
    public List<QueryMcpVO> queryMcpList() {
        return queryRepository.queryMcpList();
    }

    @Override
    public List<QueryRagVO> queryRagList() {
        return queryRepository.queryRagList();
    }

    @Override
    public List<QueryModelVO> queryModelList() {
        return queryRepository.queryModelList();
    }

    @Override
    public Map<String, QueryRoleVO> queryRoleMap() {
        Map<String, QueryRoleVO> roleMap = new LinkedHashMap<>();
        for (ClientRoleType roleType : ClientRoleType.values()) {
            QueryRoleVO queryRoleVO = QueryRoleVO.builder()
                    .strategy(roleType.getStrategy())
                    .roleName(roleType.getRoleName())
                    .roleDesc(roleType.getRoleDesc())
                    .build();
            roleMap.put(roleType.getRoleName(), queryRoleVO);
        }
        return roleMap;
    }

}
