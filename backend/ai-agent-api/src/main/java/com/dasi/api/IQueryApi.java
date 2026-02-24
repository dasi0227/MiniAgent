package com.dasi.api;

import com.dasi.domain.user.model.vo.query.ChatRagVO;
import com.dasi.domain.user.model.vo.query.WorkAgentVO;
import com.dasi.domain.user.model.vo.query.ChatClientVO;
import com.dasi.domain.user.model.vo.query.ChatMcpVO;
import com.dasi.types.dto.result.Result;

import java.util.List;

public interface IQueryApi {

    Result<List<ChatClientVO>> queryChatClientResponseList();

    Result<List<ChatMcpVO>> queryChatMcpResponseList();

    Result<List<ChatRagVO>> queryRagTagList();

    Result<List<WorkAgentVO>> queryWorkAgentResponseList();

}
