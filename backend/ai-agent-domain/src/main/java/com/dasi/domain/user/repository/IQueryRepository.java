package com.dasi.domain.user.repository;

import com.dasi.domain.user.model.vo.ChatClientVO;
import com.dasi.domain.user.model.vo.ChatMcpVO;
import com.dasi.domain.user.model.vo.ChatRagVO;
import com.dasi.domain.user.model.vo.WorkAgentVO;

import java.util.List;

public interface IQueryRepository {

    List<ChatClientVO> queryChatClientResponseList();

    List<ChatRagVO> queryChatRagList();

    List<ChatMcpVO> queryChatMcpResponseList();

    List<WorkAgentVO> queryWorkAgentResponseList();

}
