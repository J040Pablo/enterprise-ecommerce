package com.joaopablo.ecommerce.cart.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request schema for adding items to the shopping cart")
public class CreateCartRequest {

    @Schema(description = "UUID of the product to be added", example = "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11")
    @NotNull(message = "Product ID cannot be null")
    private UUID productId;

    @Schema(description = "Quantity of the product to be added", example = "2")
    @NotNull(message = "Quantity cannot be null")
    @Positive(message = "Quantity must be greater than zero")
    private Integer quantity;
}
