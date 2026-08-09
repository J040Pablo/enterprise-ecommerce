package com.joaopablo.ecommerce.auth.service;

import com.joaopablo.ecommerce.auth.dto.internal.OAuthLoginCodePayload;
import com.joaopablo.ecommerce.auth.dto.response.LoginResponseDTO;
import com.joaopablo.ecommerce.auth.exception.InvalidOAuthLoginCodeException;
import com.joaopablo.ecommerce.auth.repository.OAuthLoginCodeRedisRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;
import java.util.Objects;

/**
 * Issues and consumes one-time OAuth login codes so JWT/refresh tokens never appear in redirect URLs.
 */
@Service
@RequiredArgsConstructor
public class OAuthLoginCodeService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final int CODE_BYTES = 32;

    private final OAuthLoginCodeRedisRepository repository;

    @Value("${app.oauth2.login-code-ttl-seconds:90}")
    private long ttlSeconds;

    /**
     * Stores the login response under a cryptographically random opaque code.
     *
     * @return the opaque code to place in the frontend redirect (never log tokens)
     */
    public String issue(LoginResponseDTO loginResponse) {
        Objects.requireNonNull(loginResponse, "loginResponse is required");
        Objects.requireNonNull(loginResponse.getUser(), "loginResponse.user is required");
        Objects.requireNonNull(loginResponse.getUser().getId(), "loginResponse.user.id is required");

        String code = generateOpaqueCode();
        OAuthLoginCodePayload payload = OAuthLoginCodePayload.builder()
                .accessToken(loginResponse.getToken())
                .refreshToken(loginResponse.getRefreshToken())
                .type(loginResponse.getType())
                .expiresIn(loginResponse.getExpiresIn())
                .userId(loginResponse.getUser().getId())
                .firstName(loginResponse.getUser().getFirstName())
                .lastName(loginResponse.getUser().getLastName())
                .email(loginResponse.getUser().getEmail())
                .roles(loginResponse.getUser().getRoles())
                .build();

        repository.save(code, payload, Duration.ofSeconds(Math.max(1, ttlSeconds)));
        return code;
    }

    /**
     * Single-use exchange: returns tokens and removes the code immediately.
     */
    public LoginResponseDTO exchange(String code) {
        if (!StringUtils.hasText(code)) {
            throw new InvalidOAuthLoginCodeException("Invalid or expired OAuth login code");
        }

        OAuthLoginCodePayload payload = repository.consume(code.trim());
        if (payload == null
                || !StringUtils.hasText(payload.getAccessToken())
                || !StringUtils.hasText(payload.getRefreshToken())
                || payload.getUserId() == null) {
            throw new InvalidOAuthLoginCodeException("Invalid or expired OAuth login code");
        }

        return LoginResponseDTO.builder()
                .token(payload.getAccessToken())
                .refreshToken(payload.getRefreshToken())
                .type(payload.getType() != null ? payload.getType() : "Bearer")
                .expiresIn(payload.getExpiresIn())
                .user(LoginResponseDTO.UserLoginResponse.builder()
                        .id(payload.getUserId())
                        .firstName(payload.getFirstName())
                        .lastName(payload.getLastName())
                        .email(payload.getEmail())
                        .roles(payload.getRoles())
                        .build())
                .build();
    }

    private static String generateOpaqueCode() {
        byte[] bytes = new byte[CODE_BYTES];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
