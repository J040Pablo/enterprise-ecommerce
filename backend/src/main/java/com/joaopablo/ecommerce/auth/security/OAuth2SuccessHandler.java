package com.joaopablo.ecommerce.auth.security;

import com.joaopablo.ecommerce.auth.config.OAuth2Properties;
import com.joaopablo.ecommerce.auth.dto.response.LoginResponseDTO;
import com.joaopablo.ecommerce.auth.service.OAuthUserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final OAuthUserService oAuthUserService;
    private final OAuth2Properties oAuth2Properties;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {
        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
        LoginResponseDTO loginResponse = oAuthUserService.loginWithGoogle(oauth2User);

        // Inclui o UUID real do usuário no redirect para que o Angular possa
        // armazená-lo corretamente em user.id (o JWT sub contém apenas o e-mail)
        String targetUrl = UriComponentsBuilder.fromUriString(oAuth2Properties.getRedirectUri())
                .queryParam("token", loginResponse.getToken())
                .queryParam("refreshToken", loginResponse.getRefreshToken())
                .queryParam("userId", loginResponse.getUser().getId())
                .build()
                .toUriString();

        response.sendRedirect(targetUrl);
    }
}
