package com.dasi.credential;

import org.springframework.stereotype.Component;

@Component
public class McpHeaderContext {

    public static final String CONTEXT_KEY = "CsdnCredential";

    private final ThreadLocal<CsdnCredential> credentialThreadLocal = new ThreadLocal<>();

    public void setCredential(CsdnCredential credential) {
        credentialThreadLocal.set(credential);
    }

    public CsdnCredential getCredential() {
        return credentialThreadLocal.get();
    }

    public void clear() {
        credentialThreadLocal.remove();
    }

}
