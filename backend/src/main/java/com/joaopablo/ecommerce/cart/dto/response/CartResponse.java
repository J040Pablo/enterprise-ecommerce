package com.joaopablo.ecommerce.cart.dto.response;

import lombok.Builder;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

@Builder
@Schema(description = "Response schema representing a shopping cart")
public record CartResponse(
        @Schema(description = "Unique database ID of the cart", example = "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11")
        UUID id,

        @Schema(description = "Database ID of the customer owning this cart", example = "b1ffcd00-0d1c-5fg9-cc7e-7cc0ce491b22")
        UUID userId,

        @Schema(description = "Set of items currently in the cart")
        Set<CartItemResponse> items,

        @Schema(description = "Total cost of all items in the cart", example = "149.90")
        BigDecimal total
) {}
