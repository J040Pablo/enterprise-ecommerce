package com.joaopablo.ecommerce.auth.dto.internal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

/**
 * Redis payload for a one-time OAuth login code.
 * The opaque code itself never embeds tokens or PII — only this server-side value does.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OAuthLoginCodePayload {

    private String accessToken;
    private String refreshToken;
    private String type;
    private long expiresIn;
    private UUID userId;
    private String firstName;
    private String lastName;
    private String email;
    private List<String> roles;
}
