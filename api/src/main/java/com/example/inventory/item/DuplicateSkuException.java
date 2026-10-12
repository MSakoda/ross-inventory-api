package com.example.inventory.item;

public class DuplicateSkuException extends RuntimeException {

    public DuplicateSkuException(String sku) {
        super("An item with SKU '" + sku + "' already exists");
    }
}
