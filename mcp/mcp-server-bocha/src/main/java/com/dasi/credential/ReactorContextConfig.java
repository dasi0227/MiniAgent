package com.dasi.credential;

import io.micrometer.context.ContextRegistry;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Hooks;

@Configuration
public class ReactorContextConfig {

    @Resource
    private McpHeaderContext mcpHeaderContext;

    @PostConstruct
    public void registerAccessor() {
        ContextRegistry registry = ContextRegistry.getInstance();
        registry.removeThreadLocalAccessor(McpHeaderContext.CONTEXT_KEY);
        registry.registerThreadLocalAccessor(
                McpHeaderContext.CONTEXT_KEY,
                mcpHeaderContext::getCredential,
                mcpHeaderContext::setCredential,
                mcpHeaderContext::clear
        );
        Hooks.enableAutomaticContextPropagation();
    }

}
