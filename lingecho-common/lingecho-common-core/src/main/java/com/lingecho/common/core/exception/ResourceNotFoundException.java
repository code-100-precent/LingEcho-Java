package com.lingecho.common.core.exception;

import lombok.Getter;

@Getter
public class ResourceNotFoundException extends RuntimeException {
    private final Integer code;

    public ResourceNotFoundException(String message) {
        super(message);
        this.code = 400;
    }

    public ResourceNotFoundException(Integer code, String message) {
        super(message);
        this.code = code;
    }
}