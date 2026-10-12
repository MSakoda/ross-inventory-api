package com.example.inventory.item;

public class ItemNotFoundException extends RuntimeException {

    public ItemNotFoundException(Long id) {
        super("Item " + id + " not found");
    }
}
