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

        EmailCredential credential = parseCredential(encodedSecret, userId);
        return chain.filter(exchange)
                .contextWrite(context -> context.put(McpHeaderContext.CONTEXT_KEY, credential));
    }

    private EmailCredential parseCredential(String encodedSecret, String userId) {
        try {
            if (!StringUtils.hasText(userId) || !StringUtils.hasText(encodedSecret)) {
                return new EmailCredential();
            }

            byte[] decodedBytes = Base64.getDecoder().decode(encodedSecret);
            String secretJson = new String(decodedBytes, StandardCharsets.UTF_8);
            if (!StringUtils.hasText(secretJson)) {
                return new EmailCredential();
            }

            Map<String, String> secretMap = objectMapper.readValue(secretJson, new TypeReference<>() {});
            Integer smtpPort = parsePort(secretMap.get("smtpPort"));

            return EmailCredential.builder()
                    .smtpHost(secretMap.get("smtpHost"))
                    .smtpPort(smtpPort)
                    .smtpUsername(secretMap.get("smtpUsername"))
                    .smtpPassword(secretMap.get("smtpPassword"))
                    .fromAddress(secretMap.get("fromAddress"))
                    .fromName(secretMap.get("fromName"))
                    .userId(userId)
                    .build();
        } catch (Exception e) {
            log.error("【Header 解析】email credential 解析失败, userId={}", userId, e);
            return new EmailCredential();
        }
    }

    private Integer parsePort(String smtpPortRaw) {
        if (!StringUtils.hasText(smtpPortRaw)) {
            return null;
        }
        try {
            return Integer.parseInt(smtpPortRaw);
        } catch (Exception e) {
            return null;
        }
    }

}
