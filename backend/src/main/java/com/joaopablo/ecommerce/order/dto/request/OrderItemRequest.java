package com.joaopablo.ecommerce.order.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(name = "OrderItemRequest", description = "A single product line item within an order")
public class OrderItemRequest {

    @NotNull(message = "Product ID is required")
    @Schema(
            description = "UUID of the product to order",
            example = "a2e3c1b0-1234-4abc-8def-000000000001",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private UUID productId;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    @Schema(
            description = "Number of units to order (minimum 1)",
            example = "3",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Integer quantity;

}
