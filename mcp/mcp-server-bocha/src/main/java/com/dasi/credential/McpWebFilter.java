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

        BochaCredential credential = parseCredential(encodedSecret, userId);
        return chain.filter(exchange)
                .contextWrite(context -> context.put(McpHeaderContext.CONTEXT_KEY, credential));
    }

    private BochaCredential parseCredential(String encodedSecret, String userId) {
        try {
            if (!StringUtils.hasText(userId) || !StringUtils.hasText(encodedSecret)) {
                return new BochaCredential();
            }

            byte[] decodedBytes = Base64.getDecoder().decode(encodedSecret);
            String secretJson = new String(decodedBytes, StandardCharsets.UTF_8);
            if (!StringUtils.hasText(secretJson)) {
                return new BochaCredential();
            }

            Map<String, String> secretMap = objectMapper.readValue(secretJson, new TypeReference<>() {});
            return BochaCredential.builder()
                    .apiKey(secretMap.get("apiKey"))
                    .userId(userId)
                    .build();
        } catch (Exception e) {
            log.error("【Header 解析】bocha credential 解析失败, userId={}", userId, e);
            return new BochaCredential();
        }
    }

}
