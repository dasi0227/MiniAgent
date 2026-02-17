package com.dasi.type.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

public final class SecretHeaderUtil {

    public static final String MCP_SECRET_MAP_HEADER = "X-MCP-SECRET-MAP";

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private SecretHeaderUtil() {
    }

    public static Map<String, String> getSecretMap() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes == null) {
                return Map.of();
            }
            HttpServletRequest request = attributes.getRequest();
            if (request == null) {
                return Map.of();
            }
            String headerValue = request.getHeader(MCP_SECRET_MAP_HEADER);
            if (!StringUtils.hasText(headerValue)) {
                return Map.of();
            }

            byte[] decoded = Base64.getDecoder().decode(headerValue);
            String json = new String(decoded, StandardCharsets.UTF_8);
            Map<String, String> rawMap = OBJECT_MAPPER.readValue(json, new TypeReference<>() {
            });
            if (rawMap == null || rawMap.isEmpty()) {
                return Map.of();
            }

            Map<String, String> resultMap = new LinkedHashMap<>();
            for (Map.Entry<String, String> entry : rawMap.entrySet()) {
                if (!StringUtils.hasText(entry.getKey()) || !StringUtils.hasText(entry.getValue())) {
                    continue;
                }
                resultMap.put(entry.getKey(), entry.getValue());
            }
            return resultMap;
        } catch (Exception ignored) {
            return Map.of();
        }
    }

    public static String resolve(Map<String, String> secretMap, String key, String defaultValue) {
        if (secretMap == null || secretMap.isEmpty() || !StringUtils.hasText(key)) {
            return defaultValue;
        }
        String value = secretMap.get(key);
        return StringUtils.hasText(value) ? value : defaultValue;
    }
}
