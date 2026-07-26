package com.joaopablo.ecommerce.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LogoutRequestDTO {

    @NotBlank(message = "Refresh token is required.")
    private String refreshToken;
}
