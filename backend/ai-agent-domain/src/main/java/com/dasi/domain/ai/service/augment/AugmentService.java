package com.dasi.domain.ai.service.augment;

import com.dasi.domain.ai.repository.IAiRepository;
import com.dasi.domain.user.repository.IUserMcpRepository;
import com.dasi.domain.util.jwt.AuthContext;
import com.dasi.domain.ai.model.enumeration.AiMcpType;
import com.dasi.domain.ai.model.vo.AiMcpVO;
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientSseClientTransport;
import io.modelcontextprotocol.client.transport.ServerParameters;
import io.modelcontextprotocol.client.transport.StdioClientTransport;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Locale;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AugmentService implements IAugmentService {

    private static final String MCP_SECRET_MAP_HEADER = "X-MCP-SECRET-MAP";

    public static final String RAG_SYSTEM_PROMPT = """
            你是一个检索增强问答助手（RAG），你会收到一段参考资料（DOCUMENTS）。
            
            请严格遵守以下规则：
            - 事实依据：所有可核验的事实必须来自 DOCUMENTS；不要引入 DOCUMENTS 之外的具体事实、数字、名称、结论；
            - 推理允许：可以基于 DOCUMENTS 做必要的归纳、对比与推理，但必须明确区分“资料原文信息”与“你的推断”；
            - 冲突处理：若 DOCUMENTS 内部信息矛盾，指出矛盾点，并给出你认为更可信的依据；
            - 引用方式：以自然口吻作答，但关键结论要在句子中体现依据来自 CONTEXT 中的什么内容；
            - 输出约束：用简体中文回答，优先条目化，简洁直接；
            - 空处理：如果 DOCUMENTS 内容为空，就直接当作什么都没有提供，直接回答即可。
            
            DOCUMENTS:
            {documents}
            """;

    @Resource
    private PgVectorStore pgVectorStore;

    @Resource
    private IAiRepository aiRepository;

    @Resource
    private IUserMcpRepository userMcpRepository;

    @Resource
    private AuthContext authContext;

    @Override
    public List<Message> augmentRagMessage(String userMessage, String ragTag) {

        if (ragTag == null || ragTag.isEmpty()) {
            return List.of(new UserMessage(userMessage));
        }

        // 构建向量检索条件
        FilterExpressionBuilder filterExpressionBuilder = new FilterExpressionBuilder();
        Filter.Expression expression = filterExpressionBuilder.eq("knowledge", ragTag).build();

        // 构建向量检索请求
        SearchRequest searchRequest = SearchRequest.builder()
                .query(userMessage)
                .filterExpression(expression)
                .topK(5)
                .build();

        // 执行向量检索
        List<Document> documentList = pgVectorStore.similaritySearch(searchRequest);

        // 将检索结果合并为一个文本块，过滤空文档和空内容
        String documentString = (documentList == null ? List.<Document>of() : documentList).stream()
                .map(Document::getText)
                .filter(Objects::nonNull)
                .collect(Collectors.joining("\n"));

        // 用户消息 + 系统消息
        return List.of(
                new SystemPromptTemplate(RAG_SYSTEM_PROMPT).createMessage(Map.of("documents", documentString)),
                new UserMessage(userMessage)
        );
    }

    @Override
    public SyncMcpToolCallbackProvider augmentMcpTool(List<String> mcpIdList) {

        if (mcpIdList == null || mcpIdList.isEmpty()) {
            return new SyncMcpToolCallbackProvider();
        }

        List<AiMcpVO> aiMcpVOList = aiRepository.queryAiMcpVOListByMcpIdList(mcpIdList);
        if (aiMcpVOList == null || aiMcpVOList.isEmpty()) {
            return new SyncMcpToolCallbackProvider();
        }

        List<McpSyncClient> mcpSyncClientList = new ArrayList<>();

        for (AiMcpVO aiMcpVO : aiMcpVOList) {

            Map<String, String> secretMap = resolveSensitiveMcpSecretMap(aiMcpVO.getMcpId());
            String secretHeaderValue = encodeSecretHeader(secretMap);

            McpSyncClient mcpSyncClient = null;

            switch (AiMcpType.fromString(aiMcpVO.getMcpType())) {
                case SSE -> {
                    AiMcpVO.SseConfig sseConfig = aiMcpVO.getSseConfig();
                    String baseUri = sseConfig.getBaseUri();
                    String sseEndPoint = sseConfig.getSseEndPoint();

                    HttpClientSseClientTransport.Builder transportBuilder = HttpClientSseClientTransport
                            .builder(baseUri)
                            .sseEndpoint(sseEndPoint);
                    if (secretHeaderValue != null) {
                        transportBuilder.customizeRequest(builder -> builder.header(MCP_SECRET_MAP_HEADER, secretHeaderValue));
                    }

                    HttpClientSseClientTransport sseClient = transportBuilder.build();

                    mcpSyncClient = McpClient
                            .sync(sseClient)
                            .requestTimeout(Duration.ofMinutes(aiMcpVO.getMcpTimeout()))
                            .build();

                    mcpSyncClient.initialize();
                }
                case STDIO -> {
                    AiMcpVO.StdioConfig stdioConfig = aiMcpVO.getStdioConfig();
                    Map<String, AiMcpVO.StdioConfig.Stdio> stdioMap = stdioConfig.getStdio();
                    AiMcpVO.StdioConfig.Stdio stdio = stdioMap.get(aiMcpVO.getMcpId());

                    Map<String, String> env = new HashMap<>();
                    if (stdio.getEnv() != null && !stdio.getEnv().isEmpty()) {
                        env.putAll(stdio.getEnv());
                    }
                    if (secretHeaderValue != null) {
                        env.put("MCP_SECRET_MAP_B64", secretHeaderValue);
                    }
                    if (secretMap != null && !secretMap.isEmpty()) {
                        for (Map.Entry<String, String> entry : secretMap.entrySet()) {
                            env.put("DASI_SECRET_" + entry.getKey().toUpperCase(Locale.ROOT), entry.getValue());
                        }
                    }

                    ServerParameters serverParameters = ServerParameters
                            .builder(stdio.getCommand())
                            .args(stdio.getArgs())
                            .env(env)
                            .build();

                    StdioClientTransport stdioClient = new StdioClientTransport(serverParameters);

                    mcpSyncClient = McpClient
                            .sync(stdioClient)
                            .requestTimeout(Duration.ofMinutes(aiMcpVO.getMcpTimeout()))
                            .build();

                    mcpSyncClient.initialize();
                }
            }

            mcpSyncClientList.add(mcpSyncClient);
        }

        return new SyncMcpToolCallbackProvider(mcpSyncClientList.toArray(new McpSyncClient[0]));
    }

    private Map<String, String> resolveSensitiveMcpSecretMap(String mcpId) {
        if (!isSensitiveMcp(mcpId)) {
            return Map.of();
        }

        Long userId = authContext.getId();
        if (userId == null) {
            throw new IllegalStateException("请先完成配置");
        }
        Map<String, String> secretMap = userMcpRepository.querySecretPlainMap(userId, mcpId);
        if (secretMap == null || secretMap.isEmpty()) {
            throw new IllegalStateException("请先完成配置");
        }

        List<String> requiredKeyList = requiredSecretKeys(mcpId);
        for (String requiredKey : requiredKeyList) {
            String secretValue = secretMap.get(requiredKey);
            if (secretValue == null || secretValue.isBlank()) {
                throw new IllegalStateException("请先完成配置");
            }
        }

        return secretMap;
    }

    private boolean isSensitiveMcp(String mcpId) {
        if (mcpId == null) {
            return false;
        }
        return "wecom".equalsIgnoreCase(mcpId)
                || "email".equalsIgnoreCase(mcpId)
                || "csdn".equalsIgnoreCase(mcpId);
    }

    private List<String> requiredSecretKeys(String mcpId) {
        if ("wecom".equalsIgnoreCase(mcpId)) {
            return List.of("corpid", "corpsecret", "agentid");
        }
        if ("email".equalsIgnoreCase(mcpId)) {
            return List.of("smtpHost", "smtpPort", "smtpUsername", "smtpPassword", "fromAddress", "fromName");
        }
        if ("csdn".equalsIgnoreCase(mcpId)) {
            return List.of("cookie", "coverUrl", "categories", "tags");
        }
        return List.of();
    }

    private String encodeSecretHeader(Map<String, String> secretMap) {
        if (secretMap == null || secretMap.isEmpty()) {
            return null;
        }
        String json = com.alibaba.fastjson2.JSON.toJSONString(secretMap);
        return Base64.getEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8));
    }

}
