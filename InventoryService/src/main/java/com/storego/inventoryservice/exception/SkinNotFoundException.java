package com.storego.inventoryservice.exception;

public class SkinNotFoundException extends RuntimeException {
    public SkinNotFoundException(String message) {
        super(message);
    }

    public SkinNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
