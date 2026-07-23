package com.joaopablo.ecommerce.inventory.service;

import com.joaopablo.ecommerce.inventory.dto.response.InventoryResponse;
import com.joaopablo.ecommerce.inventory.entity.Inventory;
import com.joaopablo.ecommerce.inventory.exception.InsufficientStockException;
import com.joaopablo.ecommerce.inventory.mapper.InventoryMapper;
import com.joaopablo.ecommerce.inventory.repository.InventoryRepository;
import com.joaopablo.ecommerce.product.entity.Product;
import com.joaopablo.ecommerce.product.exception.ProductNotFoundException;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepository repository;
    private final InventoryMapper mapper;

    @Transactional
    public void createInventory(Product product, Integer initialQuantity) {

        Inventory inventory = Inventory.builder()
                .product(product)
                .quantity(initialQuantity)
                .build();

        repository.save(inventory);
    }

    @Transactional(readOnly = true)
    public InventoryResponse findByProductId(UUID productId) {
        return mapper.toResponse(findInventory(productId));
    }

    @Transactional
    public InventoryResponse increaseStock(UUID productId, Integer quantity) {

        validateQuantity(quantity);

        Inventory inventory = findInventory(productId);

        inventory.setQuantity(inventory.getQuantity() + quantity);

        return mapper.toResponse(repository.save(inventory));
    }

    @Transactional
    public InventoryResponse decreaseStock(UUID productId, Integer quantity) {

        validateQuantity(quantity);

        Inventory inventory = findInventory(productId);

        if (inventory.getQuantity() < quantity) {
            throw new InsufficientStockException("Insufficient stock available.");
        }

        inventory.setQuantity(inventory.getQuantity() - quantity);

        return mapper.toResponse(repository.save(inventory));
    }

    private Inventory findInventory(UUID productId) {
        return repository.findByProductId(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));
    }

    private void validateQuantity(Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero.");
        }
    }
}