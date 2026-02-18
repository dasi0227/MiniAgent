package com.dasi.api;

import com.dasi.types.dto.response.query.QueryChatRagResponse;
import com.dasi.types.dto.response.query.QueryWorkAgentResponse;
import com.dasi.types.dto.response.query.QueryChatClientResponse;
import com.dasi.types.dto.response.query.QueryChatMcpResponse;
import com.dasi.types.dto.result.Result;

import java.util.List;

public interface IQueryApi {

    Result<List<QueryChatClientResponse>> queryChatClientResponseList(Boolean mineOnly);

    Result<List<QueryChatMcpResponse>> queryChatMcpResponseList(Boolean mineOnly);

    Result<List<QueryChatRagResponse>> queryRagTagList();

    Result<List<QueryWorkAgentResponse>> queryWorkAgentResponseList();

}
