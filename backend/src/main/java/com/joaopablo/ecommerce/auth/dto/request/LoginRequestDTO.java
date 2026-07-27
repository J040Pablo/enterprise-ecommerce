package com.joaopablo.ecommerce.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Credentials for user authentication")
public class LoginRequestDTO {

    @NotBlank
    @Email
    @Schema(description = "User's registered email address", example = "joao.pablo@email.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    @NotBlank
    @Schema(description = "User's password (minimum 8 characters)", example = "Senha@123", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;
}
