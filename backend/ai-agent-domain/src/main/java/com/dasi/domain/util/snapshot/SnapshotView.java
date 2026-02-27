package com.dasi.domain.util.snapshot;

import com.dasi.domain.workspace.model.vo.TemplateVO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SnapshotView {

    private List<TemplateVO.McpInfo> mcpInfoList;

    private Map<String, String> systemPrompt;

    private List<String> userPrompt;

    public static SnapshotView empty() {
        return new SnapshotView(List.of(), new LinkedHashMap<>(), List.of());
    }
}
