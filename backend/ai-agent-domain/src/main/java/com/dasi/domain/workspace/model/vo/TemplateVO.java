package com.dasi.domain.workspace.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TemplateVO {

    // template 信息
    private String templateId;

    // user 信息
    private String userName;

    // plaza 信息
    private String plazaTitle;
    private String plazaDesc;
    private Integer likeCount;
    private Integer favorCount;
    private Integer commentCount;

    // agent 信息
    private String agentName;
    private String agentType;
    private String agentDesc;

    // model 信息
    private String apiBaseUrl;
    private String apiCompletionUrl;
    private String modelName;
    private String modelType;

    List<McpInfo> mcpInfoList;
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class McpInfo {
        private String mcpName;
        private String mcpType;
        private Object mcpParamTemplate;
        private String mcpDesc;
        private List<String> requiredSecrets;
    }

    private Map<String, String> systemPrompt;

    private List<String> userPrompt;

}
