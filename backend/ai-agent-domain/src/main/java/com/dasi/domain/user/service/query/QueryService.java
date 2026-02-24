package com.dasi.domain.user.service.query;

import com.dasi.domain.user.repository.IQueryRepository;
import com.dasi.domain.user.model.vo.query.ChatRagVO;
import com.dasi.domain.user.model.vo.query.WorkAgentVO;
import com.dasi.domain.user.model.vo.query.ChatClientVO;
import com.dasi.domain.user.model.vo.query.ChatMcpVO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QueryService implements IQueryService {

    @Resource
    private IQueryRepository queryRepository;

    @Override
    public List<ChatClientVO> queryChatClientResponseList() {
        return queryRepository.queryChatClientResponseList();
    }

    @Override
    public List<ChatMcpVO> queryChatMcpResponseList() {
        return queryRepository.queryChatMcpResponseList();
    }

    @Override
    public List<ChatRagVO> queryChatRagList() {
        return queryRepository.queryChatRagList();
    }

    @Override
    public List<WorkAgentVO> queryWorkAgentResponseList() {
        return queryRepository.queryWorkAgentResponseList();
    }

}
