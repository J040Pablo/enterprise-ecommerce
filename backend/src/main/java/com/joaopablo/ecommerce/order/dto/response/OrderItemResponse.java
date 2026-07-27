package com.joaopablo.ecommerce.order.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(name = "OrderItemResponse", description = "A single product line item within an order")
public class OrderItemResponse {

    @Schema(description = "UUID of the product", example = "a2e3c1b0-1234-4abc-8def-000000000001")
    private UUID productId;

    @Schema(description = "Display name of the product at order time", example = "Wireless Noise-Cancelling Headphones")
    private String productName;

    @Schema(description = "Quantity ordered", example = "2")
    private Integer quantity;

    @Schema(description = "Unit price at order time", example = "89.99")
    private BigDecimal unitPrice;

    @Schema(description = "quantity × unitPrice", example = "179.98")
    private BigDecimal subtotal;

}
