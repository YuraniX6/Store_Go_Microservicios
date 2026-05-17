package com.storego.inventoryservice.exception;

public class SkinAccessDeniedException extends RuntimeException {
    public SkinAccessDeniedException(String message) {
        super(message);
    }

    public SkinAccessDeniedException(String message, Throwable cause) {
        super(message, cause);
    }
}
