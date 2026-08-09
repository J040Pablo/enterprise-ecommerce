package com.joaopablo.ecommerce.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Exchanges a one-time OAuth login code for JWT tokens")
public class OAuthCodeExchangeRequest {

    @NotBlank(message = "code is required")
    @Schema(
            description = "One-time opaque code received on the OAuth frontend callback",
            example = "dGhpc2lzYW9wYXF1ZWNvZGU"
    )
    private String code;
}
