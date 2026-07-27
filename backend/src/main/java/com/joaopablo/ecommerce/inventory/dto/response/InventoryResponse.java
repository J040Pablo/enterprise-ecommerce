package com.joaopablo.ecommerce.inventory.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.UUID;

@Builder
@Schema(name = "InventoryResponse", description = "Current stock record for a product")
public record InventoryResponse(
        @Schema(description = "Unique identifier of the inventory record", example = "f9a0b1c2-0000-4def-8abc-111111111111")
        UUID id,
        @Schema(description = "UUID of the product this record belongs to", example = "a2e3c1b0-1234-4abc-8def-000000000001")
        UUID productId,
        @Schema(description = "Current available quantity in stock", example = "150")
        Integer quantity
) {}