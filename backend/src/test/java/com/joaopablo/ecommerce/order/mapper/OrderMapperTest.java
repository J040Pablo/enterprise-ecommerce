package com.joaopablo.ecommerce.order.mapper;

import com.joaopablo.ecommerce.order.dto.response.OrderItemResponse;
import com.joaopablo.ecommerce.order.dto.response.OrderResponse;
import com.joaopablo.ecommerce.order.entity.Order;
import com.joaopablo.ecommerce.order.entity.OrderItem;
import com.joaopablo.ecommerce.order.entity.OrderStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class OrderMapperTest {

    private OrderMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new OrderMapper();
    }

    @Test
    void shouldMapEntityToResponse() {
        // Arrange
        Order order = new Order();
        order.setId(UUID.randomUUID());
        order.setUserId(UUID.randomUUID());
        order.setStatus(OrderStatus.PENDING);
        order.setTotalAmount(BigDecimal.valueOf(100.0));

        OrderItem item = new OrderItem();
        item.setProductId(UUID.randomUUID());
        item.setProductName("Test Product");
        item.setQuantity(2);
        item.setUnitPrice(BigDecimal.valueOf(50.0));
        item.setSubtotal(BigDecimal.valueOf(100.0));

        order.addItem(item);

        // Act
        OrderResponse response = mapper.toResponse(order);

        // Assert
        assertNotNull(response);
        assertEquals(order.getId(), response.getId());
        assertEquals(order.getUserId(), response.getUserId());
        assertEquals(order.getStatus(), response.getStatus());
        assertEquals(order.getTotalAmount(), response.getTotalAmount());

        assertNotNull(response.getItems());
        assertEquals(1, response.getItems().size());
        
        OrderItemResponse itemResponse = response.getItems().get(0);
        assertEquals(item.getProductId(), itemResponse.getProductId());
        assertEquals(item.getProductName(), itemResponse.getProductName());
        assertEquals(item.getQuantity(), itemResponse.getQuantity());
        assertEquals(item.getUnitPrice(), itemResponse.getUnitPrice());
        assertEquals(item.getSubtotal(), itemResponse.getSubtotal());
    }
}
