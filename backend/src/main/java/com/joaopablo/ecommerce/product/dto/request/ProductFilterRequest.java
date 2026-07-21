package com.joaopablo.ecommerce.product.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
public class ProductFilterRequest {

    private String name;

    private BigDecimal minPrice;

    private BigDecimal maxPrice;

    private Boolean active;

    private UUID categoryId;
}