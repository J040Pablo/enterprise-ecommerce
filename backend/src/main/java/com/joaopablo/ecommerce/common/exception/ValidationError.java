package com.joaopablo.ecommerce.common.exception;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Field-level validation error detail")
public class ValidationError {

    @Schema(description = "Name of the field that failed validation", example = "email")
    private String field;

    @Schema(description = "Validation error message", example = "must be a well-formed email address")
    private String message;
}
