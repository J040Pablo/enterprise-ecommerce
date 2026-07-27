package com.joaopablo.ecommerce.order.dto.response;


import com.joaopablo.ecommerce.order.entity.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(name = "OrderResponse", description = "Full details of a placed order including items, payment, and shipping")
public class OrderResponse {

    @Schema(description = "Unique identifier of the order", example = "c3d4e5f6-0000-4bcd-9abc-222222222222")
    private UUID id;

    @Schema(description = "UUID of the user who placed the order", example = "d5e6f7a8-0000-4cde-aabc-333333333333")
    private UUID userId;

    @Schema(description = "Current status of the order", example = "PENDING")
    private OrderStatus status;

    @Schema(description = "List of product line items in the order")
    private List<OrderItemResponse> items;

    @Schema(description = "Total monetary value of the order", example = "299.97")
    private BigDecimal totalAmount;

    @Schema(description = "Summary of the associated payment, if any")
    private PaymentSummaryResponse payment;

    @Schema(description = "Summary of the associated shipment, if any")
    private ShippingSummaryResponse shipping;

}
