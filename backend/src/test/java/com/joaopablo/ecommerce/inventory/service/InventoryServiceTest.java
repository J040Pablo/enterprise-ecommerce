package com.joaopablo.ecommerce.inventory.service;

import com.joaopablo.ecommerce.inventory.dto.response.InventoryResponse;
import com.joaopablo.ecommerce.inventory.entity.Inventory;
import com.joaopablo.ecommerce.inventory.exception.InsufficientStockException;
import com.joaopablo.ecommerce.inventory.mapper.InventoryMapper;
import com.joaopablo.ecommerce.inventory.repository.InventoryRepository;
import com.joaopablo.ecommerce.product.entity.Product;
import com.joaopablo.ecommerce.product.exception.ProductNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private InventoryRepository repository;

    @Mock
    private InventoryMapper mapper;

    @InjectMocks
    private InventoryService service;

    private UUID productId;
    private Inventory inventory;

    @BeforeEach
    void setUp() {
        productId = UUID.randomUUID();
        Product product = Product.builder().name("Test").build();
        product.setId(productId);

        inventory = Inventory.builder()
                .product(product)
                .quantity(10)
                .build();
        inventory.setId(UUID.randomUUID());
    }

    @Test
    void getQuantityOrZero_shouldReturnQuantityWhenInventoryExists() {
        when(repository.findByProductId(productId)).thenReturn(Optional.of(inventory));

        assertEquals(10, service.getQuantityOrZero(productId));
    }

    @Test
    void getQuantityOrZero_shouldReturnZeroWhenInventoryMissing() {
        when(repository.findByProductId(productId)).thenReturn(Optional.empty());

        assertEquals(0, service.getQuantityOrZero(productId));
    }

    @Test
    void setStock_shouldSetHigherAbsoluteQuantity() {
        when(repository.findByProductId(productId)).thenReturn(Optional.of(inventory));
        when(repository.save(inventory)).thenReturn(inventory);
        when(mapper.toResponse(inventory)).thenAnswer(inv ->
                InventoryResponse.builder()
                        .id(inventory.getId())
                        .productId(productId)
                        .quantity(inventory.getQuantity())
                        .build()
        );

        InventoryResponse response = service.setStock(productId, 100);

        assertEquals(100, inventory.getQuantity());
        assertEquals(100, response.quantity());
        verify(repository).save(inventory);
    }

    @Test
    void setStock_shouldSetLowerAbsoluteQuantity() {
        when(repository.findByProductId(productId)).thenReturn(Optional.of(inventory));
        when(repository.save(inventory)).thenReturn(inventory);
        when(mapper.toResponse(inventory)).thenAnswer(inv ->
                InventoryResponse.builder()
                        .id(inventory.getId())
                        .productId(productId)
                        .quantity(inventory.getQuantity())
                        .build()
        );

        InventoryResponse response = service.setStock(productId, 3);

        assertEquals(3, inventory.getQuantity());
        assertEquals(3, response.quantity());
    }

    @Test
    void setStock_shouldAllowZero() {
        when(repository.findByProductId(productId)).thenReturn(Optional.of(inventory));
        when(repository.save(inventory)).thenReturn(inventory);
        when(mapper.toResponse(inventory)).thenAnswer(inv ->
                InventoryResponse.builder()
                        .id(inventory.getId())
                        .productId(productId)
                        .quantity(inventory.getQuantity())
                        .build()
        );

        InventoryResponse response = service.setStock(productId, 0);

        assertEquals(0, inventory.getQuantity());
        assertEquals(0, response.quantity());
    }

    @Test
    void setStock_shouldRejectNegativeQuantity() {
        assertThrows(IllegalArgumentException.class, () -> service.setStock(productId, -1));
        verify(repository, never()).save(any());
    }

    @Test
    void setStock_shouldRejectNullQuantity() {
        assertThrows(IllegalArgumentException.class, () -> service.setStock(productId, null));
        verify(repository, never()).save(any());
    }

    @Test
    void increaseStock_shouldAddToCurrentQuantity() {
        when(repository.findByProductId(productId)).thenReturn(Optional.of(inventory));
        when(repository.save(inventory)).thenReturn(inventory);
        when(mapper.toResponse(inventory)).thenAnswer(inv ->
                InventoryResponse.builder()
                        .id(inventory.getId())
                        .productId(productId)
                        .quantity(inventory.getQuantity())
                        .build()
        );

        InventoryResponse response = service.increaseStock(productId, 5);

        assertEquals(15, inventory.getQuantity());
        assertEquals(15, response.quantity());
    }

    @Test
    void decreaseStock_shouldSubtractFromCurrentQuantity() {
        when(repository.findByProductId(productId)).thenReturn(Optional.of(inventory));
        when(repository.save(inventory)).thenReturn(inventory);
        when(mapper.toResponse(inventory)).thenAnswer(inv ->
                InventoryResponse.builder()
                        .id(inventory.getId())
                        .productId(productId)
                        .quantity(inventory.getQuantity())
                        .build()
        );

        InventoryResponse response = service.decreaseStock(productId, 4);

        assertEquals(6, inventory.getQuantity());
        assertEquals(6, response.quantity());
    }

    @Test
    void decreaseStock_shouldThrowWhenInsufficient() {
        when(repository.findByProductId(productId)).thenReturn(Optional.of(inventory));

        assertThrows(InsufficientStockException.class, () -> service.decreaseStock(productId, 50));
        verify(repository, never()).save(any());
    }

    @Test
    void findByProductId_shouldThrowWhenMissing() {
        when(repository.findByProductId(productId)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> service.findByProductId(productId));
    }
}
