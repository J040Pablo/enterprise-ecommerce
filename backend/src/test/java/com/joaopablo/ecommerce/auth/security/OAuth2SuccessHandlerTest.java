package com.joaopablo.ecommerce.auth.security;

import com.joaopablo.ecommerce.auth.config.OAuth2Properties;
import com.joaopablo.ecommerce.auth.dto.response.LoginResponseDTO;
import com.joaopablo.ecommerce.auth.service.OAuthLoginCodeService;
import com.joaopablo.ecommerce.auth.service.OAuthUserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OAuth2SuccessHandlerTest {

    @Mock
    private OAuthUserService oAuthUserService;

    @Mock
    private OAuthLoginCodeService oAuthLoginCodeService;

    @Mock
    private OAuth2Properties oAuth2Properties;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private Authentication authentication;

    @Mock
    private OAuth2User oAuth2User;

    @InjectMocks
    private OAuth2SuccessHandler oAuth2SuccessHandler;

    @BeforeEach
    void setUp() {
        when(oAuth2Properties.getRedirectUri()).thenReturn("http://localhost:4200/auth/callback");
    }

    @Test
    void onAuthenticationSuccess_shouldRedirectWithOpaqueCodeOnly() throws Exception {
        when(authentication.getPrincipal()).thenReturn(oAuth2User);
        UUID userId = UUID.fromString("a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11");
        LoginResponseDTO loginResponse = LoginResponseDTO.builder()
                .token("mock-access-token")
                .refreshToken("mock-refresh-token")
                .type("Bearer")
                .expiresIn(86400000)
                .user(LoginResponseDTO.UserLoginResponse.builder()
                        .id(userId)
                        .firstName("João")
                        .lastName("Pablo")
                        .email("joao.pablo@email.com")
                        .roles(List.of("CUSTOMER"))
                        .build())
                .build();
        when(oAuthUserService.loginWithGoogle(oAuth2User)).thenReturn(loginResponse);
        when(oAuthLoginCodeService.issue(loginResponse)).thenReturn("opaque-one-time-code");

        oAuth2SuccessHandler.onAuthenticationSuccess(request, response, authentication);

        ArgumentCaptor<String> locationCaptor = ArgumentCaptor.forClass(String.class);
        verify(response).sendRedirect(locationCaptor.capture());
        String location = locationCaptor.getValue();

        assertTrue(location.startsWith("http://localhost:4200/auth/callback?code="));
        assertTrue(location.contains("code=opaque-one-time-code"));
        assertFalse(location.contains("token="));
        assertFalse(location.contains("refreshToken"));
        assertFalse(location.contains("accessToken"));
        assertFalse(location.contains("userId"));
        assertFalse(location.contains("mock-access-token"));
        assertFalse(location.contains("mock-refresh-token"));
        verify(oAuthLoginCodeService).issue(loginResponse);
    }

    @Test
    void buildSafeRedirectUrl_rejectsNonHttpSchemes() {
        when(oAuth2Properties.getRedirectUri()).thenReturn("javascript:alert(1)");
        assertThrows(IllegalStateException.class,
                () -> oAuth2SuccessHandler.buildSafeRedirectUrl("code"));
    }

    @Test
    void buildSafeRedirectUrl_stripsExistingQueryAndAddsCodeOnly() {
        when(oAuth2Properties.getRedirectUri())
                .thenReturn("https://shop.example/auth/callback?evil=1");
        String url = oAuth2SuccessHandler.buildSafeRedirectUrl("abc123");
        assertEquals("https://shop.example/auth/callback?code=abc123", url);
    }
}
