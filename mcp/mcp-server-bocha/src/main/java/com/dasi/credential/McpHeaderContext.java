package com.dasi.credential;

import org.springframework.stereotype.Component;

@Component
public class McpHeaderContext {

    public static final String CONTEXT_KEY = "BochaCredential";

    private final ThreadLocal<BochaCredential> credentialThreadLocal = new ThreadLocal<>();

    public void setCredential(BochaCredential credential) {
        credentialThreadLocal.set(credential);
    }

    public BochaCredential getCredential() {
        return credentialThreadLocal.get();
    }

    public void clear() {
        credentialThreadLocal.remove();
    }

}
