package com.storrego.catalog.exception;

public class DuplicateCatalogSkinException extends RuntimeException {

    public DuplicateCatalogSkinException(String id) {
        super("Catalog skin already exists: " + id);
    }
}
