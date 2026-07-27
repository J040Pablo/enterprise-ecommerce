package com.joaopablo.ecommerce.product.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@Schema(description = "Data required to create a new product in the catalog")
public class CreateProductRequest {

    @NotBlank(message = "Name is required.")
    @Size(max = 255)
    @Schema(description = "Product name", example = "Notebook Gamer", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Size(max = 2000)
    @Schema(description = "Detailed product description", example = "High-performance gaming laptop with RTX 4060")
    private String description;

    @NotNull(message = "Price is required.")
    @DecimalMin(value = "0.0", inclusive = false)
    @Schema(description = "Product price — must be greater than zero", example = "5999.99", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal price;

    @NotNull(message = "Initial quantity is required.")
    @PositiveOrZero
    @Schema(description = "Initial stock quantity — zero or more", example = "50", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer initialQuantity;

    @NotNull(message = "Category is required.")
    @Schema(description = "UUID of the category this product belongs to", example = "b1ffcd00-0d1c-5fg9-cc7e-7cc0ce491b22", requiredMode = Schema.RequiredMode.REQUIRED)
    private UUID categoryId;
}