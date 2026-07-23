package com.joaopablo.ecommerce.inventory.dto.response;

import lombok.Builder;

import java.util.UUID;

@Builder
public record InventoryResponse(
        UUID id,
        UUID productId,
        Integer quantity
) {}