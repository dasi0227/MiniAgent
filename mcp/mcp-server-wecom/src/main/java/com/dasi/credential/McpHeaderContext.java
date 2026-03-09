package com.dasi.credential;

import org.springframework.stereotype.Component;

@Component
public class McpHeaderContext {

    public static final String CONTEXT_KEY = "WeComCredential";

    private final ThreadLocal<WeComCredential> credentialThreadLocal = new ThreadLocal<>();

    public void setCredential(WeComCredential credential) {
        credentialThreadLocal.set(credential);
    }

    public WeComCredential getCredential() {
        return credentialThreadLocal.get();
    }

    public void clear() {
        credentialThreadLocal.remove();
    }

}
