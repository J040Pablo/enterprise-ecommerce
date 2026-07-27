package com.joaopablo.ecommerce.category.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(name = "UpdateCategoryRequest", description = "Payload for partially updating an existing category — all fields are optional")
public class UpdateCategoryRequest {

    @Size(max = 255)
    @Schema(
            description = "New name for the category (leave null to keep current value)",
            example = "Consumer Electronics",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String name;

    @Size(max = 2000)
    @Schema(
            description = "New description for the category (leave null to keep current value)",
            example = "Updated category covering all consumer electronic goods",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String description;

    @Schema(
            description = "Set to false to deactivate the category without deleting it",
            example = "true",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private Boolean active;

}