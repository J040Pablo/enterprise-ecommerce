package com.joaopablo.ecommerce.auth.service;

import com.joaopablo.ecommerce.auth.entity.RefreshToken;
import com.joaopablo.ecommerce.auth.entity.User;
import com.joaopablo.ecommerce.auth.exception.InvalidRefreshTokenException;
import com.joaopablo.ecommerce.auth.repository.RefreshTokenRedisRepository;
import com.joaopablo.ecommerce.auth.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final RefreshTokenRedisRepository refreshTokenRedisRepository;

    @Value("${jwt.refresh-expiration-ms:604800000}")
    private long refreshExpirationMs;

    @Transactional
    public RefreshToken create(User user) {
        RefreshToken refreshToken = RefreshToken.builder()
                .token(UUID.randomUUID().toString())
                .user(user)
                .expiresAt(Instant.now().plusMillis(refreshExpirationMs))
                .revoked(false)
                .build();

        RefreshToken saved = refreshTokenRepository.save(refreshToken);

        refreshTokenRedisRepository.save(
                saved.getToken(),
                user.getId(),
                Duration.ofMillis(refreshExpirationMs)
        );

        return saved;
    }

    @Transactional(readOnly = true)
    public RefreshToken validate(String token) {

        RefreshToken refreshToken =
                refreshTokenRepository.findByTokenWithUser(token)
                        .orElseThrow(() ->
                                new InvalidRefreshTokenException(
                                        "Refresh token not found."
                                )
                        );

        if (Boolean.TRUE.equals(refreshToken.getRevoked())) {
            throw new InvalidRefreshTokenException(
                    "Refresh token has been revoked."
            );
        }

        if (refreshToken.isExpired()) {

            refreshTokenRedisRepository.delete(token);

            throw new InvalidRefreshTokenException(
                    "Refresh token has expired."
            );
        }

        UUID storedUserId = refreshTokenRedisRepository.findUserIdByToken(token);

        if(storedUserId == null){
            throw new InvalidRefreshTokenException(
                    "Refresh token not found in cache."
            );
        }

        return refreshToken;
    }

    @Transactional
    public RefreshToken rotate(String token) {

        RefreshToken existing = validate(token);
        existing.setRevoked(true);

        refreshTokenRepository.save(existing);
        refreshTokenRedisRepository.delete(token);

        return create(existing.getUser());
    }

    public long getRefreshExpirationMs() {
        return refreshExpirationMs;
    }

    @Transactional
    public void logout(String token) {

        if (!StringUtils.hasText(token)) {
            return;
        }


        refreshTokenRedisRepository.delete(token);


        refreshTokenRepository.findByToken(token)
                .ifPresent(refreshToken -> {

                    if (Boolean.TRUE.equals(refreshToken.getRevoked())) {
                        return;
                    }

                    refreshToken.setRevoked(true);

                    refreshTokenRepository.save(refreshToken);

                });
    }
}
