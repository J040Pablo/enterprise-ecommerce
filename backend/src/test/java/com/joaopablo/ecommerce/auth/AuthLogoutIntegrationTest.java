package com.joaopablo.ecommerce.auth;

import com.joaopablo.ecommerce.auth.dto.request.CreateUserRequest;
import com.joaopablo.ecommerce.auth.dto.request.LoginRequestDTO;
import com.joaopablo.ecommerce.auth.dto.request.LogoutRequestDTO;
import com.joaopablo.ecommerce.auth.dto.request.RefreshTokenRequestDTO;
import com.joaopablo.ecommerce.auth.dto.response.LoginResponseDTO;
import com.joaopablo.ecommerce.auth.dto.response.TokenRefreshResponseDTO;
import com.joaopablo.ecommerce.auth.entity.RefreshToken;
import com.joaopablo.ecommerce.auth.exception.InvalidRefreshTokenException;
import com.joaopablo.ecommerce.auth.repository.RefreshTokenRepository;
import com.joaopablo.ecommerce.auth.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class AuthLogoutIntegrationTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    private String userEmail;
    private String userPassword;

    @BeforeEach
    void setUp() {
        userEmail = "logout.test." + System.currentTimeMillis() + "@email.com";
        userPassword = "Password@123";

        authService.register(CreateUserRequest.builder()
                .firstName("Logout")
                .lastName("Tester")
                .email(userEmail)
                .password(userPassword)
                .cpf(String.valueOf(System.currentTimeMillis()).substring(0, 11))
                .phone("11988887777")
                .build());
    }

    @Test
    @DisplayName("Logout revokes refresh token successfully")
    void logoutShouldRevokeTokenSuccessfully() {
        LoginResponseDTO loginResponse = authService.login(LoginRequestDTO.builder()
                .email(userEmail)
                .password(userPassword)
                .build());

        String refreshTokenStr = loginResponse.getRefreshToken();

        authService.logout(LogoutRequestDTO.builder()
                .refreshToken(refreshTokenStr)
                .build());

        RefreshToken refreshTokenInDb = refreshTokenRepository.findByToken(refreshTokenStr).orElseThrow();
        assertTrue(refreshTokenInDb.getRevoked());
    }

    @Test
    @DisplayName("Logout is idempotent when called repeatedly")
    void logoutShouldBeIdempotentOnRepeatedCalls() {
        LoginResponseDTO loginResponse = authService.login(LoginRequestDTO.builder()
                .email(userEmail)
                .password(userPassword)
                .build());

        String refreshTokenStr = loginResponse.getRefreshToken();

        assertDoesNotThrow(() -> authService.logout(LogoutRequestDTO.builder().refreshToken(refreshTokenStr).build()));
        assertDoesNotThrow(() -> authService.logout(LogoutRequestDTO.builder().refreshToken(refreshTokenStr).build()));

        RefreshToken refreshTokenInDb = refreshTokenRepository.findByToken(refreshTokenStr).orElseThrow();
        assertTrue(refreshTokenInDb.getRevoked());
    }

    @Test
    @DisplayName("Refresh attempt after logout must fail")
    void refreshAttemptAfterLogoutShouldFail() {
        LoginResponseDTO loginResponse = authService.login(LoginRequestDTO.builder()
                .email(userEmail)
                .password(userPassword)
                .build());

        String refreshTokenStr = loginResponse.getRefreshToken();

        authService.logout(LogoutRequestDTO.builder().refreshToken(refreshTokenStr).build());

        InvalidRefreshTokenException ex = assertThrows(
                InvalidRefreshTokenException.class,
                () -> authService.refresh(RefreshTokenRequestDTO.builder().refreshToken(refreshTokenStr).build())
        );

        assertTrue(ex.getMessage().contains("revoked"));
    }

    @Test
    @DisplayName("Subsequent login generates new valid refresh token while old remains revoked")
    void subsequentLoginGeneratesNewValidRefreshTokenAndOldRemainsRevoked() {
        LoginResponseDTO loginResponse1 = authService.login(LoginRequestDTO.builder()
                .email(userEmail)
                .password(userPassword)
                .build());

        String firstRefreshToken = loginResponse1.getRefreshToken();

        authService.logout(LogoutRequestDTO.builder().refreshToken(firstRefreshToken).build());

        LoginResponseDTO loginResponse2 = authService.login(LoginRequestDTO.builder()
                .email(userEmail)
                .password(userPassword)
                .build());

        String secondRefreshToken = loginResponse2.getRefreshToken();

        assertNotEquals(firstRefreshToken, secondRefreshToken);

        // Old token remains invalid
        assertThrows(InvalidRefreshTokenException.class, () ->
                authService.refresh(RefreshTokenRequestDTO.builder().refreshToken(firstRefreshToken).build()));

        // New token works for refresh
        TokenRefreshResponseDTO refreshResponse = authService.refresh(
                RefreshTokenRequestDTO.builder().refreshToken(secondRefreshToken).build());

        assertNotNull(refreshResponse.getAccessToken());
        assertNotNull(refreshResponse.getRefreshToken());
    }
}
