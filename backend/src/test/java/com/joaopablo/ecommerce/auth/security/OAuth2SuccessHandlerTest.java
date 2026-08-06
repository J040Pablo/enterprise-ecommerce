package com.joaopablo.ecommerce.auth.security;

import com.joaopablo.ecommerce.auth.config.OAuth2Properties;
import com.joaopablo.ecommerce.auth.dto.response.LoginResponseDTO;
import com.joaopablo.ecommerce.auth.service.OAuthUserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OAuth2SuccessHandlerTest {

    @Mock
    private OAuthUserService oAuthUserService;

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
    void onAuthenticationSuccess_shouldRedirectWithTokensInQueryParams() throws Exception {
        when(authentication.getPrincipal()).thenReturn(oAuth2User);
        LoginResponseDTO loginResponse = LoginResponseDTO.builder()
                .token("mock-access-token")
                .refreshToken("mock-refresh-token")
                .type("Bearer")
                .expiresIn(86400000)
                .build();
        when(oAuthUserService.loginWithGoogle(oAuth2User)).thenReturn(loginResponse);

        oAuth2SuccessHandler.onAuthenticationSuccess(request, response, authentication);

        verify(response).sendRedirect("http://localhost:4200/auth/callback?token=mock-access-token&refreshToken=mock-refresh-token");
    }
}
