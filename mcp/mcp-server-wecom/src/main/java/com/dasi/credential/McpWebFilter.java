package com.dasi.credential;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

@Slf4j
@Component
public class McpWebFilter implements WebFilter {

    @Resource
    private ObjectMapper objectMapper;

    @Value("${miniagent.mcp.header.secret:X-MiniAgent-Mcp-Secret}")
    private String mcpSecretHeader;

    @Value("${miniagent.mcp.header.user-id:X-MiniAgent-Mcp-UserId}")
    private String mcpUserIdHeader;

    @Override
    public @NotNull Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String encodedSecret = exchange.getRequest().getHeaders().getFirst(mcpSecretHeader);
        String userId = exchange.getRequest().getHeaders().getFirst(mcpUserIdHeader);

        WeComCredential credential = parseCredential(encodedSecret, userId);
        return chain.filter(exchange)
                .contextWrite(context -> context.put(McpHeaderContext.CONTEXT_KEY, credential));
    }

    private WeComCredential parseCredential(String encodedSecret, String userId) {
        try {
            if (!StringUtils.hasText(userId) || !StringUtils.hasText(encodedSecret)) {
                return new WeComCredential();
            }

            byte[] decodedBytes = Base64.getDecoder().decode(encodedSecret);
            String secretJson = new String(decodedBytes, StandardCharsets.UTF_8);
            if (!StringUtils.hasText(secretJson)) {
                return new WeComCredential();
            }

            Map<String, String> secretMap = objectMapper.readValue(secretJson, new TypeReference<>() {});
            String corpId = secretMap.get("corpId");
            String corpSecret = secretMap.get("corpSecret");
            String agentId = secretMap.get("agentId");

            WeComCredential weComCredential = WeComCredential.builder()
                    .corpId(corpId)
                    .corpSecret(corpSecret)
                    .agentId(Integer.parseInt(agentId))
                    .userId(userId)
                    .build();

            log.info("【Header 解析】weComCredential={}", weComCredential);
            return weComCredential;
        } catch (Exception e) {
            log.error("【Header 解析】失败：encodedSecret={}, userId={}", encodedSecret, userId, e);
            return new WeComCredential();
        }
    }
}
