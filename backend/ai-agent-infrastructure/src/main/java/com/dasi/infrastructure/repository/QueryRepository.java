package com.dasi.infrastructure.repository;

import com.dasi.domain.user.repository.IQueryRepository;
import com.dasi.domain.util.jwt.UserContext;
import com.dasi.infrastructure.persistent.dao.IAiAgentDao;
import com.dasi.infrastructure.persistent.dao.IAiClientDao;
import com.dasi.infrastructure.persistent.dao.IAiMcpDao;
import com.dasi.infrastructure.persistent.po.AiAgent;
import com.dasi.infrastructure.persistent.po.AiClient;
import com.dasi.infrastructure.persistent.po.AiMcp;
import com.dasi.types.annotation.Cacheable;
import com.dasi.domain.user.model.vo.ChatClientVO;
import com.dasi.domain.user.model.vo.ChatMcpVO;
import com.dasi.domain.user.model.vo.ChatRagVO;
import com.dasi.domain.user.model.vo.WorkAgentVO;
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

    @Resource(name = "postgresqlTemplate")
    private JdbcTemplate jdbcTemplate;

    @Resource
    private UserContext userContext;

    @Value("${openai.embedding.schema-name}")
    private String embeddingSchemaName;

    @Value("${openai.embedding.table-name}")
    private String embeddingTableName;

    @Override
    @Cacheable(cacheKey = QUERY_CHAT_CLIENT_KEY, cacheClass = ChatClientVO.class, cacheType = CacheType.LIST)
    public List<ChatClientVO> queryChatClientVOList() {

        Long userId = userContext.getUserId();

        List<AiClient> aiClientList = aiClientDao.queryChatClientList(userId);
        if (aiClientList == null || aiClientList.isEmpty()) {
            return new ArrayList<>();
        }

        return aiClientList.stream()
                .filter(c -> c != null && Integer.valueOf(1).equals(c.getClientStatus()))
                .map(aiClient -> ChatClientVO.builder()
                        .clientId(aiClient.getClientId())
                        .modelName(aiClient.getModelName())
                        .build())
                .toList();
    }


    @Override
    @Cacheable(cacheKey = QUERY_CHAT_MCP_KEY, cacheClass = ChatMcpVO.class, cacheType = CacheType.LIST)
    public List<ChatMcpVO> queryChatMcpVOList() {

        Long userId = userContext.getUserId();

        List<AiMcp> aiMcpList = aiMcpDao.queryChatMcpList(userId);
        if (aiMcpList == null || aiMcpList.isEmpty()) {
            return new ArrayList<>();
        }

        return aiMcpList.stream()
                .map(aiMcp -> ChatMcpVO.builder()
                        .mcpId(aiMcp.getMcpId())
                        .mcpName(aiMcp.getMcpName())
                        .mcpDesc(aiMcp.getMcpDesc())
                        .build())
                .toList();
    }

    @Override
    @Cacheable(cacheKey = QUERY_WORK_AGENT_KEY, cacheClass = WorkAgentVO.class, cacheType = CacheType.LIST)
    public List<WorkAgentVO> queryWorkAgentVOList() {

        Long userId = userContext.getUserId();

        List<AiAgent> aiAgentList = aiAgentDao.queryWorkAgentList(userId);
        if (aiAgentList == null || aiAgentList.isEmpty()) {
            return new ArrayList<>();
        }

        // TODO：还需要把用户 fork 到仓库的 agent 加入进来

        return aiAgentList.stream()
                .filter(a -> a != null && Integer.valueOf(1).equals(a.getAgentStatus()))
                .map(aiAgent -> WorkAgentVO.builder()
                        .agentId(aiAgent.getAgentId())
                        .agentName(aiAgent.getAgentName())
                        .agentDesc(aiAgent.getAgentDesc())
                        .build())
                .toList();
    }

    @Override
    @Cacheable(cacheKey = QUERY_CHAT_RAG_KEY, cacheClass = ChatRagVO.class, cacheType = CacheType.LIST)
    public List<ChatRagVO> queryRagVOList() {

        String tableRef = embeddingSchemaName + "." + embeddingTableName;
        String sql = """
                SELECT DISTINCT metadata::jsonb->>'knowledge' AS knowledge
                FROM %s
                WHERE metadata::jsonb ? 'knowledge' AND metadata::jsonb->>'knowledge' <> ''
                """
                .formatted(tableRef);
        List<String> ragTagList = jdbcTemplate.queryForList(sql, String.class);
        return ragTagList.stream()
                .map(ragTag -> ChatRagVO.builder().ragTag(ragTag).build())
                .toList();
    }

}
