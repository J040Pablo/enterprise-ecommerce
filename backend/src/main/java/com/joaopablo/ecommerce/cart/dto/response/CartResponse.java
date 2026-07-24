package com.joaopablo.ecommerce.cart.dto.response;

import lombok.Builder;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.Set;

@Builder
@Schema(description = "Response schema representing a shopping cart")
public record CartResponse(
        @Schema(description = "Unique database ID of the cart", example = "1")
        Long id,

        @Schema(description = "Database ID of the customer owning this cart", example = "10")
        Long userId,

        @Schema(description = "Set of items currently in the cart")
        Set<CartItemResponse> items,

        @Schema(description = "Total cost of all items in the cart", example = "149.90")
        BigDecimal total
) {}
