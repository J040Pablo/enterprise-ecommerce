package com.joaopablo.ecommerce.product.dto.response;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.UUID;

@Builder
public record ProductResponse(

        UUID id,

        String name,

        String description,

        BigDecimal price,

        Integer stock,

        Boolean active,

        UUID categoryId,

        String categoryName

) {
}