package com.dasi.infrastructure.util;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.dasi.domain.util.snapshot.ISnapshotUtil;
import com.dasi.domain.util.snapshot.SnapshotView;
import com.dasi.domain.workspace.model.enumeration.ConfigType;
import com.dasi.domain.workspace.model.vo.TemplateVO;
import com.dasi.infrastructure.persistent.dao.*;
import com.dasi.infrastructure.persistent.po.*;
import com.dasi.types.exception.WorkException;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;

import static com.dasi.types.constant.ExceptionMessage.*;

@Slf4j
@Service
public class SnapshotUtil implements ISnapshotUtil {

    @Resource
    private IAiFlowDao aiFlowDao;

    @Resource
    private IAiClientDao aiClientDao;

    @Resource
    private IAiConfigDao aiConfigDao;

    @Resource
    private IAiPromptDao aiPromptDao;

    @Resource
    private IAiMcpDao aiMcpDao;

    @Override
    public String buildSnapshot(String agentId) {
        List<AiFlow> aiFlowList = Optional.ofNullable(aiFlowDao.queryByAgentId(agentId)).orElse(List.of());
        if (aiFlowList.isEmpty()) {
            throw new WorkException(PUBLISH_FLOW_EMPTY);
        }
        List<AiFlow> sortedFlowList = aiFlowList.stream()
                .sorted(Comparator.comparing(AiFlow::getFlowSeq, Comparator.nullsLast(Integer::compareTo)))
                .toList();

        Map<String, JSONObject> mcpSnapshotMap = new LinkedHashMap<>();
        List<JSONObject> promptList = new ArrayList<>();

        for (AiFlow aiFlow : sortedFlowList) {
            String clientId = aiFlow.getClientId();
            AiClient aiClient = aiClientDao.queryByClientId(clientId);
            if (aiClient == null) {
                throw new WorkException(PUBLISH_CLIENT_MISSING);
            }
            AiPrompt aiPrompt = queryPromptByClientId(clientId);

            JSONObject prompt = new JSONObject();
            prompt.put("clientRole", aiFlow.getClientRole());
            prompt.put("system", aiPrompt.getSystenPrompt());
            prompt.put("user", aiFlow.getUserPrompt());
            promptList.add(prompt);

            List<AiConfig> mcpConfigList = aiConfigDao.queryByClientIdAndConfigType(aiClient.getClientId(), ConfigType.MCP.getType());
            if (mcpConfigList == null || mcpConfigList.isEmpty()) {
                continue;
            }

            for (AiConfig mcpConfig : mcpConfigList) {
                String mcpId = mcpConfig.getConfigValue();
                if (!StringUtils.hasText(mcpId) || mcpSnapshotMap.containsKey(mcpId)) {
                    continue;
                }
                AiMcp aiMcp = aiMcpDao.queryByMcpId(mcpId);
                if (aiMcp == null) {
                    throw new WorkException(PUBLISH_MCP_MISSING);
                }
                JSONObject mcpInfo = new JSONObject();
                mcpInfo.put("mcpName", aiMcp.getMcpName());
                mcpInfo.put("mcpType", aiMcp.getMcpType());
                mcpInfo.put("mcpDesc", aiMcp.getMcpDesc());
                mcpInfo.put("mcpParam", normalizeMcpParam(aiMcp.getMcpParam()));
                mcpInfo.put("requiredSecrets", extractSecretKeyList(aiMcp.getMcpSecret()));
                mcpSnapshotMap.put(mcpId, mcpInfo);
            }
        }

        JSONObject snapshot = new JSONObject();
        snapshot.put("version", 1);
        snapshot.put("mcps", mcpSnapshotMap.values());
        snapshot.put("prompts", promptList);

        return JSON.toJSONString(snapshot);
    }

    @Override
    public SnapshotView parseSnapshot(String snapshotRaw) {
        try {
            JSONObject snapshot = JSON.parseObject(snapshotRaw);
            return new SnapshotView(
                    parseSnapshotMcpList(snapshot.getJSONArray("mcps")),
                    parsePromptList(snapshot.getJSONArray("prompts"))
            );
        } catch (Exception e) {
            log.error("【解析快照】失败", e);
            return new SnapshotView(List.of(), List.of());
        }
    }

