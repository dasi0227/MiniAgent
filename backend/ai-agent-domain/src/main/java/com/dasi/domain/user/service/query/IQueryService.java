package com.dasi.domain.user.service.query;

import com.dasi.domain.user.model.vo.query.ChatRagVO;
import com.dasi.domain.user.model.vo.query.WorkAgentVO;
import com.dasi.domain.user.model.vo.query.ChatClientVO;
import com.dasi.domain.user.model.vo.query.ChatMcpVO;

import java.util.List;

public interface IQueryService {

    List<ChatClientVO> queryChatClientResponseList();

    List<ChatMcpVO> queryChatMcpResponseList();

    List<ChatRagVO> queryChatRagList();

    List<WorkAgentVO> queryWorkAgentResponseList();

}
