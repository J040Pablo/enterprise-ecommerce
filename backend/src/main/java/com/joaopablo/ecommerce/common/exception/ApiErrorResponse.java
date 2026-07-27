package com.joaopablo.ecommerce.common.exception;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Standard error response body returned by all API error handlers")
public class ApiErrorResponse {

    @Schema(description = "Timestamp of when the error occurred (ISO 8601 UTC)", example = "2026-07-26T17:00:00Z")
    private Instant timestamp;

    @Schema(description = "HTTP status code", example = "404")
    private int status;

    @Schema(description = "HTTP error reason phrase", example = "Not Found")
    private String error;

    @Schema(description = "Human-readable description of the error", example = "Resource not found")
    private String message;

    @Schema(description = "Request URI that triggered the error", example = "/api/v1/products/a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11")
    private String path;

    @Schema(description = "List of field-level validation errors, present only on 400 Bad Request responses")
    private List<ValidationError> errors;
}
