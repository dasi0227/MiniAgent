package com.dasi.api;

import com.dasi.domain.user.model.vo.ChatRagVO;
import com.dasi.domain.user.model.vo.WorkAgentVO;
import com.dasi.domain.user.model.vo.ChatClientVO;
import com.dasi.domain.user.model.vo.ChatMcpVO;
import com.dasi.types.dto.result.Result;

import java.util.List;

public interface IQueryApi {

    Result<List<ChatClientVO>> queryChatClientResponseList();

    Result<List<ChatMcpVO>> queryChatMcpResponseList();

    Result<List<ChatRagVO>> queryRagTagList();

    Result<List<WorkAgentVO>> queryWorkAgentResponseList();

}
