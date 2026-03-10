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
import com.dasi.types.exception.MiniAgentException;
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
            throw new MiniAgentException(PUBLISH_FLOW_EMPTY);
        }
        List<AiFlow> sortedFlowList = aiFlowList.stream()
                .sorted(Comparator.comparing(AiFlow::getFlowSeq, Comparator.nullsLast(Integer::compareTo)))
                .toList();

        List<JSONObject> mcpSnapshotList = new ArrayList<>();
        List<JSONObject> systemPromptList = new ArrayList<>();
        List<JSONObject> userPromptList = new ArrayList<>();

        for (AiFlow aiFlow : sortedFlowList) {
            String clientId = aiFlow.getClientId();
            AiClient aiClient = aiClientDao.queryByClientId(clientId);
            if (aiClient == null) {
                throw new MiniAgentException(PUBLISH_CLIENT_MISSING);
            }
            AiPrompt aiPrompt = queryPromptByClientId(clientId);

            JSONObject systemPrompt = new JSONObject();
            systemPrompt.put("clientRole", aiFlow.getClientRole());
            systemPrompt.put("content", aiPrompt.getSystenPrompt());

            JSONObject userPrompt = new JSONObject();
            userPrompt.put("seq", aiFlow.getFlowSeq());
            userPrompt.put("content", aiFlow.getUserPrompt());

            systemPromptList.add(systemPrompt);
            userPromptList.add(userPrompt);

            List<AiConfig> mcpConfigList = aiConfigDao.queryByClientIdAndConfigType(aiClient.getClientId(), ConfigType.MCP.getType());
            if (mcpConfigList == null || mcpConfigList.isEmpty()) {
                continue;
            }

            for (AiConfig mcpConfig : mcpConfigList) {
                String mcpId = mcpConfig.getConfigValue();
                AiMcp aiMcp = aiMcpDao.queryByMcpId(mcpId);
                if (aiMcp == null) {
                    throw new MiniAgentException(PUBLISH_MCP_MISSING);
                }
                JSONObject mcpInfo = new JSONObject();
                mcpInfo.put("mcpName", aiMcp.getMcpName());
                mcpInfo.put("mcpType", aiMcp.getMcpType());
                mcpInfo.put("mcpDesc", aiMcp.getMcpDesc());
                mcpInfo.put("mcpParamTemplate", aiMcp.getMcpParam());
                mcpInfo.put("requiredSecrets", extractSecretKeyList(aiMcp.getMcpSecret()));
                mcpSnapshotList.add(mcpInfo);
            }
        }

        JSONObject snapshot = new JSONObject();
        snapshot.put("version", 1);
        snapshot.put("mcps", mcpSnapshotList);
        snapshot.put("systemPrompts", systemPromptList);
        snapshot.put("userPrompts", userPromptList);

        return JSON.toJSONString(snapshot);
    }

    @Override
    public SnapshotView parseSnapshot(String snapshotRaw) {
        try {
            JSONObject snapshot = JSON.parseObject(snapshotRaw);
            return new SnapshotView(
                    parseSnapshotMcpList(snapshot.getJSONArray("mcps")),
                    parseSnapshotSystemPrompt(snapshot.getJSONArray("systemPrompts")),
                    parseSnapshotUserPrompt(snapshot.getJSONArray("userPrompts"))
            );
        } catch (Exception e) {
            log.warn("解析 Template 快照失败", e);
            return SnapshotView.empty();
        }
    }

    private List<TemplateVO.McpInfo> parseSnapshotMcpList(JSONArray mcpArray) {
        if (mcpArray == null || mcpArray.isEmpty()) {
            return List.of();
        }
        List<TemplateVO.McpInfo> result = new ArrayList<>();
        for (int i = 0; i < mcpArray.size(); i++) {
            JSONObject mcp = mcpArray.getJSONObject(i);
            if (mcp == null) {
                continue;
            }
            result.add(TemplateVO.McpInfo.builder()
                    .mcpName(mcp.getString("mcpName"))
                    .mcpType(mcp.getString("mcpType"))
                    .mcpDesc(mcp.getString("mcpDesc"))
                    .mcpParamTemplate(mcp.get("mcpParamTemplate"))
                    .requiredSecrets(parseStringList(mcp.getJSONArray("requiredSecrets")))
                    .build());
        }
        return result;
    }

    private Map<String, String> parseSnapshotSystemPrompt(JSONArray systemPromptArray) {
        Map<String, String> systemPromptMap = new LinkedHashMap<>();
        if (systemPromptArray == null) {
            return systemPromptMap;
        }
        for (int i = 0; i < systemPromptArray.size(); i++) {
            JSONObject item = systemPromptArray.getJSONObject(i);
            if (item == null) {
                continue;
            }
            String clientRole = item.getString("clientRole");
            if (!StringUtils.hasText(clientRole)) {
                continue;
            }
            systemPromptMap.put(clientRole, item.getString("content"));
        }
        return systemPromptMap;
    }

    private List<String> parseSnapshotUserPrompt(JSONArray userPromptArray) {
        if (userPromptArray == null) {
            return List.of();
        }
        List<JSONObject> tempList = new ArrayList<>();
        for (int i = 0; i < userPromptArray.size(); i++) {
            JSONObject item = userPromptArray.getJSONObject(i);
            if (item != null) {
                tempList.add(item);
            }
        }
        tempList.sort(Comparator.comparing(item -> item.getInteger("seq"), Comparator.nullsLast(Integer::compareTo)));

        List<String> result = new ArrayList<>();
        for (JSONObject item : tempList) {
            result.add(item.getString("content"));
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
            log.warn("解析 mcp_secret 失败，secret={}", secretRaw, e);
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

    private AiPrompt queryPromptByClientId(String clientId) {
        List<AiConfig> promptConfigList = aiConfigDao.queryByClientIdAndConfigType(clientId, ConfigType.PROMPT.getType());
        if (promptConfigList == null || promptConfigList.isEmpty()) {
            throw new MiniAgentException(PUBLISH_PROMPT_CONFIG_MISSING);
        }
        String promptId = promptConfigList.get(0).getConfigValue();
        AiPrompt aiPrompt = aiPromptDao.queryByPromptId(promptId);
        if (aiPrompt == null) {
            throw new MiniAgentException(PUBLISH_PROMPT_MISSING);
        }
        return aiPrompt;
    }

}
