package com.joaopablo.ecommerce.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Newly registered user account details")
public class UserResponse {

    @Schema(description = "User's unique identifier (UUID)", example = "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11")
    private UUID id;

    @Schema(description = "User's first name", example = "João")
    private String firstName;

    @Schema(description = "User's last name", example = "Pablo")
    private String lastName;

    @Schema(description = "User's email address", example = "joao.pablo@email.com")
    private String email;

    @Schema(description = "User's CPF (Brazilian tax ID)", example = "123.456.789-00")
    private String cpf;

    @Schema(description = "User's phone number", example = "+55 11 99999-9999")
    private String phone;

    @Schema(description = "Whether the account is enabled", example = "true")
    private Boolean enabled;

    @Schema(description = "Whether the email has been verified", example = "false")
    private Boolean emailVerified;
}