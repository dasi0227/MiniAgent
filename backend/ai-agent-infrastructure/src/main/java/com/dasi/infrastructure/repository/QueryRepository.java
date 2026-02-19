package com.dasi.infrastructure.repository;

import com.dasi.domain.query.repository.IQueryRepository;
import com.dasi.domain.util.jwt.AuthContext;
import com.dasi.infrastructure.persistent.dao.IAiAgentDao;
import com.dasi.infrastructure.persistent.dao.IAiClientDao;
import com.dasi.infrastructure.persistent.dao.IAiMcpDao;
import com.dasi.infrastructure.persistent.dao.IAiRepoDao;
import com.dasi.infrastructure.persistent.po.AiAgent;
import com.dasi.infrastructure.persistent.po.AiClient;
import com.dasi.infrastructure.persistent.po.AiMcp;
import com.dasi.infrastructure.persistent.po.AiRepo;
import com.dasi.types.dto.response.query.QueryChatClientResponse;
import com.dasi.types.dto.response.query.QueryChatMcpResponse;
import com.dasi.types.dto.response.query.QueryChatRagResponse;
import com.dasi.types.dto.response.query.QueryWorkAgentResponse;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
    private IAiRepoDao aiRepoDao;

    @Resource
    private AuthContext authContext;

    @Resource(name = "postgresqlTemplate")
    private JdbcTemplate jdbcTemplate;

    @Value("${openai.embedding.schema-name}")
    private String embeddingSchemaName;

    @Value("${openai.embedding.table-name}")
    private String embeddingTableName;

    @Override
    public List<QueryChatClientResponse> queryChatClientResponseList() {

        Long userId = authContext.getId();
        List<AiClient> aiClientList = aiClientDao.queryChatClientList(userId);
        if (aiClientList == null || aiClientList.isEmpty()) {
            return new ArrayList<>();
        }

        Map<String, QueryChatClientResponse> resultMap = new LinkedHashMap<>();
        for (AiClient aiClient : aiClientList) {
            if (aiClient == null || !Integer.valueOf(1).equals(aiClient.getClientStatus())) {
                continue;
            }
            resultMap.putIfAbsent(aiClient.getClientId(), QueryChatClientResponse.builder()
                    .clientId(aiClient.getClientId())
                    .modelName(aiClient.getModelName())
                    .clientDesc(aiClient.getClientDesc())
                    .clientFrom(aiClient.getClientFrom() != null && aiClient.getClientFrom() > 0 ? "mine" : "system")
                    .build());
        }
        return new ArrayList<>(resultMap.values());
    }


    @Override
    public List<QueryChatMcpResponse> queryChatMcpResponseList() {

        Long userId = authContext.getId();
        List<AiMcp> aiMcpList = aiMcpDao.queryChatMcpList(userId);
        if (aiMcpList == null || aiMcpList.isEmpty()) {
            return new ArrayList<>();
        }

        Map<String, QueryChatMcpResponse> resultMap = new LinkedHashMap<>();
        for (AiMcp aiMcp : aiMcpList) {
            if (aiMcp == null) {
                continue;
            }
            resultMap.putIfAbsent(aiMcp.getMcpId(), QueryChatMcpResponse.builder()
                    .mcpId(aiMcp.getMcpId())
                    .mcpName(aiMcp.getMcpName())
                    .mcpDesc(aiMcp.getMcpDesc())
                    .sourceType(aiMcp.getMcpFrom() != null && aiMcp.getMcpFrom() > 0 ? "mine" : "system")
                    .build());
        }
        return new ArrayList<>(resultMap.values());
    }

    @Override
    public List<QueryWorkAgentResponse> queryWorkAgentResponseList() {

        Long userId = authContext.getId();
        Map<String, QueryWorkAgentResponse> resultMap = new LinkedHashMap<>();

        List<AiAgent> systemAgentList = aiAgentDao.queryAgentList();
        for (AiAgent aiAgent : systemAgentList) {
            if (aiAgent == null || !Integer.valueOf(1).equals(aiAgent.getAgentStatus())) {
                continue;
            }
            resultMap.put(aiAgent.getAgentId(), QueryWorkAgentResponse.builder()
                    .agentId(aiAgent.getAgentId())
                    .agentName(aiAgent.getAgentName())
                    .agentDesc(aiAgent.getAgentDesc())
                    .sourceType("system")
                    .build());
        }

        if (userId != null) {
            List<AiRepo> aiRepoList = aiRepoDao.queryByUserIdAndStatus(userId, 1);
            if (aiRepoList != null && !aiRepoList.isEmpty()) {
                List<String> agentIdList = aiRepoList.stream().map(AiRepo::getAgentId).distinct().toList();
                if (!agentIdList.isEmpty()) {
                    Map<String, String> repoTypeMap = new LinkedHashMap<>();
                    for (AiRepo aiRepo : aiRepoList) {
                        repoTypeMap.putIfAbsent(aiRepo.getAgentId(), aiRepo.getRepoType());
                    }
                    List<AiAgent> repoAgentList = aiAgentDao.queryAgentListByIdList(agentIdList);
                    for (AiAgent aiAgent : repoAgentList) {
                        if (aiAgent == null || !Integer.valueOf(1).equals(aiAgent.getAgentStatus())) {
                            continue;
                        }
                        if (resultMap.containsKey(aiAgent.getAgentId())) {
                            continue;
                        }
                        String repoType = repoTypeMap.getOrDefault(aiAgent.getAgentId(), "fork");
                        String sourceType = "self".equalsIgnoreCase(repoType) ? "mine" : "fork";
                        resultMap.put(aiAgent.getAgentId(), QueryWorkAgentResponse.builder()
                                .agentId(aiAgent.getAgentId())
                                .agentName(aiAgent.getAgentName())
                                .agentDesc(aiAgent.getAgentDesc())
                                .sourceType(sourceType)
                                .build());
                    }
                }
            }
        }

        return new ArrayList<>(resultMap.values());
    }

    @Override
    public List<QueryChatRagResponse> queryChatRagList() {

        String tableRef = embeddingSchemaName + "." + embeddingTableName;
        String sql = """
                SELECT DISTINCT metadata::jsonb->>'knowledge' AS knowledge
                FROM %s
                WHERE metadata::jsonb ? 'knowledge' AND metadata::jsonb->>'knowledge' <> ''
                """
                .formatted(tableRef);
        List<String> ragTagList = jdbcTemplate.queryForList(sql, String.class);
        return ragTagList.stream()
                .map(ragTag -> QueryChatRagResponse.builder().ragTag(ragTag).build())
                .toList();
    }

}
