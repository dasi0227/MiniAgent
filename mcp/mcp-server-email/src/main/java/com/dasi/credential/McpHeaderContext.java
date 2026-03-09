package com.dasi.credential;

import org.springframework.stereotype.Component;

@Component
public class McpHeaderContext {

    public static final String CONTEXT_KEY = "EmailCredential";

    private final ThreadLocal<EmailCredential> credentialThreadLocal = new ThreadLocal<>();

    public void setCredential(EmailCredential credential) {
        credentialThreadLocal.set(credential);
    }

    public EmailCredential getCredential() {
        return credentialThreadLocal.get();
    }

    public void clear() {
        credentialThreadLocal.remove();
    }

}
