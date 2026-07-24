package com.joaopablo.ecommerce.cart.dto.response;

import lombok.Builder;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.UUID;

@Builder
@Schema(description = "Response schema representing an individual shopping cart item")
public record CartItemResponse(
        @Schema(description = "Unique database ID of the cart item", example = "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11")
        UUID id,

        @Schema(description = "UUID of the product in the cart", example = "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11")
        UUID productId,

        @Schema(description = "Name of the product in the cart", example = "Product 1")
        String productName,

        @Schema(description = "Unit price of the product", example = "49.99")
        BigDecimal productPrice,

        @Schema(description = "Quantity of the product in the cart", example = "3")
        Integer quantity,

        @Schema(description = "Subtotal cost of the current item (price * quantity)", example = "149.97")
        BigDecimal subtotal
) {}
