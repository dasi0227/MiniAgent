package com.dasi.domain.workspace.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentVO {

    // agent 信息
    private String agentId;
    private String agentName;
    private String agentType;
    private String agentDesc;

    // model 信息
    private String modelId;
    private String modelName;
    private String modelType;
    private String apiId;
    private String apiBaseUrl;
    private String apiCompletionUrl;

    // mcp 信息
    private List<McpInfo> mcpInfoList;
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class McpInfo {
        private String mcpId;
        private String mcpName;
        private String mcpType;
        private String mcpParam;
        private String mcpDesc;
        private String mcpSecret;
    }

    // client 信息
    private List<ClientInfo> clientInfoList;
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ClientInfo {
        private String clientId;
        private String clientRole;
        private String promptId;
        private String systemPrompt;
        private Long flowId;
        private String userPrompt;
    }
}
