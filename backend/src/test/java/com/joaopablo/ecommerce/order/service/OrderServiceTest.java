package com.joaopablo.ecommerce.order.service;

import com.joaopablo.ecommerce.inventory.exception.InsufficientStockException;
import com.joaopablo.ecommerce.inventory.service.InventoryService;
import com.joaopablo.ecommerce.order.dto.request.CreateOrderRequest;
import com.joaopablo.ecommerce.order.dto.request.OrderItemRequest;
import com.joaopablo.ecommerce.order.dto.request.UpdateOrderStatusRequest;
import com.joaopablo.ecommerce.order.dto.response.OrderResponse;
import com.joaopablo.ecommerce.order.entity.Order;
import com.joaopablo.ecommerce.order.entity.OrderStatus;
import com.joaopablo.ecommerce.order.exception.InvalidOrderStatusException;
import com.joaopablo.ecommerce.order.mapper.OrderMapper;
import com.joaopablo.ecommerce.order.repository.OrderRepository;
import com.joaopablo.ecommerce.product.dto.response.ProductResponse;
import com.joaopablo.ecommerce.product.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository repository;

    @Mock
    private OrderMapper mapper;

    @Mock
    private ProductService productService;

    @Mock
    private InventoryService inventoryService;

    @InjectMocks
    private OrderService service;

    private UUID userId;
    private UUID productId;
    
    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        productId = UUID.randomUUID();
    }

    @Test
    void shouldCreateOrderSuccessfully() {
        // Arrange
        CreateOrderRequest request = new CreateOrderRequest(userId, List.of(
            new OrderItemRequest(productId, 2)
        ));

        ProductResponse product = ProductResponse.builder()
            .id(productId)
            .name("Product A")
            .price(BigDecimal.valueOf(100.0))
            .build();

        when(productService.findById(productId)).thenReturn(product);
        
        Order savedOrder = new Order();
        savedOrder.setId(UUID.randomUUID());
        savedOrder.setStatus(OrderStatus.PENDING);
        savedOrder.setTotalAmount(BigDecimal.valueOf(200.0));

        OrderResponse mappedResponse = new OrderResponse();
        mappedResponse.setId(savedOrder.getId());
        mappedResponse.setTotalAmount(BigDecimal.valueOf(200.0));

        when(repository.save(any(Order.class))).thenReturn(savedOrder);
        when(mapper.toResponse(savedOrder)).thenReturn(mappedResponse);

        // Act
        OrderResponse response = service.createOrder(request);

        // Assert
        assertNotNull(response);
        assertEquals(BigDecimal.valueOf(200.0), response.getTotalAmount());
        
        verify(inventoryService).decreaseStock(productId, 2);
        verify(repository).save(any(Order.class));
    }

    @Test
    void shouldThrowInsufficientStockException() {
        // Arrange
        CreateOrderRequest request = new CreateOrderRequest(userId, List.of(
            new OrderItemRequest(productId, 10)
        ));

        ProductResponse product = ProductResponse.builder()
            .id(productId)
            .name("Product A")
            .price(BigDecimal.valueOf(100.0))
            .build();

        when(productService.findById(productId)).thenReturn(product);
        doThrow(new InsufficientStockException("")).when(inventoryService).decreaseStock(productId, 10);

        // Act & Assert
        assertThrows(InsufficientStockException.class, () -> service.createOrder(request));
        
        verify(repository, never()).save(any(Order.class));
    }

    @Test
    void shouldUpdateStatusSuccessfully() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        Order order = new Order();
        order.setId(orderId);
        order.setStatus(OrderStatus.PENDING);

        when(repository.findById(orderId)).thenReturn(Optional.of(order));
        
        Order updatedOrder = new Order();
        updatedOrder.setId(orderId);
        updatedOrder.setStatus(OrderStatus.CONFIRMED);
        
        when(repository.save(order)).thenReturn(updatedOrder);
        
        OrderResponse mappedResponse = new OrderResponse();
        mappedResponse.setStatus(OrderStatus.CONFIRMED);
        
        when(mapper.toResponse(updatedOrder)).thenReturn(mappedResponse);

        // Act
        OrderResponse response = service.updateStatus(orderId, new UpdateOrderStatusRequest(OrderStatus.CONFIRMED));

        // Assert
        assertEquals(OrderStatus.CONFIRMED, response.getStatus());
        assertEquals(OrderStatus.CONFIRMED, order.getStatus());
    }

    @Test
    void shouldThrowExceptionWhenCancellingDeliveredOrder() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        Order order = new Order();
        order.setId(orderId);
        order.setStatus(OrderStatus.DELIVERED);

        when(repository.findById(orderId)).thenReturn(Optional.of(order));

        // Act & Assert
        assertThrows(InvalidOrderStatusException.class, () -> service.cancelOrder(orderId));
    }
}
