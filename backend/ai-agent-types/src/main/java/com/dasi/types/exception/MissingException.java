package com.dasi.types.exception;

public class MissingException extends MiniAgentException {
    public MissingException(String message) {
        super(message);
    }

    public MissingException(String message, Throwable cause) {
        super(message, cause);
    }

    public MissingException(Throwable cause) {
        super(cause);
    }
}
