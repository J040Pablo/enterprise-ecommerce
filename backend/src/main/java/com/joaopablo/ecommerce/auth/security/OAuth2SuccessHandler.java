package com.joaopablo.ecommerce.auth.security;

import com.joaopablo.ecommerce.auth.config.OAuth2Properties;
import com.joaopablo.ecommerce.auth.dto.response.LoginResponseDTO;
import com.joaopablo.ecommerce.auth.service.OAuthLoginCodeService;
import com.joaopablo.ecommerce.auth.service.OAuthUserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.util.Objects;

/**
 * After Google OAuth succeeds, redirects to the frontend with a one-time opaque code only.
 * Access/refresh tokens are never placed in the URL.
 */
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final OAuthUserService oAuthUserService;
    private final OAuthLoginCodeService oAuthLoginCodeService;
    private final OAuth2Properties oAuth2Properties;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {
        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
        LoginResponseDTO loginResponse = oAuthUserService.loginWithGoogle(oauth2User);

        Objects.requireNonNull(
                loginResponse.getUser(),
                "OAuth login response must include the authenticated user"
        );
        Objects.requireNonNull(
                loginResponse.getUser().getId(),
                "OAuth login response must include the persisted user UUID"
        );

        String code = oAuthLoginCodeService.issue(loginResponse);
        String targetUrl = buildSafeRedirectUrl(code);
        response.sendRedirect(targetUrl);
    }

    /**
     * Redirect target is always the configured {@code OAUTH2_REDIRECT_URI} plus {@code code}.
     * No user-controlled redirect parameter is accepted (open-redirect safe).
     */
    String buildSafeRedirectUrl(String code) {
        String configured = oAuth2Properties.getRedirectUri();
        if (!StringUtils.hasText(configured)) {
            throw new IllegalStateException("OAUTH2_REDIRECT_URI is not configured");
        }

        URI uri = URI.create(configured.trim());
        String scheme = uri.getScheme();
        if (scheme == null
                || (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme))
                || !uri.isAbsolute()
                || uri.getHost() == null) {
            throw new IllegalStateException(
                    "OAUTH2_REDIRECT_URI must be an absolute http(s) URL with a host"
            );
        }

        return UriComponentsBuilder.fromUri(uri)
                .replaceQuery(null)
                .queryParam("code", code)
                .build()
                .toUriString();
    }
}
