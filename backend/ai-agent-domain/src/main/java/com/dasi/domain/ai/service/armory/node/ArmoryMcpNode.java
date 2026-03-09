package com.dasi.domain.ai.service.armory.node;

import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import com.dasi.domain.ai.model.entity.ArmoryRequestEntity;
import com.dasi.domain.ai.model.enumeration.AiMcpType;
import com.dasi.domain.ai.model.vo.AiMcpVO;
import com.dasi.domain.ai.service.armory.ArmoryContext;
import com.dasi.domain.util.jwt.UserContext;
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientSseClientTransport;
import io.modelcontextprotocol.client.transport.ServerParameters;
import io.modelcontextprotocol.client.transport.StdioClientTransport;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.Map;
import java.util.Set;

import static com.dasi.domain.ai.model.enumeration.AiType.MCP;

@Slf4j
@Service
public class ArmoryMcpNode extends AbstractArmoryNode {

    @Resource
    private ArmoryAdvisorNode armoryAdvisorNode;

    @Resource
    private UserContext userContext;

    @Value("${miniagent.mcp.header.secret:X-MiniAgent-Mcp-Secret}")
    private String mcpSecretHeader;

    @Value("${miniagent.mcp.header.user-id:X-MiniAgent-Mcp-UserId}")
    private String mcpUserIdHeader;

    @Override
    protected String doApply(ArmoryRequestEntity armoryRequestEntity, ArmoryContext armoryContext) throws Exception {

        Set<AiMcpVO> aiMcpVOList = armoryContext.getValue(MCP.getType());

        if (aiMcpVOList == null || aiMcpVOList.isEmpty()) {
            return router(armoryRequestEntity, armoryContext);
        }

        for (AiMcpVO aiMcpVO : aiMcpVOList) {
            McpSyncClient mcpSyncClient = null;

            switch (AiMcpType.fromString(aiMcpVO.getMcpType())) {
                case SSE -> {
                    AiMcpVO.SseConfig sseConfig = aiMcpVO.getSseConfig();
                    String baseUri = sseConfig.getBaseUri();
                    String sseEndPoint = sseConfig.getSseEndPoint();
                    Long userId = userContext.getUserId();
                    String mcpSecret = aiMcpVO.getMcpSecret();

                    HttpClientSseClientTransport sseClient = HttpClientSseClientTransport
                            .builder(baseUri)
                            .sseEndpoint(sseEndPoint)
                            .customizeRequest(requestBuilder -> {
                                requestBuilder.header(mcpUserIdHeader, String.valueOf(userId));
                                if (StringUtils.hasText(mcpSecret)) {
                                    requestBuilder.header(mcpSecretHeader, Base64.getEncoder().encodeToString(mcpSecret.getBytes(StandardCharsets.UTF_8)));
                                }
                            })
                            .build();

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

                    ServerParameters serverParameters = ServerParameters
                            .builder(stdio.getCommand())
                            .args(stdio.getArgs())
                            .env(stdio.getEnv())
                            .build();

                    StdioClientTransport stdioClient = new StdioClientTransport(serverParameters);

                    mcpSyncClient = McpClient
                            .sync(stdioClient)
                            .requestTimeout(Duration.ofMinutes(aiMcpVO.getMcpTimeout()))
                            .build();

                    mcpSyncClient.initialize();
                }
            }

            String mcpBeanName = MCP.getBeanName(aiMcpVO.getMcpId());
            registerBean(mcpBeanName, McpSyncClient.class, mcpSyncClient);
            log.info("【装配节点】ArmoryMcpNode：mcpBeanName={}, mcpType={}", mcpBeanName, aiMcpVO.getMcpType());
        }

        return router(armoryRequestEntity, armoryContext);
    }

    @Override
    public StrategyHandler<ArmoryRequestEntity, ArmoryContext, String> get(ArmoryRequestEntity armoryRequestEntity, ArmoryContext armoryContext) {
        return armoryAdvisorNode;
    }

}
