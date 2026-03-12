package com.dasi.types.exception;

import lombok.Getter;

import java.util.List;

@Getter
public class DependencyConflictException extends MiniAgentException {

    public DependencyConflictException(String message) {
        super(message);
    }

}
