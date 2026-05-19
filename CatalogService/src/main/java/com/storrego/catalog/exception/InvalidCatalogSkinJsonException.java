package com.storrego.catalog.exception;

public class InvalidCatalogSkinJsonException extends RuntimeException {

    public InvalidCatalogSkinJsonException(String reason) {
        super(reason);
    }
}
