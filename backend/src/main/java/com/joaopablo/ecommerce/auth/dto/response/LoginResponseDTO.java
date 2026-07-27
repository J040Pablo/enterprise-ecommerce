package com.joaopablo.ecommerce.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Authentication response containing JWT tokens and authenticated user information")
public class LoginResponseDTO {

    @Schema(description = "JWT access token", example = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2FvLnBhYmxvQGVtYWlsLmNvbSJ9...")
    private String token;

    @Schema(description = "Refresh token used to obtain a new access token", example = "550e8400-e29b-41d4-a716-446655440000")
    private String refreshToken;

    @Schema(description = "Token type, always 'Bearer'", example = "Bearer")
    private String type;

    @Schema(description = "Access token validity in milliseconds", example = "86400000")
    private long expiresIn;

    @Schema(description = "Basic information about the authenticated user")
    private UserLoginResponse user;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "Authenticated user summary")
    public static class UserLoginResponse {

        @Schema(description = "User's unique identifier", example = "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11")
        private UUID id;

        @Schema(description = "User's first name", example = "João")
        private String firstName;

        @Schema(description = "User's last name", example = "Pablo")
        private String lastName;

        @Schema(description = "User's email address", example = "joao.pablo@email.com")
        private String email;

        @Schema(description = "Roles assigned to the user", example = "[\"CUSTOMER\"]")
        private List<String> roles;
    }
}
