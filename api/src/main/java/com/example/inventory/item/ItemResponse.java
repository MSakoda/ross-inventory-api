package com.example.inventory.item;

import java.math.BigDecimal;

public record ItemResponse(Long id, String sku, String name, int quantity, BigDecimal price) {

    static ItemResponse from(Item item) {
        return new ItemResponse(item.getId(), item.getSku(), item.getName(), item.getQuantity(), item.getPrice());
    }
}
