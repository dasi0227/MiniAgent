package com.dasi.domain.user.service.query;

import com.dasi.domain.user.model.vo.*;
import com.dasi.domain.user.repository.IQueryRepository;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

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

}
