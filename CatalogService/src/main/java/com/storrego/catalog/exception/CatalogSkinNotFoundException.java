package com.storrego.catalog.exception;

public class CatalogSkinNotFoundException extends RuntimeException {

    public CatalogSkinNotFoundException(String id) {
        super("Catalog skin not found: " + id);
    }
}
