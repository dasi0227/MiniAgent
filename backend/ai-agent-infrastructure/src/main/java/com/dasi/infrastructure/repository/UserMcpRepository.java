package com.dasi.infrastructure.repository;

import com.alibaba.fastjson2.JSON;
import com.dasi.domain.user.repository.IUserMcpRepository;
import com.dasi.infrastructure.persistent.dao.IAiMcpDao;
import com.dasi.infrastructure.persistent.dao.IAiSecretDao;
import com.dasi.infrastructure.persistent.po.AiMcp;
import com.dasi.infrastructure.persistent.po.AiSecret;
import com.dasi.infrastructure.util.SecretCryptoService;
import com.dasi.types.dto.request.user.mcp.UserMcpListRequest;
import com.dasi.types.dto.request.user.mcp.UserMcpManageRequest;
import com.dasi.types.dto.response.user.mcp.UserMcpItemResponse;
import com.dasi.types.exception.AdminException;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;

@Repository
public class UserMcpRepository implements IUserMcpRepository {

    @Resource
    private IAiMcpDao aiMcpDao;

    @Resource
    private IAiSecretDao aiSecretDao;

    @Resource
    private SecretCryptoService secretCryptoService;

    @Override
    public List<UserMcpItemResponse> list(Long userId, UserMcpListRequest request) {
        List<AiMcp> aiMcpList = aiMcpDao.queryVisibleList(userId, request.getIdKeyword(), request.getNameKeyword());
        if (aiMcpList == null || aiMcpList.isEmpty()) {
            return List.of();
        }

        Map<String, UserMcpItemResponse> resultMap = new LinkedHashMap<>();
        for (AiMcp aiMcp : aiMcpList) {
            List<AiSecret> secretList = aiSecretDao.queryByUserRef(userId, "mcp", aiMcp.getMcpId());
            // queryVisibleList 已按 mcp_from DESC 排序，优先保留用户自定义记录，避免 system/mine 同名重复。
            resultMap.putIfAbsent(aiMcp.getMcpId(), UserMcpItemResponse.builder()
                    .id(aiMcp.getId())
                    .mcpId(aiMcp.getMcpId())
                    .mcpName(aiMcp.getMcpName())
                    .mcpType(aiMcp.getMcpType())
                    .mcpConfig(aiMcp.getMcpConfig())
                    .mcpDesc(aiMcp.getMcpDesc())
                    .mcpTimeout(aiMcp.getMcpTimeout())
                    .mcpChat(aiMcp.getMcpChat())
                    .sourceType(aiMcp.getMcpFrom() != null && aiMcp.getMcpFrom() > 0 ? "mine" : "system")
                    .editable(Objects.equals(aiMcp.getMcpFrom(), userId))
                    .secretConfigured(secretList != null && !secretList.isEmpty())
                    .build());
        }
        return new ArrayList<>(resultMap.values());
    }

    @Override
    public UserMcpItemResponse queryByMcpId(Long userId, String mcpId) {
        AiMcp aiMcp = aiMcpDao.queryByMcpIdWithFrom(mcpId, userId);
        return toUserMcpItemResponse(userId, aiMcp);
    }

    @Override
    public UserMcpItemResponse queryById(Long userId, Long id) {
        AiMcp aiMcp = aiMcpDao.queryByIdWithOwner(id, userId);
        if (aiMcp == null) {
            aiMcp = aiMcpDao.queryById(id);
        }
        return toUserMcpItemResponse(userId, aiMcp);
    }

    @Override
    public void insert(Long userId, UserMcpManageRequest request) {
        AiMcp exists = aiMcpDao.queryByMcpIdByOwner(request.getMcpId(), userId);
        if (exists != null) {
            throw new AdminException("MCP 已存在，请修改后重新添加");
        }

        AiMcp aiMcp = AiMcp.builder()
                .mcpId(request.getMcpId())
                .mcpName(request.getMcpName())
                .mcpType(request.getMcpType())
                .mcpConfig(request.getMcpConfig())
                .mcpDesc(defaultDesc(request.getMcpDesc()))
                .mcpTimeout(request.getMcpTimeout() == null ? 180 : request.getMcpTimeout())
                .mcpChat(request.getMcpChat() == null ? 1 : request.getMcpChat())
                .mcpFrom(userId)
                .build();
        aiMcpDao.insert(aiMcp);

        saveSecretMap(userId, request.getMcpId(), request.getSecretMap());
    }

