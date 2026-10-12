package com.example.inventory.item;

import org.springframework.data.domain.Page;

import java.util.List;

/** Stable JSON shape for pages, so the API contract doesn't depend on Spring's PageImpl internals. */
public record PageResponse<T>(List<T> content, int page, int size, long totalElements, int totalPages) {

    static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(page.getContent(), page.getNumber(), page.getSize(),
                page.getTotalElements(), page.getTotalPages());
    }
}
