package com.example.inventory.item;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ItemRequest(
        @NotBlank String sku,
        @NotBlank String name,
        @Min(0) int quantity,
        @NotNull @DecimalMin("0.00") BigDecimal price) {
}
