package com.joaopablo.ecommerce.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request body for refreshing JWT tokens")
public class RefreshTokenRequestDTO {

    @NotBlank(message = "Refresh token is required.")
    @Schema(
            description = "The refresh token previously issued on login or last refresh",
            example = "550e8400-e29b-41d4-a716-446655440000",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String refreshToken;
}
