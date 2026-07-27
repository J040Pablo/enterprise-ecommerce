package com.joaopablo.ecommerce.product.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@Schema(description = "Optional query parameters for filtering the product list")
public class ProductFilterRequest {

    @Schema(description = "Filter by product name (partial match)", example = "Notebook")
    private String name;

    @Schema(description = "Minimum price filter (inclusive)", example = "100.00")
    private BigDecimal minPrice;

    @Schema(description = "Maximum price filter (inclusive)", example = "9999.99")
    private BigDecimal maxPrice;

    @Schema(description = "Filter by active status — true returns only active products", example = "true")
    private Boolean active;

    @Schema(description = "Filter by category UUID", example = "b1ffcd00-0d1c-5fg9-cc7e-7cc0ce491b22")
    private UUID categoryId;
}