package com.dasi.trigger.http;

import com.dasi.api.IQueryApi;
import com.dasi.domain.user.model.vo.query.ChatClientVO;
import com.dasi.domain.user.model.vo.query.ChatMcpVO;
import com.dasi.domain.user.model.vo.query.ChatRagVO;
import com.dasi.domain.user.model.vo.query.WorkAgentVO;
import com.dasi.domain.user.service.query.IQueryService;
import com.dasi.types.dto.result.Result;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/query")
public class QueryController implements IQueryApi {

    @Resource
    private IQueryService queryService;

    @PostMapping("/chat-client-list")
    @Override
    public Result<List<ChatClientVO>> queryChatClientResponseList() {
        return Result.success(queryService.queryChatClientResponseList());
    }

    @PostMapping("/chat-mcp-list")
    @Override
    public Result<List<ChatMcpVO>> queryChatMcpResponseList() {
        return Result.success(queryService.queryChatMcpResponseList());
    }

    @PostMapping("/chat-rag-list")
    @Override
    public Result<List<ChatRagVO>> queryRagTagList() {
        return Result.success(queryService.queryChatRagList());
    }

    @PostMapping("/agent-list")
    @Override
    public Result<List<WorkAgentVO>> queryWorkAgentResponseList() {
        return Result.success(queryService.queryWorkAgentResponseList());
    }

}