    @Override
    public void update(Long userId, UserMcpManageRequest request) {
        if (request.getId() == null) {
            throw new AdminException("缺少 MCP id");
        }
        AiMcp exists = aiMcpDao.queryByIdWithOwner(request.getId(), userId);
        if (exists == null) {
            throw new AdminException("MCP 不存在或无权限修改");
        }

        AiMcp aiMcp = AiMcp.builder()
                .id(request.getId())
                .mcpName(request.getMcpName())
                .mcpType(request.getMcpType())
                .mcpConfig(request.getMcpConfig())
                .mcpDesc(defaultDesc(request.getMcpDesc()))
                .mcpTimeout(request.getMcpTimeout() == null ? exists.getMcpTimeout() : request.getMcpTimeout())
                .mcpChat(request.getMcpChat() == null ? exists.getMcpChat() : request.getMcpChat())
                .mcpFrom(userId)
                .build();
        aiMcpDao.updateByOwner(aiMcp);

        saveSecretMap(userId, exists.getMcpId(), request.getSecretMap());
    }

    @Override
    public void delete(Long userId, Long id) {
        AiMcp exists = aiMcpDao.queryByIdWithOwner(id, userId);
        if (exists == null) {
            throw new AdminException("MCP 不存在或无权限删除");
        }

        aiMcpDao.deleteByOwner(id, userId);
        aiSecretDao.deleteByUserRef(userId, "mcp", exists.getMcpId());
    }

    @Override
    public void toggle(Long userId, Long id, Integer mcpChat) {
        AiMcp exists = aiMcpDao.queryByIdWithOwner(id, userId);
        if (exists == null) {
            throw new AdminException("MCP 不存在或无权限切换");
        }
        aiMcpDao.toggleByOwner(AiMcp.builder().id(id).mcpFrom(userId).mcpChat(mcpChat).build());
    }

    @Override
    public Map<String, Object> test(Long userId, String mcpId) {
        UserMcpItemResponse userMcp = queryByMcpId(userId, mcpId);
        if (userMcp == null) {
            throw new AdminException("MCP 不存在");
        }

        List<String> requiredKeys = requiredSecretKeys(mcpId);
        Map<String, String> secretMap = querySecretPlainMap(userId, mcpId);
        List<String> missingKeys = requiredKeys.stream().filter(k -> !StringUtils.hasText(secretMap.get(k))).toList();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("mcpId", userMcp.getMcpId());
        result.put("ok", missingKeys.isEmpty());
        result.put("missingKeys", missingKeys);
        result.put("message", missingKeys.isEmpty() ? "连接参数已配置" : "请先完成配置");
        result.put("checkedAt", LocalDateTime.now().toString());
        return result;
    }

    @Override
    public Map<String, Object> export(Long userId, String mcpId) {
        UserMcpItemResponse userMcp = queryByMcpId(userId, mcpId);
        if (userMcp == null) {
            throw new AdminException("MCP 不存在");
        }

        List<AiSecret> secretList = aiSecretDao.queryByUserRef(userId, "mcp", mcpId);
        List<String> secretKeyList = secretList.stream().map(AiSecret::getSecretKey).toList();

        Map<String, Object> exportData = new LinkedHashMap<>();
        exportData.put("mcpId", userMcp.getMcpId());
        exportData.put("mcpName", userMcp.getMcpName());
        exportData.put("mcpType", userMcp.getMcpType());
        exportData.put("mcpConfig", safeParseJson(userMcp.getMcpConfig()));
        exportData.put("mcpDesc", userMcp.getMcpDesc());
        exportData.put("mcpTimeout", userMcp.getMcpTimeout());
        exportData.put("mcpChat", userMcp.getMcpChat());
        exportData.put("sourceType", userMcp.getSourceType());
        exportData.put("secretKeys", secretKeyList);
        exportData.put("exportAt", LocalDateTime.now().toString());
        return exportData;
    }

