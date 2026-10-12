package com.example.inventory.item;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
class ItemRepositoryTest {

    @Autowired
    ItemRepository repository;

    @Test
    void savesAndFindsItem() {
        Item saved = repository.save(new Item("SKU-1", "Widget", 5, new BigDecimal("9.99")));

        assertThat(saved.getId()).isNotNull();
        assertThat(repository.findById(saved.getId())).get()
                .satisfies(i -> assertThat(i.getSku()).isEqualTo("SKU-1"));
    }

    @Test
    void existsBySkuAndIdNotIgnoresTheItemItself() {
        Item a = repository.save(new Item("SKU-A", "A", 1, BigDecimal.ONE));
        Item b = repository.save(new Item("SKU-B", "B", 1, BigDecimal.ONE));

        assertThat(repository.existsBySku("SKU-A")).isTrue();
        assertThat(repository.existsBySkuAndIdNot("SKU-A", a.getId())).isFalse();
        assertThat(repository.existsBySkuAndIdNot("SKU-A", b.getId())).isTrue();
    }

    @Test
    void paginates() {
        for (int i = 0; i < 5; i++) {
            repository.save(new Item("SKU-" + i, "Item " + i, i, BigDecimal.TEN));
        }

        var page = repository.findAll(PageRequest.of(1, 2));

        assertThat(page.getContent()).hasSize(2);
        assertThat(page.getTotalElements()).isEqualTo(5);
        assertThat(page.getTotalPages()).isEqualTo(3);
    }

    @Test
    void rejectsDuplicateSkuAtDatabaseLevel() {
        repository.saveAndFlush(new Item("DUP", "One", 1, BigDecimal.ONE));

        assertThatThrownBy(() -> repository.saveAndFlush(new Item("DUP", "Two", 1, BigDecimal.ONE)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
