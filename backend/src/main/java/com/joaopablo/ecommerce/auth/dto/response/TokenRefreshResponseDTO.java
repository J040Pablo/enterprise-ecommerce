package com.joaopablo.ecommerce.auth.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TokenRefreshResponseDTO {

    private String accessToken;
    private String refreshToken;
    private String type;
    private long expiresIn;
}
