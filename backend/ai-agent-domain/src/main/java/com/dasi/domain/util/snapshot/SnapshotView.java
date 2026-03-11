package com.dasi.domain.util.snapshot;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SnapshotView {

    private List<McpView> mcps;
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class McpView {
        private String mcpName;
        private String mcpType;
        private String mcpParam;
        private String mcpDesc;
        private List<String> requiredSecrets;
    }

    private List<PromptView> prompts;
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PromptView {
        private String clientRole;
        private String systemPrompt;
        private String userPrompt;
    }

}
