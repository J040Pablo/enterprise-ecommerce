package com.joaopablo.ecommerce.category.dto.response;

import lombok.Builder;

import java.util.UUID;

@Builder
public record CategoryResponse(

        UUID id,

        String name,

        String description,

        Boolean active

) {
}