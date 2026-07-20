package com.joaopablo.ecommerce.product.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

@Builder
@Getter
public class ProductResponse {

    private UUID id;

    private String name;

    private String description;

    private BigDecimal price;

    private Integer stock;

    private Boolean active;
}
