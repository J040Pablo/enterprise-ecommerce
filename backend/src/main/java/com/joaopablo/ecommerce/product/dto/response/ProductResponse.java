package com.joaopablo.ecommerce.product.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.UUID;

@Builder
@Schema(description = "Product details returned by the API")
public record ProductResponse(

        @Schema(description = "Product unique identifier (UUID)", example = "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11")
        UUID id,

        @Schema(description = "Product name", example = "Notebook Gamer")
        String name,

        @Schema(description = "Product description", example = "High-performance gaming laptop with RTX 4060")
        String description,

        @Schema(description = "Product price", example = "5999.99")
        BigDecimal price,

        @Schema(description = "Whether the product is active and visible in the catalog", example = "true")
        Boolean active,

        @Schema(description = "UUID of the product's category", example = "b1ffcd00-0d1c-5fg9-cc7e-7cc0ce491b22")
        UUID categoryId,

        @Schema(description = "Name of the product's category", example = "Electronics")
        String categoryName

) {}