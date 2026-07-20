package com.joaopablo.ecommerce.product.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CreateProductRequest {

    @NotBlank
    private String name;

    @NotBlank
    private String description;

    @Positive
    private BigDecimal price;

    @PositiveOrZero
    private Integer stock;
}