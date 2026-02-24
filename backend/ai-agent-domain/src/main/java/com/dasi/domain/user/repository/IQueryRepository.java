package com.dasi.domain.user.repository;

import com.dasi.domain.user.model.vo.query.ChatClientVO;
import com.dasi.domain.user.model.vo.query.ChatMcpVO;
import com.dasi.domain.user.model.vo.query.ChatRagVO;
import com.dasi.domain.user.model.vo.query.WorkAgentVO;

import java.util.List;

public interface IQueryRepository {

    List<ChatClientVO> queryChatClientResponseList();

    List<ChatRagVO> queryChatRagList();

    List<ChatMcpVO> queryChatMcpResponseList();

    List<WorkAgentVO> queryWorkAgentResponseList();

}
