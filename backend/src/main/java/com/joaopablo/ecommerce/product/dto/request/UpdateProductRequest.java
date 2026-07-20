package com.joaopablo.ecommerce.product.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class UpdateProductRequest {

    private String name;
    private String description;
    private BigDecimal price;
    private Integer stock;
    private Boolean active;
}
