package com.example.inventory.item;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ItemService {

    private final ItemRepository repository;

    public ItemService(ItemRepository repository) {
        this.repository = repository;
    }

    public PageResponse<ItemResponse> list(Pageable pageable) {
        return PageResponse.from(repository.findAll(pageable).map(ItemResponse::from));
    }

    public ItemResponse get(Long id) {
        return ItemResponse.from(find(id));
    }

    @Transactional
    public ItemResponse create(ItemRequest request) {
        if (repository.existsBySku(request.sku())) {
            throw new DuplicateSkuException(request.sku());
        }
        Item item = new Item(request.sku(), request.name(), request.quantity(), request.price());
        return ItemResponse.from(repository.save(item));
    }

    @Transactional
    public ItemResponse update(Long id, ItemRequest request) {
        Item item = find(id);
        if (repository.existsBySkuAndIdNot(request.sku(), id)) {
            throw new DuplicateSkuException(request.sku());
        }
        item.setSku(request.sku());
        item.setName(request.name());
        item.setQuantity(request.quantity());
        item.setPrice(request.price());
        // No save() needed: the entity is managed, so JPA flushes the changes when the transaction commits.
        return ItemResponse.from(item);
    }

    @Transactional
    public void delete(Long id) {
        repository.delete(find(id));
    }

    private Item find(Long id) {
        return repository.findById(id).orElseThrow(() -> new ItemNotFoundException(id));
    }
}
