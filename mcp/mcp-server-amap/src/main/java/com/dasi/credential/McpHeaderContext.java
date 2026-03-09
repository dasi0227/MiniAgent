package com.dasi.credential;

import org.springframework.stereotype.Component;

@Component
public class McpHeaderContext {

    public static final String CONTEXT_KEY = "AmapCredential";

    private final ThreadLocal<AmapCredential> credentialThreadLocal = new ThreadLocal<>();

    public void setCredential(AmapCredential credential) {
        credentialThreadLocal.set(credential);
    }

    public AmapCredential getCredential() {
        return credentialThreadLocal.get();
    }

    public void clear() {
        credentialThreadLocal.remove();
    }

}
