package com.joaopablo.ecommerce.order.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(name = "CreateOrderRequest", description = "Payload for placing a new order")
public class CreateOrderRequest {

    @NotNull(message = "User ID is required")
    @Schema(
            description = "UUID of the user placing the order",
            example = "d5e6f7a8-0000-4cde-aabc-333333333333",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private UUID userId;

    @NotEmpty(message = "Order must have at least one item")
    @Valid
    @Schema(
            description = "List of items to include in the order — must contain at least one entry",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private List<OrderItemRequest> items;

}
