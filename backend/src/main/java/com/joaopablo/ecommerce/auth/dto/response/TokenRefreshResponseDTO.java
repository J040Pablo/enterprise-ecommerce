package com.joaopablo.ecommerce.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Response containing the newly issued JWT tokens after a refresh operation")
public class TokenRefreshResponseDTO {

    @Schema(description = "New JWT access token", example = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2FvLnBhYmxvQGVtYWlsLmNvbSJ9...")
    private String accessToken;

    @Schema(description = "New refresh token (previous token is revoked)", example = "660f9511-f30c-52e5-b827-557766551111")
    private String refreshToken;

    @Schema(description = "Token type, always 'Bearer'", example = "Bearer")
    private String type;

    @Schema(description = "Access token validity in milliseconds", example = "86400000")
    private long expiresIn;
}
