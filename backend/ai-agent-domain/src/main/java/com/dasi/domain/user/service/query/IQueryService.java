package com.dasi.domain.user.service.query;

import com.dasi.domain.user.model.vo.ChatRagVO;
import com.dasi.domain.user.model.vo.WorkAgentVO;
import com.dasi.domain.user.model.vo.ChatClientVO;
import com.dasi.domain.user.model.vo.ChatMcpVO;

import java.util.List;

public interface IQueryService {

    List<ChatClientVO> queryChatClientVOList();

    List<ChatMcpVO> queryChatMcpVOList();

    List<ChatRagVO> queryRagVOList();

    List<WorkAgentVO> queryWorkAgentVOList();

}
