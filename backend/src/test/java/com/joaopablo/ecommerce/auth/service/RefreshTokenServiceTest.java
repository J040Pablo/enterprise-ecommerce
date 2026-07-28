package com.joaopablo.ecommerce.auth.service;

import com.joaopablo.ecommerce.auth.entity.RefreshToken;
import com.joaopablo.ecommerce.auth.entity.User;
import com.joaopablo.ecommerce.auth.exception.InvalidRefreshTokenException;
import com.joaopablo.ecommerce.auth.repository.RefreshTokenRedisRepository;
import com.joaopablo.ecommerce.auth.repository.RefreshTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private RefreshTokenRedisRepository refreshTokenRedisRepository;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    private User user;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(refreshTokenService, "refreshExpirationMs", 604800000L);
        user = User.builder()
                .email("user@email.com")
                .password("encoded")
                .build();
        user.setId(UUID.randomUUID());
    }

    @Test
    void createShouldPersistRefreshTokenForUser() {
        when(refreshTokenRepository.save(any(RefreshToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        RefreshToken created = refreshTokenService.create(user);

        assertNotNull(created.getToken());
        assertEquals(user, created.getUser());
        assertFalse(created.getRevoked());
        assertTrue(created.getExpiresAt().isAfter(Instant.now()));

        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository).save(captor.capture());
        assertEquals(created.getToken(), captor.getValue().getToken());
        verify(refreshTokenRedisRepository).save(eq(created.getToken()), eq(user.getId()), any(Duration.class));
    }

    @Test
    void validateShouldRejectMissingToken() {
        when(refreshTokenRepository.findByTokenWithUser("missing"))
                .thenReturn(Optional.empty());

        assertThrows(InvalidRefreshTokenException.class,
                () -> refreshTokenService.validate("missing"));
    }

    @Test
    void validateShouldRejectRevokedToken() {
        RefreshToken token = RefreshToken.builder()
                .token("revoked-token")
                .user(user)
                .expiresAt(Instant.now().plus(Duration.ofDays(1)))
                .revoked(true)
                .build();

        when(refreshTokenRepository.findByTokenWithUser("revoked-token"))
                .thenReturn(Optional.of(token));

        InvalidRefreshTokenException ex = assertThrows(
                InvalidRefreshTokenException.class,
                () -> refreshTokenService.validate("revoked-token")
        );
        assertTrue(ex.getMessage().contains("revoked"));
    }

    @Test
    void validateShouldRejectExpiredToken() {
        RefreshToken token = RefreshToken.builder()
                .token("expired-token")
                .user(user)
                .expiresAt(Instant.now().minus(Duration.ofDays(1)))
                .revoked(false)
                .build();

        when(refreshTokenRepository.findByTokenWithUser("expired-token"))
                .thenReturn(Optional.of(token));

        InvalidRefreshTokenException ex = assertThrows(
                InvalidRefreshTokenException.class,
                () -> refreshTokenService.validate("expired-token")
        );
        assertTrue(ex.getMessage().contains("expired"));
        verify(refreshTokenRedisRepository).delete("expired-token");
    }

    @Test
    void validateShouldRejectValidTokenMissingFromCache() {
        RefreshToken token = RefreshToken.builder()
                .token("cached-token")
                .user(user)
                .expiresAt(Instant.now().plus(Duration.ofDays(1)))
                .revoked(false)
                .build();

        when(refreshTokenRepository.findByTokenWithUser("cached-token"))
                .thenReturn(Optional.of(token));
        when(refreshTokenRedisRepository.findUserIdByToken("cached-token"))
                .thenReturn(null);

        InvalidRefreshTokenException ex = assertThrows(
                InvalidRefreshTokenException.class,
                () -> refreshTokenService.validate("cached-token")
        );
        assertTrue(ex.getMessage().contains("cache"));
    }

    @Test
    void rotateShouldRevokeOldTokenAndCreateNewOne() {
        RefreshToken existing = RefreshToken.builder()
                .token("old-token")
                .user(user)
                .expiresAt(Instant.now().plus(Duration.ofDays(1)))
                .revoked(false)
                .build();

        when(refreshTokenRepository.findByTokenWithUser("old-token"))
                .thenReturn(Optional.of(existing));
        when(refreshTokenRedisRepository.findUserIdByToken("old-token"))
                .thenReturn(user.getId());
        when(refreshTokenRepository.save(any(RefreshToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        RefreshToken rotated = refreshTokenService.rotate("old-token");

        assertTrue(existing.getRevoked());
        assertNotEquals("old-token", rotated.getToken());
        assertEquals(user, rotated.getUser());
        verify(refreshTokenRepository, times(2)).save(any(RefreshToken.class));
        verify(refreshTokenRedisRepository).delete("old-token");
        verify(refreshTokenRedisRepository).save(eq(rotated.getToken()), eq(user.getId()), any(Duration.class));
    }

    @Test
    void logoutShouldRevokeActiveToken() {
        RefreshToken token = RefreshToken.builder()
                .token("active-token")
                .user(user)
                .expiresAt(Instant.now().plus(Duration.ofDays(1)))
                .revoked(false)
                .build();

        when(refreshTokenRepository.findByToken("active-token"))
                .thenReturn(Optional.of(token));

        refreshTokenService.logout("active-token");

        assertTrue(token.getRevoked());
        verify(refreshTokenRedisRepository).delete("active-token");
        verify(refreshTokenRepository).save(token);
    }

    @Test
    void logoutShouldBeIdempotentForAlreadyRevokedToken() {
        RefreshToken token = RefreshToken.builder()
                .token("revoked-token")
                .user(user)
                .expiresAt(Instant.now().plus(Duration.ofDays(1)))
                .revoked(true)
                .build();

        when(refreshTokenRepository.findByToken("revoked-token"))
                .thenReturn(Optional.of(token));

        refreshTokenService.logout("revoked-token");

        assertTrue(token.getRevoked());
        verify(refreshTokenRedisRepository).delete("revoked-token");
        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    void logoutShouldHandleNonExistentTokenGracefully() {
        when(refreshTokenRepository.findByToken("unknown-token"))
                .thenReturn(Optional.empty());

        assertDoesNotThrow(() -> refreshTokenService.logout("unknown-token"));
        verify(refreshTokenRedisRepository).delete("unknown-token");
        verify(refreshTokenRepository, never()).save(any());
    }
}
