package com.joaopablo.ecommerce.product.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@Schema(description = "Fields available to update on an existing product — all fields are optional")
public class UpdateProductRequest {

    @Size(max = 255)
    @Schema(description = "New product name", example = "Notebook Gamer Pro")
    private String name;

    @Size(max = 2000)
    @Schema(description = "New product description", example = "Updated description with latest specs")
    private String description;

    @DecimalMin(value = "0.0", inclusive = false)
    @Schema(description = "New product price — must be greater than zero", example = "5499.99")
    private BigDecimal price;

    @Schema(description = "Whether the product is active and visible in the catalog", example = "true")
    private Boolean active;

    @Schema(description = "UUID of the new category for this product", example = "b1ffcd00-0d1c-5fg9-cc7e-7cc0ce491b22")
    private UUID categoryId;
}