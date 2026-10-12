package com.example.inventory.item;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemController.class)
class ItemControllerTest {

    @Autowired
    MockMvc mvc;

    @MockitoBean
    ItemService service;

    private static final ItemResponse WIDGET = new ItemResponse(1L, "SKU-1", "Widget", 5, new BigDecimal("9.99"));

    @Test
    void listReturnsPage() throws Exception {
        given(service.list(any(Pageable.class)))
                .willReturn(new PageResponse<>(List.of(WIDGET), 0, 10, 1, 1));

        mvc.perform(get("/api/items"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].sku").value("SKU-1"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void createReturns201WithLocation() throws Exception {
        given(service.create(any(ItemRequest.class))).willReturn(WIDGET);

        mvc.perform(post("/api/items").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"sku":"SKU-1","name":"Widget","quantity":5,"price":9.99}"""))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/items/1"))
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void createWithInvalidBodyReturns400WithFieldErrors() throws Exception {
        mvc.perform(post("/api/items").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"sku":"","name":"Widget","quantity":-1,"price":9.99}"""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.fieldErrors.sku").exists())
                .andExpect(jsonPath("$.fieldErrors.quantity").exists());

        verifyNoInteractions(service);
    }

    @Test
    void getMissingItemReturns404() throws Exception {
        given(service.get(99L)).willThrow(new ItemNotFoundException(99L));

        mvc.perform(get("/api/items/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Item 99 not found"));
    }

    @Test
    void duplicateSkuReturns409() throws Exception {
        given(service.create(any(ItemRequest.class))).willThrow(new DuplicateSkuException("SKU-1"));

        mvc.perform(post("/api/items").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"sku":"SKU-1","name":"Widget","quantity":5,"price":9.99}"""))
                .andExpect(status().isConflict());
    }

    @Test
    void deleteReturns204() throws Exception {
        mvc.perform(delete("/api/items/1")).andExpect(status().isNoContent());
    }

    @Test
    void deleteMissingReturns404() throws Exception {
        doThrow(new ItemNotFoundException(5L)).when(service).delete(eq(5L));

        mvc.perform(delete("/api/items/5")).andExpect(status().isNotFound());
    }
}