    @Override
    public Map<String, String> querySecretPlainMap(Long userId, String mcpId) {
        List<AiSecret> secretList = aiSecretDao.queryByUserRef(userId, "mcp", mcpId);
        if (secretList == null || secretList.isEmpty()) {
            return Map.of();
        }

        Map<String, String> result = new HashMap<>();
        for (AiSecret aiSecret : secretList) {
            result.put(aiSecret.getSecretKey(), secretCryptoService.decrypt(aiSecret.getSecretCiphertext(), aiSecret.getSecretNonce()));
        }
        return result;
    }

    private void saveSecretMap(Long userId, String mcpId, Map<String, String> secretMap) {
        if (secretMap == null || secretMap.isEmpty()) {
            return;
        }

        for (Map.Entry<String, String> entry : secretMap.entrySet()) {
            String secretKey = entry.getKey();
            String plainValue = entry.getValue();
            if (!StringUtils.hasText(secretKey) || !StringUtils.hasText(plainValue)) {
                continue;
            }

            SecretCryptoService.EncryptedSecret encryptedSecret = secretCryptoService.encrypt(plainValue);
            AiSecret exists = aiSecretDao.queryByUserRefAndKey(userId, "mcp", mcpId, secretKey);

            if (exists == null) {
                AiSecret aiSecret = AiSecret.builder()
                        .secretId(UUID.randomUUID().toString().replace("-", ""))
                        .userId(userId)
                        .secretScene(resolveSecretScene(mcpId))
                        .refType("mcp")
                        .refId(mcpId)
                        .secretKey(secretKey)
                        .secretCiphertext(encryptedSecret.getCiphertext())
                        .secretNonce(encryptedSecret.getNonce())
                        .secretAlgo(encryptedSecret.getAlgo())
                        .keyVersion(encryptedSecret.getKeyVersion())
                        .secretStatus(1)
                        .build();
                aiSecretDao.insert(aiSecret);
            } else {
                AiSecret aiSecret = AiSecret.builder()
                        .id(exists.getId())
                        .userId(userId)
                        .secretScene(resolveSecretScene(mcpId))
                        .secretCiphertext(encryptedSecret.getCiphertext())
                        .secretNonce(encryptedSecret.getNonce())
                        .secretAlgo(encryptedSecret.getAlgo())
                        .keyVersion(encryptedSecret.getKeyVersion())
                        .secretStatus(1)
                        .build();
                aiSecretDao.update(aiSecret);
            }
        }
    }

    private UserMcpItemResponse toUserMcpItemResponse(Long userId, AiMcp aiMcp) {
        if (aiMcp == null) {
            return null;
        }
        List<AiSecret> secretList = aiSecretDao.queryByUserRef(userId, "mcp", aiMcp.getMcpId());
        return UserMcpItemResponse.builder()
                .id(aiMcp.getId())
                .mcpId(aiMcp.getMcpId())
                .mcpName(aiMcp.getMcpName())
                .mcpType(aiMcp.getMcpType())
                .mcpConfig(aiMcp.getMcpConfig())
                .mcpDesc(aiMcp.getMcpDesc())
                .mcpTimeout(aiMcp.getMcpTimeout())
                .mcpChat(aiMcp.getMcpChat())
                .sourceType(aiMcp.getMcpFrom() != null && aiMcp.getMcpFrom() > 0 ? "mine" : "system")
                .editable(Objects.equals(aiMcp.getMcpFrom(), userId))
                .secretConfigured(secretList != null && !secretList.isEmpty())
                .build();
    }

    private String resolveSecretScene(String mcpId) {
        if (mcpId == null) {
            return "mcp";
        }
        if ("wecom".equalsIgnoreCase(mcpId)) {
            return "wecom";
        }
        if ("email".equalsIgnoreCase(mcpId)) {
            return "email";
        }
        if ("csdn".equalsIgnoreCase(mcpId)) {
            return "csdn";
        }
        return "mcp";
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

    private Object safeParseJson(String json) {
        if (!StringUtils.hasText(json)) {
            return Map.of();
        }
        try {
            return JSON.parse(json);
        } catch (Exception e) {
            return json;
        }
    }

    private String defaultDesc(String desc) {
        return StringUtils.hasText(desc) ? desc : "暂无";
    }
}
