package com.joaopablo.ecommerce.category.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.UUID;

@Builder
@Schema(name = "CategoryResponse", description = "Category details returned by the API")
public record CategoryResponse(

        @Schema(description = "Unique identifier of the category", example = "b1ffcd00-0d1c-4fa9-cc7e-7cc0ce491b22")
        UUID id,

        @Schema(description = "Display name of the category", example = "Electronics")
        String name,

        @Schema(description = "Optional description of the category", example = "Electronic devices and accessories")
        String description,

        @Schema(description = "Whether the category is active and publicly visible", example = "true")
        Boolean active

) {
}