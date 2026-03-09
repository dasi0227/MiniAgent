package com.dasi.domain.user.service.query;

import com.dasi.domain.user.repository.IQueryRepository;
import com.dasi.domain.user.model.vo.ChatRagVO;
import com.dasi.domain.user.model.vo.WorkAgentVO;
import com.dasi.domain.user.model.vo.ChatClientVO;
import com.dasi.domain.user.model.vo.ChatMcpVO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QueryService implements IQueryService {

    @Resource
    private IQueryRepository queryRepository;

    @Override
    public List<ChatClientVO> queryChatClientList() {
        return queryRepository.queryChatClientVOList();
    }

    @Override
    public List<ChatMcpVO> queryMcpList() {
        return queryRepository.queryChatMcpVOList();
    }

    @Override
    public List<ChatRagVO> queryRagList() {
        return queryRepository.queryRagVOList();
    }

    @Override
    public List<WorkAgentVO> queryWorkAgentList() {
        return queryRepository.queryWorkAgentVOList();
    }

}
