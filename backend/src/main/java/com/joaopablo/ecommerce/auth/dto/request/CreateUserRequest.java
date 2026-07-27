package com.joaopablo.ecommerce.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Data required to register a new user account")
public class CreateUserRequest {

    @NotBlank
    @Schema(description = "User's first name", example = "João", requiredMode = Schema.RequiredMode.REQUIRED)
    private String firstName;

    @NotBlank
    @Schema(description = "User's last name", example = "Pablo", requiredMode = Schema.RequiredMode.REQUIRED)
    private String lastName;

    @NotBlank
    @Email
    @Schema(description = "User's email address (must be unique)", example = "joao.pablo@email.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    @NotBlank
    @Size(min = 8)
    @Schema(description = "User's password — minimum 8 characters", example = "Senha@123", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;

    @NotBlank
    @Schema(description = "User's CPF (Brazilian tax ID, must be unique)", example = "123.456.789-00", requiredMode = Schema.RequiredMode.REQUIRED)
    private String cpf;

    @NotBlank
    @Schema(description = "User's phone number", example = "+55 11 99999-9999", requiredMode = Schema.RequiredMode.REQUIRED)
    private String phone;
}
