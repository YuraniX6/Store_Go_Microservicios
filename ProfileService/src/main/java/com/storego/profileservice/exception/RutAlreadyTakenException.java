package com.storego.profileservice.exception;

public class RutAlreadyTakenException extends RuntimeException {
    public RutAlreadyTakenException(String message) {
        super(message);
    }
}
