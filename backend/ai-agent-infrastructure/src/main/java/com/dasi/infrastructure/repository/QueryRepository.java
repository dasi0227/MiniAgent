package com.dasi.infrastructure.repository;

import com.dasi.domain.user.model.vo.*;
import com.dasi.domain.user.repository.IQueryRepository;
import com.dasi.domain.util.jwt.UserContext;
import com.dasi.infrastructure.persistent.dao.IAiAgentDao;
import com.dasi.infrastructure.persistent.dao.IAiClientDao;
import com.dasi.infrastructure.persistent.dao.IAiMcpDao;
import com.dasi.infrastructure.persistent.dao.IAiModelDao;
import com.dasi.infrastructure.persistent.po.AiAgent;
import com.dasi.infrastructure.persistent.po.AiClient;
import com.dasi.infrastructure.persistent.po.AiMcp;
import com.dasi.infrastructure.persistent.po.AiModel;
import com.dasi.types.annotation.Cacheable;
import com.dasi.types.enumeration.CacheType;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

import static com.dasi.types.constant.RedisConstant.*;

@Repository
@Slf4j
public class QueryRepository implements IQueryRepository {

    @Resource
    private IAiClientDao aiClientDao;

    @Resource
    private IAiMcpDao aiMcpDao;

    @Resource
    private IAiAgentDao aiAgentDao;

    @Resource
    private IAiModelDao aiModelDao;

    @Resource
    private UserContext userContext;

    @Resource(name = "postgresqlTemplate")
    private JdbcTemplate jdbcTemplate;

    @Value("${openai.embedding.schema-name}")
    private String embeddingSchemaName;

    @Value("${openai.embedding.table-name}")
    private String embeddingTableName;

    @Override
    @Cacheable(cacheKey = QUERY_WORK_AGENT_KEY, cacheClass = QueryWorkAgentVO.class, cacheType = CacheType.LIST)
    public List<QueryWorkAgentVO> queryWorkAgentList() {

        Long userId = userContext.getUserId();

        List<AiAgent> aiAgentList = aiAgentDao.queryWorkAgentByUserId(userId);
        if (aiAgentList == null || aiAgentList.isEmpty()) {
            return new ArrayList<>();
        }

        return aiAgentList.stream()
                .filter(a -> a != null && Integer.valueOf(1).equals(a.getAgentStatus()))
                .map(aiAgent -> QueryWorkAgentVO.builder()
                        .agentId(aiAgent.getAgentId())
                        .agentName(aiAgent.getAgentName())
                        .agentDesc(aiAgent.getAgentDesc())
                        .build())
                .toList();
    }

    @Override
    @Cacheable(cacheKey = QUERY_CHAT_CLIENT_KEY, cacheClass = QueryChatClientVO.class, cacheType = CacheType.LIST)
    public List<QueryChatClientVO> queryChatClientList() {

        Long userId = userContext.getUserId();

        List<AiClient> aiClientList = aiClientDao.queryChatClientByUserId(userId);
        if (aiClientList == null || aiClientList.isEmpty()) {
            return new ArrayList<>();
        }

        return aiClientList.stream()
                .filter(c -> c != null && Integer.valueOf(1).equals(c.getClientStatus()))
                .map(aiClient -> QueryChatClientVO.builder()
                        .clientId(aiClient.getClientId())
                        .modelName(aiClient.getModelName())
                        .build())
                .toList();
    }

    @Override
    @Cacheable(cacheKey = QUERY_MCP_KEY, cacheClass = QueryMcpVO.class, cacheType = CacheType.LIST)
    public List<QueryMcpVO> queryMcpList() {

        Long userId = userContext.getUserId();

        List<AiMcp> aiMcpList = aiMcpDao.queryMcpByUserId(userId);
        if (aiMcpList == null || aiMcpList.isEmpty()) {
            return new ArrayList<>();
        }

        return aiMcpList.stream()
                .map(aiMcp -> QueryMcpVO.builder()
                        .mcpId(aiMcp.getMcpId())
                        .mcpName(aiMcp.getMcpName())
                        .mcpDesc(aiMcp.getMcpDesc())
                        .build())
                .toList();
    }

    @Override
    @Cacheable(cacheKey = QUERY_API_KEY, cacheClass = QueryModelVO.class, cacheType = CacheType.LIST)
    public List<QueryModelVO> queryModelList() {

        Long userId = userContext.getUserId();

        List<AiModel> aiModelList = aiModelDao.queryModelByUserId(userId);
        if (aiModelList == null || aiModelList.isEmpty()) {
            return new ArrayList<>();
        }

        return aiModelList.stream()
                .map(aiModel -> QueryModelVO.builder()
                        .apiId(aiModel.getApiId())
                        .modelId(aiModel.getModelId())
                        .modelName(aiModel.getModelName())
                        .build())
                .toList();
    }

    @Override
    @Cacheable(cacheKey = QUERY_RAG_KEY, cacheClass = QueryRagVO.class, cacheType = CacheType.LIST)
    public List<QueryRagVO> queryRagList() {
        Long userId = userContext.getUserId();
        if (userId == null) {
            return List.of();
        }

        String tableRef = embeddingSchemaName + "." + embeddingTableName;
        String sql = """
                SELECT DISTINCT metadata::jsonb->>'knowledge' AS knowledge
                FROM %s
                WHERE metadata::jsonb ? 'knowledge'
                  AND metadata::jsonb ? 'userId'
                  AND metadata::jsonb->>'knowledge' <> ''
                  AND metadata::jsonb->>'userId' = ?
                """
                .formatted(tableRef);
        List<String> ragTagList = jdbcTemplate.queryForList(sql, String.class, String.valueOf(userId));
        return ragTagList.stream()
                .map(ragTag -> QueryRagVO.builder().ragTag(ragTag).build())
                .toList();
    }

}
