package com.joaopablo.ecommerce.order.dto.response;

import com.joaopablo.ecommerce.order.entity.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponse {

    private UUID id;
    private UUID userId;
    private OrderStatus status;
    private List<OrderItemResponse> items;
    private BigDecimal totalAmount;

}
