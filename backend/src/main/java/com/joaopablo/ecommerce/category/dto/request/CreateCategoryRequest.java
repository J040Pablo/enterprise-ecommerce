package com.joaopablo.ecommerce.category.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(name = "CreateCategoryRequest", description = "Payload for creating a new product category")
public class CreateCategoryRequest {

    @NotBlank(message = "Name is required.")
    @Size(max = 255)
    @Schema(
            description = "Unique display name of the category",
            example = "Electronics",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String name;

    @Size(max = 2000)
    @Schema(
            description = "Optional long-form description of the category",
            example = "Electronic devices, accessories, and components",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String description;

}