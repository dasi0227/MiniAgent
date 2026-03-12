package com.dasi.types.exception;

public class MiniAgentException extends RuntimeException {
    public MiniAgentException(String message) {
        super(message);
    }

    public MiniAgentException(String message, Throwable cause) {
        super(message, cause);
    }

    public MiniAgentException(Throwable cause) {
        super(cause);
    }
}