    @Override
    public List<TemplateVO.ClientInfo> toTemplateClientInfoList(List<SnapshotView.PromptView> promptViewList) {
        if (promptViewList == null || promptViewList.isEmpty()) {
            return List.of();
        }
        return promptViewList.stream()
                .map(promptView -> TemplateVO.ClientInfo.builder()
                        .clientRole(promptView.getClientRole())
                        .systemPrompt(promptView.getSystemPrompt())
                        .userPrompt(promptView.getUserPrompt())
                        .build())
                .toList();
    }

    @Override
    public List<TemplateVO.McpInfo> toTemplateMcpInfoList(List<SnapshotView.McpView> mcpViewList) {
        if (mcpViewList == null || mcpViewList.isEmpty()) {
            return List.of();
        }
        return mcpViewList.stream()
                .map(mcpView -> TemplateVO.McpInfo.builder()
                        .mcpName(mcpView.getMcpName())
                        .mcpType(mcpView.getMcpType())
                        .mcpParam(mcpView.getMcpParam())
                        .mcpDesc(mcpView.getMcpDesc())
                        .requiredSecrets(mcpView.getRequiredSecrets())
                        .build())
                .toList();
    }

    private List<SnapshotView.McpView> parseSnapshotMcpList(JSONArray mcpArray) {
        if (mcpArray == null || mcpArray.isEmpty()) {
            return List.of();
        }
        List<SnapshotView.McpView> result = new ArrayList<>();
        for (int i = 0; i < mcpArray.size(); i++) {
            JSONObject mcp = mcpArray.getJSONObject(i);
            if (mcp == null) {
                continue;
            }
            result.add(SnapshotView.McpView.builder()
                    .mcpName(mcp.getString("mcpName"))
                    .mcpType(mcp.getString("mcpType"))
                    .mcpDesc(mcp.getString("mcpDesc"))
                    .mcpParam(mcp.getString("mcpParam"))
                    .requiredSecrets(parseStringList(mcp.getJSONArray("requiredSecrets")))
                    .build());
        }
        return result;
    }

    private List<SnapshotView.PromptView> parsePromptList(JSONArray promptArray) {
        if (promptArray == null || promptArray.isEmpty()) {
            return List.of();
        }
        List<SnapshotView.PromptView> result = new ArrayList<>();
        for (int i = 0; i < promptArray.size(); i++) {
            JSONObject item = promptArray.getJSONObject(i);
            if (item == null) {
                continue;
            }
            result.add(SnapshotView.PromptView.builder()
                    .clientRole(item.getString("clientRole"))
                    .systemPrompt(item.getString("system"))
                    .userPrompt(item.getString("user"))
                    .build());
        }
        return result;
    }

    private List<String> extractSecretKeyList(String secretRaw) {
        if (!StringUtils.hasText(secretRaw)) {
            return List.of();
        }
        try {
            Object parsed = JSON.parse(secretRaw.trim());
            if (parsed instanceof JSONObject secretObj) {
                return new ArrayList<>(secretObj.keySet());
            }
            if (parsed instanceof JSONArray secretArray) {
                return parseStringList(secretArray);
            }
        } catch (Exception e) {
            log.error("【解析快照】失败", e);
        }
        return List.of();
    }

    private List<String> parseStringList(JSONArray jsonArray) {
        if (jsonArray == null) {
            return List.of();
        }
        List<String> values = jsonArray.toJavaList(String.class);
        if (values == null || values.isEmpty()) {
            return List.of();
        }
        return values.stream().filter(StringUtils::hasText).toList();
    }

    private String normalizeMcpParam(String raw) {
        if (!StringUtils.hasText(raw)) {
            return "{}";
        }
        try {
            Object parsed = JSON.parse(raw);
            return JSON.toJSONString(parsed);
        } catch (Exception e) {
            log.error("【解析快照】失败", e);
            return raw;
        }
    }

    private AiPrompt queryPromptByClientId(String clientId) {
        List<AiConfig> promptConfigList = aiConfigDao.queryByClientIdAndConfigType(clientId, ConfigType.PROMPT.getType());
        if (promptConfigList == null || promptConfigList.isEmpty()) {
            throw new WorkException(PUBLISH_PROMPT_CONFIG_MISSING);
        }
        String promptId = promptConfigList.get(0).getConfigValue();
        AiPrompt aiPrompt = aiPromptDao.queryByPromptId(promptId);
        if (aiPrompt == null) {
            throw new WorkException(PUBLISH_PROMPT_MISSING);
        }
        return aiPrompt;
    }

}
